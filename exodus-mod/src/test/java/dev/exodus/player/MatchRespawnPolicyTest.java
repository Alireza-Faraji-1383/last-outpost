package dev.exodus.player;

import dev.exodus.domain.Association;
import dev.exodus.MatchState;
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

    @Test void cleanupResetRequiresACommittedMatchPlayerRespawn() {
        assertFalse(MatchRespawnPolicy.hasCommittedRespawns(MatchState.STARTING));
        assertTrue(MatchRespawnPolicy.hasCommittedRespawns(MatchState.RUNNING));
        assertTrue(MatchRespawnPolicy.hasCommittedRespawns(MatchState.ENDING));
        assertFalse(MatchRespawnPolicy.resetOnCleanup(MatchState.STARTING, Association.MATCH_PLAYER));
        assertTrue(MatchRespawnPolicy.resetOnCleanup(MatchState.RUNNING, Association.MATCH_PLAYER));
        assertTrue(MatchRespawnPolicy.resetOnCleanup(MatchState.ENDING, Association.MATCH_PLAYER));
        assertTrue(MatchRespawnPolicy.resetOnCleanup(MatchState.ENDING, Association.AUTO_SPECTATOR));
        assertFalse(MatchRespawnPolicy.resetOnCleanup(MatchState.ENDING, Association.INITIAL_SPECTATOR));
    }
}
