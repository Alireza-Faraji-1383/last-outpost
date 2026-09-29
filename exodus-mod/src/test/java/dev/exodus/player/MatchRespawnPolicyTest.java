package dev.exodus.player;

import dev.exodus.domain.Association;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchRespawnPolicyTest {
    @Test void assignsCommittedMatchPlayerWithBase() {
        assertTrue(MatchRespawnPolicy.assignBase(Association.MATCH_PLAYER, true, true));
    }

    @Test void rejectsUncommittedMissingBaseAndSpectatorAssignments() {
        assertFalse(MatchRespawnPolicy.assignBase(Association.MATCH_PLAYER, false, true));
        assertFalse(MatchRespawnPolicy.assignBase(Association.MATCH_PLAYER, true, false));
        assertFalse(MatchRespawnPolicy.assignBase(Association.INITIAL_SPECTATOR, true, true));
        assertFalse(MatchRespawnPolicy.assignBase(Association.AUTO_SPECTATOR, true, true));
    }
}
