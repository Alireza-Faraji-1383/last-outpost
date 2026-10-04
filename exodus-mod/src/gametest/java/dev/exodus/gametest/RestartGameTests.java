package dev.exodus.gametest;

import dev.exodus.*;
import dev.exodus.domain.Association;
import dev.exodus.session.MatchSessionState;
import dev.exodus.party.PartyService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.GameType;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("exodus_restart")
@PrefixGameTestTemplate(false)
public final class RestartGameTests {
 @GameTest(template="smoke",timeoutTicks=80)
 public static void resumeSavedMatch(GameTestHelper h){
  var level=h.getLevel();var server=level.getServer();var d=ExodusSavedData.get(server);
  UUID id=UUID.randomUUID(),second=UUID.randomUUID();BlockPos base=h.absolutePos(new BlockPos(2,1,2));
  var border=level.getWorldBorder();d.oldBorderX=border.getCenterX();d.oldBorderZ=border.getCenterZ();d.oldBorderSize=border.getSize();d.oldDamagePerBlock=border.getDamagePerBlock();d.oldSafeZone=border.getDamageSafeZone();d.oldWarningBlocks=border.getWarningBlocks();d.oldWarningTime=border.getWarningTime();
  d.state=MatchState.RUNNING;d.matchId=UUID.randomUUID();d.dimension=level.dimension().location().toString();d.centerX=base.getX();d.centerZ=base.getZ();d.session=new MatchSessionState();d.session.matchId=d.matchId;d.session.elapsedTicks=24000;d.session.borderSize=2000;
  d.associations.put(id,Association.MATCH_PLAYER);d.associations.put(second,Association.MATCH_PLAYER);d.session.roster.addAll(List.of(id,second));
  d.bases.put(id,new PlayerBaseData(id,"restart",base,base,base.above(),new net.minecraft.resources.ResourceLocation("exodus:starter_base")));
  var party=PartyService.state(server);party.create(id);party.invite(id,second,24000,200);party.accept(second,id,24001);
  UUID match=d.matchId;
  d.teleporter.active(new dev.exodus.teleporter.TeleporterSavedState.Active(match,d.dimension,base.asLong(),level.getGameTime(),60,1000,2));
  UUID drop=UUID.randomUUID();d.session.events.drops.put(drop,new dev.exodus.event.EventSavedState.Drop(drop,UUID.randomUUID(),base.asLong(),level.getGameTime()+100,"Test Drop",false,false));
  var enemy=new net.minecraft.nbt.CompoundTag();enemy.putUUID("id",UUID.randomUUID());enemy.putUUID("owner",id);enemy.putUUID("match",match);enemy.putString("kind","ZOMBIE");enemy.putDouble("x",base.getX());enemy.putDouble("y",base.getY());enemy.putDouble("z",base.getZ());d.enemyRoster.add(enemy);
  MatchManager.suspend(server);
  var restored=ExodusSavedData.load(d.save(new net.minecraft.nbt.CompoundTag()));server.overworld().getDataStorage().set("exodus",restored);
  MatchManager.recover(server);MatchManager.tick(server);
  dev.exodus.enemy.EnemySpawnService.tick(server);
  long dayTime=level.getDayTime();level.setDayTime(dayTime+5000);
  server.getWorldData().overworldData().setGameTime(level.getGameTime()+5000);
  MatchManager.tick(server);dev.exodus.enemy.EnemySpawnService.tick(server);
  h.assertTrue(level.getDayTime()==dayTime,"World day pauses until first active return");
  h.assertTrue(dev.exodus.enemy.EnemySpawnService.snapshot().size()==1,"Waiting beyond cleanup delay must preserve enemies");
  var timer=restored.teleporter.active();h.assertTrue(timer.startTick()+1200-level.getGameTime()==1200,"Teleporter countdown pauses while waiting beyond its original expiry");
  h.assertTrue(restored.session.events.drops.get(drop).expires()-level.getGameTime()==100,"Drop expiration pauses while waiting");
  h.assertTrue(restored.state==MatchState.RUNNING&&match.equals(restored.matchId),"Restart must keep match identity");
  h.assertTrue(restored.session.elapsedTicks==24000,"Waiting for first return must pause session");
  h.assertTrue(PartyService.sameParty(server,id,second),"Party survives recovery");
  var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND){
   @Override public void send(net.minecraft.network.protocol.Packet<?> p){}
   @Override public void send(net.minecraft.network.protocol.Packet<?> p,net.minecraft.network.PacketSendListener listener){}
  };
  new io.netty.channel.embedded.EmbeddedChannel(connection);
  var player=new ServerPlayer(server,level,new com.mojang.authlib.GameProfile(id,"restart"));
  player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND,7));
  server.getPlayerList().placeNewPlayer(connection,player);
  try{
   h.assertTrue(player.gameMode.getGameModeForPlayer()==GameType.SURVIVAL,"Returning player resumes Survival");
   h.assertTrue(player.getInventory().countItem(net.minecraft.world.item.Items.DIAMOND)==7,"Resume preserves inventory");
   h.assertTrue(!restored.pendingPlayers.containsKey(id),"Returning member leaves pending state");
   MatchManager.tick(server);h.assertTrue(restored.session.elapsedTicks==24001,"Clock resumes on first return");
   h.assertTrue(restored.pendingPlayers.get(second)==server.getTickCount()+ExodusConfig.DISCONNECT_GRACE_SECONDS.get()*20L,"Offline teammate receives grace on first return");
   MatchManager.stop(server,"Restart test complete");h.assertTrue(restored.state==MatchState.IDLE,"Explicit stop still ends resumed match");h.succeed();
  }finally{server.getPlayerList().remove(player);player.discard();restored.state=MatchState.IDLE;restored.matchId=null;restored.associations.clear();restored.pendingPlayers.clear();restored.restartPlayers.clear();restored.session=new MatchSessionState();dev.exodus.enemy.EnemySpawnService.reset();MatchManager.recover(server);}
 }
}
