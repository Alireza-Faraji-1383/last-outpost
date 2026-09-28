package dev.exodus.wasteland.placement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class PlacementPolicy {
    private PlacementPolicy() {}

    public static PlacementPlan plan(long seed, int centerX, int centerZ, int arenaSize,
                                     int abandonedCount, int occupiedCount, int minimumCampDistance,
                                     List<String> abandonedPool, List<String> occupiedPool) {
        if (abandonedPool.isEmpty() || occupiedPool.isEmpty()) throw new IllegalArgumentException("Both camp pools must contain a structure");
        Random random = new Random(seed);
        List<PlacementPlan.Entry> entries = new ArrayList<>();
        int quarter = arenaSize / 4;
        entries.add(new PlacementPlan.Entry("russian_base", "exodus:russian_base", PlacementPlan.Kind.RUSSIAN_BASE,
                centerX - quarter - 28, centerZ - 30, 57, 60, PlacementPlan.Rotation.NONE, true));
        entries.add(new PlacementPlan.Entry("american_base", "exodus:american_base", PlacementPlan.Kind.AMERICAN_BASE,
                centerX + quarter - 28, centerZ - 30, 57, 60, PlacementPlan.Rotation.NONE, true));
        addCamps(entries, random, centerX, centerZ, arenaSize, abandonedCount, minimumCampDistance, abandonedPool, PlacementPlan.Kind.ABANDONED_CAMP);
        addCamps(entries, random, centerX, centerZ, arenaSize, occupiedCount, minimumCampDistance, occupiedPool, PlacementPlan.Kind.OCCUPIED_CAMP);
        return new PlacementPlan(entries);
    }

    public static boolean acceptableNaturalSlope(int minimumY, int maximumY) { return maximumY >= minimumY && maximumY - minimumY <= 2; }
    public static boolean overlaps(PlacementPlan.Entry a, PlacementPlan.Entry b, int margin) {
        return a.x() - margin <= b.maxX() + margin && a.maxX() + margin >= b.x() - margin
                && a.z() - margin <= b.maxZ() + margin && a.maxZ() + margin >= b.z() - margin;
    }

    private static void addCamps(List<PlacementPlan.Entry> entries, Random random, int cx, int cz, int size,
                                 int count, int minimumDistance, List<String> pool, PlacementPlan.Kind kind) {
        int attempts = 0, placed = 0, half = size / 2;
        while (placed < count && attempts++ < Math.max(100, count * 100)) {
            int x = cx - half + 32 + random.nextInt(Math.max(1, size - 64));
            int z = cz - half + 32 + random.nextInt(Math.max(1, size - 64));
            PlacementPlan.Entry candidate = new PlacementPlan.Entry(kind.name().toLowerCase() + "_" + placed,
                    pool.get(random.nextInt(pool.size())), kind, x, z, 11, 16,
                    PlacementPlan.Rotation.values()[random.nextInt(4)], false);
            if (entries.stream().allMatch(existing -> distance(existing, candidate) >= minimumDistance)) {
                entries.add(candidate);
                placed++;
            }
        }
        if (placed != count) throw new IllegalStateException("Unable to place requested camp count with configured separation");
    }

    private static double distance(PlacementPlan.Entry a, PlacementPlan.Entry b) {
        double dx = a.x() + a.width() / 2.0 - b.x() - b.width() / 2.0;
        double dz = a.z() + a.depth() / 2.0 - b.z() - b.depth() / 2.0;
        return Math.sqrt(dx * dx + dz * dz);
    }
}
