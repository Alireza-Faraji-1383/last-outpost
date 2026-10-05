package dev.exodus.event;

import dev.exodus.event.domain.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class DailyEventDrawTest {
    private final UUID a=new UUID(0,1), b=new UUID(0,2), c=new UUID(0,3), d=new UUID(0,4);
    private EventDefinition definition(String id, EventDefinition.Objective objective, int day, int priority, int chance, int increase) {
        var json=com.google.gson.JsonParser.parseString("""
          {"title":"Test", "scope":"TARGETED", "objective":"KILL_ENTITY", "minDay":2,
           "participantSelector":"SINGLE_ACTIVE", "chancePercent":30,"chanceIncreasePercent":30,"priority":2}
          """).getAsJsonObject();
        json.addProperty("objective",objective.name());json.addProperty("minDay",day);json.addProperty("priority",priority);
        json.addProperty("chancePercent",chance);json.addProperty("chanceIncreasePercent",increase);
        if(objective==EventDefinition.Objective.KILL_PLAYER)json.addProperty("participantSelector","RANDOM_PAIR");
        if(objective==EventDefinition.Objective.WORLD_DROP){json.addProperty("scope","GLOBAL");json.addProperty("participantSelector","ALL_ACTIVE");json.addProperty("lootTable","exodus:events/medical");}
        return EventDefinitionParser.parse("exodus:"+id,json);
    }
    @Test void independentChancesIncreaseByConfiguredAmountAndOnlyOwnGrantResets() {
        var state=new EventChanceState();var hunt=definition("hunt",EventDefinition.Objective.KILL_ENTITY,2,2,50,10);
        state.missed(hunt,a);state.missed(hunt,a);assertEquals(70,state.chance(hunt,a));assertEquals(50,state.chance(hunt,b));
        var man=definition("man",EventDefinition.Objective.KILL_PLAYER,3,1,30,30);state.missed(man,a);
        state.granted(man,a);assertEquals(30,state.chance(man,a));assertEquals(70,state.chance(hunt,a));
        for(int i=0;i<20;i++)state.missed(hunt,a);assertEquals(100,state.chance(hunt,a));
        var copy=new EventChanceState();copy.restore(state.snapshot());assertEquals(100,copy.chance(hunt,a));
    }
    @Test void manhuntRunsFirstAndOddWinnerFallsThroughToPersonalHunt() {
        var hunt=definition("hunt",EventDefinition.Objective.KILL_ENTITY,2,2,100,30);
        var man=definition("man",EventDefinition.Objective.KILL_PLAYER,3,1,100,30);
        var state=new EventChanceState();List<DailyEventDraw.Assignment> granted=new ArrayList<>();
        DailyEventDraw.missions(List.of(hunt,man),3,List.of(a,b,c),Set.of(),(x,y)->false,state,new Random(4),assignment->{granted.add(assignment);return true;});
        assertEquals(2,granted.size());assertEquals(man,granted.get(0).definition());assertEquals(2,granted.get(0).players().size());
        assertEquals(hunt,granted.get(1).definition());assertEquals(1,granted.get(1).players().size());
        UUID leftover=granted.get(1).players().get(0);assertEquals(100,state.chance(man,leftover));
        assertEquals(1,state.snapshot().get(new EventChanceState.Key(man.id(),leftover)));
        for(UUID paired:granted.get(0).players())assertEquals(1,state.snapshot().get(new EventChanceState.Key(hunt.id(),paired)));
    }
    @Test void offlineAndNotYetUnlockedPlayersNeverAccumulateChance() {
        var hunt=definition("hunt",EventDefinition.Objective.KILL_ENTITY,2,2,0,30);
        var man=definition("man",EventDefinition.Objective.KILL_PLAYER,3,1,0,30);var state=new EventChanceState();
        DailyEventDraw.missions(List.of(hunt,man),2,List.of(a),Set.of(),(x,y)->false,state,new Random(1),assignment->true);
        assertEquals(30,state.chance(hunt,a));assertEquals(0,state.chance(hunt,b));assertEquals(0,state.chance(man,a));
    }
    @Test void allWinningPlayersCanFormSeveralDisjointPairsWithoutAllies() {
        var man=definition("man",EventDefinition.Objective.KILL_PLAYER,3,1,100,30);List<DailyEventDraw.Assignment> results=new ArrayList<>();
        DailyEventDraw.missions(List.of(man),3,List.of(a,b,c,d),Set.of(),(x,y)->Set.of(a,b).contains(x)&&Set.of(a,b).contains(y),new EventChanceState(),new Random(2),assignment->{results.add(assignment);return true;});
        assertEquals(2,results.size());assertTrue(results.stream().noneMatch(pair->Set.copyOf(pair.players()).equals(Set.of(a,b))));
        assertEquals(4,results.stream().flatMap(pair->pair.players().stream()).distinct().count());
    }
    @Test void failedAssignmentAndExistingMissionNeverResetOrDuplicatePlayer() {
        var hunt=definition("hunt",EventDefinition.Objective.KILL_ENTITY,2,2,100,30);var state=new EventChanceState();List<UUID> attempted=new ArrayList<>();
        DailyEventDraw.missions(List.of(hunt),2,List.of(a,b),Set.of(a),(x,y)->false,state,new Random(1),assignment->{attempted.addAll(assignment.players());return false;});
        assertEquals(List.of(b),attempted);assertEquals(1,state.snapshot().get(new EventChanceState.Key(hunt.id(),b)));
    }
    @Test void airdropsUsePriorityCapAndIndependentFailureIncreasesWithoutForcedFilling() {
        var medical=definition("medical",EventDefinition.Objective.WORLD_DROP,2,8,100,30);
        var rpg=definition("rpg",EventDefinition.Objective.WORLD_DROP,8,1,100,20);var state=new EventChanceState();List<String> queued=new ArrayList<>();
        DailyEventDraw.airdrops(List.of(medical,rpg),8,1,state,new Random(1),drop->{queued.add(drop.id());return true;});
        assertEquals(List.of(rpg.id()),queued);assertEquals(1,state.snapshot().get(new EventChanceState.Key(medical.id(),null)));
        assertEquals(1,state.snapshot().get(new EventChanceState.Key(rpg.id(),null))); // queue is not delivery
        state.granted(rpg,null);assertFalse(state.snapshot().containsKey(new EventChanceState.Key(rpg.id(),null)));
        var impossible=definition("none",EventDefinition.Objective.WORLD_DROP,2,1,0,20);
        queued.clear();DailyEventDraw.airdrops(List.of(impossible),4,2,state,new Random(1),drop->{queued.add(drop.id());return true;});assertTrue(queued.isEmpty());
    }
    @Test void capsGrowOnApprovedDays() {
        assertEquals(0,DailyEventDraw.dropCap(1,List.of("2:1","4:2","6:3","8:4")));
        for(int day=2;day<=9;day++)assertEquals(Math.min(4,day/2),DailyEventDraw.dropCap(day,List.of("2:1","4:2","6:3","8:4")));
    }
    @Test void sunriseAndNoonAreOncePerWorldDayIncludingSleepAndRestart() {
        var clock=new EventDayClock();clock.observe(0,24000);assertEquals(1,clock.day());assertFalse(clock.morning(6000));
        clock.observe(24000,24000);assertTrue(clock.morning(6000));assertFalse(clock.morning(6000));assertFalse(clock.noon(6000,12000));
        clock.observe(30000,24000);assertTrue(clock.noon(6000,12000));assertFalse(clock.noon(6000,12000));
        var restored=new EventDayClock();restored.restore(clock.snapshot());restored.observe(30001,24000);assertFalse(restored.noon(6000,12000));
        restored.observe(48000,24000);assertTrue(restored.morning(6000));assertEquals(3,restored.day());
        restored.observe(24000,24000);assertFalse(restored.morning(6000));assertFalse(restored.noon(6000,12000));
    }
    @Test void afternoonOrNightDoesNotReplayMissedMorningOrNoon() {
        var clock=new EventDayClock();clock.observe(44000,24000);assertFalse(clock.morning(6000));assertFalse(clock.noon(6000,12000));
    }
}
