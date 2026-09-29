package dev.exodus.player;

import dev.exodus.domain.Association;
import dev.exodus.MatchState;

public final class MatchRespawnPolicy {
    private MatchRespawnPolicy() {}

    public static boolean assignBase(Association association, boolean committed, boolean hasBase) {
        return committed && hasBase && association == Association.MATCH_PLAYER;
    }

    public static boolean hasCommittedRespawns(MatchState state) {
        return state == MatchState.RUNNING || state == MatchState.ENDING;
    }

    public static boolean resetOnCleanup(MatchState state, Association association) {
        return hasCommittedRespawns(state)
                && (association == Association.MATCH_PLAYER || association == Association.AUTO_SPECTATOR);
    }
}
