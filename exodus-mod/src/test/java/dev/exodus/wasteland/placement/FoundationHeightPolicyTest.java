package dev.exodus.wasteland.placement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class FoundationHeightPolicyTest {
    @Test void supportStartsBelowStructureOwnedBottomLayer() {
        assertEquals(63, FoundationHeightPolicy.supportTopY(64));
        assertEquals(62, FoundationHeightPolicy.fillStartY(64));
        assertEquals(-1, FoundationHeightPolicy.supportTopY(0));
        assertNotEquals(64, FoundationHeightPolicy.supportTopY(64));
    }

    @Test void buriedPlayerBaseUsesActualStructureBottom() {
        assertEquals(66, FoundationHeightPolicy.structureBottomY(72, 6));
        assertEquals(65, FoundationHeightPolicy.supportTopY(
                FoundationHeightPolicy.structureBottomY(72, 6)));
    }
}
