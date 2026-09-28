package dev.exodus.wasteland.domain;

public record ArenaGeometry(int arenaSize, int pregenerationBuffer, int safetyGap,
                            int generationSize, int gridSpacing) {
    private static final int GRID_QUANTUM = 1024;

    public static ArenaGeometry of(int arenaSize, int pregenerationBuffer, int safetyGap) {
        if (arenaSize <= 0 || pregenerationBuffer < 0 || safetyGap < 0) {
            throw new IllegalArgumentException("Arena geometry values are out of range");
        }
        long generationSize = (long) arenaSize + 2L * pregenerationBuffer;
        long requiredSpacing = generationSize + safetyGap;
        long gridSpacing = ((requiredSpacing + GRID_QUANTUM - 1L) / GRID_QUANTUM) * GRID_QUANTUM;
        if (generationSize > Integer.MAX_VALUE || gridSpacing > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Arena geometry exceeds supported coordinates");
        }
        return new ArenaGeometry(arenaSize, pregenerationBuffer, safetyGap,
                (int) generationSize, (int) gridSpacing);
    }

    public int playableRadius() {
        return arenaSize / 2;
    }

    public int generationRadius() {
        return generationSize / 2;
    }
}
