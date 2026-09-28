package dev.exodus.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StructurePlacementGeometryTest {
    @Test void centersTemplateAndBuriesOriginSixBlocksBelowSurface() {
        var origin = StructurePlacementGeometry.origin(100, 72, -40, 26, 27, 6);

        assertEquals(87, origin.x());
        assertEquals(66, origin.y());
        assertEquals(-53, origin.z());
    }

    @Test void roundsFootprintUpForOddTemplateDimensions() {
        var footprint = StructurePlacementGeometry.footprint(26, 27);

        assertEquals(13, footprint.halfX());
        assertEquals(14, footprint.halfZ());
    }
}
