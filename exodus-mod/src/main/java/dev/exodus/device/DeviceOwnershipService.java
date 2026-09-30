package dev.exodus.device;
import dev.exodus.*;import dev.exodus.teleporter.blockentity.ExodusTeleporterBlockEntity;import dev.exodus.player.RespawnService;
import net.minecraft.core.BlockPos;import net.minecraft.server.*;import net.minecraft.server.level.*;import net.minecraft.network.chat.Component;import java.util.*;
public final class DeviceOwnershipService {

 private static final Map<MinecraftServer,ClaimRegistry> CLAIMS=new WeakHashMap<>();
 private DeviceOwnershipService(){}
 private static ClaimRegistry registry(MinecraftServer s){return CLAIMS.computeIfAbsent(s,k->new ClaimRegistry());}
 private static Map<Long,ClaimRegistry.Claim> claims(MinecraftServer s){return registry(s).entries();}
 public static boolean canClaim(ServerPlayer p,BlockPos pos){var d=ExodusSavedData.get(p.server);var record=d.session.devices.get(pos.asLong());return record!=null&&ClaimPolicy.canClaim(record.protectedOwner()!=null,record.active(),MatchManager.isActiveMatchPlayer(p)&&p.isAlive())&&!p.getUUID().equals(record.owner());}
 public static boolean canSetSpawn(ServerPlayer p,BlockPos pos){var d=ExodusSavedData.get(p.server);var record=d.session.devices.get(pos.asLong());return p.isAlive()&&MatchManager.isActiveMatchPlayer(p)&&d.teleporter.active()==null&&record!=null&&p.getUUID().equals(record.owner());}
 public static void requestClaim(ServerPlayer p,BlockPos pos){
  if(!(p.serverLevel().getBlockEntity(pos) instanceof ExodusTeleporterBlockEntity be))return;DeviceIndex.observe(be);
  if(!canClaim(p,pos)){p.sendSystemMessage(Component.literal("This device cannot be claimed."));return;}
  if(p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)>ExodusConfig.CLAIM_RADIUS.get()*(double)ExodusConfig.CLAIM_RADIUS.get())return;
  if(claims(p.server).containsKey(pos.asLong())){p.sendSystemMessage(Component.literal("This device is being claimed."));return;}
  var d=ExodusSavedData.get(p.server);registry(p.server).start(pos.asLong(),new ClaimRegistry.Claim(d.matchId,p.getUUID(),d.session.elapsedTicks,ExodusConfig.CLAIM_SECONDS.get()*20L,ExodusConfig.CLAIM_RADIUS.get()));
  var old=d.session.devices.get(pos.asLong());notify(p.server,old.owner(),"Your device at "+coordinates(pos)+" is being claimed.");
 }
 public static void setSpawn(ServerPlayer p,BlockPos pos){
  if(!canSetSpawn(p,pos))return;var d=ExodusSavedData.get(p.server);d.session.selectedSpawns.put(p.getUUID(),pos.asLong());RespawnService.setBase(p,p.serverLevel(),pos.above());d.setDirty();p.sendSystemMessage(Component.literal("Spawnpoint set to device at "+coordinates(pos)+"."));
 }
 public static void tick(MinecraftServer server){
  var d=ExodusSavedData.get(server);Iterator<Map.Entry<Long,ClaimRegistry.Claim>> iterator=claims(server).entrySet().iterator();
  while(iterator.hasNext()){var e=iterator.next();ClaimRegistry.Claim c=e.getValue();BlockPos pos=BlockPos.of(e.getKey());ServerPlayer p=server.getPlayerList().getPlayer(c.player());DeviceRecord record=d.session.devices.get(e.getKey());
   boolean valid=d.state==MatchState.RUNNING&&Objects.equals(d.matchId,c.match())&&p!=null&&MatchManager.isActiveMatchPlayer(p)&&p.isAlive()&&record!=null&&record.protectedOwner()==null&&!record.active()&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=c.radius()*(double)c.radius()&&p.serverLevel().getBlockEntity(pos) instanceof ExodusTeleporterBlockEntity be&&!be.locked();
   if(!valid){iterator.remove();if(p!=null)p.sendSystemMessage(Component.literal("Device claim cancelled."));continue;}
   long elapsed=d.session.elapsedTicks-c.start();p.displayClientMessage(Component.literal("Claiming Device: "+Math.min(c.duration()/20,elapsed/20)+"/"+c.duration()/20+"s"),true);
   if(ClaimPolicy.complete(c.start(),d.session.elapsedTicks,c.duration(),true,false)){iterator.remove();UUID previous=record.owner();d.session.devices.put(e.getKey(),record.ownedBy(p.getUUID()));resetSelected(server,previous,e.getKey());notify(server,previous,"Your device at "+coordinates(pos)+" was claimed by "+p.getGameProfile().getName()+".");p.sendSystemMessage(Component.literal("Device claimed."));d.setDirty();}
  }
  if(d.state==MatchState.RUNNING&&d.session.elapsedTicks%20==0)reconcileRemovals(server,d);
 }
 private static void reconcileRemovals(MinecraftServer server,ExodusSavedData d){
  var level=server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,new net.minecraft.resources.ResourceLocation(d.dimension)));if(level==null)return;
  for(var record:List.copyOf(d.session.devices.values())){BlockPos pos=BlockPos.of(record.position());if(level.hasChunkAt(pos)&&!(level.getBlockEntity(pos) instanceof ExodusTeleporterBlockEntity)){d.session.devices.remove(record.position());resetSelected(server,record.owner(),record.position());d.setDirty();}}
 }
 public static void resetSelected(MinecraftServer server,UUID player,long device){if(player==null)return;var d=ExodusSavedData.get(server);if(Objects.equals(d.session.selectedSpawns.get(player),device)){d.session.selectedSpawns.remove(player);
 if(d.session.pendingRespawns.containsKey(player)){Long primary=d.session.originalSpawns.get(player);var base=d.bases.get(player);if(primary!=null)d.session.pendingRespawns.put(player,BlockPos.of(primary).above().asLong());else if(base!=null)d.session.pendingRespawns.put(player,base.spawn().asLong());else d.session.pendingRespawns.remove(player);}
 ServerPlayer p=server.getPlayerList().getPlayer(player);if(p!=null)RespawnService.selectMatchSpawn(p);notify(server,player,"Your spawnpoint returned to your original base.");d.setDirty();}}
 public static void cancelFor(MinecraftServer server,UUID player){claims(server).entrySet().removeIf(e->e.getValue().player().equals(player));}
 public static void cleanup(MinecraftServer server){CLAIMS.remove(server);}
 public static void notify(MinecraftServer server,UUID player,String text){if(player==null)return;ServerPlayer p=server.getPlayerList().getPlayer(player);if(p!=null)p.sendSystemMessage(Component.literal(text));else{var d=ExodusSavedData.get(server);d.pendingNotices.computeIfAbsent(player,k->new ArrayList<>()).add(text);d.setDirty();}}
 public static void deliverNotices(ServerPlayer p){var d=ExodusSavedData.get(p.server);List<String> lines=d.pendingNotices.remove(p.getUUID());if(lines!=null){lines.forEach(line->p.sendSystemMessage(Component.literal(line)));d.setDirty();}}
 private static String coordinates(BlockPos p){return "X "+p.getX()+" Y "+p.getY()+" Z "+p.getZ();}
}
