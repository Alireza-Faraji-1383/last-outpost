package dev.exodus.event.domain;

/** World-clock windows with persisted claims. Backward time changes cannot replay a day. */
public final class EventDayClock {
    public record Snapshot(int day,int morningDay,int noonDay) {}
    private int day=1,morningDay=1,noonDay=1,observedDay=1;
    private long phase;
    public void observe(long worldTime,int dayLength) {
        observedDay=EventSchedule.day(worldTime,dayLength);
        day=Math.max(day,observedDay);phase=Math.floorMod(worldTime,dayLength);
    }
    public int day() { return day; }
    public boolean morning(int noonTick) {
        if(day<=1||observedDay!=day||morningDay>=day||phase>=noonTick)return false;
        morningDay=day;return true;
    }
    public boolean noon(int noonTick,int nightTick) {
        if(day<=1||observedDay!=day||noonDay>=day||phase<noonTick||phase>=nightTick)return false;
        noonDay=day;return true;
    }
    public Snapshot snapshot() { return new Snapshot(day,morningDay,noonDay); }
    public void restore(Snapshot saved) {
        day=Math.max(1,saved.day());morningDay=Math.max(1,saved.morningDay());noonDay=Math.max(1,saved.noonDay());
    }
}
