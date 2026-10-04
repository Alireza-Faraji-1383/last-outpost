package dev.exodus.wasteland.placement;

import java.util.function.IntFunction;

/** Plans bounded top-down clearing without changing the world during inspection. */
public final class SurfaceRecoveryPolicy {
    private SurfaceRecoveryPolicy() {}

    public static <T> Result<T> find(int initialCeilingY, int maximumDepth, IntFunction<T> sample) {
        if (maximumDepth < 0) throw new IllegalArgumentException("Recovery depth cannot be negative");
        for (int depth = 0; depth <= maximumDepth; depth++) {
            int ceiling = Math.subtractExact(initialCeilingY, depth);
            try {
                return new Result<>(ceiling, sample.apply(ceiling));
            } catch (SurfacePlacementPolicy.NoQuorumException failure) {
                if (depth == maximumDepth) throw failure;
            }
        }
        throw new AssertionError("Unreachable surface recovery state");
    }

    public record Result<T>(int ceilingY, T sample) {}
}
