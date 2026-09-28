package dev.exodus.teleporter.domain;

import java.util.UUID;

public record EscapeCandidate(UUID playerId,double distanceSquared,boolean eligible) {}
