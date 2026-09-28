package dev.exodus.wasteland.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProgressMilestonesTest {
    @Test
    void emitsEveryTenPercentBoundaryCrossed() {
        assertEquals(List.of(10), ProgressMilestones.crossed(9, 10, 100));
        assertEquals(List.of(20, 30), ProgressMilestones.crossed(19, 31, 100));
        assertEquals(List.of(100), ProgressMilestones.crossed(99, 100, 100));
    }

    @Test
    void doesNotRepeatBoundariesAfterResume() {
        assertEquals(List.of(), ProgressMilestones.crossed(37, 37, 100));
        assertEquals(List.of(40), ProgressMilestones.crossed(37, 41, 100));
    }

    @Test
    void validatesCounters() {
        assertThrows(IllegalArgumentException.class, () -> ProgressMilestones.crossed(-1, 1, 100));
        assertThrows(IllegalArgumentException.class, () -> ProgressMilestones.crossed(2, 1, 100));
        assertThrows(IllegalArgumentException.class, () -> ProgressMilestones.crossed(0, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> ProgressMilestones.crossed(0, 101, 100));
    }
}
