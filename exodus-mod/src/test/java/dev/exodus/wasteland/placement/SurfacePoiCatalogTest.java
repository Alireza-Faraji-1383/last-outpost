package dev.exodus.wasteland.placement;

import dev.exodus.wasteland.structure.CompositeDefinition;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SurfacePoiCatalogTest {
    @Test
    void groupsRussianPartsIntoOneCompositeFootprint() {
        var entries = new ArrayList<>(russianParts());
        entries.add(campEntry("abandoned_camp_0", 40, -80));
        var pois = SurfacePoiCatalog.from(entries, 0, 0);
        var russian = pois.stream().filter(p -> p.id().equals("russian_base")).findFirst().orElseThrow();
        assertEquals(57, russian.width());
        assertEquals(60, russian.depth());
        assertEquals(20, russian.height());
        assertEquals(4, russian.parts().size());
        assertTrue(russian.parts().stream().allMatch(p -> p.kind() == PlacementPlan.Kind.RUSSIAN_BASE));
    }

    @Test
    void keepsEachCampAsItsOwnLogicalPoi() {
        var camp = SurfacePoiCatalog.from(List.of(campEntry("abandoned_camp_0", 40, -80)), 0, 0).get(0);
        assertEquals("abandoned_camp_0", camp.id());
        assertEquals(11, camp.width());
        assertEquals(16, camp.depth());
        assertEquals(11, camp.height());
        assertEquals(0, camp.yOffset());
    }

    @Test
    void usesTransformedBoundsForClockwiseCampWithoutMovingItsOrigin() {
        var entry = new PlacementPlan.Entry("occupied_camp_0", "exodus:occupied_camp_01",
                PlacementPlan.Kind.OCCUPIED_CAMP, 100, 200, 11, 16,
                PlacementPlan.Rotation.CLOCKWISE_90, true);
        var camp = SurfacePoiCatalog.from(List.of(entry), 0, 0).get(0);
        assertEquals(85, camp.x());
        assertEquals(200, camp.z());
        assertEquals(16, camp.width());
        assertEquals(11, camp.depth());
        assertEquals(100, camp.parts().get(0).x());
        assertEquals(200, camp.parts().get(0).z());
    }

    @Test
    void appliesOneConfiguredYOffsetToWholeFactionComposite() {
        var russian = SurfacePoiCatalog.from(russianParts(), -3, 4).get(0);
        assertEquals(-3, russian.yOffset());
    }

    @Test
    void rejectsIncompleteOrMisalignedFactionComposite() {
        assertThrows(IllegalStateException.class,
                () -> SurfacePoiCatalog.from(russianParts().subList(0, 3), 0, 0));
        var shifted = new ArrayList<>(russianParts());
        var part = shifted.get(3);
        shifted.set(3, new PlacementPlan.Entry(part.placementId(), part.structureId(), part.kind(),
                part.x() + 1, part.z(), part.width(), part.depth(), part.rotation(), part.aggressiveTerrainRepair()));
        assertThrows(IllegalStateException.class, () -> SurfacePoiCatalog.from(shifted, 0, 0));
    }

    private static List<PlacementPlan.Entry> russianParts() {
        int x = 100;
        int z = -200;
        return CompositeDefinition.faction("russian").parts().stream()
                .map(part -> new PlacementPlan.Entry(part.id(), "exodus:" + part.id(),
                        PlacementPlan.Kind.RUSSIAN_BASE, x + part.offsetX(), z + part.offsetZ(),
                        part.size().x(), part.size().z(), PlacementPlan.Rotation.NONE, true))
                .toList();
    }

    private static PlacementPlan.Entry campEntry(String id, int x, int z) {
        return new PlacementPlan.Entry(id, "exodus:abandoned_camp_01", PlacementPlan.Kind.ABANDONED_CAMP,
                x, z, 11, 16, PlacementPlan.Rotation.NONE, true);
    }
}
