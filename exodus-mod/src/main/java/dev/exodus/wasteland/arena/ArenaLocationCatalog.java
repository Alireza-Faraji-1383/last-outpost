package dev.exodus.wasteland.arena;

import dev.exodus.wasteland.placement.PlacementPlan;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ArenaLocationCatalog {
    private final Map<String, ArenaLocation> locations;

    private ArenaLocationCatalog(Map<String, ArenaLocation> locations) {
        this.locations = Collections.unmodifiableMap(new LinkedHashMap<>(locations));
    }

    public static ArenaLocationCatalog from(List<PlacementPlan.Entry> entries, Map<String, Integer> placementY) {
        Map<String, ArenaLocation> locations = new LinkedHashMap<>();
        for (PlacementPlan.Entry entry : entries) {
            Integer y = placementY.get(entry.placementId());
            if (y == null) continue;
            int height = switch (entry.kind()) {
                case RUSSIAN_BASE, AMERICAN_BASE -> 20;
                case ABANDONED_CAMP, OCCUPIED_CAMP -> 11;
            };
            locations.put(entry.placementId(), new ArenaLocation(entry.placementId(), entry.kind(),
                    entry.x() + entry.width() / 2, y, entry.z() + entry.depth() / 2, y + height + 2));
        }
        return new ArenaLocationCatalog(locations);
    }

    public List<String> ids() { return List.copyOf(locations.keySet()); }
    public List<ArenaLocation> locations() { return List.copyOf(locations.values()); }
    public Optional<ArenaLocation> find(String placementId) { return Optional.ofNullable(locations.get(placementId)); }
}
