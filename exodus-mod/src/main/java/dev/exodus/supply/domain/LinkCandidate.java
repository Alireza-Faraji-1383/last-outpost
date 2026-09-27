package dev.exodus.supply.domain;

public record LinkCandidate(
        boolean matchRunning,
        boolean activePlayer,
        boolean sameMatchDimension,
        boolean insideBorder,
        double distance,
        int maximumDistance,
        boolean exactExistingPair,
        boolean endpointAlreadyLinked) {}
