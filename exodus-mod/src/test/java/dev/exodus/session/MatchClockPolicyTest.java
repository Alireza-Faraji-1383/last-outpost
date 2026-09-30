package dev.exodus.session;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MatchClockPolicyTest {
 @Test void dayBoundaries(){assertEquals(1,MatchClockPolicy.day(0,24000));assertEquals(1,MatchClockPolicy.day(23999,24000));assertEquals(2,MatchClockPolicy.day(24000,24000));}
 @Test void invalidClockRejected(){assertThrows(IllegalArgumentException.class,()->MatchClockPolicy.day(-1,24000));assertThrows(IllegalArgumentException.class,()->MatchClockPolicy.day(1,0));}
}
