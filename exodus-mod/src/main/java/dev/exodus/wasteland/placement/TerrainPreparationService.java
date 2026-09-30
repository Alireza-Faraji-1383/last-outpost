package dev.exodus.wasteland.placement;

import dev.exodus.ExodusConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

public final class TerrainPreparationService {
    private static final int SURFACE_SCAN_DEPTH = 8;

    private TerrainPreparationService() {}

    public static SurfaceSample sample(ServerLevel level, SurfacePoi poi, int quorumPercent,
                                       int tolerance, int preferredMinY, int preferredMaxY) {
        return sample(level, TerrainFootprint.from(poi), quorumPercent, tolerance, preferredMinY, preferredMaxY);
    }

    public static SurfaceSample sample(ServerLevel level, TerrainFootprint poi, int quorumPercent,
                                       int tolerance, int preferredMinY, int preferredMaxY) {
        return sample(level, poi, quorumPercent, tolerance, preferredMinY, preferredMaxY,
                level.getMaxBuildHeight() - 1, null);
    }

    /** Used only for arena POIs; player-base preflight must remain read-only. */
    public static SurfaceSample sampleWithRecovery(ServerLevel level, SurfacePoi poi, int quorumPercent,
                                                   int tolerance, int preferredMinY, int preferredMaxY) {
        TerrainFootprint footprint = TerrainFootprint.from(poi);
        int highest = level.getMinBuildHeight();
        for (int x = poi.x(); x <= poi.maxX(); x++) {
            for (int z = poi.z(); z <= poi.maxZ(); z++) {
                highest = Math.max(highest, level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1);
            }
        }
        final int initialCeiling = highest;
        Set<BlockPos> glass = new HashSet<>();
        var recovery = SurfaceRecoveryPolicy.find(initialCeiling,
                Math.min(ExodusConfig.MAX_SURFACE_CLEAR_DEPTH.get(), initialCeiling - level.getMinBuildHeight()),
                ceiling -> sample(level, footprint, quorumPercent, tolerance, preferredMinY, preferredMaxY,
                        ceiling, glass));
        // Check foundations before making irreversible terrain changes.
        validateFoundation(level, footprint, recovery.sample().selection().platformY(),
                ExodusConfig.MAX_FOUNDATION_DEPTH.get());
        int cleared = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = poi.x(); x <= poi.maxX(); x++) {
            for (int z = poi.z(); z <= poi.maxZ(); z++) {
                for (int y = initialCeiling; y > recovery.ceilingY(); y--) {
                    BlockPos pos = cursor.set(x, y, z);
                    if (!level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        cleared++;
                    }
                }
            }
        }
        int brokenGlass = 0;
        for (BlockPos pos : glass) {
            if (TerrainColumnPolicy.classify(level.getBlockState(pos)) == TerrainColumnPolicy.StateKind.GLASS) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                brokenGlass++;
            }
        }
        if (cleared > 0 || brokenGlass > 0) {
            LogUtils.getLogger().info("[Exodus] POI {} surface recovery: cleared={} glass={} depth={}; resampling floor.",
                    poi.id(), cleared, brokenGlass, initialCeiling - recovery.ceilingY());
        }
        return sample(level, footprint, quorumPercent, tolerance, preferredMinY, preferredMaxY);
    }

    private static SurfaceSample sample(ServerLevel level, TerrainFootprint poi, int quorumPercent,
                                        int tolerance, int preferredMinY, int preferredMaxY,
                                        int ceilingY, Set<BlockPos> encounteredGlass) {
        List<Integer> supports = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int totalColumns = Math.multiplyExact(poi.width(), poi.depth());
        for (int x = poi.x(); x <= poi.maxX(); x++) {
            for (int z = poi.z(); z <= poi.maxZ(); z++) {
                int columnX = x;
                int columnZ = z;
                int firstCandidate = Math.min(ceilingY, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1);
                int oceanFloorCandidate = Math.min(ceilingY, level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z) - 1);
                Integer support = TerrainColumnPolicy.findSurfaceSupportY(
                        y -> {
                            var kind = TerrainColumnPolicy.classify(level.getBlockState(cursor.set(columnX, y, columnZ)));
                            if (kind == TerrainColumnPolicy.StateKind.GLASS && encounteredGlass != null) {
                                encounteredGlass.add(cursor.immutable());
                            }
                            return kind;
                        },
                        firstCandidate, oceanFloorCandidate, level.getMinBuildHeight(),
                        ceilingY, SURFACE_SCAN_DEPTH);
                if (support != null) supports.add(support);
            }
        }
        if (supports.isEmpty()) {
            throw new IllegalStateException("POI " + poi.id() + " found no solid surface support across "
                    + totalColumns + " footprint columns");
        }
        SurfacePlacementPolicy.Selection raw = SurfacePlacementPolicy.select(supports, totalColumns,
                quorumPercent, tolerance, preferredMinY, preferredMaxY);
        int platformY = Math.addExact(raw.platformY(), poi.yOffset());
        validateBuildHeight(level, poi, platformY);
        var adjusted = new SurfacePlacementPolicy.Selection(platformY, raw.quorumCount(), raw.totalColumns(),
                raw.minimumSupportY(), raw.maximumSupportY());
        return new SurfaceSample(adjusted, supports.size(), totalColumns - supports.size());
    }

    public static TerrainPreparationReport prepare(ServerLevel level, SurfacePoi poi, int platformY,
                                                   int margin, int maximumFoundationDepth) {
        return prepare(level, TerrainFootprint.from(poi), platformY, margin, maximumFoundationDepth);
    }

    public static TerrainPreparationReport prepare(ServerLevel level, TerrainFootprint poi, int platformY,
                                                   int margin, int maximumFoundationDepth) {
        if (margin < 0) throw new IllegalArgumentException("Terrain margin cannot be negative");
        validateBuildHeight(level, poi, platformY);
        int[][] depths = new int[poi.width()][poi.depth()];
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = poi.x(); x <= poi.maxX(); x++) {
            for (int z = poi.z(); z <= poi.maxZ(); z++) {
                depths[x - poi.x()][z - poi.z()] = foundationDepth(level, cursor, poi.id(), x, z,
                        FoundationHeightPolicy.fillStartY(platformY), maximumFoundationDepth);
            }
        }

        int foundationBlocks = 0;
        int deepestFill = 0;
        for (int x = poi.x(); x <= poi.maxX(); x++) {
            for (int z = poi.z(); z <= poi.maxZ(); z++) {
                level.setBlock(cursor.set(x, FoundationHeightPolicy.supportTopY(platformY), z),
                        Blocks.STONE.defaultBlockState(), 3);
                int depth = depths[x - poi.x()][z - poi.z()];
                deepestFill = Math.max(deepestFill, depth);
                for (int offset = 0; offset < depth; offset++) {
                    BlockPos pos = cursor.set(x, FoundationHeightPolicy.fillStartY(platformY) - offset, z);
                    if (TerrainColumnPolicy.needsFoundation(level.getBlockState(pos))) {
                        level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
                        foundationBlocks++;
                    }
                }
            }
        }

        int clearedBlocks = 0;
        for (int x = poi.x() - margin; x <= poi.maxX() + margin; x++) {
            for (int z = poi.z() - margin; z <= poi.maxZ() + margin; z++) {
                for (int y = platformY; y <= platformY + poi.height() + 1; y++) {
                    BlockPos pos = cursor.set(x, y, z);
                    if (!level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        clearedBlocks++;
                    }
                }
            }
        }
        return new TerrainPreparationReport(clearedBlocks, foundationBlocks, deepestFill);
    }

    public static void validateFoundation(ServerLevel level, TerrainFootprint poi, int platformY,
                                          int maximumFoundationDepth) {
        validateBuildHeight(level, poi, platformY);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = poi.x(); x <= poi.maxX(); x++) {
            for (int z = poi.z(); z <= poi.maxZ(); z++) {
                foundationDepth(level, cursor, poi.id(), x, z,
                        FoundationHeightPolicy.fillStartY(platformY), maximumFoundationDepth);
            }
        }
    }

    public static void verify(ServerLevel level, SurfacePoi poi, int platformY, int maximumFoundationDepth) {
        verify(level, TerrainFootprint.from(poi), platformY, maximumFoundationDepth);
    }

    public static void verify(ServerLevel level, TerrainFootprint poi, int platformY, int maximumFoundationDepth) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = poi.x(); x <= poi.maxX(); x++) {
            for (int z = poi.z(); z <= poi.maxZ(); z++) {
                int supportTopY = FoundationHeightPolicy.supportTopY(platformY);
                BlockPos platform = cursor.set(x, supportTopY, z);
                if (!TerrainColumnPolicy.isSurfaceSupport(level.getBlockState(platform))) {
                    throw failure(poi.id(), "platform is not solid", x, supportTopY, z);
                }
                foundationDepth(level, cursor, poi.id(), x, z,
                        FoundationHeightPolicy.fillStartY(platformY), maximumFoundationDepth);
            }
        }
    }

    public static int surfaceY(ServerLevel level, PlacementPlan.Entry entry) {
        SurfacePoi poi = new SurfacePoi(entry.placementId(), entry.kind(), entry.x(), entry.z(),
                entry.width(), entry.depth(), height(entry), 0, List.of(entry));
        return sample(level, poi, ExodusConfig.SURFACE_QUORUM_PERCENT.get(),
                ExodusConfig.SURFACE_HEIGHT_TOLERANCE.get(), ExodusConfig.PREFERRED_SURFACE_MIN_Y.get(),
                ExodusConfig.PREFERRED_SURFACE_MAX_Y.get()).selection().platformY();
    }

    public static void prepare(ServerLevel level, PlacementPlan.Entry entry, int baseY,
                               int structureHeight, int margin) {
        SurfacePoi poi = new SurfacePoi(entry.placementId(), entry.kind(), entry.x(), entry.z(),
                entry.width(), entry.depth(), structureHeight, 0, List.of(entry));
        prepare(level, poi, baseY, margin, ExodusConfig.MAX_FOUNDATION_DEPTH.get());
    }

    private static int height(PlacementPlan.Entry entry) {
        return entry.kind() == PlacementPlan.Kind.ABANDONED_CAMP
                || entry.kind() == PlacementPlan.Kind.OCCUPIED_CAMP ? 11 : 20;
    }

    private static int foundationDepth(ServerLevel level, BlockPos.MutableBlockPos cursor, String poiId,
                                       int x, int z, int firstY, int maximumDepth) {
        if (maximumDepth <= 0) throw new IllegalArgumentException("Maximum foundation depth must be positive");
        for (int depth = 0; depth < maximumDepth; depth++) {
            int y = firstY - depth;
            if (y < level.getMinBuildHeight()) break;
            if (!TerrainColumnPolicy.needsFoundation(level.getBlockState(cursor.set(x, y, z)))) return depth;
        }
        throw failure(poiId, "no foundation support within " + maximumDepth + " blocks", x, firstY, z);
    }

    private static void validateBuildHeight(ServerLevel level, TerrainFootprint poi, int platformY) {
        if (platformY <= level.getMinBuildHeight() + 4) {
            throw new IllegalStateException("POI " + poi.id() + " selected implausible platform Y=" + platformY);
        }
        if ((long) platformY + poi.height() + 2 >= level.getMaxBuildHeight()) {
            throw new IllegalStateException("POI " + poi.id() + " exceeds maximum build height at Y=" + platformY);
        }
    }

    private static IllegalStateException failure(String poiId, String reason, int x, int y, int z) {
        return new IllegalStateException("POI " + poiId + " " + reason + " at " + x + "," + y + "," + z);
    }
}
