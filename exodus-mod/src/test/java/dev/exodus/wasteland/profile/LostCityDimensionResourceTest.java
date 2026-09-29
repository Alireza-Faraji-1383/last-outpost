package dev.exodus.wasteland.profile;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LostCityDimensionResourceTest {
    @Test
    void packagesFixedPlainsLostCityDimension() throws Exception {
        try (var stream = getClass().getResourceAsStream("/data/lostcities/dimension/lostcity.json")) {
            assertNotNull(stream);
            var root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("lostcities:lostcity", root.get("type").getAsString());
            var generator = root.getAsJsonObject("generator");
            assertEquals("minecraft:noise", generator.get("type").getAsString());
            assertEquals("minecraft:overworld", generator.get("settings").getAsString());
            var biome = generator.getAsJsonObject("biome_source");
            assertEquals("minecraft:fixed", biome.get("type").getAsString());
            assertEquals("minecraft:plains", biome.get("biome").getAsString());
            assertTrue(root.get("forge:use_server_seed").getAsBoolean());
        }
    }
}
