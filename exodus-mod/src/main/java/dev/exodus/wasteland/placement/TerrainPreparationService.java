package dev.exodus.wasteland.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

public final class TerrainPreparationService {
    private TerrainPreparationService() {}

    public static int surfaceY(ServerLevel level, PlacementPlan.Entry entry) {
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int x = entry.x(); x <= entry.maxX(); x++) {
            for (int z = entry.z(); z <= entry.maxZ(); z++) {
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                min = Math.min(min, y);
                max = Math.max(max, y);
            }
        }
        if (!entry.aggressiveTerrainRepair() && !PlacementPolicy.acceptableNaturalSlope(min, max)) {
            throw new IllegalStateException("Camp terrain exceeds the two-block natural slope limit");
        }
        return entry.aggressiveTerrainRepair() ? (min + max) / 2 : min;
    }

    public static void prepare(ServerLevel level, PlacementPlan.Entry entry, int baseY, int structureHeight, int margin) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = entry.x() - margin; x <= entry.maxX() + margin; x++) {
            for (int z = entry.z() - margin; z <= entry.maxZ() + margin; z++) {
                level.setBlock(cursor.set(x, baseY - 1, z), Blocks.STONE.defaultBlockState(), 3);
                for (int y = baseY; y < baseY + structureHeight + 2; y++) {
                    level.setBlock(cursor.set(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }
}
