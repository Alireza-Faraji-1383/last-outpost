package dev.exodus.enemy;

import dev.exodus.ExodusConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.List;

/** Every candidate checks chunk availability before reading terrain. Shared budget bounds a tick. */
public final class EnemyPlacement {
    private int remaining;
    public void beginTick() { remaining=ExodusConfig.ENEMY_CANDIDATES_PER_TICK.get(); }
    public BlockPos findGroupMember(ServerLevel level,ServerPlayer owner,List<ServerPlayer> players,Mob mob,BlockPos anchor) {
        int spread=ExodusConfig.SOLDIER_GROUP_SPREAD.get();
        for (int attempt=0;attempt<ExodusConfig.ENEMY_PLACEMENT_ATTEMPTS.get() && remaining>0;attempt++) {
            remaining--;
            int x=anchor.getX()+level.random.nextInt(spread*2+1)-spread;
            int z=anchor.getZ()+level.random.nextInt(spread*2+1)-spread;
            BlockPos result=validate(level,owner,players,mob,x,z,true);
            if (result!=null) return result;
        }
        return null;
    }
    public BlockPos find(ServerLevel level, ServerPlayer owner, List<ServerPlayer> players, Mob mob, boolean soldier, BlockPos device) {
        int minimum=soldier?ExodusConfig.SOLDIER_SPAWN_MIN.get():ExodusConfig.ZOMBIE_SPAWN_MIN.get();
        int maximum=Math.max(minimum,soldier?ExodusConfig.SOLDIER_SPAWN_MAX.get():ExodusConfig.ZOMBIE_SPAWN_MAX.get());
        for (int attempt=0; attempt<ExodusConfig.ENEMY_PLACEMENT_ATTEMPTS.get() && remaining>0; attempt++) {
            remaining--;
            double angle=level.random.nextDouble()*Math.PI*2;
            double radius=minimum+level.random.nextDouble()*(maximum-minimum);
            double anchorX=device==null?owner.getX():device.getX()+.5;
            double anchorZ=device==null?owner.getZ():device.getZ()+.5;
            int x=(int)Math.floor(anchorX+Math.cos(angle)*radius), z=(int)Math.floor(anchorZ+Math.sin(angle)*radius);
            BlockPos position=validate(level,owner,players,mob,x,z,soldier);
            if (position!=null) return position;
        }
        return null;
    }
    private BlockPos validate(ServerLevel level,ServerPlayer owner,List<ServerPlayer> players,Mob mob,int x,int z,boolean soldier) {
            int minimum=soldier?ExodusConfig.SOLDIER_SPAWN_MIN.get():ExodusConfig.ZOMBIE_SPAWN_MIN.get();
            int maximum=Math.max(minimum,soldier?ExodusConfig.SOLDIER_SPAWN_MAX.get():ExodusConfig.ZOMBIE_SPAWN_MAX.get());
            BlockPos probe=new BlockPos(x,owner.getBlockY(),z);
            // Collision/path reads can touch neighboring chunks; require the complete local footprint.
            if (!level.hasChunkAt(probe) || !level.hasChunkAt(probe.offset(-1,0,-1)) || !level.hasChunkAt(probe.offset(1,0,1))
                    || !level.hasChunkAt(probe.offset(-1,0,1)) || !level.hasChunkAt(probe.offset(1,0,-1))) return null;
            int y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);
            BlockPos position=new BlockPos(x,y,z);
            if (y<=level.getMinBuildHeight() || y+2>=level.getMaxBuildHeight()) return null;
            if (!level.getWorldBorder().isWithinBounds(position) || !level.getWorldBorder().isWithinBounds(position.offset(1,0,1))) return null;
            if (!level.getBlockState(position.below()).isFaceSturdy(level,position.below(),net.minecraft.core.Direction.UP)) return null;
            if (!level.getFluidState(position).isEmpty() || !level.getFluidState(position.below()).isEmpty() || !level.getFluidState(position.above()).isEmpty()) return null;
            double ownerDistance=owner.distanceToSqr(x+.5,owner.getY(),z+.5);
            if (ownerDistance<minimum*(double)minimum || ownerDistance>maximum*(double)maximum) return null;
            if (players.stream().anyMatch(player -> player.distanceToSqr(x+.5,y,z+.5)<minimum*(double)minimum)) return null;
            mob.moveTo(x+.5,y,z+.5,level.random.nextFloat()*360,0);
            if (!level.noCollision(mob) || !level.isUnobstructed(mob)) return null;
            return position;
    }
}
