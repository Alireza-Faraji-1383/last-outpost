package dev.exodus.wasteland.access;

import org.junit.jupiter.api.Test;
import static dev.exodus.wasteland.access.DimensionAccessPolicy.Decision.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DimensionAccessPolicyTest {
    @Test void enforcesMembershipAndOperatorBypass() {
        assertEquals(REJECT, DimensionAccessPolicy.decide(false, false, false, false));
        assertEquals(ADMIT_PLAYER, DimensionAccessPolicy.decide(true, true, false, false));
        assertEquals(ADMIT_SPECTATOR, DimensionAccessPolicy.decide(true, false, true, false));
        assertEquals(REJECT, DimensionAccessPolicy.decide(true, false, false, false));
        assertEquals(ADMIT_OPERATOR_VISIT, DimensionAccessPolicy.decide(false, false, false, true));
    }
}
