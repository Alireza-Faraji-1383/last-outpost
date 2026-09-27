package dev.exodus;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import java.util.UUID;

public record PlayerBaseData(UUID uuid, String name, BlockPos center, BlockPos origin,
                             BlockPos spawn, ResourceLocation structureId) {}
