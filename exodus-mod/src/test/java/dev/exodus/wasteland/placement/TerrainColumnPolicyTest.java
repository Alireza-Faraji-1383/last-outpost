package dev.exodus.wasteland.placement;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TerrainColumnPolicyTest {
    @Test
    void airFluidLeavesAndReplaceablePlantsAreNotSurfaceSupport() {
        assertFalse(TerrainColumnPolicy.isSurfaceSupport(TerrainColumnPolicy.StateKind.AIR));
        assertFalse(TerrainColumnPolicy.isSurfaceSupport(TerrainColumnPolicy.StateKind.FLUID));
        assertFalse(TerrainColumnPolicy.isSurfaceSupport(TerrainColumnPolicy.StateKind.LEAVES));
        assertFalse(TerrainColumnPolicy.isSurfaceSupport(TerrainColumnPolicy.StateKind.REPLACEABLE));
    }

    @Test
    void stoneDirtAndBuildingBlocksAreSupport() {
        assertTrue(TerrainColumnPolicy.isSurfaceSupport(TerrainColumnPolicy.StateKind.SOLID));
    }

    @Test
    void foundationStopsAtFirstSolidAndHonorsDepthLimit() {
        var column = List.of(TerrainColumnPolicy.StateKind.AIR, TerrainColumnPolicy.StateKind.FLUID,
                TerrainColumnPolicy.StateKind.AIR, TerrainColumnPolicy.StateKind.AIR,
                TerrainColumnPolicy.StateKind.SOLID);
        assertEquals(4, TerrainColumnPolicy.fillCountKinds(column, 96));
        assertThrows(IllegalStateException.class, () -> TerrainColumnPolicy.fillCountKinds(
                Collections.nCopies(97, TerrainColumnPolicy.StateKind.AIR), 96));
    }

    @Test
    void foundationRejectsNonPositiveDepth() {
        assertThrows(IllegalArgumentException.class,
                () -> TerrainColumnPolicy.fillCountKinds(List.of(TerrainColumnPolicy.StateKind.SOLID), 0));
    }
}
