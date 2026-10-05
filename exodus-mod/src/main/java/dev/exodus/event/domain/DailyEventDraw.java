package dev.exodus.event.domain;

import java.util.*;
import java.util.function.*;
import java.util.random.RandomGenerator;

/** Ordered independent rolls. Delivery adapters decide whether an assignment actually succeeds. */
public final class DailyEventDraw {
    public record Assignment(EventDefinition definition,List<UUID> players) {
        public Assignment { players=List.copyOf(players); }
    }
    private DailyEventDraw() {}
    private static List<EventDefinition> ordered(Collection<EventDefinition> definitions,int day,boolean drops) {
        return definitions.stream().filter(d->(d.objective()==EventDefinition.Objective.WORLD_DROP)==drops)
                .filter(d->d.milestoneDay()==0&&!d.oneTime()&&day>=d.minDay()&&(d.maxDay()==0||day<=d.maxDay()))
                .sorted(Comparator.comparingInt(EventDefinition::priority).thenComparing(EventDefinition::id)).toList();
    }
    public static void missions(Collection<EventDefinition> definitions,int day,List<UUID> online,Set<UUID> busy,
                                BiPredicate<UUID,UUID> sameParty,EventChanceState state,RandomGenerator random,
                                Predicate<Assignment> grant) {
        Set<UUID> assigned=new HashSet<>(busy);
        for(var def:ordered(definitions,day,false)) {
            List<UUID> winners=new ArrayList<>();Set<UUID> delivered=new HashSet<>();
            for(UUID player:online)if(!assigned.contains(player)&&random.nextInt(100)<state.chance(def,player))winners.add(player);
            if(def.objective()==EventDefinition.Objective.KILL_PLAYER) {
                for(var pair:ManhuntPartyPolicy.disjointPairs(winners,sameParty,random)) {
                    var ids=List.of(pair.hunter(),pair.prey());
                    if(grant.test(new Assignment(def,ids)))delivered.addAll(ids);
                }
            } else if(def.participantSelector()==EventDefinition.ParticipantSelector.SINGLE_ACTIVE) {
                for(UUID player:winners)if(grant.test(new Assignment(def,List.of(player))))delivered.add(player);
            } else if(!winners.isEmpty()&&grant.test(new Assignment(def,winners)))delivered.addAll(winners);
            assigned.addAll(delivered);
            // Online eligible players skipped by an earlier assignment still missed this definition.
            for(UUID player:online)if(delivered.contains(player))state.granted(def,player);else state.missed(def,player);
        }
    }
    public static void airdrops(Collection<EventDefinition> definitions,int day,int cap,EventChanceState state,
                                RandomGenerator random,Predicate<EventDefinition> queue) {
        int queued=0;
        for(var def:ordered(definitions,day,true)) {
            boolean won=queued<cap&&random.nextInt(100)<state.chance(def,null);
            // Reset only after a real entity is spawned, not when placement has merely been queued.
            state.missed(def,null);
            if(won&&queue.test(def))queued++;
        }
    }
    public static boolean validCap(Object entry) {
        if(!(entry instanceof String text)||!text.matches("[1-9][0-9]*:[0-9]+"))return false;
        try {String[] parts=text.split(":");return Integer.parseInt(parts[0])>0&&Integer.parseInt(parts[1])<=64;}
        catch(NumberFormatException e){return false;}
    }
    public static int dropCap(int day,List<? extends String> thresholds) {
        int result=0,latest=0;
        for(String text:thresholds)if(validCap(text)) {
            String[] parts=text.split(":");int start=Integer.parseInt(parts[0]);
            if(start<=day&&start>=latest){latest=start;result=Integer.parseInt(parts[1]);}
        }
        return result;
    }
}
