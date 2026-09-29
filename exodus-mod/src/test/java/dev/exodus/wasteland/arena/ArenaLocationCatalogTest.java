package dev.exodus.wasteland.arena;

import dev.exodus.wasteland.placement.PlacementPlan;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArenaLocationCatalogTest {
    @Test void exposesOnlyPlacementsWithFinalHeightsAndResolvesIds() {
        var russian = entry("russian_base_1", PlacementPlan.Kind.RUSSIAN_BASE, 100, 200, 29, 30);
        var camp = entry("abandoned_camp_0", PlacementPlan.Kind.ABANDONED_CAMP, -40, 80, 11, 16);

        ArenaLocationCatalog catalog = ArenaLocationCatalog.from(List.of(russian, camp), Map.of("russian_base_1", 71));

        assertEquals(List.of("russian_base_1"), catalog.ids());
        ArenaLocation location = catalog.find("russian_base_1").orElseThrow();
        assertEquals(114, location.x());
        assertEquals(93, location.teleportY());
        assertEquals(215, location.z());
        assertTrue(catalog.find("missing").isEmpty());
    }

    @Test void exposesEveryCompositePartAtOneSharedPlatformHeight() {
        var parts = List.of(
                entry("russian_base_1", PlacementPlan.Kind.RUSSIAN_BASE, 100, 200, 29, 30),
                entry("russian_base_2", PlacementPlan.Kind.RUSSIAN_BASE, 129, 200, 28, 30),
                entry("russian_base_3", PlacementPlan.Kind.RUSSIAN_BASE, 100, 230, 29, 30),
                entry("russian_base_4", PlacementPlan.Kind.RUSSIAN_BASE, 129, 230, 28, 30));
        var heights = Map.of("russian_base_1", 66, "russian_base_2", 66,
                "russian_base_3", 66, "russian_base_4", 66);

        ArenaLocationCatalog catalog = ArenaLocationCatalog.from(parts, heights);

        assertEquals(List.of("russian_base_1", "russian_base_2", "russian_base_3", "russian_base_4"), catalog.ids());
        assertTrue(catalog.locations().stream().allMatch(location -> location.y() == 66));
        assertTrue(catalog.locations().stream().allMatch(location -> location.teleportY() == 88));
        assertTrue(catalog.find("russian_base").isEmpty());
    }

    @Test void usesTransformedCenterForRotatedCamp() {
        var camp = new PlacementPlan.Entry("occupied_camp_0", "exodus:occupied_camp_01",
                PlacementPlan.Kind.OCCUPIED_CAMP, 100, 200, 11, 16,
                PlacementPlan.Rotation.CLOCKWISE_90, true);
        var location = ArenaLocationCatalog.from(List.of(camp), Map.of("occupied_camp_0", 64))
                .find("occupied_camp_0").orElseThrow();
        assertEquals(93, location.x());
        assertEquals(205, location.z());
    }

    private static PlacementPlan.Entry entry(String id, PlacementPlan.Kind kind, int x, int z, int width, int depth) {
        return new PlacementPlan.Entry(id, "exodus:" + id, kind, x, z, width, depth,
                PlacementPlan.Rotation.NONE, true);
    }
}
