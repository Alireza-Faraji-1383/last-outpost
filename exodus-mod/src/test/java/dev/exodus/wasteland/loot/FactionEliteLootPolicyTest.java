package dev.exodus.wasteland.loot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FactionEliteLootPolicyTest {
    @Test
    void assignsExactlyOneRussianKeyChest() {
        var first = FactionEliteLootPolicy.resolve(LootMarker.GENERAL_ELITE, "russian_base_1", false);
        var second = FactionEliteLootPolicy.resolve(LootMarker.GENERAL_ELITE, "russian_base_1", true);

        assertEquals("exodus:chests/faction/russian_key", first.lootTable());
        assertTrue(first.claimsUniqueKey());
        assertEquals("exodus:chests/general/elite", second.lootTable());
        assertFalse(second.claimsUniqueKey());
    }

    @Test
    void assignsExactlyOneAmericanKeyChest() {
        var first = FactionEliteLootPolicy.resolve(LootMarker.GENERAL_ELITE, "american_base_1", false);
        var second = FactionEliteLootPolicy.resolve(LootMarker.GENERAL_ELITE, "american_base_1", true);

        assertEquals("exodus:chests/faction/american_key", first.lootTable());
        assertTrue(first.claimsUniqueKey());
        assertEquals("exodus:chests/general/elite", second.lootTable());
        assertFalse(second.claimsUniqueKey());
    }

    @Test
    void neverAddsKeysToOtherMarkersOrPieces() {
        assertFalse(FactionEliteLootPolicy.resolve(LootMarker.GENERAL_ELITE, "russian_base_2", false).claimsUniqueKey());
        assertFalse(FactionEliteLootPolicy.resolve(LootMarker.FOOD, "russian_base_1", false).claimsUniqueKey());
        assertFalse(FactionEliteLootPolicy.resolve(LootMarker.GENERAL_ELITE, "occupied_camp_0", false).claimsUniqueKey());
    }
}
