package dev.exodus.teleporter.domain;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ComponentPolicyTest {
    private static final UUID CURRENT=UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test void acceptsOnlyTheExactBoundComponentForTheSlot() {
        assertTrue(ComponentPolicy.accepts(0,TeleporterComponent.REINFORCED_FRAME,new ComponentStackState(CURRENT,false),CURRENT));
        assertFalse(ComponentPolicy.accepts(1,TeleporterComponent.REINFORCED_FRAME,new ComponentStackState(CURRENT,false),CURRENT));
    }

    @Test void rejectsAValidComponentBoundToAnotherMatch() {
        UUID stale=UUID.fromString("00000000-0000-0000-0000-000000000002");
        assertFalse(ComponentPolicy.accepts(0,TeleporterComponent.REINFORCED_FRAME,new ComponentStackState(stale,false),CURRENT));
    }

    @Test void rejectsTemplateAndUnboundStacks() {
        assertFalse(ComponentPolicy.accepts(3,TeleporterComponent.FACILITY_ALPHA_KEY,new ComponentStackState(null,true),CURRENT));
        assertFalse(ComponentPolicy.accepts(0,TeleporterComponent.REINFORCED_FRAME,new ComponentStackState(null,false),CURRENT));
    }
}
