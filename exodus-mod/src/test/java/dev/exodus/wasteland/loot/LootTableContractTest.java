package dev.exodus.wasteland.loot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LootTableContractTest {
    private static final Set<String> TABLES = Set.of("general/common", "general/standard", "general/valuable",
            "general/elite", "food", "weapons", "medical", "utility", "tech");

    @Test void allMarkerTablesArePresentAndValidJson() throws Exception {
        for (String table : TABLES) {
            String path = "/data/exodus/loot_tables/chests/" + table + ".json";
            try (var stream = getClass().getResourceAsStream(path)) {
                assertNotNull(stream, path);
                JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                assertTrue(json.has("pools"));
                assertFalse(json.toString().contains("facility_alpha_key"));
                assertFalse(json.toString().contains("facility_beta_key"));
                assertFalse(json.toString().contains("dimensional_core"));
            }
        }
    }

    @Test void weaponsFavorAmmoThenAttachmentsThenGuns() throws Exception {
        String json;
        try (var stream = getClass().getResourceAsStream("/data/exodus/loot_tables/chests/weapons.json")) {
            assertNotNull(stream);
            json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertTrue(weightFor(json, "tacz:ammo") > weightFor(json, "tacz:attachment"));
        assertTrue(weightFor(json, "tacz:attachment") > weightFor(json, "tacz:modern_kinetic_gun"));
    }

    @Test void lostCitiesMappingNeverUsesEliteAndKeepsOrdinaryTables() throws Exception {
        try (var stream = getClass().getResourceAsStream("/data/lostcities/lostcities/conditions/chestloot.json")) {
            assertNotNull(stream);
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertFalse(json.contains("elite"));
            assertTrue(json.contains("general/common"));
        }
    }

    private static int weightFor(String json, String item) {
        int total = 0, cursor = 0;
        while ((cursor = json.indexOf("\"name\":\"" + item + "\"", cursor)) >= 0) {
            int at = json.indexOf("\"weight\":", cursor);
            int start = at + 9, end = start;
            while (Character.isDigit(json.charAt(end))) end++;
            total += Integer.parseInt(json.substring(start, end));
            cursor = end;
        }
        return total;
    }
}
