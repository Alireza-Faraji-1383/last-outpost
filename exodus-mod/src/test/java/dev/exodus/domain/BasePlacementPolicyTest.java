package dev.exodus.domain;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BasePlacementPolicyTest {
    private final BasePlacementPolicy policy = new BasePlacementPolicy(1000, 100, 250, 6, 13, 14);

    @Test void acceptsCandidateInsideSafeBorderWithFlatTerrain() {
        var candidate = new BaseCandidate(800, 70, 0, 4, false, true, true);
        assertTrue(policy.isValid(candidate, 0, 0, List.of(), List.of()));
    }

    @Test void rejectsCandidateWhoseFootprintCrossesSafeBorder() {
        var candidate = new BaseCandidate(887, 70, 0, 1, false, true, true);
        assertTrue(policy.isValid(candidate, 0, 0, List.of(), List.of()));
        candidate = new BaseCandidate(887, 70, 887, 1, false, true, true);
        assertFalse(policy.isValid(candidate, 0, 0, List.of(), List.of()));
    }

    @Test void acceptsAnySurfaceWhenTheVolumeAboveTheBaseIsClear() {
        assertTrue(policy.isValid(new BaseCandidate(0, 70, 0, 40, true, false, true), 0, 0, List.of(), List.of()));
    }

    @Test void rejectsABlockedOrFloodedVolumeAboveTheBase() {
        assertFalse(policy.isValid(new BaseCandidate(0, 70, 0, 1, false, true, false), 0, 0, List.of(), List.of()));
    }

    @Test void rejectsCurrentAndPersistedBasesCloserThanMinimumDistance() {
        var candidate = new BaseCandidate(0, 70, 0, 1, false, true, true);
        assertFalse(policy.isValid(candidate, 0, 0, List.of(new BasePoint(100, 0)), List.of()));
        assertFalse(policy.isValid(candidate, 0, 0, List.of(), List.of(new BasePoint(0, 249))));
        assertTrue(policy.isValid(candidate, 0, 0, List.of(new BasePoint(250, 0)), List.of()));
    }
}
