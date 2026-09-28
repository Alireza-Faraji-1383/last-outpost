package dev.exodus.teleporter.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RareClaimPolicyTest {
    @Test void onlyFirstTemplateClaimOfEachRareTypeSucceeds() {
        assertEquals(RareClaimPolicy.Result.CLAIM,RareClaimPolicy.claim(false,true));
        assertEquals(RareClaimPolicy.Result.DELETE_DUPLICATE,RareClaimPolicy.claim(true,true));
    }

    @Test void boundAndOrdinaryComponentsDoNotClaimAgain() {
        assertEquals(RareClaimPolicy.Result.ALREADY_BOUND,RareClaimPolicy.claim(false,false));
        assertEquals(RareClaimPolicy.Result.NOT_RARE,RareClaimPolicy.claimOrdinary());
    }
}
