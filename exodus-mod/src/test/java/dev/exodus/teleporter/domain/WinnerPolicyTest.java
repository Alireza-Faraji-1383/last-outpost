package dev.exodus.teleporter.domain;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class WinnerPolicyTest {
    @Test void selectsNearestTwoInsideInclusiveRadiusWithStableTieBreak() {
        UUID a=UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID b=UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID c=UUID.fromString("00000000-0000-0000-0000-000000000003");
        var candidates=List.of(new EscapeCandidate(c,25.0,true),new EscapeCandidate(b,4.0,true),new EscapeCandidate(a,4.0,true));
        assertEquals(List.of(a,b),WinnerPolicy.select(candidates,5.0,2));
    }

    @Test void excludesOutsideAndIneligiblePlayers() {
        UUID outside=UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID spectator=UUID.fromString("00000000-0000-0000-0000-000000000002");
        assertTrue(WinnerPolicy.select(List.of(new EscapeCandidate(outside,25.0001,true),new EscapeCandidate(spectator,1.0,false)),5.0,2).isEmpty());
    }
}
