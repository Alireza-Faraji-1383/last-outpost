package dev.exodus.wasteland.arena;

import java.util.LinkedHashSet;
import java.util.Set;

public final class PreparationCheckpoint {
    public ArenaPhase phase = ArenaPhase.DEPENDENCY_CHECK;
    public int chunkCursor;
    public int citySamples;
    public int cityChunks;
    public int lastAnnouncedPercent;
    public boolean placementNeedsRevalidation;
    public final Set<String> completedPlacements = new LinkedHashSet<>();
}
