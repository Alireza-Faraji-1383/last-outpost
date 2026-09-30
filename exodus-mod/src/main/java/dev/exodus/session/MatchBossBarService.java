package dev.exodus.session;
import dev.exodus.*;import dev.exodus.device.*;import dev.exodus.teleporter.item.*;import dev.exodus.teleporter.domain.CountdownSnapshot;
import net.minecraft.server.*;import net.minecraft.server.level.*;import net.minecraft.world.BossEvent;import net.minecraft.network.chat.Component;import net.minecraft.core.BlockPos;import java.util.*;
public final class MatchBossBarService {
 private static final Map<MinecraftServer,Map<UUID,ServerBossEvent>> BARS=new WeakHashMap<>();private MatchBossBarService(){}
 public static void tick(MinecraftServer server){
  var d=ExodusSavedData.get(server);if(d.state!=MatchState.RUNNING){cleanup(server);return;}
  Map<UUID,ServerBossEvent> bars=BARS.computeIfAbsent(server,k->new HashMap<>());
  for(UUID id:new HashSet<>(bars.keySet()))if(!d.associations.containsKey(id)||server.getPlayerList().getPlayer(id)==null){bars.remove(id).removeAllPlayers();}
  for(ServerPlayer p:MatchManager.associatedOnlinePlayers(server)){
   ServerBossEvent bar=bars.computeIfAbsent(p.getUUID(),k->new ServerBossEvent(Component.literal("Exodus"),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.PROGRESS));bar.addPlayer(p);
   long day=MatchClockPolicy.day(d.session.elapsedTicks,ExodusConfig.DAY_TICKS.get());var active=d.teleporter.active();
   if(active!=null){var level=server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,new net.minecraft.resources.ResourceLocation(active.dimension())));if(level==null)continue;var timer=new CountdownSnapshot(active.startTick(),active.durationSeconds(),active.radiusMilliblocks()/1000.0,active.capacity());long remaining=timer.remainingTicks(level.getGameTime());BlockPos pos=BlockPos.of(active.blockPos());bar.setProgress((float)timer.progress(level.getGameTime()));bar.setName(Component.literal(String.format("Day %d — Teleporter: %02d:%02d — X: %d Y: %d Z: %d",day,remaining/1200,(remaining/20)%60,pos.getX(),pos.getY(),pos.getZ())));}
   else{boolean playing=MatchManager.isActiveMatchPlayer(p);int mask=0;if(playing)for(int i=0;i<p.getInventory().getContainerSize();i++){var stack=p.getInventory().getItem(i);if(ComponentStacks.isCurrent(stack,d.matchId)&&stack.getItem() instanceof TeleporterComponentItem item)mask|=1<<item.component().ordinal();}
    int count=playing?ComponentProgressPolicy.bestCount(mask,DeviceIndex.ownedMasks(d.session,p.getUUID())):0;bar.setProgress(count/9f);bar.setName(Component.literal("Day "+day+(playing?" — Components: "+count+"/9":"")));}
  }
 }
 public static void cleanup(MinecraftServer server){var bars=BARS.remove(server);if(bars!=null)bars.values().forEach(ServerBossEvent::removeAllPlayers);}
}
