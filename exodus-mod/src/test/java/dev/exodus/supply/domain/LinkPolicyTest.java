package dev.exodus.supply.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LinkPolicyTest {
    @Test void rejectsASecondEndpointAlreadyLinkedElsewhere() {
        LinkCandidate candidate = new LinkCandidate(true, true, true, true, 64.0, 128, false, true);

        assertEquals(LinkPolicy.Result.ENDPOINT_OCCUPIED, LinkPolicy.evaluate(candidate));
    }

    @Test void acceptsAnExactExistingPairForTakeover() {
        LinkCandidate candidate = new LinkCandidate(true, true, true, true, 64.0, 128, true, true);

        assertEquals(LinkPolicy.Result.ALLOW, LinkPolicy.evaluate(candidate));
    }

    @Test void reportsEligibilityAndGeometryFailuresInStableOrder() {
        assertEquals(LinkPolicy.Result.NO_MATCH,
                LinkPolicy.evaluate(new LinkCandidate(false, false, false, false, 999, 128, false, false)));
        assertEquals(LinkPolicy.Result.NOT_ACTIVE_PLAYER,
                LinkPolicy.evaluate(new LinkCandidate(true, false, false, false, 999, 128, false, false)));
        assertEquals(LinkPolicy.Result.WRONG_DIMENSION,
                LinkPolicy.evaluate(new LinkCandidate(true, true, false, false, 999, 128, false, false)));
        assertEquals(LinkPolicy.Result.OUTSIDE_BORDER,
                LinkPolicy.evaluate(new LinkCandidate(true, true, true, false, 999, 128, false, false)));
        assertEquals(LinkPolicy.Result.TOO_FAR,
                LinkPolicy.evaluate(new LinkCandidate(true, true, true, true, 128.01, 128, false, false)));
    }
}
