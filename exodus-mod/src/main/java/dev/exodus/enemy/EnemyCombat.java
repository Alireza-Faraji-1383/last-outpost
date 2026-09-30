package dev.exodus.enemy;

import dev.exodus.ExodusConfig;
import dev.exodus.MatchManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.core.BlockPos;
import java.util.*;

public final class EnemyCombat {
    public static boolean soldier(LivingEntity entity) {
        if (!(entity instanceof Mob mob)) return false;
        var id = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        return id != null && (id.toString().equals(EnemyKind.RUSSIAN.entityId()) || id.toString().equals(EnemyKind.AMERICAN.entityId()));
    }
    public static boolean zombiePair(Mob mob, LivingEntity target) {
        return (soldier(mob) && target instanceof Zombie) || (mob instanceof Zombie && soldier(target));
    }
    public static boolean allowed(Mob mob, LivingEntity target, boolean pursuing) {
        if (target == null) return true;
        if (zombiePair(mob, target)) return EnemyRangePolicy.allowed(mob.distanceToSqr(target), pursuing,
                ExodusConfig.ZOMBIE_SOLDIER_ACQUIRE.get(), ExodusConfig.ZOMBIE_SOLDIER_RELEASE.get());
        if (mob instanceof ExodusZombie && target instanceof ServerPlayer player)
            return MatchManager.isActiveMatchPlayer(player) && EnemyRangePolicy.allowed(mob.distanceToSqr(target), pursuing,
                    ExodusConfig.ZOMBIE_PLAYER_ACQUIRE.get(), ExodusConfig.ZOMBIE_PLAYER_RELEASE.get());
        return !(mob instanceof ExodusZombie);
    }
    public static void install(Mob mob) {
        if (mob instanceof ExodusZombie zombie) zombie.configureManagedGoals();
        mob.goalSelector.addGoal(1, new DeviceApproachGoal(mob));
    }
    public static final class ZombieTargetGoal extends Goal {
        private final ExodusZombie zombie;
        private LivingEntity candidate;
        private long nextScan;
        public ZombieTargetGoal(ExodusZombie zombie) { this.zombie=zombie; setFlags(EnumSet.of(Flag.TARGET)); }
        @Override public boolean canUse() {
            long now = zombie.level().getGameTime();
            if(candidate!=null && candidate.isAlive() && allowed(zombie,candidate,false) && zombie.hasLineOfSight(candidate)) return true;
            candidate=null;
            if (now < nextScan) return false;
            EnemyAiWork.requestTargetScan(zombie,this::scan);
            return false;
        }
        private void scan() {
            long now=zombie.level().getGameTime();
            nextScan = now + ExodusConfig.ENEMY_AI_INTERVAL_TICKS.get();
            double radius = Math.max(ExodusConfig.ZOMBIE_PLAYER_ACQUIRE.get(), ExodusConfig.ZOMBIE_SOLDIER_ACQUIRE.get());
            candidate = zombie.level().getEntitiesOfClass(LivingEntity.class, zombie.getBoundingBox().inflate(radius),
                    target -> target != zombie && target.isAlive() && (target instanceof ServerPlayer || soldier(target))
                            && allowed(zombie, target, false) && zombie.hasLineOfSight(target))
                    .stream().min(Comparator.comparingDouble(zombie::distanceToSqr)).orElse(null);
        }
        @Override public boolean canContinueToUse() {
            var target=zombie.getTarget();
            return target != null && target.isAlive() && allowed(zombie, target, true);
        }
        @Override public void start() { EnemyAiWork.cancelTargetScan(zombie);zombie.setTarget(candidate); }
        @Override public void stop() { EnemyAiWork.cancelTargetScan(zombie);zombie.setTarget(null);candidate=null; }
    }
    private static final class DeviceApproachGoal extends Goal {
        private final Mob mob;
        private long nextPath;
        DeviceApproachGoal(Mob mob) { this.mob=mob; setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() {
            BlockPos device = EnemySpawnService.deviceFor(mob);
            return mob.getTarget() == null && device != null
                    && mob.distanceToSqr(device.getX()+.5, device.getY(), device.getZ()+.5) <= square(ExodusConfig.ENEMY_DEVICE_RADIUS.get());
        }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public void tick() {
            long now=mob.level().getGameTime();
            if (now < nextPath) return;
            nextPath=now+ExodusConfig.ENEMY_AI_INTERVAL_TICKS.get();
            EnemyAiWork.requestNavigation(mob);
        }
        @Override public void stop() { EnemyAiWork.cancelNavigation(mob);mob.getNavigation().stop(); }
        private static double square(double value) { return value*value; }
    }
}
