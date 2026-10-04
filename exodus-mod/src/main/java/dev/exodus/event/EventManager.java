package dev.exodus.event;

import com.mojang.logging.LogUtils;
import dev.exodus.*;
import dev.exodus.event.domain.*;
import dev.exodus.party.PartyService;
import dev.exodus.supply.event.EventAirdropService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

public final class EventManager {
    public record Run(UUID id,EventDefinition definition,ObjectiveProgress progress,int huntReward,int hunterReward,int preyReward){}
    private static final class Runtime {
        final UUID match;final Random random;final List<Run> runs=new ArrayList<>();
        Runtime(UUID id){match=id;random=new Random(id.getMostSignificantBits()^id.getLeastSignificantBits());}
    }
    private static final Map<MinecraftServer,Runtime> RUNTIME=new WeakHashMap<>();
    private EventManager(){}
    private static Runtime runtime(MinecraftServer server){
        var d=ExodusSavedData.get(server);var current=RUNTIME.get(server);
        if(current==null||!current.match.equals(d.matchId)){current=new Runtime(d.matchId);RUNTIME.put(server,current);}return current;
    }
    public static List<ServerPlayer> players(MinecraftServer server){return MatchManager.associatedOnlinePlayers(server).stream().filter(p->MatchManager.isActiveMatchPlayer(p)&&p.isAlive()).sorted(Comparator.comparing(p->p.getUUID().toString())).toList();}
    public static int day(MinecraftServer server){return EventSchedule.day(ExodusSavedData.get(server).session.elapsedTicks,ExodusConfig.EVENT_DAY_TICKS.get());}
    public static void tick(MinecraftServer server){
        var d=ExodusSavedData.get(server);if(d.state!=MatchState.RUNNING||d.matchId==null)return;
        var r=runtime(server);long now=d.session.elapsedTicks;
        partyChanged(server);
        for(var run:new ArrayList<>(r.runs)){
            var p=run.progress();
            if(p.hunter()!=null&&p.outcome()==ObjectiveProgress.Outcome.ACTIVE){
                boolean eligible=p.participants().stream().allMatch(id->{var player=server.getPlayerList().getPlayer(id);return player!=null&&player.isAlive()&&MatchManager.isActiveMatchPlayer(player);});
                if(!eligible)p.cancel();
            }
            if(p.hunter()==null&&p.participants().stream().noneMatch(id->{var player=server.getPlayerList().getPlayer(id);return player!=null&&player.isAlive()&&MatchManager.isActiveMatchPlayer(player);}))p.cancel();
            p.advance(now);if(p.outcome()!=ObjectiveProgress.Outcome.ACTIVE)finish(server,r,run);
        }
        EventAirdropService.tick(server);
        if(ExodusConfig.EVENTS_ENABLED.get()){
            var schedule=d.session.events.schedule;int day=day(server);
            if(now% (ExodusConfig.EVENT_RETRY_SECONDS.get()*20L)==1)for(var def:schedule.milestones(EventCatalog.all(),day))start(server,def,false);
            if(schedule.rollDay(day)){d.setDirty();if(r.random.nextDouble()<ExodusConfig.EVENT_DAILY_CHANCE.get()){
                var eligible=EventCatalog.all().stream().filter(def->available(server,def)).toList();schedule.random(eligible,day,r.random).ifPresent(def->start(server,def,false));
            }}
        }
        if(now%20==1)render(server,r);
    }
    private static boolean available(MinecraftServer server,EventDefinition def){
        var r=runtime(server);
        return switch(def.objective()){
            case KILL_ENTITY -> !players(server).isEmpty()&&r.runs.stream().noneMatch(run->run.progress().hunter()==null);
            case KILL_PLAYER -> !legalPairs(server,r).isEmpty();
            case WORLD_DROP -> EventAirdropService.available(server,def);
        };
    }
    private static List<ServerPlayer> availablePairs(MinecraftServer server,Runtime r){
        return players(server).stream().filter(p->r.runs.stream().filter(run->run.progress().hunter()!=null&&run.progress().participants().contains(p.getUUID())).count()<ExodusConfig.EVENT_MAX_TARGETED_PER_PLAYER.get()).toList();
    }
    public static boolean start(MinecraftServer server,String id){return EventCatalog.get(id).map(def->start(server,def,true)).orElse(false);}
    private static List<ManhuntPartyPolicy.Pair> legalPairs(MinecraftServer server,Runtime r){return ManhuntPartyPolicy.pairs(availablePairs(server,r).stream().map(ServerPlayer::getUUID).toList(),(a,b)->PartyService.sameParty(server,a,b));}
    public static void partyChanged(MinecraftServer server){
        var r=RUNTIME.get(server);if(r==null)return;
        for(var run:new ArrayList<>(r.runs)){var p=run.progress();if(p.hunter()!=null&&p.outcome()==ObjectiveProgress.Outcome.ACTIVE&&PartyService.sameParty(server,p.hunter(),p.prey())){p.failAlliance();finish(server,r,run);}}
    }
    private static boolean start(MinecraftServer server,EventDefinition def,boolean admin){
        var d=ExodusSavedData.get(server);if(d.state!=MatchState.RUNNING||d.matchId==null)return false;
        int day=day(server);if(admin?def.oneTime()&&d.session.events.schedule.history().containsKey(def.id()):!d.session.events.schedule.eligible(def,day))return false;
        if(!available(server,def))return false;
        var r=runtime(server);
        if(def.objective()==EventDefinition.Objective.WORLD_DROP)return EventAirdropService.queue(server,def);
        ObjectiveProgress progress;
        if(def.objective()==EventDefinition.Objective.KILL_ENTITY){Set<UUID> ids=new HashSet<>();players(server).forEach(p->ids.add(p.getUUID()));progress=ObjectiveProgress.hunt(ids,d.session.elapsedTicks,value(def.durationSeconds(),ExodusConfig.EVENT_ZOMBIE_SECONDS.get())*20L,value(def.targetCount(),ExodusConfig.EVENT_ZOMBIE_GOAL.get()));}
        else{var pairs=legalPairs(server,r);if(pairs.isEmpty())return false;var pair=pairs.get(r.random.nextInt(pairs.size()));progress=ObjectiveProgress.manhunt(pair.hunter(),pair.prey(),d.session.elapsedTicks,value(def.durationSeconds(),ExodusConfig.EVENT_MANHUNT_SECONDS.get())*20L);}
        var run=new Run(UUID.randomUUID(),def,progress,value(def.rewardEmeralds(),ExodusConfig.EVENT_ZOMBIE_REWARD.get()),value(def.hunterEmeralds(),ExodusConfig.EVENT_HUNTER_REWARD.get()),value(def.preyEmeralds(),ExodusConfig.EVENT_PREY_REWARD.get()));r.runs.add(run);
        d.session.events.schedule.started(def,day);d.setDirty();
        for(UUID id:progress.participants()){var p=server.getPlayerList().getPlayer(id);if(p!=null)p.sendSystemMessage(Component.literal(def.title()+" started. "+(progress.hunter()==null?"Kill "+progress.goal()+" zombies before time runs out.":id.equals(progress.hunter())?"You are the hunter. Your target is "+name(server,progress.prey())+".":"You are the prey. Survive until the timer expires.")));}
        LogUtils.getLogger().info("[Exodus] Event {} started ({})",def.id(),run.id());render(server,r);return true;
    }
    private static int value(int configured,int fallback){return configured<0?fallback:configured;}
    public static void entityDeath(ServerPlayer killer,UUID victim,String entityType){
        var d=ExodusSavedData.get(killer.server);var r=RUNTIME.get(killer.server);if(r==null||!MatchManager.isActiveMatchPlayer(killer))return;
        for(var run:new ArrayList<>(r.runs))if(run.definition().targetEntity().equals(entityType)&&run.progress().kill(killer.getUUID(),victim,d.session.elapsedTicks)&&run.progress().outcome()!=ObjectiveProgress.Outcome.ACTIVE)finish(killer.server,r,run);
    }
    public static void playerDeath(ServerPlayer player){
        var d=ExodusSavedData.get(player.server);var r=RUNTIME.get(player.server);
        if(r==null||d.state!=MatchState.RUNNING||!player.serverLevel().dimension().location().toString().equals(d.dimension))return;
        for(var run:new ArrayList<>(r.runs)){run.progress().death(player.getUUID(),d.session.elapsedTicks);if(run.progress().outcome()!=ObjectiveProgress.Outcome.ACTIVE)finish(player.server,r,run);}
    }
    private static void finish(MinecraftServer server,Runtime r,Run run){
        r.runs.remove(run);var progress=run.progress();
        var data=ExodusSavedData.get(server);data.session.events.outcomes.put(run.id(),new EventSavedState.Terminal(run.id(),run.definition().id(),progress.outcome(),data.session.elapsedTicks));data.setDirty();
        for(UUID winner:progress.winners()){
            var p=server.getPlayerList().getPlayer(winner);if(p==null||!p.isAlive()||!MatchManager.isActiveMatchPlayer(p)||!progress.claimReward(winner))continue;
            int amount=switch(progress.outcome()){case HUNTER_WON->run.hunterReward();case PREY_WON->run.preyReward();default->run.huntReward();};
            EventRewardService.pay(p,run.id(),amount,run.definition().title());
        }
        for(UUID id:progress.participants()){var p=server.getPlayerList().getPlayer(id);if(p!=null)p.sendSystemMessage(Component.literal(run.definition().title()+" ended: "+progress.outcome().name().toLowerCase(Locale.ROOT).replace('_',' ')+"."));}
        LogUtils.getLogger().info("[Exodus] Event {} ended: {}",run.id(),progress.outcome());render(server,r);
    }
    private static String name(MinecraftServer server,UUID id){var p=server.getPlayerList().getPlayer(id);return p==null?"Offline":p.getGameProfile().getName();}
    private static void render(MinecraftServer server,Runtime r){
        long now=ExodusSavedData.get(server).session.elapsedTicks;
        for(var player:server.getPlayerList().getPlayers()){
            if(!MatchManager.isActiveMatchPlayer(player)||!player.isAlive()){EventScoreboardService.clear(player);continue;}
            var run=r.runs.stream().filter(event->event.progress().participants().contains(player.getUUID())).sorted(Comparator.comparingInt(event->event.progress().hunter()!=null?0:1)).findFirst().orElse(null);
            if(run==null){EventScoreboardService.clear(player);continue;}
            var progress=run.progress();long seconds=(progress.remaining(now)+19)/20;String time=String.format(Locale.ROOT,"%02d:%02d",seconds/60,seconds%60);
            List<String> lines=new ArrayList<>();lines.add(run.definition().title().substring(0,Math.min(40,run.definition().title().length())));
            if(progress.hunter()==null){lines.add("Kills: "+progress.kills()+" / "+progress.goal());lines.add("Time: "+time);}
            else if(player.getUUID().equals(progress.hunter())){var prey=server.getPlayerList().getPlayer(progress.prey());lines.add("Target: "+name(server,progress.prey()));lines.add("Distance: "+(prey==null?"Unavailable":Math.round(player.distanceTo(prey))+" blocks"));lines.add("Time: "+time);}
            else lines.add("Survive: "+time);
            EventScoreboardService.show(player,lines);
        }
    }
    public static List<String> status(MinecraftServer server){
        var d=ExodusSavedData.get(server);List<String> lines=new ArrayList<>();lines.add("Events: "+(d.state==MatchState.RUNNING?"day "+day(server):"no active match"));
        var r=RUNTIME.get(server);if(r!=null)for(var run:r.runs)lines.add(run.id()+" "+run.definition().id()+" "+(run.progress().remaining(d.session.elapsedTicks)+19)/20+"s");
        lines.add("Airdrops: "+d.session.events.drops.size()+"; pending: "+EventAirdropService.pending(server));return lines;
    }
    public static void cleanup(MinecraftServer server){RUNTIME.remove(server);EventScoreboardService.cleanup(server);EventAirdropService.cleanup(server);}
    public static void stop(MinecraftServer server){
        var r=RUNTIME.get(server);if(r!=null)for(var run:new ArrayList<>(r.runs)){run.progress().cancel();finish(server,r,run);}
        EventAirdropService.cleanup(server);EventScoreboardService.cleanup(server);
    }
}
