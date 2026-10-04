package dev.exodus.event.domain;

import java.util.*;
import java.util.random.RandomGenerator;

public final class EventSchedule {
    private int lastRolledDay;
    private final Map<String,Integer> started = new HashMap<>();
    public boolean eligible(EventDefinition d, int day) {
        Integer previous = started.get(d.id());
        return day >= d.minDay() && (d.maxDay() == 0 || day <= d.maxDay())
                && (previous == null || !d.oneTime() && day - previous >= d.cooldownDays());
    }
    public void started(EventDefinition definition, int day) { started.put(definition.id(), day); }
    public void failedDelivery(String id) { started.remove(id); }
    public boolean rollDay(int day) {
        if (day <= lastRolledDay) return false;
        lastRolledDay = day; return true;
    }
    public Optional<EventDefinition> random(Collection<EventDefinition> definitions, int day, RandomGenerator random) {
        var pool = definitions.stream().filter(d -> d.milestoneDay() == 0 && d.weight() > 0 && eligible(d, day)).toList();
        int total = pool.stream().mapToInt(EventDefinition::weight).sum();
        if (total == 0) return Optional.empty();
        int roll = random.nextInt(total);
        for (var d : pool) { roll -= d.weight(); if (roll < 0) return Optional.of(d); }
        throw new IllegalStateException("Weighted pool exhausted");
    }
    public List<EventDefinition> milestones(Collection<EventDefinition> definitions, int day) {
        return definitions.stream().filter(d -> d.milestoneDay() > 0 && day >= d.milestoneDay()
                && !started.containsKey(d.id()) && eligible(d, day)).sorted(Comparator.comparing(EventDefinition::id)).toList();
    }
    public static int day(long ticks, int dayLength) { return (int)Math.min(Integer.MAX_VALUE, Math.max(0,ticks) / dayLength + 1); }
    public int lastRolledDay() { return lastRolledDay; }
    public Map<String,Integer> history() { return Collections.unmodifiableMap(started); }
    public void restore(int day, Map<String,Integer> history) { lastRolledDay=Math.max(0,day);started.clear();started.putAll(history); }
}
