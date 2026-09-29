package dev.exodus.wasteland.lostcities;

import mcjty.lostcities.api.ILostCities;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Optional;

final class LostCitiesLifecycle {
    private ILostCities api;

    void acceptApi(ILostCities api) {
        this.api = api;
    }

    void registerDimension(ResourceKey<Level> dimension, String profile) {
        if (api != null) {
            api.registerDimension(dimension, profile);
        }
    }

    Optional<ILostCities> api() {
        return Optional.ofNullable(api);
    }
}
