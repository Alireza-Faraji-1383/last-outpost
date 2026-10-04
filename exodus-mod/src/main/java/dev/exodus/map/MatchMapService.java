package dev.exodus.map;
import dev.exodus.*;import dev.exodus.network.ExodusNetwork;import dev.exodus.teleporter.item.*;import net.minecraft.core.*;import net.minecraft.server.*;import net.minecraft.server.level.*;import net.minecraft.world.entity.item.ItemEntity;import java.util.*;
public final class MatchMapService {
 private record View(List<MapLocation> locations,Set<UUID> teammates){}
 private static final Map<MinecraftServer,Map<UUID,View>> LAST=new WeakHashMap<>();
 private static final Map<MinecraftServer,Long> REVISION=new WeakHashMap<>();private MatchMapService(){}
 public static void tick(MinecraftServer server){
  var d=ExodusSavedData.get(server);if(d.state!=MatchState.RUNNING||d.session.elapsedTicks%ExodusConfig.PARTY_MAP_INTERVAL_TICKS.get()!=1)return;
  updateNow(server);
 }
 public static void updateNow(MinecraftServer server){
  var d=ExodusSavedData.get(server);if(d.state!=MatchState.RUNNING||d.matchId==null)return;
  var level=server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,new net.minecraft.resources.ResourceLocation(d.dimension)));if(level==null)return;
  JourneyMapRadarPolicy.enforce(server,level.dimension());
  var previous=LAST.computeIfAbsent(server,k->new HashMap<>());previous.keySet().removeIf(id->server.getPlayerList().getPlayer(id)==null);
  List<MapLocation> pub=new ArrayList<>(),hidden=new ArrayList<>();
  pub.addAll(dev.exodus.supply.event.EventAirdropService.markers(server));
  for(MapLocation l:d.session.locations)if(l.kind()==MapLocation.Kind.CITY||l.kind()==MapLocation.Kind.RUSSIAN_BASE||l.kind()==MapLocation.Kind.AMERICAN_BASE)pub.add(l);else hidden.add(l);
  for(var base:d.bases.values())hidden.add(new MapLocation("base:"+d.session.baseMarkerIds.computeIfAbsent(base.uuid(),id->UUID.randomUUID()),"Discovered Camp",MapLocation.Kind.PLAYER_BASE,base.center().getX(),base.center().getY(),base.center().getZ()));
  for(var device:d.session.devices.values()){BlockPos pos=BlockPos.of(device.position());hidden.add(new MapLocation("device:"+device.position(),"Discovered Device",MapLocation.Kind.DEVICE,pos.getX(),pos.getY(),pos.getZ()));}
  var active=d.teleporter.active();if(active!=null){BlockPos pos=BlockPos.of(active.blockPos());pub.add(new MapLocation("teleporter","Exodus Teleporter",MapLocation.Kind.ACTIVE_DEVICE,pos.getX(),pos.getY(),pos.getZ()));}
  for(var entity:level.getAllEntities())if(entity instanceof ItemEntity item&&ComponentStacks.isCurrent(item.getItem(),d.matchId)&&item.getItem().getItem() instanceof TeleporterComponentItem component&&component.component().rare()){BlockPos pos=item.blockPosition();pub.add(new MapLocation("rare:"+item.getUUID(),item.getItem().getHoverName().getString(),MapLocation.Kind.RARE_ITEM,pos.getX(),pos.getY(),pos.getZ()));}
  long revision=REVISION.merge(server,1L,Long::sum);
  for(ServerPlayer p:MatchManager.associatedOnlinePlayers(server)){
   Set<String> seen=d.session.discovered.computeIfAbsent(p.getUUID(),k->new HashSet<>());
   boolean playing=MatchManager.isActiveMatchPlayer(p)&&p.isAlive();if(playing)for(MapLocation l:hidden)if(DiscoveryPolicy.discovered(p.getX()-l.x(),p.getY()-l.y(),p.getZ()-l.z(),true,ExodusConfig.DISCOVERY_HORIZONTAL.get(),ExodusConfig.DISCOVERY_VERTICAL.get()))seen.add(l.id());
   List<MapLocation> owned=new ArrayList<>();var base=d.bases.get(p.getUUID());if(base!=null)owned.add(new MapLocation("base:"+d.session.baseMarkerIds.computeIfAbsent(p.getUUID(),id->UUID.randomUUID()),"My Base",MapLocation.Kind.PLAYER_BASE,base.center().getX(),base.center().getY(),base.center().getZ()));
   for(var device:d.session.devices.values())if(p.getUUID().equals(device.owner())){BlockPos pos=BlockPos.of(device.position());owned.add(new MapLocation("device:"+device.position(),"Claimed Device",MapLocation.Kind.DEVICE,pos.getX(),pos.getY(),pos.getZ()));}
   boolean privateVisible=MapProjectionPolicy.privateVisible(d.associations.get(p.getUUID()),d.session.eliminated.contains(p.getUUID()));
   List<MapLocation> visible=new ArrayList<>(MapProjectionPolicy.project(pub,hidden,privateVisible?owned:List.of(),privateVisible?seen:Set.of()));
   Set<UUID> teammates=new HashSet<>();
   dev.exodus.party.PartyService.teammate(server,p.getUUID()).ifPresent(id->{var mate=server.getPlayerList().getPlayer(id);if(mate!=null&&TeammateProjectionPolicy.visible(playing,MatchManager.isActiveMatchPlayer(mate),mate.isAlive(),p.serverLevel()==mate.serverLevel())){teammates.add(id);visible.add(new MapLocation("teammate:"+id,mate.getGameProfile().getName(),MapLocation.Kind.TEAMMATE,mate.getBlockX(),mate.getBlockY(),mate.getBlockZ()));}});
   var view=new View(List.copyOf(visible),Set.copyOf(teammates));
   if(!view.equals(previous.get(p.getUUID()))){ExodusNetwork.send(p,new MatchMapSnapshot(d.mapEpoch,revision,d.matchId,d.dimension,d.centerX,d.centerZ,d.session.borderSize,false,view.locations(),view.teammates()));previous.put(p.getUUID(),view);}
  }d.setDirty();
 }
 public static void refresh(MinecraftServer server,UUID player){var previous=LAST.get(server);if(previous!=null)previous.remove(player);}
 public static void clear(MinecraftServer server){var d=ExodusSavedData.get(server);d.mapEpoch++;long revision=REVISION.merge(server,1L,Long::sum);for(ServerPlayer p:MatchManager.associatedOnlinePlayers(server))ExodusNetwork.send(p,new MatchMapSnapshot(d.mapEpoch,revision,null,"",0,0,0,true,List.of()));LAST.remove(server);JourneyMapRadarPolicy.restore(server);}
}
