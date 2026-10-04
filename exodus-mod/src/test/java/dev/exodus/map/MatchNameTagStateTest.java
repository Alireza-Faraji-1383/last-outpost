package dev.exodus.map;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MatchNameTagStateTest {
    @Test void hidesOnlyOtherPlayersInTheActiveDimensionAndClearsOnEnding() {
        MatchNameTagState state = new MatchNameTagState();
        assertFalse(state.hidden("exodus:wasteland", true, false));
        state.receive(new MatchMapSnapshot(1, 1, UUID.randomUUID(), "exodus:wasteland", 0, 0, 2000, false, List.of()));
        assertTrue(state.hidden("exodus:wasteland", true, false));
        assertFalse(state.hidden("minecraft:overworld", true, false));
        assertFalse(state.hidden("exodus:wasteland", false, false));
        assertFalse(state.hidden("exodus:wasteland", true, true));
        state.receive(new MatchMapSnapshot(2, 2, null, "", 0, 0, 0, true, List.of()));
        assertFalse(state.hidden("exodus:wasteland", true, false));
    }

    @Test void disconnectResetsVisibility() {
        MatchNameTagState state = new MatchNameTagState();
        state.receive(new MatchMapSnapshot(1, 1, UUID.randomUUID(), "exodus:wasteland", 0, 0, 2000, false, List.of()));
        state.reset();
        assertFalse(state.hidden("exodus:wasteland", true, false));
    }
}
