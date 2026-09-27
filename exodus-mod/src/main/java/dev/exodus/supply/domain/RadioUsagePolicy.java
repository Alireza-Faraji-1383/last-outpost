package dev.exodus.supply.domain;

import java.util.Objects;
import java.util.UUID;

public final class RadioUsagePolicy {
    public enum Decision { ALLOW, QUOTA_EXHAUSTED, COOLDOWN }

    private RadioUsagePolicy() {}

    public static RadioUsage normalize(RadioUsage usage, UUID activeMatchId) {
        if (usage == null || !Objects.equals(usage.matchId(), activeMatchId))
            return new RadioUsage(activeMatchId, 0, 0L);
        return usage;
    }

    public static int remaining(RadioUsage usage, UUID activeMatchId, int maximum) {
        if (maximum == -1) return -1;
        return Math.max(0, maximum - normalize(usage, activeMatchId).acceptedRequests());
    }

    public static Decision evaluate(RadioUsage usage, UUID matchId, int maximum, long nowTick) {
        RadioUsage current = normalize(usage, matchId);
        if (maximum != -1 && current.acceptedRequests() >= maximum) return Decision.QUOTA_EXHAUSTED;
        if (current.cooldownUntilTick() > nowTick) return Decision.COOLDOWN;
        return Decision.ALLOW;
    }

    public static RadioUsage accept(RadioUsage usage, UUID matchId, long cooldownUntilTick) {
        RadioUsage current = normalize(usage, matchId);
        return new RadioUsage(matchId, current.acceptedRequests() + 1, cooldownUntilTick);
    }
}
