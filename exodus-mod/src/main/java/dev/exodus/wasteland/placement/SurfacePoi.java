package dev.exodus.wasteland.placement;

import java.util.List;

public record SurfacePoi(String id, PlacementPlan.Kind kind,
                         int x, int z, int width, int depth, int height,
                         int yOffset, List<PlacementPlan.Entry> parts) {
    public SurfacePoi {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Surface POI id cannot be blank");
        if (width <= 0 || depth <= 0 || height <= 0) throw new IllegalArgumentException("Surface POI dimensions must be positive");
        parts = List.copyOf(parts);
        if (parts.isEmpty()) throw new IllegalArgumentException("Surface POI must contain at least one placement");
    }

    public int maxX() { return Math.addExact(x, width - 1); }
    public int maxZ() { return Math.addExact(z, depth - 1); }
}
