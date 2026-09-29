package dev.exodus.wasteland.placement;

import dev.exodus.wasteland.structure.CompositeDefinition;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class SurfacePoiCatalog {
    private static final EnumSet<PlacementPlan.Kind> FACTIONS = EnumSet.of(
            PlacementPlan.Kind.RUSSIAN_BASE, PlacementPlan.Kind.AMERICAN_BASE);

    private SurfacePoiCatalog() {}

    public static List<SurfacePoi> from(List<PlacementPlan.Entry> entries,
                                        int russianYOffset, int americanYOffset) {
        List<SurfacePoi> result = new ArrayList<>();
        EnumSet<PlacementPlan.Kind> addedFactions = EnumSet.noneOf(PlacementPlan.Kind.class);
        for (PlacementPlan.Entry entry : entries) {
            if (FACTIONS.contains(entry.kind())) {
                if (addedFactions.add(entry.kind())) {
                    String nation = entry.kind() == PlacementPlan.Kind.RUSSIAN_BASE ? "russian" : "american";
                    int offset = entry.kind() == PlacementPlan.Kind.RUSSIAN_BASE ? russianYOffset : americanYOffset;
                    result.add(composite(entries, entry.kind(), nation, offset));
                }
            } else {
                PlacementBounds bounds = PlacementBounds.from(entry);
                result.add(new SurfacePoi(entry.placementId(), entry.kind(), bounds.minX(), bounds.minZ(),
                        bounds.width(), bounds.depth(), 11, 0, List.of(entry)));
            }
        }
        return List.copyOf(result);
    }

    private static SurfacePoi composite(List<PlacementPlan.Entry> entries, PlacementPlan.Kind kind,
                                        String nation, int yOffset) {
        List<PlacementPlan.Entry> parts = entries.stream().filter(entry -> entry.kind() == kind).toList();
        CompositeDefinition definition = CompositeDefinition.faction(nation);
        if (parts.size() != definition.parts().size()) {
            throw new IllegalStateException(nation + " faction base requires exactly four parts");
        }
        int minimumX = parts.stream().mapToInt(PlacementPlan.Entry::x).min().orElseThrow();
        int minimumZ = parts.stream().mapToInt(PlacementPlan.Entry::z).min().orElseThrow();
        for (CompositeDefinition.Part expected : definition.parts()) {
            PlacementPlan.Entry actual = parts.stream()
                    .filter(part -> part.placementId().equals(expected.id()))
                    .findFirst().orElseThrow(() -> new IllegalStateException("Missing faction base part " + expected.id()));
            if (actual.x() != minimumX + expected.offsetX() || actual.z() != minimumZ + expected.offsetZ()
                    || actual.width() != expected.size().x() || actual.depth() != expected.size().z()) {
                throw new IllegalStateException("Faction base part is misaligned: " + expected.id());
            }
        }
        return new SurfacePoi(definition.name(), kind, minimumX, minimumZ,
                definition.total().x(), definition.total().z(), definition.total().y(), yOffset, parts);
    }
}
