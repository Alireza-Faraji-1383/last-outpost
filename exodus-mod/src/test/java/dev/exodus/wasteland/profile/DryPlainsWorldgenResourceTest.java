package dev.exodus.wasteland.profile;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DryPlainsWorldgenResourceTest {
    private JsonObject resource(String path) throws Exception {
        try (var stream = getClass().getResourceAsStream("/data/" + path)) {
            assertNotNull(stream, path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    @Test void doesNotFloodNoiseCavitiesAndRetainsUndergroundGeneration() throws Exception {
        var settings = resource("exodus/worldgen/noise_settings/dry_plains.json");
        assertEquals("minecraft:air", settings.getAsJsonObject("default_fluid").get("Name").getAsString());
        assertFalse(settings.get("aquifers_enabled").getAsBoolean());
        assertEquals(-64, settings.get("sea_level").getAsInt());
        assertEquals("minecraft:stone", settings.getAsJsonObject("default_block").get("Name").getAsString());
        assertEquals(384, settings.getAsJsonObject("noise").get("height").getAsInt());
        assertTrue(settings.get("ore_veins_enabled").getAsBoolean());
        var router = settings.getAsJsonObject("noise_router");
        assertEquals("exodus:dry_plains/depth", router.get("depth").getAsString());
        assertTrue(router.get("final_density").toString().contains("minecraft:overworld/caves/noodle"));
        assertFalse(router.get("final_density").toString().contains("minecraft:overworld/sloped_cheese"));
    }

    @Test void usesGentleRollingTerrainInsteadOfOceanAndMountainSplines() throws Exception {
        var slope = resource("exodus/worldgen/density_function/dry_plains/sloped_cheese.json").toString();
        assertTrue(slope.contains("exodus:dry_plains/depth"));
        assertTrue(slope.contains("minecraft:overworld/base_3d_noise"));
        assertFalse(slope.contains("minecraft:overworld/jaggedness"));
        assertFalse(slope.contains("minecraft:overworld/factor"));
        var offset = resource("exodus/worldgen/density_function/dry_plains/offset.json");
        assertEquals(-0.46875, offset.get("argument1").getAsDouble());
        assertEquals(0.045, offset.getAsJsonObject("argument2").get("argument1").getAsDouble());
    }

    @Test void keepsPlainsAppearanceAndCavesWithoutWaterSpringsOrDungeons() throws Exception {
        var biome = resource("exodus/worldgen/biome/dry_plains.json");
        assertEquals(0.8, biome.get("temperature").getAsDouble());
        assertEquals(7907327, biome.getAsJsonObject("effects").get("sky_color").getAsInt());
        var features = biome.getAsJsonArray("features").toString();
        assertTrue(features.contains("minecraft:trees_plains"));
        assertTrue(features.contains("minecraft:ore_iron_middle"));
        assertFalse(features.contains("minecraft:spring_water"));
        assertFalse(features.contains("minecraft:monster_room"));
        assertFalse(features.contains("minecraft:amethyst_geode"));
        assertTrue(biome.getAsJsonObject("carvers").toString().contains("minecraft:cave"));
    }

    @Test void addsLostCitiesOnlyToIsolatedBiomeWithoutVanillaStructureTags() throws Exception {
        var modifier = resource("exodus/forge/biome_modifier/dry_plains_cities.json");
        assertEquals("forge:add_features", modifier.get("type").getAsString());
        assertEquals("exodus:dry_plains", modifier.get("biomes").getAsString());
        assertEquals("lostcities:lostcities", modifier.get("features").getAsString());
        assertEquals("raw_generation", modifier.get("step").getAsString());
        // Vanilla 1.20.1 structures all require biome tags; this biome intentionally joins none.
        var root = Path.of("src/main/resources/data");
        try (var paths = Files.walk(root)) {
            for (Path file : paths.filter(p -> p.toString().replace('\\', '/').contains("/tags/worldgen/biome/"))
                    .filter(p -> p.toString().endsWith(".json")).toList()) {
                assertFalse(Files.readString(file).contains("exodus:dry_plains"), file.toString());
            }
        }
        assertFalse(Files.exists(root.resolve("minecraft/worldgen/noise_settings/overworld.json")));
        assertFalse(Files.exists(root.resolve("minecraft/worldgen/biome/plains.json")));
    }
}
