package dev.exodus.teleporter.domain;

public record CountdownSnapshot(long startTick,int durationSeconds,double radius,int capacity) {
    public CountdownSnapshot {
        if(durationSeconds<=0)throw new IllegalArgumentException("durationSeconds must be positive");
        if(radius<0)throw new IllegalArgumentException("radius must not be negative");
        if(capacity<=0)throw new IllegalArgumentException("capacity must be positive");
    }
    public long deadlineTick(){return startTick+durationSeconds*20L;}
    public boolean expired(long nowTick){return nowTick>=deadlineTick();}
    public double progress(long nowTick){
        double remaining=(deadlineTick()-nowTick)/(durationSeconds*20.0);
        return Math.max(0.0,Math.min(1.0,remaining));
    }
    public long remainingTicks(long nowTick){return Math.max(0,deadlineTick()-nowTick);}
}
