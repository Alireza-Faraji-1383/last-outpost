package dev.exodus.wasteland.placement;

import java.util.List;

public record PlacementPlan(List<Entry> entries) {
    public enum Kind { RUSSIAN_BASE, AMERICAN_BASE, ABANDONED_CAMP, OCCUPIED_CAMP }
    public enum Rotation { NONE, CLOCKWISE_90, CLOCKWISE_180, COUNTERCLOCKWISE_90 }
    public record Entry(String placementId, String structureId, Kind kind, int x, int z,
                        int width, int depth, Rotation rotation, boolean aggressiveTerrainRepair) {
        public int maxX() { return x + width - 1; }
        public int maxZ() { return z + depth - 1; }
    }
    public PlacementPlan { entries = List.copyOf(entries); }
}
