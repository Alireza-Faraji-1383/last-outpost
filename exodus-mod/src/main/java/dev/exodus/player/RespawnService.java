package dev.exodus.player;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class RespawnService {
    private RespawnService() {}

    public static void setBase(ServerPlayer player, ServerLevel level, BlockPos spawn) {
        player.setRespawnPosition(level.dimension(), spawn, 0.0F, true, false);
    }

    public static void resetToOverworld(ServerPlayer player, ServerLevel overworld) {
        player.setRespawnPosition(overworld.dimension(), overworld.getSharedSpawnPos(), 0.0F, true, false);
    }
}
