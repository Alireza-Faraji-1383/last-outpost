package dev.exodus.map;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class MapSessionGateTest {
 @Test void oldMatchAndOldRevisionCannotReturn(){MapSessionGate gate=new MapSessionGate();assertTrue(gate.accept(1,10));assertTrue(gate.accept(2,0));assertFalse(gate.accept(1,11));assertFalse(gate.accept(2,0));assertTrue(gate.accept(2,1));gate.reset();assertTrue(gate.accept(1,0));}
}
