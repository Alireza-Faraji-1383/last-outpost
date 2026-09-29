package dev.exodus.wasteland.placement;

public final class FoundationHeightPolicy {
    private FoundationHeightPolicy() {}

    public static int structureBottomY(int surfaceY, int structureDepth) {
        if (structureDepth < 0) throw new IllegalArgumentException("Structure depth cannot be negative");
        return Math.subtractExact(surfaceY, structureDepth);
    }

    public static int supportTopY(int structureBottomY) {
        return Math.subtractExact(structureBottomY, 1);
    }

    public static int fillStartY(int structureBottomY) {
        return Math.subtractExact(structureBottomY, 2);
    }
}
