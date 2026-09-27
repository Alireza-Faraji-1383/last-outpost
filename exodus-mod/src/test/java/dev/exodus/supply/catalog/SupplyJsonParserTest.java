package dev.exodus.supply.catalog;

import com.google.gson.JsonParser;
import dev.exodus.supply.domain.RadioType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SupplyJsonParserTest {
    @Test void parsesDefaultsAndModdedIds() {
        var json=JsonParser.parseString("""
          {"display_name":"Rifle","icon":"guns:ak47","loot_table":"pack:drops/ak47",
           "radio_types":["special"],"max_requests":3,"cooldown_seconds":120,
           "drop":{"smoke_color":"#D94841"}}
          """).getAsJsonObject();
        var value=SupplyJsonParser.parse("pack:ak47",json);
        assertTrue(value.errors().isEmpty());
        assertTrue(value.definition().enabled());
        assertEquals(0,value.definition().sortOrder());
        assertEquals(0xD94841,value.definition().smokeColor());
        assertEquals(java.util.Set.of(RadioType.SPECIAL),value.definition().radioTypes());
    }

    @Test void rejectsUnknownRadioAndInvalidColor() {
        var json=JsonParser.parseString("""
          {"display_name":"Bad","icon":"minecraft:bread","loot_table":"exodus:x",
           "radio_types":["elite"],"max_requests":1,"cooldown_seconds":0,
           "drop":{"smoke_color":"red"}}
          """).getAsJsonObject();
        var result=SupplyJsonParser.parse("exodus:bad",json);
        assertTrue(result.errors().stream().anyMatch(e->e.contains("radio_types")));
        assertTrue(result.errors().stream().anyMatch(e->e.contains("drop.smoke_color")));
    }
}
