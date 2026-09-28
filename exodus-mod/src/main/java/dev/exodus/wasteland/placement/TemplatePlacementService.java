package dev.exodus.wasteland.placement;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class TemplatePlacementService {
    private TemplatePlacementService() {}

    public static boolean placeOnce(ServerLevel level, PlacementPlan.Entry entry, BlockPos origin,
                                    long seed, Set<String> completedIds) {
        if (completedIds.contains(entry.placementId())) return false;
        ResourceLocation id = ResourceLocation.tryParse(entry.structureId());
        StructureTemplate template = id == null ? null : level.getStructureManager().get(id).orElse(null);
        if (template == null) throw new IllegalStateException("Missing structure template: " + entry.structureId());
        StructurePlaceSettings settings = new StructurePlaceSettings().setMirror(Mirror.NONE).setRotation(rotation(entry.rotation()));
        if (!template.placeInWorld(level, origin, origin, settings, RandomSource.create(seed), 2)) {
            throw new IllegalStateException("Structure placement failed: " + entry.structureId());
        }
        completedIds.add(entry.placementId());
        return true;
    }

    private static Rotation rotation(PlacementPlan.Rotation value) {
        return switch (value) {
            case NONE -> Rotation.NONE;
            case CLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            case CLOCKWISE_180 -> Rotation.CLOCKWISE_180;
            case COUNTERCLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
        };
    }
}
