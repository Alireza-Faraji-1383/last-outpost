package dev.exodus.teleporter.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ActivationPolicyTest {
    @Test void activatesOnlyACompleteFirstDevice() {
        assertEquals(ActivationPolicy.Result.INCOMPLETE,ActivationPolicy.evaluate(8,false));
        assertEquals(ActivationPolicy.Result.ACTIVATE,ActivationPolicy.evaluate(9,false));
        assertEquals(ActivationPolicy.Result.REJECT_SECOND_DEVICE,ActivationPolicy.evaluate(9,true));
    }
}
