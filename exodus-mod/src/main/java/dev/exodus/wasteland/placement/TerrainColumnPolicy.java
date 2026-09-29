package dev.exodus.wasteland.placement;

import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;

public final class TerrainColumnPolicy {
    public enum StateKind { AIR, FLUID, LEAVES, REPLACEABLE, SOLID }

    private TerrainColumnPolicy() {}

    public static boolean isSurfaceSupport(BlockState state) {
        return isSurfaceSupport(classify(state));
    }

    public static boolean isSurfaceSupport(StateKind kind) {
        return kind == StateKind.SOLID;
    }

    public static boolean needsFoundation(BlockState state) {
        return !isSurfaceSupport(state);
    }

    public static int fillCount(List<BlockState> statesFromTopDown, int maximumDepth) {
        return fillCountKinds(statesFromTopDown.stream().map(TerrainColumnPolicy::classify).toList(), maximumDepth);
    }

    public static int fillCountKinds(List<StateKind> statesFromTopDown, int maximumDepth) {
        if (maximumDepth <= 0) throw new IllegalArgumentException("Maximum foundation depth must be positive");
        int limit = Math.min(statesFromTopDown.size(), maximumDepth);
        for (int depth = 0; depth < limit; depth++) {
            StateKind kind = statesFromTopDown.get(depth);
            if (kind == StateKind.SOLID) return depth;
        }
        throw new IllegalStateException("No foundation support found within " + maximumDepth + " blocks");
    }

    public static Integer findSurfaceSupportY(IntFunction<StateKind> states, int primaryY,
                                               int oceanFloorY, int minimumY, int scanDepth) {
        if (states == null) throw new IllegalArgumentException("Surface state source cannot be null");
        if (scanDepth < 0) throw new IllegalArgumentException("Surface scan depth cannot be negative");
        if (primaryY >= minimumY && states.apply(primaryY) == StateKind.FLUID) {
            return Math.addExact(primaryY, 1);
        }
        Integer primary = scanForSupport(states, primaryY, minimumY, scanDepth);
        if (primary != null || oceanFloorY == primaryY) return primary;
        return scanForSupport(states, oceanFloorY, minimumY, scanDepth);
    }

    public static Integer findSurfaceSupportY(IntFunction<StateKind> states, int primaryY,
                                               int oceanFloorY, int minimumY, int maximumY,
                                               int scanDepth) {
        if (maximumY < minimumY) throw new IllegalArgumentException("Surface scan range is invalid");
        Integer heightmapResult = findSurfaceSupportY(states, primaryY, oceanFloorY, minimumY, scanDepth);
        if (heightmapResult != null) return heightmapResult;
        for (int y = maximumY; y >= minimumY; y--) {
            StateKind kind = states.apply(y);
            if (kind == StateKind.FLUID) return Math.addExact(y, 1);
            if (kind == StateKind.SOLID) return y;
        }
        return null;
    }

    private static Integer scanForSupport(IntFunction<StateKind> states, int startY,
                                          int minimumY, int scanDepth) {
        for (int depth = 0; depth <= scanDepth; depth++) {
            int y = startY - depth;
            if (y < minimumY) return null;
            if (isSurfaceSupport(states.apply(y))) return y;
        }
        return null;
    }

    static StateKind classify(BlockState state) {
        if (state.isAir()) return StateKind.AIR;
        if (!state.getFluidState().isEmpty()) return StateKind.FLUID;
        if (state.is(BlockTags.LEAVES)) return StateKind.LEAVES;
        if (state.canBeReplaced()) return StateKind.REPLACEABLE;
        return StateKind.SOLID;
    }
}
