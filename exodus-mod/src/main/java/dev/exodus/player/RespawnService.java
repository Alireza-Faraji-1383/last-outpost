package dev.exodus.player;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class RespawnService {
    private RespawnService() {}
    private static final ThreadLocal<Boolean> INTERNAL=ThreadLocal.withInitial(()->false);
    public static boolean internalChange(){return INTERNAL.get();}

    public static void setBase(ServerPlayer player, ServerLevel level, BlockPos spawn) {
        INTERNAL.set(true);try{player.setRespawnPosition(level.dimension(), spawn, 0.0F, true, false);}finally{INTERNAL.set(false);}
    }

    public static ServerPlayer recreate(ServerPlayer player){ServerPlayer replacement=player.server.getPlayerList().respawn(player,false);replacement.connection.player=replacement;return replacement;}
    public static void resetToOverworld(ServerPlayer player, ServerLevel overworld) {
        setBase(player,overworld,overworld.getSharedSpawnPos());
    }
    public static void selectMatchSpawn(ServerPlayer player){
        var data=dev.exodus.ExodusSavedData.get(player.server);var base=data.bases.get(player.getUUID());if(base==null)return;
        var level=player.server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,new net.minecraft.resources.ResourceLocation(data.dimension)));if(level==null)return;
        Long primary=data.session.originalSpawns.get(player.getUUID()),selected=data.session.selectedSpawns.getOrDefault(player.getUUID(),primary);
        var record=selected==null?null:data.session.devices.get(selected);
        if(record==null||!player.getUUID().equals(record.owner())){data.session.selectedSpawns.remove(player.getUUID());selected=primary;}
        if(selected!=null&&!java.util.Objects.equals(primary,selected)){BlockPos point=BlockPos.of(selected);for(ServerPlayer other:level.players())if(!player.getUUID().equals(other.getUUID())&&other.isAlive()&&DeviceRespawnPolicy.fallback(false,other.distanceToSqr(point.getX()+.5,point.getY()+.5,point.getZ()+.5),dev.exodus.MatchManager.isActiveMatchPlayer(other),dev.exodus.ExodusConfig.SPAWN_EXCLUSION_RADIUS.get())){selected=primary;break;}}
        BlockPos spawn=selected==null?base.spawn():BlockPos.of(selected).above();prepare(level,spawn);setBase(player,level,spawn);data.setDirty();
    }
    public static void reserveHeadroom(ServerLevel level,BlockPos spawn){for(int i=0;i<2;i++){BlockPos cell=spawn.above(i);if(!level.getBlockState(cell).isAir())level.setBlock(cell,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);}}
    private static void prepare(ServerLevel level,BlockPos spawn){level.getChunkAt(spawn);reserveHeadroom(level,spawn);}
    public static void ensureArrival(ServerPlayer player){
        var data=dev.exodus.ExodusSavedData.get(player.server);
        var level=player.server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,new net.minecraft.resources.ResourceLocation(data.dimension)));
        Long pending=data.session.pendingRespawns.remove(player.getUUID());
        BlockPos spawn=pending==null?player.getRespawnPosition():BlockPos.of(pending);
        if(level!=null&&spawn!=null){prepare(level,spawn);setBase(player,level,spawn);player.teleportTo(level,spawn.getX()+.5,spawn.getY(),spawn.getZ()+.5,player.getYRot(),player.getXRot());data.setDirty();}
    }
}
