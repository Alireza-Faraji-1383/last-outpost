package dev.exodus;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import dev.exodus.wasteland.arena.*;
import java.util.UUID;

class ExodusSavedDataTest {
    @Test void oldSaveWithoutTeleporterFieldsLoadsEmptyState(){
        ExodusSavedData data=ExodusSavedData.load(new CompoundTag());
        assertNull(data.teleporter.active());
        assertTrue(data.teleporter.rareClaims().isEmpty());
        assertTrue(data.teleporter.invalidatedMatches().isEmpty());
    }

    @Test void arenaPreparationRoundTripsEveryResumeField(){
        ExodusSavedData original=new ExodusSavedData();
        UUID initiator=UUID.randomUUID();
        ArenaRecord arena=original.arenas.begin(initiator,7,8192,-4096,3000,128);
        arena.checkpoint().phase=ArenaPhase.STRUCTURE_PLACEMENT;
        arena.checkpoint().chunkCursor=4321;
        arena.checkpoint().citySamples=100;
        arena.checkpoint().cityChunks=44;
        arena.checkpoint().lastAnnouncedPercent=60;
        arena.checkpoint().placementNeedsRevalidation=true;
        arena.checkpoint().completedPlacements.add("russian_base:part_1");

        ExodusSavedData loaded=ExodusSavedData.load(original.save(new CompoundTag()));
        ArenaRecord restored=loaded.arenas.active().orElseThrow();
        assertEquals(arena.id(),restored.id());
        assertEquals(initiator,restored.initiator());
        assertEquals(7,restored.ordinal());
        assertEquals(8192,restored.centerX());
        assertEquals(-4096,restored.centerZ());
        assertEquals(3000,restored.arenaSize());
        assertEquals(ArenaPhase.STRUCTURE_PLACEMENT,restored.checkpoint().phase);
        assertEquals(4321,restored.checkpoint().chunkCursor);
        assertEquals(100,restored.checkpoint().citySamples);
        assertEquals(44,restored.checkpoint().cityChunks);
        assertEquals(60,restored.checkpoint().lastAnnouncedPercent);
        assertTrue(restored.checkpoint().placementNeedsRevalidation);
        assertEquals(java.util.Set.of("russian_base:part_1"),restored.checkpoint().completedPlacements);
    }
}
