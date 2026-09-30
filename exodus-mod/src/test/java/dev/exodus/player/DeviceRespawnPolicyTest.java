package dev.exodus.player;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class DeviceRespawnPolicyTest {
 @Test void primaryExemptAndSpectatorsExcluded(){assertFalse(DeviceRespawnPolicy.fallback(true,100,true,10));assertTrue(DeviceRespawnPolicy.fallback(false,100,true,10));assertFalse(DeviceRespawnPolicy.fallback(false,100.01,true,10));assertFalse(DeviceRespawnPolicy.fallback(false,0,false,10));}
 @Test void protectionEndsExactlyAtDeadline(){assertTrue(DamageProtectionPolicy.blocks(199,200));assertFalse(DamageProtectionPolicy.blocks(200,200));}
}
