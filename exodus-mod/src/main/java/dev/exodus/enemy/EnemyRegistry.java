package dev.exodus.enemy;

import dev.exodus.ExodusMod;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

public final class EnemyRegistry {
    private static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ExodusMod.MOD_ID);
    public static final RegistryObject<EntityType<ExodusZombie>> ZOMBIE = TYPES.register("zombie",
            () -> EntityType.Builder.of(ExodusZombie::new, MobCategory.MONSTER).sized(.6f, 1.95f).clientTrackingRange(8).build("exodus:zombie"));
    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.addListener(EnemyRegistry::attributes);
    }
    private static void attributes(EntityAttributeCreationEvent event) { event.put(ZOMBIE.get(), Zombie.createAttributes().build()); }
}
