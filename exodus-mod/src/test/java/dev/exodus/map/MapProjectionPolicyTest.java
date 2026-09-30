package dev.exodus.map;
import java.util.*;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class MapProjectionPolicyTest {
 @Test void onlyEliminatedSpectatorsRetainPrivateData(){assertTrue(MapProjectionPolicy.privateVisible(dev.exodus.domain.Association.MATCH_PLAYER,false));assertTrue(MapProjectionPolicy.privateVisible(dev.exodus.domain.Association.AUTO_SPECTATOR,true));assertFalse(MapProjectionPolicy.privateVisible(dev.exodus.domain.Association.AUTO_SPECTATOR,false));assertFalse(MapProjectionPolicy.privateVisible(dev.exodus.domain.Association.INITIAL_SPECTATOR,false));}
 @Test void undiscoveredCoordinatesNeverReachViewer(){var city=new MapLocation("city","City",MapLocation.Kind.CITY,1,64,2);var secret=new MapLocation("camp","Discovered Camp",MapLocation.Kind.PLAYER_BASE,900,64,900);assertEquals(List.of(city),MapProjectionPolicy.project(List.of(city),List.of(secret),List.of(),Set.of()));assertEquals(List.of(secret,city),MapProjectionPolicy.project(List.of(city),List.of(secret),List.of(),Set.of("camp")));}
 @Test void ownDeviceVisibleWithoutDiscovery(){var own=new MapLocation("own","Claimed Device",MapLocation.Kind.DEVICE,3,64,4);assertEquals(List.of(own),MapProjectionPolicy.project(List.of(),List.of(),List.of(own),Set.of()));}
}
