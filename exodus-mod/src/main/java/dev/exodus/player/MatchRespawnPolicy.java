package dev.exodus.player;

import dev.exodus.domain.Association;

public final class MatchRespawnPolicy {
    private MatchRespawnPolicy() {}

    public static boolean assignBase(Association association, boolean committed, boolean hasBase) {
        return committed && hasBase && association == Association.MATCH_PLAYER;
    }
}
