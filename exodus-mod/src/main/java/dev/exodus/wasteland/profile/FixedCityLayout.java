package dev.exodus.wasteland.profile;

import com.google.gson.Gson;
import dev.exodus.wasteland.domain.ArenaGeometry;
import dev.exodus.wasteland.domain.ArenaGrid;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Immutable world-generation contract shared with the build resource generator. */
public record FixedCityLayout(int version, int count, int arenaSize, int buffer, int safetyGap,
        int spacing, int offsetX, int offsetZ, int radius, int neighborhoodSpacing, int neighborhoodExtent, float threshold,
        int baseOffsetX, int baseOffsetZ, int poiMargin) {
    public FixedCityLayout {
        if (version <= 0 || count <= 0 || radius <= 0 || threshold <= 0 || threshold >= 1
                || neighborhoodSpacing <= 0 || neighborhoodSpacing % 16 != 0 || neighborhoodExtent < 0
                || offsetX % 16 != 0 || offsetZ % 16 != 0 || baseOffsetX <= 0 || poiMargin < 0
                || ArenaGeometry.of(arenaSize, buffer, safetyGap).gridSpacing() != spacing) {
            throw new IllegalArgumentException("Invalid fixed city layout");
        }
    }
    public static FixedCityLayout load() {
        try (var input = FixedCityLayout.class.getResourceAsStream("/data/exodus/city_layout.json")) {
            if (input == null) throw new IllegalStateException("Exodus fixed city manifest is missing");
            return new Gson().fromJson(new InputStreamReader(input, StandardCharsets.UTF_8), FixedCityLayout.class);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Cannot read Exodus fixed city manifest", exception);
        }
    }
    public ArenaGrid.Center arena(long ordinal) {
        if (ordinal < 0 || ordinal >= count) {
            throw new IllegalStateException("Fixed city catalog exhausted or invalid; add city assets before preparing more arenas");
        }
        return ArenaGrid.centerFor(ordinal, spacing);
    }
    public void validateGeometry(ArenaGeometry geometry) {
        if (geometry.arenaSize() != arenaSize || geometry.pregenerationBuffer() != buffer
                || geometry.safetyGap() != safetyGap || geometry.gridSpacing() != spacing) {
            throw new IllegalStateException("Fixed city layout requires borderSize=" + arenaSize
                    + ", pregenerationBuffer=" + buffer + ", arenaSafetyGap=" + safetyGap);
        }
    }
    /** Conservative full influence bound, including overlapping edge neighborhoods. */
    public double effectiveRadius() { return neighborhoodExtent + radius; }
    public record Neighborhood(int x, int z) {
        public String suffix() { return x == 0 && z == 0 ? "" : "_neighborhood_" + x + "_" + z; }
        public String style() { return Math.hypot(x,z) > 384 ? "exodus:outskirts" : "exodus:residential"; }
    }
    public java.util.List<Neighborhood> neighborhoods() {
        var result = new java.util.ArrayList<Neighborhood>();
        int steps = neighborhoodExtent / neighborhoodSpacing;
        for (int x=-steps; x<=steps; x++) for (int z=-steps; z<=steps; z++) {
            int px=x*neighborhoodSpacing, pz=z*neighborhoodSpacing;
            if (Math.hypot(px,pz) <= neighborhoodExtent) result.add(new Neighborhood(px,pz));
        }
        return java.util.List.copyOf(result);
    }
    public double cityFactor(int relativeX, int relativeZ) {
        double factor=0;
        for (var neighborhood : neighborhoods()) {
            factor += Math.max(0,1-Math.hypot(relativeX-offsetX-neighborhood.x(),
                    relativeZ-offsetZ-neighborhood.z())/radius);
        }
        return factor;
    }
    public void requireVersion(int storedVersion) {
        if (storedVersion != version) throw new IllegalStateException(
                "Cannot resume legacy or incompatible arena with fixed cities; cancel it and prepare a fresh arena");
    }
    public String cityId(long ordinal) { arena(ordinal); return "exodus:arena_" + ordinal; }
    public boolean intersectsCity(long arenaX, long arenaZ, long minX, long minZ,
                                  long maxX, long maxZ, int margin) {
        double cityX = arenaX + offsetX, cityZ = arenaZ + offsetZ;
        double nearestX = Math.max(minX, Math.min(maxX, cityX));
        double nearestZ = Math.max(minZ, Math.min(maxZ, cityZ));
        return Math.hypot(nearestX - cityX, nearestZ - cityZ) <= effectiveRadius() + margin;
    }
}
