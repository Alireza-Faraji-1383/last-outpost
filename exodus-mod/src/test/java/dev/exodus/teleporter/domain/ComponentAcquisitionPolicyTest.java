package dev.exodus.teleporter.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static dev.exodus.teleporter.domain.ComponentAcquisitionPolicy.Action.*;

class ComponentAcquisitionPolicyTest {
    @Test void unboundCraftableComponentsFromGiveAndLootBindWithoutUniqueClaims() {
        assertEquals(BIND, ComponentAcquisitionPolicy.action(false, false, false));
        assertEquals(BIND, ComponentAcquisitionPolicy.action(false, false, true));
    }

    @Test void rareComponentsRemainUniqueAndBoundComponentsAreNotRebound() {
        assertEquals(CLAIM_RARE, ComponentAcquisitionPolicy.action(false, true, false));
        assertEquals(DELETE, ComponentAcquisitionPolicy.action(false, true, true));
        assertEquals(KEEP, ComponentAcquisitionPolicy.action(true, false, false));
        assertEquals(KEEP, ComponentAcquisitionPolicy.action(true, true, true));
    }
}
