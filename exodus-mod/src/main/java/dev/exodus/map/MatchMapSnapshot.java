package dev.exodus.map;
import java.util.*;
public record MatchMapSnapshot(long epoch,long revision,UUID matchId,String dimension,int centerX,int centerZ,int borderSize,boolean clear,List<MapLocation> locations,Set<UUID> teammates) {
 public static final int MAX_LOCATIONS=1024;
 public MatchMapSnapshot(long epoch,long revision,UUID matchId,String dimension,int centerX,int centerZ,int borderSize,boolean clear,List<MapLocation> locations){this(epoch,revision,matchId,dimension,centerX,centerZ,borderSize,clear,locations,Set.of());}
 public MatchMapSnapshot {locations=List.copyOf(locations);teammates=Set.copyOf(teammates);if(epoch<0||revision<0||dimension==null||dimension.length()>160||locations.size()>MAX_LOCATIONS||teammates.size()>1||clear&&(!teammates.isEmpty()||!locations.isEmpty())||!clear&&(matchId==null||borderSize<=0))throw new IllegalArgumentException("Invalid map snapshot");}
}
