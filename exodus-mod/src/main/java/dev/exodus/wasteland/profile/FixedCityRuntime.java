package dev.exodus.wasteland.profile;

import dev.exodus.wasteland.lostcities.LostCitiesIntegration;
import mcjty.lostcities.config.ProfileSetup;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.lost.City;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

/** Bounded validation at the world-generation boundary, without candidate search. */
public final class FixedCityRuntime {
    private FixedCityRuntime() {}
    public static void validate(ServerLevel level, FixedCityLayout layout, long ordinal) {
        var center = layout.arena(ordinal);
        if (!"exodus".equals(mcjty.lostcities.setup.Config.getProfileForDimension(level.dimension()))) {
            throw new IllegalStateException("Fixed arena dimension is not using the Exodus profile");
        }
        var profile = ProfileSetup.STANDARD_PROFILES.get("exodus");
        if (profile == null || profile.isSpace() || profile.isSpheres() || profile.CITY_CHANCE != 0
                || profile.CITY_MAXRADIUS < layout.radius() || profile.GENERATE_SPAWNERS
                || !"exodus:wasteland".equals(profile.getWorldStyle())) {
            throw new IllegalStateException("Exodus fixed city profile is missing or incompatible");
        }
        for (var neighborhood : layout.neighborhoods()) {
            var city = City.getPredefinedCity(level, new ChunkCoord(level.dimension(),
                    Math.toIntExact((center.x()+layout.offsetX()+neighborhood.x()) >> 4),
                    Math.toIntExact((center.z()+layout.offsetZ()+neighborhood.z()) >> 4)));
            String id=layout.cityId(ordinal)+neighborhood.suffix();
            if (city == null || !id.equals(city.getId().toString()) || city.getRadius()!=layout.radius()
                    || !neighborhood.style().equals(city.getCityStyle())) {
                throw new IllegalStateException("Missing or incompatible predefined city neighborhood " + id);
            }
        }
        AssetRegistries.CITYSTYLES.getOrThrow(level,"exodus:residential");
        AssetRegistries.CITYSTYLES.getOrThrow(level,"exodus:outskirts");
        AssetRegistries.BUILDINGS.getOrThrow(level,"exodus:tower");
        AssetRegistries.WORLDSTYLES.getOrThrow(level,"exodus:wasteland");
    }
    public static void requireFresh(ServerLevel level, FixedCityLayout layout, long ordinal) {
        var center = layout.arena(ordinal);
        int half = layout.arenaSize()/2 + layout.buffer();
        int minX = Math.toIntExact((center.x()-half) >> 4), minZ = Math.toIntExact((center.z()-half) >> 4);
        int maxX = Math.toIntExact((center.x()+half-1) >> 4), maxZ = Math.toIntExact((center.z()+half-1) >> 4);
        try {
            var path = level.getServer().getWorldPath(LevelResource.ROOT).resolve("dimensions/lostcities/lostcity/region");
            if (RegionGenerationGuard.hasGeneratedChunks(path,minX,minZ,maxX,maxZ)) throw usedRegion();
            for (int x=minX; x<=maxX; x++) for (int z=minZ; z<=maxZ; z++) {
                if (level.getChunkSource().getChunkNow(x,z) != null) throw usedRegion();
            }
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Cannot verify unused fixed city region", exception);
        }
    }
    private static IllegalStateException usedRegion() {
        return new IllegalStateException("Fixed arena region already contains generated chunks; use a fresh world for the new city layout");
    }
}
