package dev.exodus.wasteland.loot;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LootMarkerTest {
    @Test void mapsAllNineMarkers() {
        for (LootMarker marker : LootMarker.values()) {
            assertEquals(marker, LootMarker.parse(marker.metadata()).orElseThrow());
            assertEquals("exodus:chests/" + marker.metadata().substring("exodus:loot/".length()), marker.lootTable());
        }
        assertEquals(9, LootMarker.values().length);
    }
    @Test void rejectsUnknownExodusMarker() {
        assertThrows(IllegalArgumentException.class, () -> LootMarker.requireKnown("exodus:loot/mystery"));
    }
    @Test void preservesForeignDataMarker() { assertTrue(LootMarker.parse("other:data").isEmpty()); }
}
