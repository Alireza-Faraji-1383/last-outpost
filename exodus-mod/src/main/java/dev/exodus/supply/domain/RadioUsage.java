package dev.exodus.supply.domain;

import java.util.UUID;

public record RadioUsage(UUID matchId, int acceptedRequests, long cooldownUntilTick) {}
