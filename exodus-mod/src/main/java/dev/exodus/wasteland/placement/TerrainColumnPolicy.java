package dev.exodus.wasteland.placement;

import java.util.List;
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
        return state.isAir() || !state.getFluidState().isEmpty();
    }

    public static int fillCount(List<BlockState> statesFromTopDown, int maximumDepth) {
        return fillCountKinds(statesFromTopDown.stream().map(TerrainColumnPolicy::classify).toList(), maximumDepth);
    }

    public static int fillCountKinds(List<StateKind> statesFromTopDown, int maximumDepth) {
        if (maximumDepth <= 0) throw new IllegalArgumentException("Maximum foundation depth must be positive");
        int limit = Math.min(statesFromTopDown.size(), maximumDepth);
        for (int depth = 0; depth < limit; depth++) {
            StateKind kind = statesFromTopDown.get(depth);
            if (kind != StateKind.AIR && kind != StateKind.FLUID) return depth;
        }
        throw new IllegalStateException("No foundation support found within " + maximumDepth + " blocks");
    }

    private static StateKind classify(BlockState state) {
        if (state.isAir()) return StateKind.AIR;
        if (!state.getFluidState().isEmpty()) return StateKind.FLUID;
        if (state.is(BlockTags.LEAVES)) return StateKind.LEAVES;
        if (state.canBeReplaced()) return StateKind.REPLACEABLE;
        return StateKind.SOLID;
    }
}
