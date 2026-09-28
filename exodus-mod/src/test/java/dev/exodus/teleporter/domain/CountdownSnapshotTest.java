package dev.exodus.teleporter.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CountdownSnapshotTest {
    @Test void capturedSettingsAndBoundariesRemainStable() {
        var snapshot=new CountdownSnapshot(100,1200,5.0,2);
        assertEquals(0.5,snapshot.progress(12_100),0.0001);
        assertFalse(snapshot.expired(24_099));
        assertTrue(snapshot.expired(24_100));
        assertEquals(5.0,snapshot.radius());
        assertEquals(2,snapshot.capacity());
    }

    @Test void progressClampsAtBothEnds() {
        var snapshot=new CountdownSnapshot(100,10,5,2);
        assertEquals(1.0,snapshot.progress(0));
        assertEquals(0.0,snapshot.progress(1_000));
    }
}
