package dev.exodus.wasteland.lostcities;

import java.util.ArrayList;
import java.util.List;

/** Replace one dimension mapping without changing unrelated Lost Cities dimensions. */
public final class DimensionProfileBinding {
    private DimensionProfileBinding() {}
    public static List<String> replace(List<? extends String> mappings, String dimension, String profile) {
        var result = new ArrayList<String>();
        for (String mapping : mappings) {
            if (!mapping.split("=", 2)[0].trim().equals(dimension)) result.add(mapping);
        }
        result.add(dimension + "=" + profile);
        return List.copyOf(result);
    }
}
