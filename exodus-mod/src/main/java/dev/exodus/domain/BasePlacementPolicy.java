package dev.exodus.domain;

import java.util.List;

public final class BasePlacementPolicy {
    private final int radius;
    private final int borderSafeDistance;
    private final int minimumDistance;
    private final int maximumHeightVariation;
    private final int halfFootprintX;
    private final int halfFootprintZ;

    public BasePlacementPolicy(int radius, int borderSafeDistance, int minimumDistance, int maximumHeightVariation,
                               int halfFootprintX, int halfFootprintZ) {
        this.radius = radius;
        this.borderSafeDistance = borderSafeDistance;
        this.minimumDistance = minimumDistance;
        this.maximumHeightVariation = maximumHeightVariation;
        this.halfFootprintX = halfFootprintX;
        this.halfFootprintZ = halfFootprintZ;
    }

    public boolean isValid(BaseCandidate candidate, int centerX, int centerZ,
                           List<BasePoint> currentBases, List<BasePoint> persistedBases) {
        int limitX = radius - borderSafeDistance - halfFootprintX;
        int limitZ = radius - borderSafeDistance - halfFootprintZ;
        if (Math.abs(candidate.x() - centerX) > limitX || Math.abs(candidate.z() - centerZ) > limitZ) return false;
        if (candidate.hasLiquid() || !candidate.solidSurface() || candidate.heightVariation() > maximumHeightVariation) return false;
        return farEnough(candidate, currentBases) && farEnough(candidate, persistedBases);
    }

    private boolean farEnough(BaseCandidate candidate, List<BasePoint> bases) {
        long minimumSquared = (long) minimumDistance * minimumDistance;
        for (BasePoint base : bases) {
            long dx = (long) candidate.x() - base.x();
            long dz = (long) candidate.z() - base.z();
            if (dx * dx + dz * dz < minimumSquared) return false;
        }
        return true;
    }
}
