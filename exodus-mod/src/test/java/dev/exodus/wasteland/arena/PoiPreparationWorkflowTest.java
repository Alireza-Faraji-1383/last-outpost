package dev.exodus.wasteland.arena;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PoiPreparationWorkflowTest {
    @Test void requiredPoiChunksRunIndependentlyOfBroadPregeneration(){
        var state=new PoiPreparationState();
        assertEquals(PoiPreparationWorkflow.Action.GENERATE_CHUNK,PoiPreparationWorkflow.next(state,49));
    }

    @Test void samplingWaitsUntilEveryRequiredChunkIsFull(){
        var state=new PoiPreparationState();
        state.chunkCursor=48;
        assertEquals(PoiPreparationWorkflow.Action.GENERATE_CHUNK,PoiPreparationWorkflow.next(state,49));
        state.chunkCursor=49;
        state.chunksComplete=true;
        assertEquals(PoiPreparationWorkflow.Action.SELECT_SURFACE,PoiPreparationWorkflow.next(state,49));
    }

    @Test void terrainPlacementAndVerificationStayOrdered(){
        var state=selectedSurface(64,9);
        assertEquals(PoiPreparationWorkflow.Action.PREPARE_TERRAIN,PoiPreparationWorkflow.next(state,9));
        state.terrainPrepared=true;
        assertEquals(PoiPreparationWorkflow.Action.PLACE_STRUCTURE,PoiPreparationWorkflow.next(state,9));
        state.structurePlaced=true;
        assertEquals(PoiPreparationWorkflow.Action.VERIFY,PoiPreparationWorkflow.next(state,9));
        state.verified=true;
        assertEquals(PoiPreparationWorkflow.Action.DONE,PoiPreparationWorkflow.next(state,9));
    }

    @Test void rejectsInconsistentPersistedProgress(){
        var cursorTooLarge=new PoiPreparationState();cursorTooLarge.chunkCursor=10;
        assertThrows(IllegalStateException.class,()->PoiPreparationWorkflow.next(cursorTooLarge,9));
        var prematureComplete=new PoiPreparationState();prematureComplete.chunkCursor=8;prematureComplete.chunksComplete=true;
        assertThrows(IllegalStateException.class,()->PoiPreparationWorkflow.next(prematureComplete,9));
        var placementWithoutTerrain=selectedSurface(64,9);placementWithoutTerrain.structurePlaced=true;
        assertThrows(IllegalStateException.class,()->PoiPreparationWorkflow.next(placementWithoutTerrain,9));
    }

    private static PoiPreparationState selectedSurface(int y,int chunks){
        var state=new PoiPreparationState();state.chunkCursor=chunks;state.chunksComplete=true;
        state.surfaceSelected=true;state.platformY=y;return state;
    }
}
