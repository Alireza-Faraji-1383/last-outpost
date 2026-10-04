package dev.exodus.horse;

import dev.exodus.*;
import dev.exodus.enemy.SoldierSchedule;
import dev.exodus.wasteland.lostcities.LostCitiesIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.*;

/** Two global daytime opportunities; loaded surface placement and a cap including unloaded horses. */
public final class HorseSpawnService {
    private static final String MATCH="ExodusHorseMatch";
    private static final HorsePopulation POPULATION=new HorsePopulation();
    private static final Map<UUID,Horse> HORSES=new HashMap<>();
    private static final SoldierSchedule SCHEDULE=new SoldierSchedule(6000,24000);
    private static UUID currentMatch;
    private static int offset;
    private HorseSpawnService() {}

    public static boolean managed(Entity entity) {
        return entity instanceof Horse && entity.getPersistentData().hasUUID(MATCH);
    }
    public static boolean allowed(Entity entity,ExodusSavedData data) {
        return managed(entity) && HorseAdmission.allowed(data.state==MatchState.RUNNING,
                entity.getPersistentData().getUUID(MATCH),data.matchId,
                entity.level().dimension().location().toString(),data.dimension);
    }
    public static void join(Horse horse) {
        POPULATION.add(horse.getUUID());HORSES.put(horse.getUUID(),horse);
    }
    public static void leave(Horse horse) {
        if(horse.getRemovalReason()==Entity.RemovalReason.UNLOADED_TO_CHUNK) HORSES.put(horse.getUUID(),null);
        else { HORSES.remove(horse.getUUID());POPULATION.remove(horse.getUUID()); }
    }
    public static void tick(MinecraftServer server) {
        ServerLevel level=server.getLevel(LostCitiesIntegration.WASTELAND_DIMENSION);
        if(level==null)return;
        ExodusSavedData data=ExodusSavedData.get(server);
        UUID match=data.state==MatchState.RUNNING && level.dimension().location().toString().equals(data.dimension)?data.matchId:null;
        if(!Objects.equals(match,currentMatch)) {
            reset();currentMatch=match;
            offset=level.random.nextInt(ExodusConfig.HORSE_SCHEDULE_WINDOW.get());
        }
        for(Horse horse:new ArrayList<>(HORSES.values())) {
            if(horse!=null && (!horse.isAlive() || horse.isRemoved()))leave(horse);
        }
        if(match==null)return;
        var players=level.players().stream().filter(MatchManager::isActiveMatchPlayer).toList();
        if(players.isEmpty())return;
        if(SCHEDULE.due(match,level.getDayTime(),offset)==null)return;
        int minimum=ExodusConfig.HORSE_BATCH_MIN.get();
        int maximum=Math.max(minimum,ExodusConfig.HORSE_BATCH_MAX.get());
        int count=Math.min(POPULATION.remaining(ExodusConfig.HORSE_GLOBAL_CAP.get()),minimum+level.random.nextInt(maximum-minimum+1));
        int spawned=0;
        for(int i=0;i<count;i++) {
            Horse horse=EntityType.HORSE.create(level);
            if(horse==null)break;
            ServerPlayer anchor=players.get(level.random.nextInt(players.size()));
            if(!place(level,anchor,players,horse))continue;
            horse.getPersistentData().putUUID(MATCH,match);
            horse.finalizeSpawn(level,level.getCurrentDifficultyAt(horse.blockPosition()),MobSpawnType.EVENT,null,null);
            horse.setAge(0);horse.setTamed(true);horse.setPersistenceRequired();
            horse.equipSaddle(null);
            if(level.addFreshEntity(horse))spawned++;
            else { HORSES.remove(horse.getUUID());POPULATION.remove(horse.getUUID()); }
        }
        com.mojang.logging.LogUtils.getLogger().info("Exodus horse opportunity: spawned={}, remainingCapacity={}, match={}",
                spawned,POPULATION.remaining(ExodusConfig.HORSE_GLOBAL_CAP.get()),match);
    }
    private static boolean place(ServerLevel level,ServerPlayer anchor,List<ServerPlayer> players,Horse horse) {
        int minimum=ExodusConfig.HORSE_SPAWN_MIN.get();
        int maximum=Math.max(minimum,ExodusConfig.HORSE_SPAWN_MAX.get());
        for(int attempt=0;attempt<ExodusConfig.HORSE_PLACEMENT_ATTEMPTS.get();attempt++) {
            double angle=level.random.nextDouble()*Math.PI*2;
            double radius=minimum+level.random.nextDouble()*(maximum-minimum);
            int x=(int)Math.floor(anchor.getX()+Math.cos(angle)*radius);
            int z=(int)Math.floor(anchor.getZ()+Math.sin(angle)*radius);
            BlockPos probe=new BlockPos(x,anchor.getBlockY(),z);
            boolean loaded=true;
            for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)
                loaded &= level.hasChunkAt(probe.offset(dx,0,dz));
            if(!loaded)continue;
            int y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);
            BlockPos position=new BlockPos(x,y,z);
            if(y<=level.getMinBuildHeight() || y+3>=level.getMaxBuildHeight())continue;
            if(!level.getWorldBorder().isWithinBounds(position.offset(-1,0,-1))
                    || !level.getWorldBorder().isWithinBounds(position.offset(1,0,1)))continue;
            if(!level.getBlockState(position.below()).isFaceSturdy(level,position.below(),Direction.UP))continue;
            if(!level.getFluidState(position.below()).isEmpty() || !level.getFluidState(position).isEmpty()
                    || !level.getFluidState(position.above()).isEmpty())continue;
            if(players.stream().anyMatch(p->p.distanceToSqr(x+.5,y,z+.5)<minimum*(double)minimum))continue;
            horse.moveTo(x+.5,y,z+.5,level.random.nextFloat()*360,0);
            if(level.noCollision(horse) && level.isUnobstructed(horse))return true;
        }
        return false;
    }
    public static void reset() {
        for(Horse horse:new ArrayList<>(HORSES.values()))if(horse!=null && !horse.isRemoved())horse.discard();
        HORSES.clear();POPULATION.clear();SCHEDULE.clear();currentMatch=null;
    }
}
