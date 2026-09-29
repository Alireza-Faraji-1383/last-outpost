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

    public String geometrySignature() {
        StringBuilder value = new StringBuilder(id).append('|').append(kind).append('|')
                .append(x).append(',').append(z).append('|').append(width).append('x').append(depth)
                .append('x').append(height).append('|').append(yOffset);
        for (PlacementPlan.Entry part : parts) {
            value.append('|').append(part.placementId()).append('@').append(part.x()).append(',').append(part.z())
                    .append(':').append(part.width()).append('x').append(part.depth()).append(':').append(part.rotation());
        }
        return value.toString();
    }
}
