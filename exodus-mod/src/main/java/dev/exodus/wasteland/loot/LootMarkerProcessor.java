package dev.exodus.wasteland.loot;

import java.util.Set;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;

public final class LootMarkerProcessor {
    private LootMarkerProcessor() {}

    public static int process(ServerLevel level, BlockPos minimum, BlockPos maximum, long seed, Set<String> completedIds) {
        return process(level, minimum, maximum, seed, completedIds, "");
    }

    public static int process(ServerLevel level, BlockPos minimum, BlockPos maximum, long seed,
                              Set<String> completedIds, String structurePlacementId) {
        int replaced = 0;
        String uniqueKeyId = "faction-key@" + structurePlacementId;
        for (BlockPos cursor : BlockPos.betweenClosed(minimum, maximum)) {
            if (!(level.getBlockEntity(cursor) instanceof StructureBlockEntity marker)
                    || marker.getMode() != StructureMode.DATA) continue;
            String metadata = marker.getMetaData();
            if (!metadata.startsWith("exodus:loot/")) continue;
            LootMarker lootMarker = LootMarker.requireKnown(metadata);
            FactionEliteLootPolicy.Assignment assignment = FactionEliteLootPolicy.resolve(
                    lootMarker, structurePlacementId, completedIds.contains(uniqueKeyId));
            String placementId = "loot@" + cursor.asLong();
            if (completedIds.contains(placementId)) continue;
            BlockPos target = cursor.immutable();
            level.setBlock(target, Blocks.CHEST.defaultBlockState(), 3);
            if (level.getBlockEntity(target) instanceof RandomizableContainerBlockEntity chest) {
                chest.setLootTable(Objects.requireNonNull(ResourceLocation.tryParse(assignment.lootTable())), seed ^ target.asLong());
                if (assignment.claimsUniqueKey()) completedIds.add(uniqueKeyId);
                completedIds.add(placementId);
                replaced++;
            }
        }
        return replaced;
    }
}
