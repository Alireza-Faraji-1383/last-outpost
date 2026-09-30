package dev.exodus.map;
import dev.exodus.wasteland.arena.*;import net.minecraft.core.BlockPos;import java.util.*;
public final class ArenaMapCatalog {
 private ArenaMapCatalog(){}
 public static List<MapLocation> locations(ArenaRecord arena,BlockPos city){
  List<MapLocation> result=new ArrayList<>();if(city!=null)result.add(new MapLocation("city","City",MapLocation.Kind.CITY,city.getX(),city.getY(),city.getZ()));
  Map<MapLocation.Kind,List<ArenaLocation>> factions=new EnumMap<>(MapLocation.Kind.class);
  for(ArenaLocation l:ArenaPreparationService.locations(arena)){MapLocation.Kind kind=MapLocation.Kind.valueOf(l.kind().name());if(kind==MapLocation.Kind.RUSSIAN_BASE||kind==MapLocation.Kind.AMERICAN_BASE)factions.computeIfAbsent(kind,k->new ArrayList<>()).add(l);else result.add(new MapLocation(l.placementId(),kind==MapLocation.Kind.ABANDONED_CAMP?"Abandoned Camp":"Occupied Camp",kind,l.x(),l.y(),l.z()));}
  factions.forEach((kind,parts)->{int x=(int)parts.stream().mapToInt(ArenaLocation::x).average().orElseThrow(),z=(int)parts.stream().mapToInt(ArenaLocation::z).average().orElseThrow(),y=parts.get(0).y();result.add(new MapLocation(kind.name(),kind==MapLocation.Kind.RUSSIAN_BASE?"Russian Base":"American Base",kind,x,y,z));});return List.copyOf(result);
 }
}
