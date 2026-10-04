package dev.exodus.session;
import dev.exodus.device.DeviceRecord;import dev.exodus.map.MapLocation;
import net.minecraft.nbt.*;import java.util.*;
public final class MatchSessionState {
 public dev.exodus.event.EventSavedState events=new dev.exodus.event.EventSavedState();
 public UUID matchId,arenaId;public long elapsedTicks;public int borderSize;
 public final Set<UUID> roster=new HashSet<>(),eliminated=new HashSet<>();
 public final Map<Long,DeviceRecord> devices=new LinkedHashMap<>();
 public final Map<UUID,Long> selectedSpawns=new HashMap<>(),originalSpawns=new HashMap<>(),protectedUntil=new HashMap<>(),pendingRespawns=new HashMap<>();
 public final Map<UUID,UUID> baseMarkerIds=new HashMap<>();
 public final Map<UUID,Set<String>> discovered=new HashMap<>();
 public final List<MapLocation> locations=new ArrayList<>();
 public CompoundTag save(){
  CompoundTag t=new CompoundTag();t.putInt("version",1);t.put("events",events.save());if(matchId!=null)t.putUUID("matchId",matchId);if(arenaId!=null)t.putUUID("arenaId",arenaId);t.putLong("elapsed",elapsedTicks);t.putInt("borderSize",borderSize);
  ListTag players=new ListTag();Set<UUID> ids=new HashSet<>(roster);ids.addAll(discovered.keySet());ids.addAll(selectedSpawns.keySet());
  for(UUID id:ids){CompoundTag p=new CompoundTag();p.putUUID("id",id);p.putBoolean("roster",roster.contains(id));p.putBoolean("dead",eliminated.contains(id));if(selectedSpawns.containsKey(id))p.putLong("selected",selectedSpawns.get(id));if(pendingRespawns.containsKey(id))p.putLong("pendingRespawn",pendingRespawns.get(id));if(baseMarkerIds.containsKey(id))p.putUUID("baseMarker",baseMarkerIds.get(id));if(originalSpawns.containsKey(id))p.putLong("original",originalSpawns.get(id));if(protectedUntil.containsKey(id))p.putLong("protection",protectedUntil.get(id));ListTag seen=new ListTag();discovered.getOrDefault(id,Set.of()).forEach(s->seen.add(StringTag.valueOf(s)));p.put("seen",seen);players.add(p);}t.put("players",players);
  ListTag deviceTags=new ListTag();for(DeviceRecord d:devices.values()){CompoundTag a=new CompoundTag();a.putUUID("match",d.matchId());a.putString("dimension",d.dimension());a.putLong("pos",d.position());if(d.owner()!=null)a.putUUID("owner",d.owner());if(d.protectedOwner()!=null)a.putUUID("protected",d.protectedOwner());a.putInt("mask",d.componentMask());a.putBoolean("active",d.active());deviceTags.add(a);}t.put("devices",deviceTags);
  ListTag loc=new ListTag();for(MapLocation l:locations){CompoundTag a=new CompoundTag();a.putString("id",l.id());a.putString("label",l.label());a.putString("kind",l.kind().name());a.putInt("x",l.x());a.putInt("y",l.y());a.putInt("z",l.z());loc.add(a);}t.put("locations",loc);return t;
 }
 public static MatchSessionState load(CompoundTag t){
  MatchSessionState s=new MatchSessionState();s.events=dev.exodus.event.EventSavedState.load(t.getCompound("events"));if(t.hasUUID("matchId"))s.matchId=t.getUUID("matchId");if(t.hasUUID("arenaId"))s.arenaId=t.getUUID("arenaId");s.elapsedTicks=Math.max(0,t.getLong("elapsed"));s.borderSize=t.getInt("borderSize");
  for(Tag raw:t.getList("players",Tag.TAG_COMPOUND)){CompoundTag p=(CompoundTag)raw;if(!p.hasUUID("id"))continue;UUID id=p.getUUID("id");if(p.getBoolean("roster"))s.roster.add(id);if(p.getBoolean("dead"))s.eliminated.add(id);if(p.contains("selected"))s.selectedSpawns.put(id,p.getLong("selected"));if(p.contains("pendingRespawn"))s.pendingRespawns.put(id,p.getLong("pendingRespawn"));if(p.hasUUID("baseMarker"))s.baseMarkerIds.put(id,p.getUUID("baseMarker"));if(p.contains("original"))s.originalSpawns.put(id,p.getLong("original"));if(p.contains("protection"))s.protectedUntil.put(id,p.getLong("protection"));Set<String> seen=new HashSet<>();for(Tag v:p.getList("seen",Tag.TAG_STRING))seen.add(v.getAsString());s.discovered.put(id,seen);}
  for(Tag raw:t.getList("devices",Tag.TAG_COMPOUND)){CompoundTag a=(CompoundTag)raw;if(!a.hasUUID("match")||a.getString("dimension").isBlank())continue;DeviceRecord d=new DeviceRecord(a.getUUID("match"),a.getString("dimension"),a.getLong("pos"),a.hasUUID("owner")?a.getUUID("owner"):null,a.hasUUID("protected")?a.getUUID("protected"):null,a.getInt("mask"),a.getBoolean("active"));s.devices.put(d.position(),d);}
  for(Tag raw:t.getList("locations",Tag.TAG_COMPOUND)){CompoundTag a=(CompoundTag)raw;try{s.locations.add(new MapLocation(a.getString("id"),a.getString("label"),MapLocation.Kind.valueOf(a.getString("kind")),a.getInt("x"),a.getInt("y"),a.getInt("z")));}catch(IllegalArgumentException ignored){}}
  return s;
 }
}
