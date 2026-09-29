package dev.exodus.wasteland.placement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SurfacePlacementPolicyTest {
    @Test
    void rejectsSingleHighOutlier() {
        var supports = new ArrayList<Integer>();
        for (int i = 0; i < 99; i++) supports.add(64);
        supports.add(120);
        var result = SurfacePlacementPolicy.select(supports, 100, 50, 2, 55, 70);
        assertEquals(64, result.platformY());
        assertEquals(99, result.quorumCount());
        assertEquals(99, result.quorumPercent());
        assertEquals(64, result.minimumSupportY());
        assertEquals(120, result.maximumSupportY());
    }

    @Test
    void rejectsNeighboringHeightSingletonInsideToleranceBand() {
        var supports = new ArrayList<Integer>();
        supports.addAll(Collections.nCopies(99, 64));
        supports.add(65);
        var result = SurfacePlacementPolicy.select(supports, 100, 50, 2, 55, 70);
        assertEquals(64, result.platformY());
    }

    @Test
    void choosesGreaterHeightWhenItHasStrongerSupport() {
        var supports = new ArrayList<Integer>();
        supports.addAll(Collections.nCopies(50, 62));
        supports.addAll(Collections.nCopies(51, 74));
        var result = SurfacePlacementPolicy.select(supports, 101, 50, 2, 55, 70);
        assertEquals(74, result.platformY());
        assertEquals(51, result.quorumCount());
    }

    @Test
    void preferredBandBreaksEqualScoreTie() {
        var result = SurfacePlacementPolicy.select(List.of(60, 60, 72, 72), 4, 50, 0, 55, 70);
        assertEquals(60, result.platformY());
    }

    @Test
    void unsupportedColumnsCountAgainstQuorum() {
        assertThrows(IllegalStateException.class,
                () -> SurfacePlacementPolicy.select(Collections.nCopies(49, 64), 100, 50, 2, 55, 70));
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> SurfacePlacementPolicy.select(List.of(), 1, 50, 2, 55, 70));
        assertThrows(IllegalArgumentException.class,
                () -> SurfacePlacementPolicy.select(List.of(64), 1, 0, 2, 55, 70));
        assertThrows(IllegalArgumentException.class,
                () -> SurfacePlacementPolicy.select(List.of(64), 1, 50, -1, 55, 70));
        assertThrows(IllegalArgumentException.class,
                () -> SurfacePlacementPolicy.select(List.of(64), 1, 50, 2, 71, 70));
    }
}
