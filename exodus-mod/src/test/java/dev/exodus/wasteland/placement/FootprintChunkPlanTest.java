package dev.exodus.wasteland.placement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FootprintChunkPlanTest {
    @Test
    void includesAllIntersectingChunksAndOneChunkMargin() {
        var plan = FootprintChunkPlan.forBounds(16, 31, 72, 90, 1);
        assertEquals(0, plan.minimumChunkX());
        assertEquals(0, plan.minimumChunkZ());
        assertEquals(5, plan.maximumChunkX());
        assertEquals(6, plan.maximumChunkZ());
        assertEquals(42, plan.total());
        assertEquals(0, plan.chunkX(0));
        assertEquals(0, plan.chunkZ(0));
        assertEquals(5, plan.chunkX(41));
        assertEquals(6, plan.chunkZ(41));
    }

    @Test
    void handlesNegativeBlockCoordinatesWithFloorDivision() {
        var plan = FootprintChunkPlan.forBounds(-17, -1, -1, 15, 1);
        assertEquals(-3, plan.minimumChunkX());
        assertEquals(-2, plan.minimumChunkZ());
        assertEquals(0, plan.maximumChunkX());
        assertEquals(1, plan.maximumChunkZ());
    }

    @Test
    void rejectsInvalidBoundsMarginAndIndex() {
        assertThrows(IllegalArgumentException.class, () -> FootprintChunkPlan.forBounds(1, 0, 0, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> FootprintChunkPlan.forBounds(0, 1, 1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> FootprintChunkPlan.forBounds(0, 0, 1, 1, -1));
        var plan = FootprintChunkPlan.forBounds(0, 0, 1, 1, 0);
        assertThrows(IndexOutOfBoundsException.class, () -> plan.chunkX(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> plan.chunkZ(plan.total()));
    }
}
