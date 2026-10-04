package dev.exodus.wasteland.profile;

import dev.exodus.wasteland.domain.ArenaGrid;
import dev.exodus.wasteland.domain.ArenaGeometry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FixedCityLayoutTest {
    @Test void citiesMatchAllArenaGridEntriesAndStayInsidePlayableBorder() {
        var layout = FixedCityLayout.load();
        assertEquals(64, layout.count());
        assertEquals(2, layout.version());
        for (int i = 0; i < layout.count(); i++) {
            var arena = layout.arena(i);
            assertEquals(ArenaGrid.centerFor(i, 4096), arena);
            assertTrue(layout.effectiveRadius() + Math.abs(layout.offsetZ()) + 16 < 1000);
        }
        int urbanSamples = 0;
        for (int x=-960; x<=960; x+=96) for (int z=-960; z<=960; z+=96) {
            if (layout.cityFactor(x,z) > layout.threshold()) urbanSamples++;
        }
        double coverage = urbanSamples / 441.0;
        assertTrue(coverage > .40 && coverage < .45);
        assertEquals(69, layout.neighborhoods().size());
        assertTrue(layout.radius() <= 256, "Keep neighborhood scan cost bounded");
        assertFalse(layout.intersectsCity(0, 0, -528, 770, -472, 829, 16));
        assertFalse(layout.intersectsCity(0, 0, 472, 770, 528, 829, 16));
        assertTrue(layout.intersectsCity(0, 0, -5, -165, 5, -155, 16));
    }
    @Test void rejectsExhaustionAndIncompatibleConfiguration() {
        var layout = FixedCityLayout.load();
        assertThrows(IllegalStateException.class, () -> layout.arena(64));
        assertThrows(IllegalStateException.class, () -> layout.arena(-1));
        assertDoesNotThrow(() -> layout.validateGeometry(ArenaGeometry.of(2000, 128, 1024)));
        assertThrows(IllegalStateException.class, () -> layout.validateGeometry(ArenaGeometry.of(3000, 128, 1024)));
        assertThrows(IllegalStateException.class, () -> layout.validateGeometry(ArenaGeometry.of(2000, 256, 1024)));
    }
}
