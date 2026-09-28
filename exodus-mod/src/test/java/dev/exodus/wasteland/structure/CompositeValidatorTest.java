package dev.exodus.wasteland.structure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompositeValidatorTest {
    @Test
    void validatesExactFourPartLayout() {
        Map<String, CompositeDefinition.Size> sizes = new HashMap<>();
        for (String nation : List.of("russian", "american")) {
            sizes.put(nation + "_base_1", new CompositeDefinition.Size(29, 20, 30));
            sizes.put(nation + "_base_2", new CompositeDefinition.Size(28, 20, 30));
            sizes.put(nation + "_base_3", new CompositeDefinition.Size(29, 20, 30));
            sizes.put(nation + "_base_4", new CompositeDefinition.Size(28, 20, 30));
        }

        assertTrue(CompositeValidator.validate(sizes::get).isEmpty());
    }

    @Test
    void reportsMissingAndWrongSize() {
        Map<String, CompositeDefinition.Size> sizes = new HashMap<>();
        sizes.put("russian_base_1", new CompositeDefinition.Size(30, 20, 30));

        List<String> errors = CompositeValidator.validate(sizes::get);

        assertTrue(errors.stream().anyMatch(error -> error.contains("russian_base_1") && error.contains("expected 29x20x30")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("american_base_4") && error.contains("missing")));
    }

    @Test
    void sortsNumberedCampVariants() {
        assertEquals(List.of("abandoned_camp_01", "abandoned_camp_02", "abandoned_camp_10"),
                StructureCatalog.campPool(List.of("abandoned_camp_10", "occupied_camp_01", "abandoned_camp_02", "abandoned_camp_01"), "abandoned_camp"));
    }

    @Test
    void rejectsAnEmptyCampPool() {
        assertEquals(List.of("Occupied camp structure pool is empty"),
                StructureCatalog.validateCampPools(List.of("abandoned_camp_01")));
    }
}
