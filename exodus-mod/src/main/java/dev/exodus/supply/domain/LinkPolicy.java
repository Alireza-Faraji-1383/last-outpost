package dev.exodus.supply.domain;

public final class LinkPolicy {
    public enum Result { ALLOW, NO_MATCH, NOT_ACTIVE_PLAYER, WRONG_DIMENSION, OUTSIDE_BORDER, TOO_FAR, ENDPOINT_OCCUPIED }

    private LinkPolicy() {}

    public static Result evaluate(LinkCandidate candidate) {
        if (!candidate.matchRunning()) return Result.NO_MATCH;
        if (!candidate.activePlayer()) return Result.NOT_ACTIVE_PLAYER;
        if (!candidate.sameMatchDimension()) return Result.WRONG_DIMENSION;
        if (!candidate.insideBorder()) return Result.OUTSIDE_BORDER;
        if (candidate.distance() > candidate.maximumDistance()) return Result.TOO_FAR;
        if (candidate.endpointAlreadyLinked() && !candidate.exactExistingPair()) return Result.ENDPOINT_OCCUPIED;
        return Result.ALLOW;
    }
}
