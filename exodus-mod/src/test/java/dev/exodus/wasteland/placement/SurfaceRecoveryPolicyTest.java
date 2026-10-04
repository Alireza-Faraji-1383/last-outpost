package dev.exodus.wasteland.placement;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SurfaceRecoveryPolicyTest {
    @Test void findsRealFloorBelowSeparatedRoofLayers() {
        var roofs = List.of(80, 74, 68, 62);
        var result = SurfaceRecoveryPolicy.find(80, 20, ceiling -> {
            var supports = roofs.stream().map(roof -> TerrainColumnPolicy.findSurfaceSupportY(
                    y -> y == roof || y <= 60 ? TerrainColumnPolicy.StateKind.SOLID
                            : TerrainColumnPolicy.StateKind.AIR,
                    Math.min(roof, ceiling), Math.min(roof, ceiling), -64, ceiling, 8)).toList();
            return SurfacePlacementPolicy.select(supports, 4, 50, 0, 55, 70);
        });
        assertEquals(73, result.ceilingY());
        assertEquals(60, result.sample().platformY());
        assertEquals(2, result.sample().quorumCount());
    }

    @Test void exposesCommonFloorBelowUnevenObstacles() {
        var result = SurfaceRecoveryPolicy.find(80, 20, ceiling -> {
            var supports = List.of(Math.min(80, ceiling), Math.min(74, ceiling), Math.min(68, ceiling), 60);
            return SurfacePlacementPolicy.select(supports, 4, 50, 0, 55, 70);
        });
        assertEquals(74, result.ceilingY());
        assertEquals(2, result.sample().quorumCount());
    }

    @Test void keepsAlreadySuitableSurfaceUntouched() {
        AtomicInteger calls = new AtomicInteger();
        var result = SurfaceRecoveryPolicy.find(80, 20, ceiling -> {
            calls.incrementAndGet();
            return SurfacePlacementPolicy.select(List.of(60, 60, 60, 80), 4, 50, 2, 55, 70);
        });
        assertEquals(80, result.ceilingY());
        assertEquals(1, calls.get());
    }

    @Test void stopsAtConfiguredDepth() {
        AtomicInteger calls = new AtomicInteger();
        assertThrows(SurfacePlacementPolicy.NoQuorumException.class,
                () -> SurfaceRecoveryPolicy.find(80, 3, ceiling -> {
                    calls.incrementAndGet();
                    return SurfacePlacementPolicy.select(List.of(60, 67, 74, ceiling), 4, 50, 0, 55, 70);
                }));
        assertEquals(4, calls.get());
    }

    @Test void doesNotRetryUnrelatedFailures() {
        AtomicInteger calls = new AtomicInteger();
        assertThrows(IllegalStateException.class, () -> SurfaceRecoveryPolicy.find(80, 20, ceiling -> {
            calls.incrementAndGet();
            throw new IllegalStateException("Invalid foundation");
        }));
        assertEquals(1, calls.get());
    }

    @Test void glassIsNeitherSurfaceNorFoundationSupport() {
        assertFalse(TerrainColumnPolicy.isSurfaceSupport(TerrainColumnPolicy.StateKind.GLASS));
        assertEquals(2, TerrainColumnPolicy.fillCountKinds(List.of(
                TerrainColumnPolicy.StateKind.GLASS, TerrainColumnPolicy.StateKind.AIR,
                TerrainColumnPolicy.StateKind.SOLID), 3));
        assertEquals(60, TerrainColumnPolicy.findSurfaceSupportY(
                y -> y > 60 ? TerrainColumnPolicy.StateKind.GLASS : TerrainColumnPolicy.StateKind.SOLID,
                64, 64, 0, 8));
    }
}
