package dev.exodus.gametest;

import dev.exodus.*;
import dev.exodus.domain.Association;
import dev.exodus.event.*;
import dev.exodus.event.domain.*;
import dev.exodus.party.*;
import dev.exodus.session.MatchSessionState;
import dev.exodus.teleporter.ExodusTeleporterRegistry;
import dev.exodus.teleporter.domain.TeleporterComponent;
import dev.exodus.teleporter.item.ComponentStacks;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("exodus_party")
@PrefixGameTestTemplate(false)
public final class PartyGameTests {
 private static final Map<UUID,dev.exodus.map.MatchMapSnapshot> MAP_PACKETS=new HashMap<>();
 @GameTest(templateNamespace="exodus_events",template="smoke",timeoutTicks=100)
 public static void partyCombatProgressAndManhunt(GameTestHelper h){
  var level=h.getLevel();var server=level.getServer();var d=ExodusSavedData.get(server);var oldCatalog=EventCatalog.all();boolean oldEnabled=ExodusConfig.EVENTS_ENABLED.get();boolean oldPvp=server.isPvpAllowed();server.setPvpAllowed(true);
  var a=player(level,"PartyAlpha");var b=player(level,"PartyBeta");var stranger=player(level,"PartyOther");
  try{
   ExodusConfig.EVENTS_ENABLED.set(false);d.state=MatchState.RUNNING;d.matchId=UUID.randomUUID();d.dimension=level.dimension().location().toString();d.session=new MatchSessionState();d.session.matchId=d.matchId;d.session.borderSize=2000;
   for(var p:List.of(a,b,stranger)){p.setGameMode(GameType.SURVIVAL);p.setHealth(20);d.associations.put(p.getUUID(),Association.MATCH_PLAYER);d.names.put(p.getUUID(),p.getGameProfile().getName());d.session.roster.add(p.getUUID());}
   var def=new EventDefinition("exodus:party_manhunt","Manhunt",EventDefinition.Scope.TARGETED,EventDefinition.Objective.KILL_PLAYER,1,0,1,false,0,0,"",false);
   EventCatalog.replace(List.of(def));d.associations.remove(stranger.getUUID());
   h.assertTrue(EventManager.start(server,def.id()),"Two solo players can enter Manhunt");
   PartyService.create(a);PartyService.invite(a,b);PartyService.accept(b,a.getUUID());
   h.assertTrue(d.session.events.outcomes.values().stream().anyMatch(o->o.outcome()==ObjectiveProgress.Outcome.FAILED),"Alliance immediately fails active Manhunt");
   h.assertTrue(a.getInventory().countItem(Items.EMERALD)==0&&b.getInventory().countItem(Items.EMERALD)==0,"Neither participant receives alliance reward");
   h.assertTrue(!EventManager.start(server,def.id()),"Allied players cannot be selected as opponents");
   var gunPre=new GunPre(b,a);PartyGunIntegration.handle(gunPre);h.assertTrue(gunPre.isCanceled(),"Gun pre-hit must cancel before ignition and knockback");
   var enemyGun=new GunPre(b,stranger);PartyGunIntegration.handle(enemyGun);h.assertTrue(!enemyGun.isCanceled(),"Other players retain gun combat");
   d.associations.put(stranger.getUUID(),Association.MATCH_PLAYER);
   dev.exodus.map.MatchMapService.updateNow(server);
   h.assertTrue(MAP_PACKETS.get(a.getUUID()).teammates().equals(Set.of(b.getUUID()))&&MAP_PACKETS.get(b.getUUID()).teammates().equals(Set.of(a.getUUID())),"Each recipient receives only its own teammate identity");
   h.assertTrue(MAP_PACKETS.get(stranger.getUUID()).teammates().isEmpty()&&MAP_PACKETS.get(stranger.getUUID()).locations().stream().noneMatch(l->l.kind()==dev.exodus.map.MapLocation.Kind.TEAMMATE),"Private party positions never reach strangers");
   var dispatcher=server.getCommands().getDispatcher();var source=a.createCommandSourceStack().withPermission(0);
   h.assertTrue(dispatcher.execute("exodus party status",source)==1,"Normal player can use party commands");
   boolean adminBlocked=false;try{dispatcher.execute("exodus stop",source);}catch(com.mojang.brigadier.exceptions.CommandSyntaxException expected){adminBlocked=true;}h.assertTrue(adminBlocked&&d.state==MatchState.RUNNING,"Operator-only commands remain restricted");
   float health=b.getHealth();b.hurt(level.damageSources().playerAttack(a),5);h.assertTrue(b.getHealth()==health,"Party melee damage is blocked");
   var arrow=new Arrow(level,a);b.invulnerableTime=0;b.hurt(level.damageSources().arrow(arrow,a),5);h.assertTrue(b.getHealth()==health,"Owned projectile damage is blocked");
   b.invulnerableTime=0;b.hurt(level.damageSources().explosion(a,a),5);h.assertTrue(b.getHealth()==health,"Attributed explosion is blocked");
   b.invulnerableTime=0;b.hurt(level.damageSources().generic(),2);h.assertTrue(b.getHealth()<health,"Environmental damage remains active");
   var partA=new ItemStack(ExodusTeleporterRegistry.item(TeleporterComponent.PHASE_COIL).get());ComponentStacks.bind(partA,d.matchId);
   a.getInventory().setItem(0,partA);b.getInventory().setItem(0,partA.copy());
   h.assertTrue(PartyProgressService.count(a)==1&&PartyProgressService.count(b)==1,"Duplicate shared component counts once");
   var partB=new ItemStack(ExodusTeleporterRegistry.item(TeleporterComponent.POWER_REGULATOR).get());ComponentStacks.bind(partB,d.matchId);b.containerMenu.setCarried(partB);
   h.assertTrue(PartyProgressService.count(a)==2&&PartyProgressService.count(b)==2,"Both receive pooled inventory and cursor progress");
   b.containerMenu.setCarried(ItemStack.EMPTY);b.getInventory().setItem(0,ItemStack.EMPTY);a.getInventory().setItem(0,ItemStack.EMPTY);h.assertTrue(PartyProgressService.count(a)==0,"Losing all components reduces progress");
   PartyService.leave(b,false);h.assertTrue(PartyService.sameParty(server,a.getUUID(),b.getUUID()),"Separation retains protection");
   d.session.elapsedTicks=599;PartyService.tick(server);h.assertTrue(PartyService.sameParty(server,a.getUUID(),b.getUUID()),"Membership remains through tick 599");
   d.session.elapsedTicks=600;PartyService.tick(server);h.assertTrue(!PartyService.sameParty(server,a.getUUID(),b.getUUID()),"Membership ends at tick 600");
   h.assertTrue(MAP_PACKETS.get(a.getUUID()).teammates().isEmpty()&&MAP_PACKETS.get(a.getUUID()).locations().stream().noneMatch(l->l.kind()==dev.exodus.map.MapLocation.Kind.TEAMMATE),"Departure immediately clears teammate marker and tag snapshot");
   b.invulnerableTime=0;health=b.getHealth();b.hurt(level.damageSources().playerAttack(a),2);h.assertTrue(b.getHealth()<health,"Former teammate damage is enabled after deadline");
   PartyService.create(a);PartyService.invite(a,b);PartyService.accept(b,a.getUUID());d.pendingPlayers.put(b.getUUID(),1200L);PartyService.tick(server);h.assertTrue(PartyService.sameParty(server,a.getUUID(),b.getUUID()),"Disconnect grace retains membership");
   d.associations.put(b.getUUID(),Association.AUTO_SPECTATOR);PartyService.tick(server);h.assertTrue(!PartyService.sameParty(server,a.getUUID(),b.getUUID()),"Final departure removes membership");
   PartyService.create(a);PartyService.invite(a,stranger);d.matchId=UUID.randomUUID();h.assertTrue(PartyService.state(server).members().isEmpty(),"New match clears membership");
   PartyService.clear(server);h.succeed();
  }catch(com.mojang.brigadier.exceptions.CommandSyntaxException e){h.fail("Command registration failed: "+e.getMessage());}
  finally{PartyService.clear(server);dev.exodus.session.MatchSessionService.cleanup(server);d.state=MatchState.IDLE;d.matchId=null;d.associations.clear();d.pendingPlayers.clear();d.session=new MatchSessionState();EventCatalog.replace(oldCatalog);ExodusConfig.EVENTS_ENABLED.set(oldEnabled);server.setPvpAllowed(oldPvp);for(var p:List.of(a,b,stranger)){server.getPlayerList().remove(p);p.discard();}MAP_PACKETS.clear();}
 }
 private static ServerPlayer player(ServerLevel level,String name){
  UUID id=UUID.randomUUID();var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND){@Override public void send(Packet<?> packet){capture(id,packet);}@Override public void send(Packet<?> packet,net.minecraft.network.PacketSendListener listener){capture(id,packet);}};
  new io.netty.channel.embedded.EmbeddedChannel(connection);var player=new ServerPlayer(level.getServer(),level,new com.mojang.authlib.GameProfile(id,name));level.getServer().getPlayerList().placeNewPlayer(connection,player);
  try{var immunity=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");immunity.setAccessible(true);immunity.setInt(player,0);}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}return player;
 }
 private static void capture(UUID id,Packet<?> packet){if(packet instanceof net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket custom&&custom.getIdentifier().toString().equals("exodus:match_map")){var buffer=new net.minecraft.network.FriendlyByteBuf(custom.getData().copy());try{if(buffer.readVarInt()!=0)throw new AssertionError("Unexpected message");MAP_PACKETS.put(id,dev.exodus.network.MatchMapPacket.decode(buffer));}finally{buffer.release();}}}
 @net.minecraftforge.eventbus.api.Cancelable public static final class GunPre extends net.minecraftforge.eventbus.api.Event {
  private final ServerPlayer victim,attacker;
  public GunPre(ServerPlayer victim,ServerPlayer attacker){this.victim=victim;this.attacker=attacker;}
  public net.minecraft.world.entity.Entity getHurtEntity(){return victim;}
  public net.minecraft.world.entity.LivingEntity getAttacker(){return attacker;}
 }
}
