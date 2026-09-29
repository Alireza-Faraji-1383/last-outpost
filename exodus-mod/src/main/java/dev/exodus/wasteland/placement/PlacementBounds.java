package dev.exodus.wasteland.placement;

public record PlacementBounds(int minX, int minZ, int maxX, int maxZ) {
    public static PlacementBounds from(PlacementPlan.Entry entry) {
        int width = entry.width();
        int depth = entry.depth();
        return switch (entry.rotation()) {
            case NONE -> new PlacementBounds(entry.x(), entry.z(),
                    Math.addExact(entry.x(), width - 1), Math.addExact(entry.z(), depth - 1));
            case CLOCKWISE_90 -> new PlacementBounds(Math.subtractExact(entry.x(), depth - 1), entry.z(),
                    entry.x(), Math.addExact(entry.z(), width - 1));
            case CLOCKWISE_180 -> new PlacementBounds(Math.subtractExact(entry.x(), width - 1),
                    Math.subtractExact(entry.z(), depth - 1), entry.x(), entry.z());
            case COUNTERCLOCKWISE_90 -> new PlacementBounds(entry.x(), Math.subtractExact(entry.z(), width - 1),
                    Math.addExact(entry.x(), depth - 1), entry.z());
        };
    }

    public int width() { return Math.addExact(Math.subtractExact(maxX, minX), 1); }
    public int depth() { return Math.addExact(Math.subtractExact(maxZ, minZ), 1); }
    public int centerX() { return minX + width() / 2; }
    public int centerZ() { return minZ + depth() / 2; }
}
