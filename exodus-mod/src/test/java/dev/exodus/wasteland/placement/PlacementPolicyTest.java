package dev.exodus.wasteland.placement;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlacementPolicyTest {
    @Test void createsDeterministicOpposingBasesAndCampCounts() {
        PlacementPlan first = PlacementPolicy.plan(42, 0, 0, 2000, 6, 3, 120,
                List.of("exodus:abandoned_camp_01"), List.of("exodus:occupied_camp_01"));
        PlacementPlan second = PlacementPolicy.plan(42, 0, 0, 2000, 6, 3, 120,
                List.of("exodus:abandoned_camp_01"), List.of("exodus:occupied_camp_01"));
        assertEquals(first, second);
        assertEquals(11, first.entries().size());
        assertTrue(first.entries().get(0).x() < 0);
        assertTrue(first.entries().get(1).x() > 0);
        assertTrue(first.entries().subList(0, 2).stream().allMatch(PlacementPlan.Entry::aggressiveTerrainRepair));
    }
    @Test void acceptsOnlyTwoBlockNaturalCampSlope() {
        assertTrue(PlacementPolicy.acceptableNaturalSlope(70, 72));
        assertFalse(PlacementPolicy.acceptableNaturalSlope(70, 73));
    }
    @Test void detectsExpandedOverlap() {
        PlacementPlan.Entry one = entry(0, 0), two = entry(14, 0);
        assertFalse(PlacementPolicy.overlaps(one, two, 1));
        assertTrue(PlacementPolicy.overlaps(one, two, 2));
    }
    private static PlacementPlan.Entry entry(int x, int z) {
        return new PlacementPlan.Entry("id", "exodus:test", PlacementPlan.Kind.ABANDONED_CAMP,
                x, z, 11, 16, PlacementPlan.Rotation.NONE, false);
    }
}
