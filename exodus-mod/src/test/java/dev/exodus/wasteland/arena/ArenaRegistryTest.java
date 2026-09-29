package dev.exodus.wasteland.arena;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ArenaRegistryTest {
    @Test void roundTripsPoiPreparationProgress(){
        ArenaRegistry registry=new ArenaRegistry();
        ArenaRecord arena=registry.begin(UUID.randomUUID(),0,4096,0,2000,128);
        PoiPreparationState state=new PoiPreparationState();
        state.chunkCursor=7;
        state.chunksComplete=true;
        state.surfaceSelected=true;
        state.platformY=66;
        state.terrainPrepared=true;
        state.structurePlaced=true;
        state.verified=false;
        arena.checkpoint().poiStates.put("russian_base",state);

        PoiPreparationState restored=ArenaRegistry.load(registry.save()).records().get(0)
                .checkpoint().poiStates.get("russian_base");

        assertNotNull(restored);
        assertEquals(7,restored.chunkCursor);
        assertTrue(restored.chunksComplete);
        assertTrue(restored.surfaceSelected);
        assertEquals(66,restored.platformY);
        assertTrue(restored.terrainPrepared);
        assertTrue(restored.structurePlaced);
        assertFalse(restored.verified);
    }

    @Test void legacySaveWithoutPoiStatesRestartsRequiredPoiPreparation(){
        ArenaRegistry registry=new ArenaRegistry();
        ArenaRecord arena=registry.begin(UUID.randomUUID(),0,4096,0,2000,128);
        arena.checkpoint().phase=ArenaPhase.TERRAIN_PREPARATION;
        arena.checkpoint().placementY.put("old",64);
        var legacy=registry.save();
        ((CompoundTag)legacy.get(0)).remove("poiStates");

        ArenaRecord restored=ArenaRegistry.load(legacy).records().get(0);

        assertTrue(restored.checkpoint().poiStates.isEmpty());
        assertEquals(ArenaPhase.POI_CHUNK_PREPARATION,restored.checkpoint().phase);
        assertTrue(restored.checkpoint().placementY.isEmpty());
        assertTrue(restored.checkpoint().placementNeedsRevalidation);
    }

    @Test void enforcesSinglePreparationAndReadyArena(){
        ArenaRegistry registry=new ArenaRegistry();
        ArenaRecord first=registry.begin(UUID.randomUUID(),0,4096,0,2000,128);
        assertEquals(ArenaState.PREPARING,first.state());
        assertThrows(IllegalStateException.class,()->registry.begin(UUID.randomUUID(),1,4096,4096,2000,128));
        registry.ready(first.id());
        assertEquals(first.id(),registry.readyArena().orElseThrow().id());
        assertThrows(IllegalStateException.class,()->registry.begin(UUID.randomUUID(),1,4096,4096,2000,128));
        registry.consume(first.id());
        assertEquals(ArenaState.CONSUMED,first.state());
        assertTrue(registry.readyArena().isEmpty());
    }

    @Test void cancelAndFailureAreTerminal(){
        ArenaRegistry cancelled=new ArenaRegistry();
        ArenaRecord a=cancelled.begin(UUID.randomUUID(),0,1,1,2000,128);
        cancelled.cancel(a.id());
        assertEquals(ArenaState.ABANDONED,a.state());
        assertThrows(IllegalStateException.class,()->cancelled.ready(a.id()));

        ArenaRegistry failed=new ArenaRegistry();
        ArenaRecord b=failed.begin(UUID.randomUUID(),0,1,1,2000,128);
        failed.fail(b.id(),"missing template");
        assertEquals(ArenaState.FAILED,b.state());
        assertEquals("missing template",b.failure());
    }

    @Test void cancelCanAbandonAReadyArenaSoAReplacementCanBePrepared(){
        ArenaRegistry registry=new ArenaRegistry();
        ArenaRecord ready=registry.begin(UUID.randomUUID(),0,1,1,2000,128);
        registry.ready(ready.id());

        registry.cancel(ready.id());

        assertEquals(ArenaState.ABANDONED,ready.state());
        assertDoesNotThrow(()->registry.begin(UUID.randomUUID(),1,4096,1,2000,128));
    }
}
