package dev.exodus.teleporter.domain;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UnboundDroppedComponentTest {
    @Test void giveOverflowSurvivesUntilAcquisitionDuringMatch() {
        assertEquals(DroppedComponentPolicy.Action.PROTECT,
                DroppedComponentPolicy.action(null, UUID.randomUUID()));
    }
    @Test void unboundItemsOutsideMatchAreRemoved() {
        assertEquals(DroppedComponentPolicy.Action.REMOVE,
                DroppedComponentPolicy.action(null, null));
    }
}
