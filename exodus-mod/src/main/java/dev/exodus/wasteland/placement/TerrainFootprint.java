package dev.exodus.wasteland.placement;

public record TerrainFootprint(String id, int x, int z, int width, int depth, int height, int yOffset) {
    public TerrainFootprint {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Terrain footprint id cannot be blank");
        if (width <= 0 || depth <= 0 || height <= 0) {
            throw new IllegalArgumentException("Terrain footprint dimensions must be positive");
        }
    }

    public static TerrainFootprint from(SurfacePoi poi) {
        return new TerrainFootprint(poi.id(), poi.x(), poi.z(), poi.width(), poi.depth(), poi.height(), poi.yOffset());
    }

    public int maxX() { return Math.addExact(x, width - 1); }
    public int maxZ() { return Math.addExact(z, depth - 1); }
}
