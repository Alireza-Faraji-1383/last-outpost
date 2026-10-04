package dev.exodus.wasteland.profile;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FixedCityResourcesTest {
    static JsonObject resource(String name) throws Exception {
        try (var input = FixedCityResourcesTest.class.getResourceAsStream(name)) {
            assertNotNull(input, name);
            return JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
    @Test void shipsEveryCityWithCorrectCoordinatesAndSupportedAssets() throws Exception {
        var layout = FixedCityLayout.load();
        for (int i = 0; i < layout.count(); i++) {
            var city = resource("/data/exodus/lostcities/predefinedcities/arena_" + i + ".json");
            var center = layout.arena(i);
            assertEquals("lostcities:lostcity", city.get("dimension").getAsString());
            assertEquals((center.x() + layout.offsetX()) / 16, city.get("chunkx").getAsLong());
            assertEquals((center.z() + layout.offsetZ()) / 16, city.get("chunkz").getAsLong());
            assertEquals(layout.radius(), city.get("radius").getAsInt());
            assertEquals("exodus:residential", city.get("citystyle").getAsString());
            assertTrue(city.getAsJsonArray("buildings").size() >= 4);
            for (var neighborhood : layout.neighborhoods()) {
                var asset = resource("/data/exodus/lostcities/predefinedcities/arena_"+i+neighborhood.suffix()+".json");
                assertEquals((center.x()+layout.offsetX()+neighborhood.x())/16, asset.get("chunkx").getAsLong());
                assertEquals((center.z()+layout.offsetZ()+neighborhood.z())/16, asset.get("chunkz").getAsLong());
                assertEquals(layout.radius(),asset.get("radius").getAsInt());
                assertEquals(neighborhood.style(),asset.get("citystyle").getAsString());
            }
        }
        for (String name : new String[]{"residential", "outskirts"}) {
            var style = resource("/data/exodus/lostcities/citystyles/" + name + ".json");
            assertEquals("citystyle_standard", style.get("inherit").getAsString());
        }
        var tower = resource("/data/exodus/lostcities/buildings/tower.json");
        assertEquals(8, tower.get("minfloors").getAsInt());
        assertEquals(12, tower.get("maxfloors").getAsInt());
        assertFalse(tower.getAsJsonArray("parts").isEmpty());
        assertNotNull(resource("/data/exodus/lostcities/worldstyles/wasteland.json").get("citystyles"));
    }
}
