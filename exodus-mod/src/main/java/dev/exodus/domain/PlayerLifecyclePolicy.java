package dev.exodus.domain;

public final class PlayerLifecyclePolicy {
    private PlayerLifecyclePolicy() {}

    public static boolean isEligible(boolean inMatchDimension, GameModeKind gameMode) {
        return inMatchDimension && gameMode == GameModeKind.SURVIVAL;
    }

    public static boolean isExpired(PendingPlayer player, long currentTick) {
        return currentTick >= player.deadlineTick();
    }

    public static boolean forceSurvivalOnCleanup(Association association) {
        return association == Association.MATCH_PLAYER || association == Association.AUTO_SPECTATOR;
    }
}
