package dev.exodus.wasteland.profile;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FixedCityZoningTest {
    @Test void zoningUsesDistanceFromWholeCityForEveryArena() {
        var layout = FixedCityLayout.load();
        for (int ordinal = 0; ordinal < layout.count(); ordinal++) {
            var center = layout.arena(ordinal);
            long x = center.x() + layout.offsetX(), z = center.z() + layout.offsetZ();
            assertEquals("exodus:residential", FixedCityZoning.styleAt(x + 320, z + 16));
            assertEquals("exodus:outskirts", FixedCityZoning.styleAt(x + 560, z + 16));
            assertNull(FixedCityZoning.styleAt(x + 1000, z));
        }
    }
}
