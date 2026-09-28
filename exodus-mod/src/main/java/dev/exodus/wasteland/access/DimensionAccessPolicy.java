package dev.exodus.wasteland.access;

public final class DimensionAccessPolicy {
    public enum Decision { ADMIT_PLAYER, ADMIT_SPECTATOR, ADMIT_OPERATOR_VISIT, REJECT }
    private DimensionAccessPolicy() {}
    public static Decision decide(boolean matchRunning, boolean matchPlayer, boolean authorizedSpectator, boolean operatorVisit) {
        if (operatorVisit) return Decision.ADMIT_OPERATOR_VISIT;
        if (matchRunning && matchPlayer) return Decision.ADMIT_PLAYER;
        if (matchRunning && authorizedSpectator) return Decision.ADMIT_SPECTATOR;
        return Decision.REJECT;
    }
}
