package dev.exodus.wasteland.structure;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public final class StructureCatalog {
    private StructureCatalog() {}

    public static List<String> campPool(Collection<String> ids, String prefix) {
        Pattern numberedVariant = Pattern.compile(Pattern.quote(prefix) + "_\\d+");
        return ids.stream()
                .filter(id -> numberedVariant.matcher(id).matches())
                .sorted(Comparator.comparingInt(StructureCatalog::numericSuffix))
                .toList();
    }

    public static List<String> validateCampPools(Collection<String> ids) {
        List<String> abandoned = campPool(ids, "abandoned_camp");
        List<String> occupied = campPool(ids, "occupied_camp");
        if (abandoned.isEmpty() && occupied.isEmpty()) {
            return List.of("Abandoned and occupied camp structure pools are empty");
        }
        if (abandoned.isEmpty()) {
            return List.of("Abandoned camp structure pool is empty");
        }
        if (occupied.isEmpty()) {
            return List.of("Occupied camp structure pool is empty");
        }
        return List.of();
    }

    private static int numericSuffix(String id) {
        return Integer.parseInt(id.substring(id.lastIndexOf('_') + 1));
    }
}
