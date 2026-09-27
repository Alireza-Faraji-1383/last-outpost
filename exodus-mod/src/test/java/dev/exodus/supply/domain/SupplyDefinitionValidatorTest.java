package dev.exodus.supply.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SupplyDefinitionValidatorTest {
    private static SupplyDefinition valid(String id, String name, int sortOrder) {
        return new SupplyDefinition(id, name, "minecraft:bread", "exodus:supply_drops/food",
                Set.of(RadioType.BASIC), 3, 20, new SupplyCost("minecraft:emerald", 4),
                0xD94841, true, sortOrder);
    }

    @Test void acceptsACompleteDefinition() {
        assertTrue(SupplyDefinitionValidator.validate(valid("exodus:food", "Food", 10)).isEmpty());
    }

    @Test void identifiesEveryInvalidFieldClass() {
        assertEquals("id", SupplyDefinitionValidator.validate(valid("bad id", "Food", 0)).get(0).field());
        assertEquals("display_name", SupplyDefinitionValidator.validate(new SupplyDefinition("exodus:food", " ",
                "minecraft:bread", "exodus:supply_drops/food", Set.of(RadioType.BASIC), 3, 0,
                null, 0, true, 0)).get(0).field());
        assertEquals("radio_types", SupplyDefinitionValidator.validate(new SupplyDefinition("exodus:food", "Food",
                "minecraft:bread", "exodus:supply_drops/food", Set.of(), 3, 0,
                null, 0, true, 0)).get(0).field());
        assertEquals("max_requests", SupplyDefinitionValidator.validate(new SupplyDefinition("exodus:food", "Food",
                "minecraft:bread", "exodus:supply_drops/food", Set.of(RadioType.BASIC), 0, 0,
                null, 0, true, 0)).get(0).field());
        assertEquals("cooldown_seconds", SupplyDefinitionValidator.validate(new SupplyDefinition("exodus:food", "Food",
                "minecraft:bread", "exodus:supply_drops/food", Set.of(RadioType.BASIC), 1, -1,
                null, 0, true, 0)).get(0).field());
        assertEquals("cost.count", SupplyDefinitionValidator.validate(new SupplyDefinition("exodus:food", "Food",
                "minecraft:bread", "exodus:supply_drops/food", Set.of(RadioType.BASIC), 1, 0,
                new SupplyCost("minecraft:emerald", 0), 0, true, 0)).get(0).field());
        assertEquals("drop.smoke_color", SupplyDefinitionValidator.validate(new SupplyDefinition("exodus:food", "Food",
                "minecraft:bread", "exodus:supply_drops/food", Set.of(RadioType.BASIC), 1, 0,
                null, 0x1_000000, true, 0)).get(0).field());
    }

    @Test void sortsDeterministicallyByOrderNameThenId() {
        List<SupplyDefinition> values = List.of(
                valid("exodus:z", "Alpha", 2),
                valid("exodus:b", "Beta", 1),
                valid("exodus:a", "Beta", 1));

        assertEquals(List.of("exodus:a", "exodus:b", "exodus:z"),
                SupplyDefinitionValidator.sorted(values).stream().map(SupplyDefinition::id).toList());
    }
}
