package dev.exodus.teleporter.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComponentCraftingStorageTest {
    @Test void craftingResultIsNotAStorageContainer() {
        assertFalse(ComponentStoragePolicy.shouldEject(false, false, true));
    }
    @Test void chestsRemainRestrictedAndPlayerInventoryAndDevicesRemainAllowed() {
        assertTrue(ComponentStoragePolicy.shouldEject(false, false, false));
        assertFalse(ComponentStoragePolicy.shouldEject(true, false, false));
        assertFalse(ComponentStoragePolicy.shouldEject(false, true, false));
    }
}
