package dev.exodus.wasteland.lostcities;

import dev.exodus.wasteland.profile.WastelandProfileDefinition;
import mcjty.lostcities.api.ILostCities;
import mcjty.lostcities.api.ILostCitiesPre;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

import java.util.function.Function;

public final class LostCitiesIntegration {
    public static final ResourceKey<Level> WASTELAND_DIMENSION = ResourceKey.create(
            Registries.DIMENSION, new ResourceLocation("lostcities", "lostcity"));

    private LostCitiesIntegration() {}

    public static void enqueue(InterModEnqueueEvent ignored) {
        InterModComms.sendTo(ILostCities.LOSTCITIES, ILostCities.GET_LOST_CITIES_PRE,
                () -> (Function<ILostCitiesPre, Void>) pre -> {
                    pre.registerProfileSetupCallback(WastelandProfileDefinition::register);
                    return null;
                });
        InterModComms.sendTo(ILostCities.LOSTCITIES, ILostCities.GET_LOST_CITIES,
                () -> (Function<ILostCities, Void>) api -> {
                    api.registerDimension(WASTELAND_DIMENSION, WastelandProfileDefinition.PROFILE_NAME);
                    return null;
                });
    }
}
