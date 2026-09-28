package dev.exodus.wasteland.arena;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ArenaRegistryTest {
    @Test void enforcesSinglePreparationAndReadyArena(){
        ArenaRegistry registry=new ArenaRegistry();
        ArenaRecord first=registry.begin(UUID.randomUUID(),0,4096,0,2000,128);
        assertEquals(ArenaState.PREPARING,first.state());
        assertThrows(IllegalStateException.class,()->registry.begin(UUID.randomUUID(),1,4096,4096,2000,128));
        registry.ready(first.id());
        assertEquals(first.id(),registry.readyArena().orElseThrow().id());
        assertThrows(IllegalStateException.class,()->registry.begin(UUID.randomUUID(),1,4096,4096,2000,128));
        registry.consume(first.id());
        assertEquals(ArenaState.CONSUMED,first.state());
        assertTrue(registry.readyArena().isEmpty());
    }

    @Test void cancelAndFailureAreTerminal(){
        ArenaRegistry cancelled=new ArenaRegistry();
        ArenaRecord a=cancelled.begin(UUID.randomUUID(),0,1,1,2000,128);
        cancelled.cancel(a.id());
        assertEquals(ArenaState.ABANDONED,a.state());
        assertThrows(IllegalStateException.class,()->cancelled.ready(a.id()));

        ArenaRegistry failed=new ArenaRegistry();
        ArenaRecord b=failed.begin(UUID.randomUUID(),0,1,1,2000,128);
        failed.fail(b.id(),"missing template");
        assertEquals(ArenaState.FAILED,b.state());
        assertEquals("missing template",b.failure());
    }
}
