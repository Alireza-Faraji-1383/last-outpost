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
        WastelandProfileDefinition.ensureInstalled();
        // Lost Cities keeps its value private; access it through Forge's public config specification.
        @SuppressWarnings("unchecked")
        net.minecraftforge.common.ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> config =
                (net.minecraftforge.common.ForgeConfigSpec.ConfigValue<java.util.List<? extends String>>)
                        mcjty.lostcities.setup.Config.COMMON_CONFIG.getValues().get("profiles.dimensionsWithProfiles");
        config.set(DimensionProfileBinding.replace(config.get(), WASTELAND_DIMENSION.location().toString(),
                WastelandProfileDefinition.PROFILE_NAME));
        mcjty.lostcities.setup.Config.resetProfileCache();
        LIFECYCLE.registerDimension(WASTELAND_DIMENSION, WastelandProfileDefinition.PROFILE_NAME);
        if (!WastelandProfileDefinition.PROFILE_NAME.equals(mcjty.lostcities.setup.Config.getProfileForDimension(WASTELAND_DIMENSION))) {
            throw new IllegalStateException("Exodus fixed city profile was not bound to lostcities:lostcity");
        }
        com.mojang.logging.LogUtils.getLogger().info("[Exodus] Fixed city profile exodus bound to lostcities:lostcity; glass domes and building spawners disabled.");
    }

    public static java.util.Optional<LostCitiesBridge> bridge(){return LIFECYCLE.api().map(LostCitiesApiBridge::new);}
}
