package dev.exodus.supply.link;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinkingSelectionTest {
    @Test void selectionRoundTripsWithoutOwnerState() {
        LinkingSelection value = new LinkingSelection("minecraft:overworld", 12, 70, -4);
        assertEquals(value, LinkingSelection.decode(value.encode()).orElseThrow());
    }

    @Test void malformedSelectionIsRejected() {
        CompoundTag malformed = new CompoundTag();
        malformed.putString("dimension", "not a dimension");
        assertTrue(LinkingSelection.decode(malformed).isEmpty());
    }
}
