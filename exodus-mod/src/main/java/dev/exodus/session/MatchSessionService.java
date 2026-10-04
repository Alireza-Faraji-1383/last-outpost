package dev.exodus.session;
import dev.exodus.*;import dev.exodus.device.*;import dev.exodus.map.*;import net.minecraft.core.*;import net.minecraft.server.*;import net.minecraft.server.level.*;import java.util.*;
public final class MatchSessionService {
 private MatchSessionService(){}
 public static void begin(MinecraftServer server,ServerLevel level,UUID arenaId,BlockPos city){
  if(!net.minecraftforge.fml.ModList.get().isLoaded("journeymap"))com.mojang.logging.LogUtils.getLogger().warn("[Exodus] JourneyMap is not installed; match map integration is unavailable.");
  var d=ExodusSavedData.get(server);d.session=new MatchSessionState();var s=d.session;s.matchId=d.matchId;s.arenaId=arenaId;s.borderSize=ExodusConfig.BORDER_SIZE.get();d.mapEpoch++;level.setDayTime(0);
  d.associations.forEach((id,role)->{if(role==dev.exodus.domain.Association.MATCH_PLAYER){s.roster.add(id);s.protectedUntil.put(id,ExodusConfig.PROTECTION_SECONDS.get()*20L);}});
  var arena=d.arenas.records().stream().filter(r->r.id().equals(arenaId)).findFirst().orElseThrow();s.locations.addAll(ArenaMapCatalog.locations(arena,city));
  for(var base:d.bases.values()){var template=level.getStructureManager().get(base.structureId()).orElse(null);if(template!=null)DeviceIndex.scan(level,base.origin(),template.getSize(),base.uuid());}
  for(var location:s.locations)if(location.kind()!=MapLocation.Kind.CITY)DeviceIndex.scan(level,new BlockPos(location.x()-64,location.y()-32,location.z()-64),new Vec3i(128,96,128),null);
  s.selectedSpawns.putAll(s.originalSpawns);d.setDirty();
 }
 public static void tick(MinecraftServer server){var d=ExodusSavedData.get(server);if(d.state!=MatchState.RUNNING)return;d.session.elapsedTicks++;DeviceOwnershipService.tick(server);dev.exodus.event.EventManager.tick(server);MatchBossBarService.tick(server);MatchMapService.tick(server);d.setDirty();}
 public static void cleanup(MinecraftServer server){var d=ExodusSavedData.get(server);dev.exodus.event.EventManager.cleanup(server);MatchMapService.clear(server);MatchBossBarService.cleanup(server);DeviceOwnershipService.cleanup(server);d.session=new MatchSessionState();d.setDirty();}
}
