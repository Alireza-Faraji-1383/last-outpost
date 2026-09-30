package dev.exodus.device;
import java.util.*;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class ClaimRegistryTest {
 @Test void oneRequestPerDeviceAndLogoutReleasesIt(){var claims=new ClaimRegistry();UUID match=UUID.randomUUID(),a=UUID.randomUUID(),b=UUID.randomUUID();assertTrue(claims.start(7,new ClaimRegistry.Claim(match,a,0,400,5)));assertFalse(claims.start(7,new ClaimRegistry.Claim(match,b,0,400,5)));assertTrue(claims.start(8,new ClaimRegistry.Claim(match,b,0,400,5)));claims.cancel(a);assertTrue(claims.start(7,new ClaimRegistry.Claim(match,b,1,400,5)));assertEquals(2,claims.entries().size());}
}
