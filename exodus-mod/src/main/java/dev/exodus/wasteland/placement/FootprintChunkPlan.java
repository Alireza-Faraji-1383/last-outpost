package dev.exodus.wasteland.placement;

public record FootprintChunkPlan(int minimumChunkX, int minimumChunkZ,
                                 int maximumChunkX, int maximumChunkZ) {
    public FootprintChunkPlan {
        if (maximumChunkX < minimumChunkX || maximumChunkZ < minimumChunkZ) {
            throw new IllegalArgumentException("Chunk bounds are invalid");
        }
        long total = (long) (maximumChunkX - minimumChunkX + 1) * (maximumChunkZ - minimumChunkZ + 1);
        if (total > Integer.MAX_VALUE) throw new IllegalArgumentException("Chunk plan is too large");
    }

    public static FootprintChunkPlan forBounds(int minX, int minZ, int maxX, int maxZ, int marginChunks) {
        if (maxX < minX || maxZ < minZ) throw new IllegalArgumentException("Block bounds are invalid");
        if (marginChunks < 0) throw new IllegalArgumentException("Chunk margin cannot be negative");
        return new FootprintChunkPlan(Math.floorDiv(minX, 16) - marginChunks,
                Math.floorDiv(minZ, 16) - marginChunks,
                Math.floorDiv(maxX, 16) + marginChunks,
                Math.floorDiv(maxZ, 16) + marginChunks);
    }

    public int total() {
        return Math.multiplyExact(width(), maximumChunkZ - minimumChunkZ + 1);
    }

    public int chunkX(int index) {
        checkIndex(index);
        return minimumChunkX + index % width();
    }

    public int chunkZ(int index) {
        checkIndex(index);
        return minimumChunkZ + index / width();
    }

    private int width() { return maximumChunkX - minimumChunkX + 1; }

    private void checkIndex(int index) {
        if (index < 0 || index >= total()) throw new IndexOutOfBoundsException("Chunk index " + index + " is outside plan");
    }
}
