package dev.exodus.enemy;

import java.util.*;

/** Opportunities are consumed before placement. Owner state survives disconnects within a match. */
public final class SoldierSchedule {
    private static final class Day {
        long number;
        int consumed;
        boolean russianFirst;
        long lastObserved;
        Day(long number, boolean russianFirst) { this.number = number; this.russianFirst = russianFirst; }
    }
    private final Map<UUID, Day> days = new HashMap<>();
    private final int halfDay, fullDay;
    public SoldierSchedule(int halfDay, int fullDay) { this.halfDay = halfDay; this.fullDay = fullDay; }
    public EnemyKind due(UUID owner, long time, int offset) {
        long number = Math.floorDiv(time, fullDay);
        int phase = (int) Math.floorMod(time, fullDay);
        int boundedOffset = Math.max(0, Math.min(halfDay - 1, offset));
        Day day = days.get(owner);
        boolean continuous=day!=null && time-day.lastObserved==1;
        if (day == null || day.number != number) {
            day = new Day(number, ((owner.hashCode() ^ number) & 1) == 0);
            // A newly seen player does not receive opportunities that have already passed.
            if (phase > boundedOffset) day.consumed |= 1;
            if (phase > halfDay + boundedOffset) day.consumed |= 2;
            days.put(owner, day);
        }
        day.lastObserved=time;
        if (phase >= halfDay * 2) { day.consumed = 3; return null; }
        int slot = phase < halfDay ? 0 : 1;
        int bit = 1 << slot;
        if ((day.consumed & bit) != 0 || phase < slot * halfDay + boundedOffset) return null;
        day.consumed |= bit;
        // Called once per world tick for active owners. A missed tick/time jump cannot backfill.
        if (phase>slot*halfDay+boundedOffset && !continuous) return null;
        return (slot == 0) == day.russianFirst ? EnemyKind.RUSSIAN : EnemyKind.AMERICAN;
    }
    public void clear() { days.clear(); }
}
