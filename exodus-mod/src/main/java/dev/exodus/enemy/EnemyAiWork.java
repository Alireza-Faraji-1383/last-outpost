package dev.exodus.enemy;

import dev.exodus.ExodusConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

/** Expensive Exodus target/path requests share global budgets and a fair navigation queue. */
public final class EnemyAiWork {
    private record Scan(Mob mob,Runnable work) {}
    private static final EnemyWorkQueue<UUID,Scan> TARGETS=new EnemyWorkQueue<>();
    private static final EnemyWorkQueue<UUID,Mob> NAVIGATION=new EnemyWorkQueue<>();
    public static void requestTargetScan(Mob mob,Runnable work) { TARGETS.request(mob.getUUID(),new Scan(mob,work)); }
    public static void cancelTargetScan(Mob mob) { TARGETS.cancel(mob.getUUID()); }
    public static void requestNavigation(Mob mob) { NAVIGATION.request(mob.getUUID(),mob); }
    public static void cancelNavigation(Mob mob) { NAVIGATION.cancel(mob.getUUID()); }
    public static void tick() {
        for (int i=0;i<ExodusConfig.ENEMY_TARGET_SCANS_PER_TICK.get();i++) {
            Scan scan=TARGETS.poll();
            if (scan==null) break;
            if (!scan.mob.isRemoved() && scan.mob.isAlive() && EnemySpawnService.currentEnemy(scan.mob)) scan.work.run();
        }
        for (int i=0;i<ExodusConfig.ENEMY_NAVIGATION_PER_TICK.get();i++) {
            Mob mob=NAVIGATION.poll();
            if (mob==null) break;
            BlockPos device=EnemySpawnService.deviceFor(mob);
            if (mob.isRemoved() || !mob.isAlive() || mob.getTarget()!=null || device==null) continue;
            double radius=ExodusConfig.ENEMY_DEVICE_RADIUS.get();
            if (mob.distanceToSqr(device.getX()+.5,device.getY(),device.getZ()+.5)>radius*radius) continue;
            Vec3 delta=Vec3.atBottomCenterOf(device).subtract(mob.position());
            Vec3 destination=mob.position().add(delta.normalize().scale(Math.min(delta.length(),ExodusConfig.ENEMY_NAVIGATION_STEP.get())));
            if (mob.level().hasChunkAt(BlockPos.containing(destination)))
                mob.getNavigation().moveTo(destination.x,destination.y,destination.z,ExodusConfig.ENEMY_DEVICE_MOVEMENT_SPEED.get());
        }
    }
    public static void reset() { TARGETS.clear();NAVIGATION.clear(); }
}
