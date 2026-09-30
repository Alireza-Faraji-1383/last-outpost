package dev.exodus.wasteland.lostcities;

import dev.exodus.wasteland.profile.WastelandProfileDefinition;
import mcjty.lostcities.api.ILostCities;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

import java.util.function.Function;

public final class LostCitiesIntegration {
    private static final LostCitiesLifecycle LIFECYCLE = new LostCitiesLifecycle();
    public static final ResourceKey<Level> WASTELAND_DIMENSION = ResourceKey.create(
            Registries.DIMENSION, new ResourceLocation("lostcities", "lostcity"));

    private LostCitiesIntegration() {}

    public static void enqueue(InterModEnqueueEvent ignored) {
        InterModComms.sendTo(ILostCities.LOSTCITIES, ILostCities.GET_LOST_CITIES,
                () -> (Function<ILostCities, Void>) api -> {
                    acceptApi(api);
                    return null;
                });
    }

    static void acceptApi(ILostCities api) {
        LIFECYCLE.acceptApi(api);
    }

    public static void registerDimensionAfterConfigsLoaded() {
        WastelandProfileDefinition.ensureRuntimeProfile();
        LIFECYCLE.registerDimension(WASTELAND_DIMENSION, WastelandProfileDefinition.PROFILE_NAME);
    }

    public static java.util.Optional<LostCitiesBridge> bridge(){return LIFECYCLE.api().map(LostCitiesApiBridge::new);}
}
