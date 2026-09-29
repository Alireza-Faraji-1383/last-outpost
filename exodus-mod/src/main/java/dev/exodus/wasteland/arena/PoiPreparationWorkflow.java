package dev.exodus.wasteland.arena;

public final class PoiPreparationWorkflow {
    public enum Action { GENERATE_CHUNK, SELECT_SURFACE, PREPARE_TERRAIN, PLACE_STRUCTURE, VERIFY, DONE }

    private PoiPreparationWorkflow() {}

    public static Action next(PoiPreparationState state, int totalChunks) {
        if (totalChunks <= 0 || state.chunkCursor < 0 || state.chunkCursor > totalChunks) {
            throw new IllegalStateException("Invalid POI chunk progress");
        }
        if (state.chunksComplete && state.chunkCursor != totalChunks) {
            throw new IllegalStateException("POI chunks marked complete before cursor completion");
        }
        if ((state.terrainPrepared || state.structurePlaced || state.verified) && !state.surfaceSelected) {
            throw new IllegalStateException("POI progress depends on an unselected surface");
        }
        if ((state.structurePlaced || state.verified) && !state.terrainPrepared) {
            throw new IllegalStateException("POI structure progress depends on unprepared terrain");
        }
        if (state.verified && !state.structurePlaced) {
            throw new IllegalStateException("POI verification depends on structure placement");
        }
        if (state.verified) return Action.DONE;
        if (state.structurePlaced) return Action.VERIFY;
        if (state.terrainPrepared) return Action.PLACE_STRUCTURE;
        if (state.surfaceSelected) return Action.PREPARE_TERRAIN;
        if (state.chunksComplete) return Action.SELECT_SURFACE;
        return Action.GENERATE_CHUNK;
    }
}
