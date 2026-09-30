package dev.exodus.enemy;

/** Extend pressure rules here without changing placement, entity ownership, or lifecycle. */
public final class EnemyPressurePolicy {
    public record Settings(int perPlayerCap, int globalCap, int dayTarget, int nightTarget, int normalBatch, int deviceBatch) {}
    public record Pressure(int zombieTarget, int batch) {}
    public static Pressure evaluate(boolean day, boolean deviceNearby, int soldiers, Settings settings) {
        int capacity = Math.max(0, settings.perPlayerCap - soldiers);
        int desired = deviceNearby ? capacity : (day ? settings.dayTarget : settings.nightTarget);
        return new Pressure(Math.min(capacity, desired), deviceNearby ? settings.deviceBatch : settings.normalBatch);
    }
}
