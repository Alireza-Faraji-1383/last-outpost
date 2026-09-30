package dev.exodus.map;
import java.util.*;
public final class MapProjectionPolicy {
 private MapProjectionPolicy(){}
 public static boolean privateVisible(dev.exodus.domain.Association role,boolean eliminated){return role==dev.exodus.domain.Association.MATCH_PLAYER||eliminated;}
 public static List<MapLocation> project(List<MapLocation> publicLocations,List<MapLocation> privateLocations,List<MapLocation> owned,Set<String> discovered){Map<String,MapLocation> visible=new TreeMap<>();publicLocations.forEach(l->visible.put(l.id(),l));privateLocations.stream().filter(l->discovered.contains(l.id())).forEach(l->visible.put(l.id(),l));owned.forEach(l->visible.put(l.id(),l));return List.copyOf(visible.values());}
}
