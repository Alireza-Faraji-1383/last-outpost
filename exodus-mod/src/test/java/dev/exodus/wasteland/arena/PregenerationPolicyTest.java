package dev.exodus.wasteland.arena;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PregenerationPolicyTest {
    @Test void zeroBudgetDisablesPregenerationForForceStart() {
        assertFalse(PregenerationPolicy.enabled(0));
        assertTrue(PregenerationPolicy.enabled(1));
    }
}
