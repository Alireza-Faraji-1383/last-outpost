package dev.exodus.event.domain;

import java.util.*;

/** Separate pity counters for each definition and player; null player is a global drop. */
public final class EventChanceState {
    public record Key(String definition, UUID player) {}
    private final Map<Key,Integer> misses=new HashMap<>();
    public int chance(EventDefinition definition,UUID player) {
        return Math.min(100,definition.chancePercent()+misses.getOrDefault(new Key(definition.id(),player),0)*definition.chanceIncreasePercent());
    }
    public void missed(EventDefinition definition,UUID player) {
        misses.compute(new Key(definition.id(),player),(key,count)->Math.min(100,count==null?1:count+1));
    }
    public void granted(EventDefinition definition,UUID player) { misses.remove(new Key(definition.id(),player)); }
    public Map<Key,Integer> snapshot() { return Map.copyOf(misses); }
    public void restore(Map<Key,Integer> saved) {
        misses.clear();saved.forEach((key,count)->{if(count>0)misses.put(key,Math.min(100,count));});
    }
}
