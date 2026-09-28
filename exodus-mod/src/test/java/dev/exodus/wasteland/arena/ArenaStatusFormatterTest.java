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
}
