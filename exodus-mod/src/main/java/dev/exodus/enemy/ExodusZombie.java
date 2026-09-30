package dev.exodus.enemy;

import dev.exodus.ExodusConfig;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

/** Vanilla appearance and melee behavior; sunlight protection is specific to this entity. */
public final class ExodusZombie extends Zombie {
    public ExodusZombie(EntityType<? extends Zombie> type, Level level) { super(type, level); }
    @Override protected boolean isSunSensitive() { return false; }
    @Override protected boolean shouldDespawnInPeaceful() { return false; }
    @Override public void setBaby(boolean baby) { super.setBaby(false); }
    @Override protected boolean convertsInWater() { return false; }
    public void configureManagedGoals() {
        setBaby(false);
        setCanPickUpLoot(false);
        for (var slot : EquipmentSlot.values()) setItemSlot(slot, net.minecraft.world.item.ItemStack.EMPTY);
        setCanBreakDoors(true);
        goalSelector.removeAllGoals(goal -> goal instanceof BreakDoorGoal);
        goalSelector.addGoal(1, new BreakDoorGoal(this, ExodusConfig.ZOMBIE_DOOR_BREAK_TICKS.get(), difficulty -> true));
        if (getNavigation() instanceof GroundPathNavigation ground) ground.setCanOpenDoors(true);
        targetSelector.removeAllGoals(goal -> true);
        targetSelector.addGoal(1, new EnemyCombat.ZombieTargetGoal(this));
    }
}
