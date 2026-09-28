package dev.exodus.wasteland.domain;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class ArenaGridTest {
    @Test
    void walksAnOutwardSquareSpiralWithoutUsingLobbyOrigin() {
        int spacing = 4096;

        assertEquals(new ArenaGrid.Center(4096, 0), ArenaGrid.centerFor(0, spacing));
        assertEquals(new ArenaGrid.Center(4096, 4096), ArenaGrid.centerFor(1, spacing));
        assertEquals(new ArenaGrid.Center(0, 4096), ArenaGrid.centerFor(2, spacing));
        assertEquals(new ArenaGrid.Center(-4096, 4096), ArenaGrid.centerFor(3, spacing));
        assertEquals(new ArenaGrid.Center(-4096, 0), ArenaGrid.centerFor(4, spacing));
        assertEquals(new ArenaGrid.Center(-4096, -4096), ArenaGrid.centerFor(5, spacing));
        assertEquals(new ArenaGrid.Center(0, -4096), ArenaGrid.centerFor(6, spacing));
        assertEquals(new ArenaGrid.Center(4096, -4096), ArenaGrid.centerFor(7, spacing));
        assertEquals(new ArenaGrid.Center(8192, -4096), ArenaGrid.centerFor(8, spacing));
    }

    @Test
    void generatedBoundsNeverOverlapForSequentialCells() {
        ArenaGeometry geometry = ArenaGeometry.of(2000, 128, 1024);
        var seen = new HashSet<ArenaGrid.Center>();

        for (long ordinal = 0; ordinal < 200; ordinal++) {
            ArenaGrid.Center center = ArenaGrid.centerFor(ordinal, geometry.gridSpacing());
            assertTrue(seen.add(center));
            for (ArenaGrid.Center previous : seen) {
                if (previous.equals(center)) continue;
                boolean separatedX = Math.abs(center.x() - previous.x()) >= geometry.generationSize();
                boolean separatedZ = Math.abs(center.z() - previous.z()) >= geometry.generationSize();
                assertTrue(separatedX || separatedZ, () -> center + " overlaps " + previous);
            }
        }
    }

    @Test
    void rejectsNegativeOrdinalsAndInvalidSpacing() {
        assertThrows(IllegalArgumentException.class, () -> ArenaGrid.centerFor(-1, 4096));
        assertThrows(IllegalArgumentException.class, () -> ArenaGrid.centerFor(0, 0));
    }
}
