package dev.exodus.player;
import dev.exodus.*;import dev.exodus.domain.Association;import dev.exodus.device.DeviceOwnershipService;import dev.exodus.session.FinalPhasePolicy;
import net.minecraft.server.level.*;import net.minecraft.network.chat.Component;import net.minecraft.world.entity.player.Player;import net.minecraftforge.event.entity.living.*;import net.minecraftforge.event.entity.player.*;import net.minecraftforge.event.level.BlockEvent;import net.minecraftforge.eventbus.api.SubscribeEvent;import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=ExodusMod.MOD_ID)
public final class MatchPlayerEvents {
 private MatchPlayerEvents(){}
 private static boolean associated(ServerPlayer p){var d=ExodusSavedData.get(p.server);return d.state==MatchState.RUNNING&&d.associations.containsKey(p.getUUID());}
 private static boolean protectedNow(ServerPlayer p){var d=ExodusSavedData.get(p.server);return MatchManager.isActiveMatchPlayer(p)&&DamageProtectionPolicy.blocks(d.session.elapsedTicks,d.session.protectedUntil.getOrDefault(p.getUUID(),0L));}
 private static boolean blockDamage(net.minecraft.world.entity.Entity victim,net.minecraft.world.damagesource.DamageSource source){return victim instanceof ServerPlayer p&&protectedNow(p)||victim instanceof Player&&source.getEntity() instanceof ServerPlayer attacker&&protectedNow(attacker);}
 @SubscribeEvent public static void attack(LivingAttackEvent e){if(blockDamage(e.getEntity(),e.getSource()))e.setCanceled(true);}
 @SubscribeEvent public static void hurt(LivingHurtEvent e){if(blockDamage(e.getEntity(),e.getSource()))e.setCanceled(true);}
 @SubscribeEvent public static void death(LivingDeathEvent e){
  if(!(e.getEntity() instanceof ServerPlayer p)||!MatchManager.isActiveMatchPlayer(p))return;
  var d=ExodusSavedData.get(p.server);DeviceOwnershipService.cancelFor(p.server,p.getUUID());
  if(d.teleporter.active()!=null){d.session.eliminated.add(p.getUUID());d.pendingPlayers.remove(p.getUUID());d.associations.put(p.getUUID(),Association.AUTO_SPECTATOR);p.sendSystemMessage(Component.literal("You have been eliminated from this match."));d.setDirty();}

  else {RespawnService.selectMatchSpawn(p);if(p.getRespawnPosition()!=null)d.session.pendingRespawns.put(p.getUUID(),p.getRespawnPosition().asLong());d.setDirty();}
 }
 @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){
  if(!(e.getEntity() instanceof ServerPlayer p))return;var d=ExodusSavedData.get(p.server);if(d.state!=MatchState.RUNNING)return;
  if(d.associations.containsKey(p.getUUID()))p.connection.player=p;
  if(d.teleporter.active()!=null&&d.associations.get(p.getUUID())==Association.MATCH_PLAYER){d.session.eliminated.add(p.getUUID());d.associations.put(p.getUUID(),Association.AUTO_SPECTATOR);d.pendingPlayers.remove(p.getUUID());d.session.pendingRespawns.remove(p.getUUID());d.session.protectedUntil.remove(p.getUUID());DeviceOwnershipService.cancelFor(p.server,p.getUUID());p.sendSystemMessage(Component.literal("Playable respawn is disabled in the final phase. You have been eliminated."));d.setDirty();}
  if(d.session.eliminated.contains(p.getUUID())){p.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);return;}
  if(d.associations.get(p.getUUID())==Association.MATCH_PLAYER&&!d.pendingPlayers.containsKey(p.getUUID())){RespawnService.ensureArrival(p);d.session.protectedUntil.put(p.getUUID(),d.session.elapsedTicks+ExodusConfig.PROTECTION_SECONDS.get()*20L);d.setDirty();}
 }
 @SubscribeEvent public static void sleep(PlayerSleepInBedEvent e){if(e.getEntity() instanceof ServerPlayer p&&associated(p))e.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);}
 @SubscribeEvent public static void spawn(PlayerSetSpawnEvent e){if(e.getEntity() instanceof ServerPlayer p&&associated(p)&&!RespawnService.internalChange())e.setCanceled(true);}
 @SubscribeEvent public static void placement(BlockEvent.EntityPlaceEvent e){
  if(!(e.getLevel() instanceof ServerLevel level))return;var d=ExodusSavedData.get(level.getServer());if(d.state!=MatchState.RUNNING||!level.dimension().location().toString().equals(d.dimension))return;
  for(var device:d.session.devices.values()){var pos=net.minecraft.core.BlockPos.of(device.position());if(e.getPos().equals(pos.above())||e.getPos().equals(pos.above(2))){e.setCanceled(true);break;}}
 }
}
