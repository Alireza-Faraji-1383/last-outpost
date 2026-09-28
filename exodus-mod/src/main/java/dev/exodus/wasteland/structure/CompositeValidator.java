package dev.exodus.wasteland.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class CompositeValidator {
    private CompositeValidator() {}

    public static List<String> validate(Function<String, CompositeDefinition.Size> sizes) {
        List<String> errors = new ArrayList<>();
        for (String nation : List.of("russian", "american")) {
            for (CompositeDefinition.Part part : CompositeDefinition.faction(nation).parts()) {
                CompositeDefinition.Size actual = sizes.apply(part.id());
                if (actual == null) {
                    errors.add(part.id() + " is missing");
                } else if (!actual.equals(part.size())) {
                    errors.add(part.id() + " expected " + part.size() + " but found " + actual);
                }
            }
        }
        return List.copyOf(errors);
    }
}
