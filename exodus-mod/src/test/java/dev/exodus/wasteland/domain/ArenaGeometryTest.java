package dev.exodus.wasteland.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArenaGeometryTest {
    @Test
    void derivesDefaultGenerationAndGridSizes() {
        ArenaGeometry geometry = ArenaGeometry.of(2000, 128, 1024);

        assertEquals(2256, geometry.generationSize());
        assertEquals(4096, geometry.gridSpacing());
        assertEquals(1000, geometry.playableRadius());
        assertEquals(1128, geometry.generationRadius());
    }

    @Test
    void growsGridSpacingWithArenaSize() {
        ArenaGeometry geometry = ArenaGeometry.of(3000, 128, 1024);

        assertEquals(3256, geometry.generationSize());
        assertEquals(5120, geometry.gridSpacing());
    }

    @Test
    void rejectsInvalidOrOverflowingInputs() {
        assertThrows(IllegalArgumentException.class, () -> ArenaGeometry.of(0, 128, 1024));
        assertThrows(IllegalArgumentException.class, () -> ArenaGeometry.of(2000, -1, 1024));
        assertThrows(IllegalArgumentException.class, () -> ArenaGeometry.of(2000, 128, -1));
        assertThrows(IllegalArgumentException.class, () -> ArenaGeometry.of(Integer.MAX_VALUE, 1, 1));
    }
}
