package dev.exodus.gametest;
import dev.exodus.*;import dev.exodus.domain.Association;import dev.exodus.device.*;import dev.exodus.player.*;import dev.exodus.session.*;import dev.exodus.teleporter.*;
import net.minecraft.core.BlockPos;import net.minecraft.gametest.framework.*;import net.minecraft.server.level.*;import net.minecraft.world.level.GameType;import net.minecraftforge.gametest.*;import net.minecraftforge.common.MinecraftForge;import net.minecraftforge.event.entity.living.LivingDeathEvent;import java.util.*;
@GameTestHolder("exodus")
@PrefixGameTestTemplate(false)
public final class MatchFeatureGameTests {
 private static final Map<UUID,dev.exodus.map.MatchMapSnapshot> MAP_PACKETS=new HashMap<>();
 @GameTest(template="smoke",timeoutTicks=80)
 public static void lifecycle(GameTestHelper h){
  ServerLevel level=h.getLevel();var server=level.getServer();server.setPvpAllowed(true);var d=ExodusSavedData.get(server);
  ServerPlayer p=mock(level,"p"),q=mock(level,"q");p.setGameMode(GameType.SURVIVAL);q.setGameMode(GameType.SURVIVAL);
  BlockPos original=h.absolutePos(new BlockPos(1,1,1)),other=h.absolutePos(new BlockPos(5,1,1));
  level.setBlock(original,ExodusTeleporterRegistry.TELEPORTER.get().defaultBlockState(),3);level.setBlock(other,ExodusTeleporterRegistry.TELEPORTER.get().defaultBlockState(),3);
  d.state=MatchState.RUNNING;d.matchId=UUID.randomUUID();d.dimension=level.dimension().location().toString();d.session=new MatchSessionState();d.session.matchId=d.matchId;d.session.borderSize=2000;d.oldBorderX=level.getWorldBorder().getCenterX();d.oldBorderZ=level.getWorldBorder().getCenterZ();d.oldBorderSize=level.getWorldBorder().getSize();
  d.associations.put(p.getUUID(),Association.MATCH_PLAYER);d.associations.put(q.getUUID(),Association.MATCH_PLAYER);d.session.roster.addAll(List.of(p.getUUID(),q.getUUID()));
  d.bases.put(p.getUUID(),new PlayerBaseData(p.getUUID(),"p",original,original,original.above(),new net.minecraft.resources.ResourceLocation("exodus:starter_base")));
  d.session.originalSpawns.put(p.getUUID(),original.asLong());d.session.selectedSpawns.put(p.getUUID(),other.asLong());
  d.session.devices.put(original.asLong(),new DeviceRecord(d.matchId,d.dimension,original.asLong(),p.getUUID(),p.getUUID(),0,false));d.session.devices.put(other.asLong(),new DeviceRecord(d.matchId,d.dimension,other.asLong(),p.getUUID(),null,0,false));
  try{
   radarPermissions(server,level,p,q);
   p.teleportTo(level,other.getX()+.5,other.getY()+1,other.getZ()+.5,0,0);q.teleportTo(level,other.getX()+.5,other.getY()+1,other.getZ()+.5,0,0);
   d.session.protectedUntil.put(p.getUUID(),200L);p.hurt(level.damageSources().generic(),4);h.assertTrue(p.getHealth()==20,"Incoming protection");
   q.hurt(level.damageSources().playerAttack(p),4);h.assertTrue(q.getHealth()==20,"Outgoing protection");
   q.hurt(level.damageSources().arrow(new net.minecraft.world.entity.projectile.Arrow(level,p),p),4);h.assertTrue(q.getHealth()==20,"Projectile protection");
   q.hurt(level.damageSources().explosion(p,p),4);h.assertTrue(q.getHealth()==20,"Player-attributed explosion protection");
   d.session.elapsedTicks=201;q.hurt(level.damageSources().playerAttack(p),4);h.assertTrue(q.getHealth()<20,"Protection must expire");
   RespawnService.selectMatchSpawn(p);h.assertTrue(original.above().equals(p.getRespawnPosition()),"Contested selected spawn must fall back");h.assertTrue(d.session.selectedSpawns.get(p.getUUID()).equals(other.asLong()),"Fallback must preserve selection");
   d.session.pendingRespawns.put(p.getUUID(),other.above().asLong());
   DeviceOwnershipService.requestClaim(q,other);d.session.elapsedTicks+=400;DeviceOwnershipService.tick(server);h.assertTrue(q.getUUID().equals(d.session.devices.get(other.asLong()).owner()),"Public device must transfer");
   h.assertTrue(d.session.pendingRespawns.get(p.getUUID()).equals(original.above().asLong()),"Transfer must replace a dead owner pending respawn");
   h.assertTrue(!DeviceOwnershipService.canClaim(q,original),"Original device must be protected");
   d.session.locations.add(new dev.exodus.map.MapLocation("hidden","Occupied Camp",dev.exodus.map.MapLocation.Kind.OCCUPIED_CAMP,original.getX()+100,original.getY(),original.getZ()+100));
   dev.exodus.map.MatchMapService.tick(server);
   h.assertTrue(MAP_PACKETS.containsKey(p.getUUID())&&MAP_PACKETS.containsKey(q.getUUID()),"Both recipients must receive snapshots");
   h.assertTrue(MAP_PACKETS.get(q.getUUID()).locations().stream().noneMatch(l->l.id().equals("hidden")||l.label().equals("My Base")),"Actual recipient packet must exclude undiscovered camp and another player's private base label");
   d.associations.put(q.getUUID(),Association.AUTO_SPECTATOR);dev.exodus.map.MatchMapService.tick(server);
   h.assertTrue(MAP_PACKETS.get(q.getUUID()).locations().isEmpty(),"Expired spectator must receive public data only");
   d.associations.put(q.getUUID(),Association.MATCH_PLAYER);dev.exodus.map.MatchMapService.tick(server);
   // Snapshot a pre-final death, then obstruct its target before vanilla recreation.
   p.setHealth(0);MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(p,level.damageSources().generic()));
   h.assertTrue(d.session.pendingRespawns.containsKey(p.getUUID()),"Pre-final death must retain its exact target");
   level.setBlock(original.above(),net.minecraft.world.level.block.Blocks.WATER.defaultBlockState(),3);
   p=server.getPlayerList().respawn(p,false);
   h.assertTrue(level.getBlockState(original.above()).isAir(),"Respawn must clear fluid obstruction");
   h.assertTrue(p.blockPosition().equals(original.above()),"Respawn must arrive above the chosen device");
   h.assertTrue(d.session.protectedUntil.get(p.getUUID())==d.session.elapsedTicks+200,"Playable respawn grants ten seconds");
   h.assertTrue(!d.session.pendingRespawns.containsKey(p.getUUID()),"Respawn target must be consumed once");
   ServerPlayer delayed=mock(level,"delayed");delayed.setGameMode(GameType.SURVIVAL);d.associations.put(delayed.getUUID(),Association.MATCH_PLAYER);d.session.roster.add(delayed.getUUID());RespawnService.setBase(delayed,level,original.above());
   delayed.setHealth(0);MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(delayed,level.damageSources().generic()));
   var completed=(dev.exodus.teleporter.blockentity.ExodusTeleporterBlockEntity)level.getBlockEntity(other);
   for(var component:dev.exodus.teleporter.domain.TeleporterComponent.values()){var stack=new net.minecraft.world.item.ItemStack(ExodusTeleporterRegistry.item(component).get());dev.exodus.teleporter.item.ComponentStacks.bind(stack,d.matchId);completed.setItem(component.ordinal(),stack);if(component.rare())d.teleporter.rareClaims().add(component);}
   DeviceIndex.observe(completed);TeleporterService.tryActivate(level,other,completed);
   h.assertTrue(d.teleporter.active()!=null&&d.teleporter.active().blockPos()==other.asLong(),"Completed device must activate");
   h.assertTrue(completed.locked(),"Active device inventory must lock");
   h.assertTrue(!DeviceOwnershipService.canSetSpawn(p,original),"Final phase must disable spawn selection");
   delayed=server.getPlayerList().respawn(delayed,false);
   h.assertTrue(delayed.isSpectator()&&d.session.eliminated.contains(delayed.getUUID()),"Activation must deny a delayed pre-final death-screen respawn");
   d.session.elapsedTicks+=200;clearVanillaShield(p);p.hurt(level.damageSources().generic(),100);
   h.assertTrue(d.session.eliminated.contains(p.getUUID()),"Lethal death must permanently eliminate");
   h.assertTrue(d.associations.get(p.getUUID())==Association.AUTO_SPECTATOR,"Elimination must retain spectator association");
   MatchManager.tick(server);
   h.assertTrue(d.state==MatchState.RUNNING,"Sole survivor must still reach the teleporter");
   h.assertTrue(server.getPlayerList().getPlayer(p.getUUID()).isSpectator(),"Vanilla recreation of eliminated player must stay spectator");
   q.hurt(level.damageSources().generic(),100);h.assertTrue(d.session.eliminated.contains(q.getUUID()),"Second lethal death must eliminate");MatchManager.tick(server);
   h.assertTrue(d.state==MatchState.IDLE&&d.teleporter.active()==null,"All-dead match must end without winners and clear final destination");
   h.assertTrue(server.getPlayerList().getPlayer(q.getUUID()).getRespawnPosition().equals(server.overworld().getSharedSpawnPos()),"Cleanup must reset world spawn");
   h.succeed();com.mojang.logging.LogUtils.getLogger().info("[Exodus Test] lifecycle assertions passed, including JourneyMap radar when installed.");
  }catch(Throwable failure){failure.printStackTrace();throw failure;}finally{d.state=MatchState.IDLE;d.matchId=null;d.teleporter.active(null);d.associations.clear();d.pendingPlayers.clear();d.bases.clear();d.session=new MatchSessionState();DeviceOwnershipService.cleanup(server);d.setDirty();if(server.isDedicatedServer())server.execute(()->server.halt(false));}
 }
 private static void radarPermissions(net.minecraft.server.MinecraftServer server,ServerLevel level,ServerPlayer op,ServerPlayer ordinary){
  if(!net.minecraftforge.fml.ModList.get().isLoaded("journeymap"))return;
  try{
   Class<?> type=Class.forName("journeymap.common.properties.PropertiesManager");Object manager=type.getMethod("getInstance").invoke(null);
   Object global=type.getMethod("getGlobalProperties").invoke(manager),dim=type.getMethod("getDimProperties",net.minecraft.resources.ResourceKey.class).invoke(manager,level.dimension());
   var original=new java.util.IdentityHashMap<Object,Object>();
   for(Object group:List.of(global,dim))for(var field:group.getClass().getFields()){Object option=field.get(group);if(option!=null)try{original.put(option,option.getClass().getMethod("get").invoke(option));}catch(NoSuchMethodException ignored){}}
   Object enabled=dim.getClass().getField("enabled").get(dim);Object before=original.get(enabled);
   dev.exodus.map.JourneyMapRadarPolicy.enforce(server,level.dimension());
   if(!java.util.Objects.equals(before,enabled.getClass().getMethod("get").invoke(enabled)))throw new AssertionError("Radar must preserve inherited dimension options");
   for(Object group:List.of(global,dim))for(String name:List.of("playerRadarEnabled","playerRadarNamesEnabled")){Object field=group.getClass().getField(name).get(group);if(!Boolean.FALSE.equals(field.getClass().getMethod("get").invoke(field)))throw new AssertionError("Player radar must be disabled");}
   Object ops=global.getClass().getField("worldPlayerRadar").get(global);if(!"NONE".equals(ops.getClass().getMethod("get").invoke(ops).toString()))throw new AssertionError("Operators must not bypass radar");
   server.getPlayerList().op(op.getGameProfile());
   try{
    Class<?> permissions=Class.forName("journeymap.common.util.PermissionsManager");Object permissionManager=permissions.getMethod("getInstance").invoke(null);var build=permissions.getDeclaredMethod("buildPermissions",ServerPlayer.class);build.setAccessible(true);
    for(ServerPlayer viewer:List.of(op,ordinary)){Object payload=build.invoke(permissionManager,viewer);for(String name:List.of("playerRadarEnabled","playerRadarNamesEnabled")){Object option=payload.getClass().getField(name).get(payload);if(!Boolean.FALSE.equals(option.getClass().getMethod("get").invoke(option)))throw new AssertionError("Operator and ordinary radar must both be denied");}Object world=payload.getClass().getField("worldPlayerRadar").get(payload);if(!"NONE".equals(world.getClass().getMethod("get").invoke(world).toString()))throw new AssertionError("Expanded radar must be denied to operators");}
   }finally{server.getPlayerList().deop(op.getGameProfile());}
   dev.exodus.map.JourneyMapRadarPolicy.restore(server);
   for(var entry:original.entrySet())if(!java.util.Objects.equals(entry.getValue(),entry.getKey().getClass().getMethod("get").invoke(entry.getKey())))throw new AssertionError("Radar settings must restore exactly");
  }catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
 }
 private static void capture(UUID player,net.minecraft.network.protocol.Packet<?> packet){
  if(packet instanceof net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket custom&&custom.getIdentifier().toString().equals("exodus:match_map")){var buffer=new net.minecraft.network.FriendlyByteBuf(custom.getData().copy());try{if(buffer.readVarInt()!=0)throw new AssertionError("Unexpected map message");MAP_PACKETS.put(player,dev.exodus.network.MatchMapPacket.decode(buffer));}finally{buffer.release();}}
 }
 private static void clearVanillaShield(ServerPlayer player){try{var immunity=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");immunity.setAccessible(true);immunity.setInt(player,0);}catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}}
 private static ServerPlayer mock(ServerLevel level,String name){
  UUID id=UUID.randomUUID();
  var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND){
   @Override public void send(net.minecraft.network.protocol.Packet<?> packet){capture(id,packet);}
   @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener){capture(id,packet);}
  };
  new io.netty.channel.embedded.EmbeddedChannel(connection);
  var player=new ServerPlayer(level.getServer(),level,new com.mojang.authlib.GameProfile(id,name));
  level.getServer().getPlayerList().placeNewPlayer(connection,player);
  clearVanillaShield(player);return player;
 }
}
