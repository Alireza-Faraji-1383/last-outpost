package dev.exodus.map;
import java.util.*;
public record MatchMapSnapshot(long epoch,long revision,UUID matchId,String dimension,int centerX,int centerZ,int borderSize,boolean clear,List<MapLocation> locations) {
 public static final int MAX_LOCATIONS=1024;
 public MatchMapSnapshot {locations=List.copyOf(locations);if(epoch<0||revision<0||dimension==null||dimension.length()>160||locations.size()>MAX_LOCATIONS||!clear&&(matchId==null||borderSize<=0))throw new IllegalArgumentException("Invalid map snapshot");}
}
