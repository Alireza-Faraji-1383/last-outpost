package dev.exodus.device;
import java.util.*;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class DevicePoliciesTest {
 @Test void distinctUnionNeverCombinesDevices(){assertEquals(2,ComponentProgressPolicy.bestCount(1,List.of(2,4)));assertEquals(3,ComponentProgressPolicy.bestCount(1,List.of(1,6)));assertEquals(1,ComponentProgressPolicy.bestCount(1,List.of(1)));assertEquals(2,ComponentProgressPolicy.bestCount(3,List.of()));}
 @Test void claimsRequireContinuousTimeAndEligibility(){assertFalse(ClaimPolicy.complete(0,399,400,true,false));assertTrue(ClaimPolicy.complete(0,400,400,true,false));assertFalse(ClaimPolicy.complete(0,400,400,true,true));assertFalse(ClaimPolicy.complete(0,400,400,false,false));}
 @Test void protectedDevicesAndActiveDevicesCannotTransfer(){assertFalse(ClaimPolicy.canClaim(true,false,true));assertFalse(ClaimPolicy.canClaim(false,true,true));assertFalse(ClaimPolicy.canClaim(false,false,false));assertTrue(ClaimPolicy.canClaim(false,false,true));}
}
