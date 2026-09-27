package dev.exodus.domain;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PlayerLifecyclePolicyTest {
    @Test void onlySurvivalPlayersInMatchDimensionAreEligible() {
        assertTrue(PlayerLifecyclePolicy.isEligible(true, GameModeKind.SURVIVAL));
        assertFalse(PlayerLifecyclePolicy.isEligible(true, GameModeKind.CREATIVE));
        assertFalse(PlayerLifecyclePolicy.isEligible(false, GameModeKind.SURVIVAL));
    }

    @Test void graceExpiresAtConfiguredDeadline() {
        var pending = new PendingPlayer(UUID.randomUUID(), 1_000L);
        assertFalse(PlayerLifecyclePolicy.isExpired(pending, 999L));
        assertTrue(PlayerLifecyclePolicy.isExpired(pending, 1_000L));
    }

    @Test void cleanupOnlyForcesPlayersAndAutomaticSpectatorsToSurvival() {
        assertTrue(PlayerLifecyclePolicy.forceSurvivalOnCleanup(Association.MATCH_PLAYER));
        assertTrue(PlayerLifecyclePolicy.forceSurvivalOnCleanup(Association.AUTO_SPECTATOR));
        assertFalse(PlayerLifecyclePolicy.forceSurvivalOnCleanup(Association.INITIAL_SPECTATOR));
    }
}
