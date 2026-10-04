package dev.exodus.supply.event;

import com.mojang.logging.LogUtils;
import dev.exodus.*;
import dev.exodus.event.*;
import dev.exodus.event.domain.EventDefinition;
import dev.exodus.map.MapLocation;
import dev.exodus.supply.ExodusSupplyRegistry;
import dev.exodus.supply.blockentity.SupplyCrateBlockEntity;
import dev.exodus.supply.domain.*;
import dev.exodus.supply.entity.SupplyDropEntity;
import dev.exodus.teleporter.domain.TeleporterComponent;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.*;

/** Reusable world delivery adapter. It never reads or changes a physical radio. */
public final class EventAirdropService {
    public static final int BLUE=0x55AAFF;
    private static final class Job {final UUID match;final EventDefinition definition;int attempts;Job(UUID match,EventDefinition d){this.match=match;definition=d;}}
    private static final Map<MinecraftServer,Map<String,Job>> JOBS=new WeakHashMap<>();
    private EventAirdropService(){}
    private static ServerLevel level(MinecraftServer server){var d=ExodusSavedData.get(server);return d.dimension.isEmpty()?null:server.getLevel(ResourceKey.create(Registries.DIMENSION,new ResourceLocation(d.dimension)));}
    public static int pending(MinecraftServer server){var jobs=JOBS.get(server);return jobs==null?0:jobs.size();}
    public static boolean available(MinecraftServer server,EventDefinition def){
        var d=ExodusSavedData.get(server);var jobs=JOBS.get(server);
        if(jobs!=null&&jobs.containsKey(def.id()))return false;
        return d.session.events.drops.size()+pending(server)<ExodusConfig.EVENT_MAX_DROPS.get()&&!EventManager.players(server).isEmpty();
    }
    public static boolean queue(MinecraftServer server,EventDefinition def){
        var d=ExodusSavedData.get(server);
        if(def.core()&&d.teleporter.rareClaims().contains(TeleporterComponent.DIMENSIONAL_CORE)){d.session.events.schedule.started(def,EventManager.day(server));d.setDirty();return true;}
        var jobs=JOBS.computeIfAbsent(server,k->new LinkedHashMap<>());if(jobs.containsKey(def.id()))return false;
        jobs.put(def.id(),new Job(d.matchId,def));return true;
    }
    public static void tick(MinecraftServer server){
        var d=ExodusSavedData.get(server);var level=level(server);if(level==null)return;
        var jobs=JOBS.get(server);
        if(jobs!=null&&!jobs.isEmpty()){
            var entry=jobs.entrySet().iterator().next();var job=entry.getValue();
            if(!Objects.equals(job.match,d.matchId)||d.state!=MatchState.RUNNING)jobs.remove(entry.getKey());
            else for(int i=0;i<ExodusConfig.EVENT_CANDIDATES_PER_TICK.get();i++){
                BlockPos candidate=candidate(server,level);job.attempts++;
                if(candidate!=null&&spawn(server,level,job.definition,candidate)){jobs.remove(entry.getKey());break;}
                if(job.attempts>=ExodusConfig.EVENT_PLACEMENT_ATTEMPTS.get()){jobs.remove(entry.getKey());LogUtils.getLogger().warn("[Exodus] Could not place event drop {}; milestones will retry.",job.definition.id());break;}
            }
        }
        if(d.session.elapsedTicks%20!=1)return;
        for(var drop:new ArrayList<>(d.session.events.drops.values())){
            BlockPos pos=BlockPos.of(drop.position());
            boolean missing=drop.landed()&&level.hasChunkAt(pos)&&(!(level.getBlockEntity(pos) instanceof SupplyCrateBlockEntity crate)||!drop.id().equals(crate.eventDropId()));
            if(missing||level.getGameTime()>=drop.expires()){remove(level,drop);d.session.events.drops.remove(drop.id());d.setDirty();}
        }
    }
    private static BlockPos candidate(MinecraftServer server,ServerLevel level){
        var players=EventManager.players(server);if(players.isEmpty())return null;
        var random=level.random;var anchor=players.get(random.nextInt(players.size()));
        int min=ExodusConfig.EVENT_DROP_MIN_DISTANCE.get(),max=Math.max(min,ExodusConfig.EVENT_DROP_MAX_DISTANCE.get());
        int dx=random.nextInt(max*2+1)-max,dz=random.nextInt(max*2+1)-max;
        if((long)dx*dx+(long)dz*dz<(long)min*min||(long)dx*dx+(long)dz*dz>(long)max*max)return null;
        int x=anchor.blockPosition().getX()+dx,z=anchor.blockPosition().getZ()+dz;
        var d=ExodusSavedData.get(server);int safe=d.session.borderSize/2-ExodusConfig.BORDER_SAFE_DISTANCE.get();
        if(Math.abs((long)x-d.centerX)>safe||Math.abs((long)z-d.centerZ)>safe||!level.hasChunk(x>>4,z>>4))return null;
        int y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);BlockPos pos=new BlockPos(x,y,z);
        if(y<=level.getMinBuildHeight()||y+ExodusConfig.LANDING_CLEARANCE.get()>=level.getMaxBuildHeight()||!level.getWorldBorder().isWithinBounds(pos))return null;
        if(!level.getFluidState(pos.below()).isEmpty()||!level.getBlockState(pos.below()).isFaceSturdy(level,pos.below(),Direction.UP))return null;
        for(int n=0;n<ExodusConfig.LANDING_CLEARANCE.get();n++)if(!level.getBlockState(pos.above(n)).isAir()||!level.getFluidState(pos.above(n)).isEmpty())return null;
        return pos;
    }
    private static boolean spawn(MinecraftServer server,ServerLevel level,EventDefinition def,BlockPos position){
        var d=ExodusSavedData.get(server);
        if(def.core()&&d.teleporter.rareClaims().contains(TeleporterComponent.DIMENSIONAL_CORE)){d.session.events.schedule.started(def,EventManager.day(server));d.setDirty();return true;}
        var loot=new ResourceLocation(def.lootTable());
        if(server.getLootData().getLootTable(loot)==net.minecraft.world.level.storage.loot.LootTable.EMPTY){LogUtils.getLogger().error("[Exodus] Event loot table is missing: {}",loot);return false;}
        UUID id=UUID.randomUUID();long expires=def.core()?Long.MAX_VALUE:level.getGameTime()+ExodusConfig.EVENT_DROP_SECONDS.get()*20L;
        var definition=new SupplyDefinition(def.id(),def.title(),"exodus:supply_crate",def.lootTable(),Set.of(),0,0,null,BLUE,true,0);
        var entity=new SupplyDropEntity(ExodusSupplyRegistry.SUPPLY_DROP.get(),level);
        entity.configure(id,d.matchId,null,position,definition,null);entity.configureEvent(expires,def.core());
        entity.setPos(position.getX()+.5,Math.min(level.getMaxBuildHeight()-2,position.getY()+ExodusConfig.DROP_SPAWN_HEIGHT.get()),position.getZ()+.5);
        if(!level.addFreshEntity(entity))return false;
        if(def.core())d.teleporter.rareClaims().add(TeleporterComponent.DIMENSIONAL_CORE);
        d.session.events.drops.put(id,new EventSavedState.Drop(id,entity.getUUID(),position.asLong(),expires,def.title(),def.core(),false));
        d.session.events.schedule.started(def,EventManager.day(server));d.setDirty();
        for(var player:MatchManager.associatedOnlinePlayers(server))player.sendSystemMessage(Component.literal(def.title()+" inbound: X "+position.getX()+" Z "+position.getZ()+". Check your map."));
        LogUtils.getLogger().info("[Exodus] Event airdrop {} spawned at {}",def.id(),position);return true;
    }
    public static void landed(ServerLevel level,UUID id,BlockPos pos){
        var d=ExodusSavedData.get(level.getServer());var drop=d.session.events.drops.get(id);if(drop==null)return;
        d.session.events.drops.put(id,new EventSavedState.Drop(id,drop.entity(),pos.asLong(),drop.expires(),drop.title(),drop.core(),true));d.setDirty();
    }
    public static void failed(ServerLevel level,UUID id,String definitionId,boolean core){
        var d=ExodusSavedData.get(level.getServer());if(d.session.events.drops.remove(id)==null)return;
        if(core){d.teleporter.rareClaims().remove(TeleporterComponent.DIMENSIONAL_CORE);d.session.events.schedule.failedDelivery(definitionId);}
        d.setDirty();LogUtils.getLogger().warn("[Exodus] Event drop {} could not land; core milestones may retry.",id);
    }
    public static List<MapLocation> markers(MinecraftServer server){
        return ExodusSavedData.get(server).session.events.drops.values().stream().map(d->{BlockPos p=BlockPos.of(d.position());return new MapLocation("airdrop:"+d.id(),d.title(),MapLocation.Kind.AIRDROP,p.getX(),p.getY(),p.getZ());}).toList();
    }
    private static void remove(ServerLevel level,EventSavedState.Drop drop){
        var entity=level.getEntity(drop.entity());if(entity instanceof SupplyDropEntity falling&&drop.id().equals(falling.dropId()))entity.discard();
        BlockPos pos=BlockPos.of(drop.position());if(drop.landed()&&level.hasChunkAt(pos)&&level.getBlockEntity(pos) instanceof SupplyCrateBlockEntity crate&&drop.id().equals(crate.eventDropId()))level.removeBlock(pos,false);
    }
    public static void cleanup(MinecraftServer server){
        JOBS.remove(server);var d=ExodusSavedData.get(server);var level=level(server);
        var drops=new ArrayList<>(d.session.events.drops.values());
        // Revoke ownership first: block removal must not look like a player breaking a live Core crate.
        d.session.events.drops.clear();d.setDirty();if(level!=null)for(var drop:drops)remove(level,drop);
    }
}
