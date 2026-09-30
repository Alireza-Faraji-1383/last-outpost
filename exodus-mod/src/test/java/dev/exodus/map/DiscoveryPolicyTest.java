package dev.exodus.map;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class DiscoveryPolicyTest {
 @Test void horizontalAndVerticalInclusive(){assertTrue(DiscoveryPolicy.discovered(30,20,40,true,50,20));assertFalse(DiscoveryPolicy.discovered(30,21,40,true,50,20));assertFalse(DiscoveryPolicy.discovered(50.01,0,0,true,50,20));assertFalse(DiscoveryPolicy.discovered(0,0,0,false,50,20));}
}
