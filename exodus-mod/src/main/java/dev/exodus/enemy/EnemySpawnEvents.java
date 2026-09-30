package dev.exodus.enemy;

import dev.exodus.*;
import dev.exodus.wasteland.lostcities.LostCitiesIntegration;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.*;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import com.mojang.brigadier.context.CommandContextBuilder;
import net.minecraft.commands.CommandSourceStack;

@Mod.EventBusSubscriber(modid=ExodusMod.MOD_ID)
public final class EnemySpawnEvents {
    private static final ThreadLocal<Boolean> ADMIN_COMMAND = ThreadLocal.withInitial(() -> false);
    private static boolean controlled(net.minecraft.world.level.Level level) {
        return !level.isClientSide && level.dimension().equals(LostCitiesIntegration.WASTELAND_DIMENSION);
    }
    private static boolean controlled(net.minecraft.world.level.ServerLevelAccessor level) { return controlled((net.minecraft.world.level.Level)level.getLevel()); }
    private static boolean exception(MobSpawnType reason) { return reason == MobSpawnType.SPAWN_EGG || reason == MobSpawnType.COMMAND; }

    /** Wrap the actual parsed summon command, including execute ... run summon and NBT summons. */
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void command(CommandEvent event) {
        var parse=event.getParseResults();
        if (!parse.getContext().getSource().hasPermission(2)) return;
        for (CommandContextBuilder<CommandSourceStack> node=parse.getContext(); node != null; node=node.getChild()) {
            boolean summon=node.getNodes().stream().anyMatch(n -> n.getNode().getName().equals("summon") || n.getNode().getName().equals("minecraft:summon"));
            var command=node.getCommand();
            if (!summon || command == null) continue;
            com.mojang.logging.LogUtils.getLogger().debug("Exodus wrapping administrator summon command.");
            node.withCommand(context -> {
                boolean previous=ADMIN_COMMAND.get();
                ADMIN_COMMAND.set(true);
                com.mojang.logging.LogUtils.getLogger().debug("Exodus administrator summon scope entered.");
                try { return command.run(context); }
                finally { ADMIN_COMMAND.set(previous); }
            });
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void placement(MobSpawnEvent.SpawnPlacementCheck event) {
        if (controlled(event.getLevel()) && !exception(event.getSpawnType())) event.setResult(Event.Result.DENY);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void position(MobSpawnEvent.PositionCheck event) {
        if (controlled(event.getLevel()) && !EnemyTags.managed(event.getEntity()) && !exception(event.getSpawnType()))
            event.setResult(Event.Result.DENY);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void finalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (!controlled(event.getLevel())) return;
        if (EnemyTags.managed(event.getEntity())) return;
        if (exception(event.getSpawnType()) || ADMIN_COMMAND.get()) event.getEntity().getPersistentData().putBoolean(EnemyTags.ADMIN,true);
        else event.setSpawnCancelled(true);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void join(EntityJoinLevelEvent event) {
        if (!controlled(event.getLevel()) || !(event.getEntity() instanceof Mob mob)) return;
        var server=((ServerLevel)event.getLevel()).getServer();
        var data=ExodusSavedData.get(server);
        boolean managed=EnemyTags.managed(mob);
        boolean current=EnemyTags.valid(mob) && data.state == MatchState.RUNNING
                && EnemyTags.match(mob).equals(data.matchId) && !EnemySpawnService.retired(mob.getUUID());
        // Fresh summon with custom NBT can bypass finalizeSpawn entirely.
        if (!managed && !event.loadedFromDisk() && (ADMIN_COMMAND.get() || exception(mob.getSpawnType())))
            mob.getPersistentData().putBoolean(EnemyTags.ADMIN,true);
        if (ADMIN_COMMAND.get()) com.mojang.logging.LogUtils.getLogger().debug("Exodus administrator summon admitted: {}",mob.getType());
        if (!EnemyAdmission.allowed(managed,current,mob.getPersistentData().getBoolean(EnemyTags.ADMIN))) {
            event.setCanceled(true);
            return;
        }
        if (current) EnemySpawnService.join(mob);
    }
    @SubscribeEvent public static void leave(EntityLeaveLevelEvent event) {
        if (controlled(event.getLevel()) && event.getEntity() instanceof Mob mob && EnemyTags.managed(mob)) EnemySpawnService.leave(mob);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void target(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !EnemyTags.managed(mob) || !controlled(mob.level())) return;
        var target=event.getNewTarget();
        if (target != null && !EnemyCombat.allowed(mob,target,mob.getTarget() == target)) event.setNewTarget(null);
    }
    @SubscribeEvent public static void grief(EntityMobGriefingEvent event) {
        if (event.getEntity() instanceof ExodusZombie mob && controlled(mob.level()) && EnemyTags.managed(mob)) event.setResult(Event.Result.ALLOW);
    }
    @SubscribeEvent public static void destroy(LivingDestroyBlockEvent event) {
        if (event.getEntity() instanceof ExodusZombie mob && controlled(mob.level()) && EnemyTags.managed(mob)
                && !event.getState().is(net.minecraft.tags.BlockTags.WOODEN_DOORS)) event.setCanceled(true);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void drops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ExodusZombie mob) || !EnemyTags.managed(mob) || mob.level().isClientSide) return;
        event.getDrops().clear();
        var random=mob.getRandom();
        var drops=ZombieLootPolicy.roll(random.nextDouble(),random.nextDouble(),random.nextDouble(),
                ExodusConfig.ZOMBIE_EMERALD_CHANCE.get(),ExodusConfig.ZOMBIE_GUNPOWDER_CHANCE.get(),ExodusConfig.ZOMBIE_QUARTZ_CHANCE.get());
        if (drops.emerald()>0) event.getDrops().add(new ItemEntity(mob.level(),mob.getX(),mob.getY(),mob.getZ(),new ItemStack(Items.EMERALD,drops.emerald())));
        if (drops.gunpowder()>0) event.getDrops().add(new ItemEntity(mob.level(),mob.getX(),mob.getY(),mob.getZ(),new ItemStack(Items.GUNPOWDER,drops.gunpowder())));
        if (drops.quartz()>0) event.getDrops().add(new ItemEntity(mob.level(),mob.getX(),mob.getY(),mob.getZ(),new ItemStack(Items.QUARTZ,drops.quartz())));
    }
    @SubscribeEvent public static void stop(ServerStoppingEvent event) { EnemySpawnService.reset(); ADMIN_COMMAND.remove(); }
}
