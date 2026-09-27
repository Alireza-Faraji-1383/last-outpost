package dev.exodus.domain;

import java.util.UUID;

public record PendingPlayer(UUID playerId, long deadlineTick) {}
