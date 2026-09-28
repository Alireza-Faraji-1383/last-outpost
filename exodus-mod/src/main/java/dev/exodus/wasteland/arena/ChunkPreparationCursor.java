package dev.exodus.wasteland.arena;

public record ChunkPreparationCursor(int minimumChunkX, int minimumChunkZ, int width, int index) {
    public static ChunkPreparationCursor forArea(long centerX, long centerZ, int generationSize, int index) {
        long minimumX = centerX - generationSize / 2L;
        long minimumZ = centerZ - generationSize / 2L;
        long maximumX = centerX + (generationSize - 1L) / 2L;
        long maximumZ = centerZ + (generationSize - 1L) / 2L;
        int minChunkX = Math.toIntExact(Math.floorDiv(minimumX, 16));
        int minChunkZ = Math.toIntExact(Math.floorDiv(minimumZ, 16));
        int maxChunkX = Math.toIntExact(Math.floorDiv(maximumX, 16));
        int maxChunkZ = Math.toIntExact(Math.floorDiv(maximumZ, 16));
        return new ChunkPreparationCursor(minChunkX, minChunkZ, maxChunkX - minChunkX + 1, index);
    }

    public int total() { return Math.multiplyExact(width, width); }
    public boolean complete() { return index >= total(); }
    public int chunkX() { return minimumChunkX + index % width; }
    public int chunkZ() { return minimumChunkZ + index / width; }
    public ChunkPreparationCursor advance() { return new ChunkPreparationCursor(minimumChunkX, minimumChunkZ, width, index + 1); }
}
