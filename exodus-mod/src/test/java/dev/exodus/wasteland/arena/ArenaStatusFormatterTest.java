package dev.exodus.wasteland.arena;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArenaStatusFormatterTest {
    @Test void reportsPhaseProgressAndLocation() {
        ArenaRegistry registry = new ArenaRegistry();
        ArenaRecord arena = registry.begin(UUID.randomUUID(), 0, 4096, 0, 2000, 128);
        arena.checkpoint().phase = ArenaPhase.PREGENERATION;
        arena.checkpoint().lastAnnouncedPercent = 30;
        String status = ArenaStatusFormatter.format(arena).get(0);
        assertTrue(status.contains("PREPARING"));
        assertTrue(status.contains("PREGENERATION"));
        assertTrue(status.contains("30%"));
        assertTrue(status.contains("4096,0"));
    }

    @Test void reportsCurrentPoiChunkProgress() {
        ArenaRecord arena = new ArenaRegistry().begin(UUID.randomUUID(), 0, 4096, 0, 2000, 128);
        arena.checkpoint().phase = ArenaPhase.POI_CHUNK_PREPARATION;
        PoiPreparationState state = new PoiPreparationState();
        state.chunkCursor = 12;
        state.totalChunks = 49;
        state.surfaceSelected = true;
        state.platformY = 66;
        state.quorumCount = 87;
        state.totalColumns = 176;
        arena.checkpoint().poiStates.put("russian_base", state);
        String status = ArenaStatusFormatter.format(arena).get(0);
        assertTrue(status.contains("poi=russian_base"));
        assertTrue(status.contains("chunks=12/49"));
        assertTrue(status.contains("y=66"));
        assertTrue(status.contains("quorum=87/176"));
    }
}
