package dev.exodus.wasteland.arena;

public final class PregenerationPolicy {
    private PregenerationPolicy() {}

    public static boolean enabled(int chunksPerTick) {
        return chunksPerTick > 0;
    }
}
