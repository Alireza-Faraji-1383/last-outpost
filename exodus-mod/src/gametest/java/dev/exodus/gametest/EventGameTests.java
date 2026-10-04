package dev.exodus.gametest;

import dev.exodus.*;
import dev.exodus.domain.Association;
import dev.exodus.event.*;
import dev.exodus.event.domain.EventDefinition;
import dev.exodus.session.MatchSessionState;
import dev.exodus.supply.*;
import dev.exodus.supply.blockentity.SupplyCrateBlockEntity;
import dev.exodus.supply.domain.SupplyDefinition;
import dev.exodus.supply.entity.SupplyDropEntity;
import dev.exodus.supply.event.EventAirdropService;
import dev.exodus.teleporter.*;
import dev.exodus.teleporter.domain.TeleporterComponent;
import dev.exodus.teleporter.item.ComponentStacks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("exodus_events")
@PrefixGameTestTemplate(false)
public final class EventGameTests {
    private static final Map<UUID,List<Packet<?>>> PACKETS=new HashMap<>();
    @GameTest(template="smoke",timeoutTicks=100)
    public static void deliveryAndObjectives(GameTestHelper h){
        var level=h.getLevel();var server=level.getServer();var d=ExodusSavedData.get(server);
        var oldCatalog=EventCatalog.all();boolean oldEnabled=ExodusConfig.EVENTS_ENABLED.get();ExodusConfig.EVENTS_ENABLED.set(false);
        ServerPlayer p=mock(level,"hunter"),q=mock(level,"prey"),spectator=mock(level,"spectator");
        d.state=MatchState.RUNNING;d.matchId=UUID.randomUUID();d.dimension=level.dimension().location().toString();d.session=new MatchSessionState();d.session.matchId=d.matchId;d.session.borderSize=2000;
        d.centerX=h.absolutePos(BlockPos.ZERO).getX();d.centerZ=h.absolutePos(BlockPos.ZERO).getZ();
        p.setGameMode(GameType.SURVIVAL);q.setGameMode(GameType.SURVIVAL);spectator.setGameMode(GameType.SPECTATOR);
        d.associations.put(p.getUUID(),Association.MATCH_PLAYER);d.associations.put(q.getUUID(),Association.MATCH_PLAYER);d.associations.put(spectator.getUUID(),Association.INITIAL_SPECTATOR);
        d.session.roster.addAll(List.of(p.getUUID(),q.getUUID()));
        EventCatalog.replace(List.of(
                new EventDefinition("exodus:zombie_hunt","Zombie Hunt",EventDefinition.Scope.GLOBAL,EventDefinition.Objective.KILL_ENTITY,1,0,40,false,1,0,"",false),
                new EventDefinition("exodus:manhunt","Manhunt",EventDefinition.Scope.TARGETED,EventDefinition.Objective.KILL_PLAYER,1,0,15,false,1,0,"",false)));
        try{
            p.teleportTo(level,d.centerX+.5,100,d.centerZ+.5,0,0);q.teleportTo(level,d.centerX+300.5,100,d.centerZ+.5,0,0);
            h.assertTrue(EventManager.start(server,"exodus:zombie_hunt"),"Zombie event starts");
            h.assertTrue(scores(p).contains("Kills: 0 / 50"),"Real packet contains hunt count");
            h.assertTrue(scores(p).contains("Time: 01:30"),"Real packet contains 90 second timer");
            h.assertTrue(scores(spectator).isEmpty(),"Spectator receives no event sidebar");
            h.assertTrue(EventManager.start(server,"exodus:manhunt"),"Private event coexists with shared hunt");
            ServerPlayer hunter=scores(p).stream().anyMatch(s->s.startsWith("Target:"))?p:q,prey=hunter==p?q:p;
            h.assertTrue(scores(hunter).contains("Distance: 300 blocks"),"Hunter receives distance in blocks");
            h.assertTrue(scores(prey).contains("Survive: 05:00"),"Prey receives survival timer");
            h.assertTrue(!scores(prey).stream().anyMatch(s->s.startsWith("Target:")||s.startsWith("Distance:")),"Prey receives no hunter information");
            h.assertTrue(!EventManager.start(server,"exodus:manhunt"),"Players cannot enter two targeted events");
            // Final-phase elimination happened before this LOWEST-priority callback: the death still wins.
            d.session.eliminated.add(prey.getUUID());d.associations.put(prey.getUUID(),Association.AUTO_SPECTATOR);
            MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(prey,level.damageSources().generic()));
            h.assertTrue(emeralds(hunter)==16,"Any-source prey death pays hunter 16 emeralds");
            h.assertTrue(chat(hunter).stream().anyMatch(s->s.contains("reward received: 16 emeralds")),"Successful payment is confirmed in chat");
            MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(prey,level.damageSources().generic()));
            h.assertTrue(emeralds(hunter)==16,"Repeated death does not pay again");
            d.session.eliminated.remove(prey.getUUID());d.associations.put(prey.getUUID(),Association.MATCH_PLAYER);
            h.assertTrue(EventManager.start(server,"exodus:manhunt"),"Admin can test another personal event");
            ServerPlayer surviving=scores(p).contains("Survive: 05:00")?p:q;
            d.session.elapsedTicks=6000;EventManager.tick(server);
            h.assertTrue(emeralds(surviving)==(surviving==hunter?24:8),"Survival timeout pays exactly 8 emeralds");
            h.assertTrue(EventManager.start(server,"exodus:zombie_hunt"),"Admin can test hunt after timeout");
            Zombie cancelledZombie=new Zombie(level);var cancelled=new LivingDeathEvent(cancelledZombie,level.damageSources().playerAttack(hunter));cancelled.setCanceled(true);MinecraftForge.EVENT_BUS.post(cancelled);
            for(int n=0;n<50;n++)MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(new Zombie(level),level.damageSources().playerAttack(hunter)));
            h.assertTrue(emeralds(hunter)==(hunter==surviving?34:26),"50 attributed zombie kills pay contributor 10 emeralds");
            h.assertTrue(emeralds(prey)==(prey==surviving?8:0),"Noncontributor gets no hunt reward");
            UUID transaction=UUID.randomUUID();for(int n=0;n<hunter.getInventory().getContainerSize();n++)hunter.getInventory().setItem(n,new ItemStack(Items.STONE,64));
            h.assertTrue(EventRewardService.pay(hunter,transaction,10,"Overflow"),"Full inventory still pays");
            h.assertTrue(!EventRewardService.pay(hunter,transaction,10,"Overflow"),"Transaction cannot be paid twice");
            boolean overflow=false;for(var entity:level.getAllEntities())if(entity instanceof ItemEntity item&&item.getItem().is(Items.EMERALD)&&item.getItem().getCount()==10)overflow=true;
            h.assertTrue(overflow,"Overflow emeralds are collectible world items");
            EventManager.stop(server);
            // Exercise actual falling tick, crate payload, NBT persistence and cleanup.
            BlockPos pos=h.absolutePos(new BlockPos(2,2,2));level.setBlock(pos.below(),Blocks.STONE.defaultBlockState(),3);level.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
            UUID dropId=UUID.randomUUID();var drop=new SupplyDropEntity(ExodusSupplyRegistry.SUPPLY_DROP.get(),level);
            drop.configure(dropId,d.matchId,null,pos,new SupplyDefinition("exodus:dimensional_core","Core", "exodus:supply_crate","exodus:events/core",Set.of(),0,0,null,0xFFFFFF,true,0),null);drop.configureEvent(Long.MAX_VALUE,true);
            d.teleporter.rareClaims().add(TeleporterComponent.DIMENSIONAL_CORE);
            d.session.events.drops.put(dropId,new EventSavedState.Drop(dropId,drop.getUUID(),pos.asLong(),Long.MAX_VALUE,"Core",true,false));
            drop.setPos(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5);level.addFreshEntity(drop);
            h.assertTrue(drop.getTeamColor()==0x55AAFF&&drop.isCurrentlyGlowing(),"Event entity is blue and glowing");
            var saved=drop.saveWithoutId(new net.minecraft.nbt.CompoundTag());var reloaded=new SupplyDropEntity(ExodusSupplyRegistry.SUPPLY_DROP.get(),level);reloaded.load(saved);
            h.assertTrue(reloaded.eventDrop()&&reloaded.getTeamColor()==0x55AAFF,"Event color survives entity serialization");
            drop.tick();h.assertTrue(drop.isRemoved(),"Falling entity ends at landing");
            h.assertTrue(level.getBlockEntity(pos) instanceof SupplyCrateBlockEntity,"Actual tick places existing supply crate");
            var crate=(SupplyCrateBlockEntity)level.getBlockEntity(pos);int coreCount=0;
            for(int n=0;n<crate.getContainerSize();n++){var stack=crate.getItem(n);if(stack.is(ExodusTeleporterRegistry.item(TeleporterComponent.DIMENSIONAL_CORE).get())){coreCount+=stack.getCount();h.assertTrue(ComponentStacks.isCurrent(stack,d.matchId),"Core is bound to this match");}}
            h.assertTrue(coreCount==1,"Crate contains exactly one core");
            var roundTrip=EventSavedState.load(d.session.events.save());h.assertTrue(roundTrip.drops.get(dropId).landed(),"Landing persists");
            h.assertTrue(roundTrip.outcomes.values().stream().anyMatch(result->result.outcome()==dev.exodus.event.domain.ObjectiveProgress.Outcome.HUNTER_WON),"Terminal objective results persist with reward journal");
            h.assertTrue(EventAirdropService.markers(server).stream().anyMatch(marker->marker.id().equals("airdrop:"+dropId)),"Landing retains map marker");
            EventManager.cleanup(server);
            h.assertTrue(level.getBlockEntity(pos)==null&&d.session.events.drops.isEmpty(),"Cleanup removes owned crate and marker");
            for(var entity:level.getAllEntities())if(entity instanceof ItemEntity item&&item.getItem().is(ExodusTeleporterRegistry.item(TeleporterComponent.DIMENSIONAL_CORE).get()))h.fail("Cleanup must not spill a unique core into the world");
            var radio=new SupplyDropEntity(ExodusSupplyRegistry.SUPPLY_DROP.get(),level);h.assertTrue(!radio.eventDrop()&&radio.getTeamColor()!=0x55AAFF,"Radio appearance remains unchanged");
            d.teleporter.rareClaims().remove(TeleporterComponent.DIMENSIONAL_CORE);d.session.elapsedTicks=96000;
            var coreDef=new EventDefinition("exodus:dimensional_core","Dimensional Core Airdrop",EventDefinition.Scope.GLOBAL,EventDefinition.Objective.WORLD_DROP,5,0,0,true,0,5,"exodus:events/core",true);
            EventAirdropService.queue(server,coreDef);
            for(int attempt=0;attempt<ExodusConfig.EVENT_PLACEMENT_ATTEMPTS.get()&&d.session.events.drops.isEmpty();attempt++)EventAirdropService.tick(server);
            h.assertTrue(d.session.events.drops.size()==1,"Queued placement creates an actual event delivery");
            h.assertTrue(d.teleporter.rareClaims().contains(TeleporterComponent.DIMENSIONAL_CORE),"Successful delivery reserves unique core");
            h.assertTrue(!d.session.events.schedule.eligible(coreDef,6),"Successful milestone cannot repeat");
            EventAirdropService.queue(server,coreDef);EventAirdropService.tick(server);
            h.assertTrue(d.session.events.drops.size()==1,"A second request cannot duplicate the core");
            h.succeed();
        }finally{
            EventManager.cleanup(server);d.state=MatchState.IDLE;d.matchId=null;d.associations.clear();d.session=new MatchSessionState();d.teleporter.clearCurrent(null);EventCatalog.replace(oldCatalog);ExodusConfig.EVENTS_ENABLED.set(oldEnabled);
            for(var player:List.of(p,q,spectator)){server.getPlayerList().remove(player);player.discard();}PACKETS.clear();
        }
    }
    private static int emeralds(ServerPlayer p){return p.getInventory().items.stream().filter(s->s.is(Items.EMERALD)).mapToInt(ItemStack::getCount).sum();}
    private static List<String> scores(ServerPlayer p){
        Map<String,Integer> values=new HashMap<>();for(var packet:PACKETS.getOrDefault(p.getUUID(),List.of())){
            if(packet instanceof ClientboundSetObjectivePacket objective&&objective.getObjectiveName().equals("exodus_event")&&objective.getMethod()==1)values.clear();
            if(packet instanceof ClientboundSetScorePacket score&&score.getObjectiveName().equals("exodus_event")){
                if(score.getMethod()==net.minecraft.server.ServerScoreboard.Method.REMOVE)values.remove(score.getOwner());else values.put(score.getOwner(),score.getScore());
            }
        }return new ArrayList<>(values.keySet());
    }
    private static List<String> chat(ServerPlayer p){return PACKETS.getOrDefault(p.getUUID(),List.of()).stream().filter(packet->packet instanceof ClientboundSystemChatPacket).map(packet->((ClientboundSystemChatPacket)packet).content().getString()).toList();}
    private static ServerPlayer mock(ServerLevel level,String name){
        UUID id=UUID.randomUUID();var packets=new ArrayList<Packet<?>>();PACKETS.put(id,packets);
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND){
            @Override public void send(Packet<?> packet){packets.add(packet);}
            @Override public void send(Packet<?> packet,net.minecraft.network.PacketSendListener listener){packets.add(packet);}
        };new io.netty.channel.embedded.EmbeddedChannel(connection);
        var player=new ServerPlayer(level.getServer(),level,new com.mojang.authlib.GameProfile(id,name));level.getServer().getPlayerList().placeNewPlayer(connection,player);return player;
    }
}
