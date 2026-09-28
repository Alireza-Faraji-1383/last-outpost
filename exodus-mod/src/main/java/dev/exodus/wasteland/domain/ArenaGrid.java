package dev.exodus.wasteland.domain;

public final class ArenaGrid {
    private ArenaGrid() {}

    public static Center centerFor(long ordinal, int spacing) {
        if (ordinal < 0 || spacing <= 0) {
            throw new IllegalArgumentException("Arena ordinal and spacing must be positive");
        }
        long x = 0;
        long z = 0;
        long dx = 1;
        long dz = 0;
        for (long index = 0; index <= ordinal; index++) {
            x += dx;
            z += dz;
            if (x == z || (x < 0 && x == -z) || (x > 0 && x == 1 - z)) {
                long previousDx = dx;
                dx = -dz;
                dz = previousDx;
            }
        }
        return new Center(Math.multiplyExact(x, spacing), Math.multiplyExact(z, spacing));
    }

    public record Center(long x, long z) {}
}
