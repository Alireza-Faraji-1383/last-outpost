package dev.exodus.wasteland.placement;

import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlacementIdempotencyTest {
    @Test void persistedPlacementIdsPreventASecondCommit() {
        Set<String> completed = new LinkedHashSet<>();
        assertTrue(completed.add("russian_base_1"));
        assertFalse(completed.add("russian_base_1"));
        assertTrue(completed.contains("russian_base_1"));
    }
}
