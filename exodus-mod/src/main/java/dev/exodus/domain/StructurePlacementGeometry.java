package dev.exodus.domain;

public final class StructurePlacementGeometry {
    private StructurePlacementGeometry() {}

    public static Origin origin(int centerX, int surfaceY, int centerZ,
                                int sizeX, int sizeZ, int depth) {
        return new Origin(centerX - sizeX / 2, surfaceY - depth, centerZ - sizeZ / 2);
    }

    public static Footprint footprint(int sizeX, int sizeZ) {
        return new Footprint((sizeX + 1) / 2, (sizeZ + 1) / 2);
    }

    public record Origin(int x, int y, int z) {}
    public record Footprint(int halfX, int halfZ) {}
}
