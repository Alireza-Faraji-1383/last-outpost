package dev.exodus.event.domain;

import java.util.*;

/** One-way objective state; handlers may only claim a winner's reward once. */
public final class ObjectiveProgress {
    public enum Outcome { ACTIVE, COMPLETED, FAILED, HUNTER_WON, PREY_WON, CANCELLED }
    private final Set<UUID> participants, contributors = new HashSet<>(), victims = new HashSet<>(), paid = new HashSet<>();
    private final UUID hunter, prey;
    private final long deadline;
    private final int goal;
    private Outcome outcome = Outcome.ACTIVE;
    private ObjectiveProgress(Set<UUID> participants, UUID hunter, UUID prey, long start, long duration, int goal) {
        if(participants.isEmpty() || duration <= 0 || goal < 0) throw new IllegalArgumentException("Invalid objective");
        this.participants=Set.copyOf(participants);this.hunter=hunter;this.prey=prey;deadline=start+duration;this.goal=goal;
    }
    public static ObjectiveProgress hunt(Set<UUID> participants, long start, long duration, int goal) {
        if(goal<1)throw new IllegalArgumentException("Positive kill goal required");
        return new ObjectiveProgress(participants,null,null,start,duration,goal);
    }
    public static ObjectiveProgress manhunt(UUID hunter, UUID prey, long start, long duration) {
        if(hunter.equals(prey))throw new IllegalArgumentException("Distinct players required");
        return new ObjectiveProgress(Set.of(hunter,prey),hunter,prey,start,duration,0);
    }
    public boolean kill(UUID player, UUID victim, long now) {
        if(outcome!=Outcome.ACTIVE || hunter!=null || now>=deadline || !participants.contains(player) || !victims.add(victim))return false;
        contributors.add(player);if(victims.size()>=goal)outcome=Outcome.COMPLETED;return true;
    }
    public void death(UUID player,long now) {
        if(outcome!=Outcome.ACTIVE || hunter==null || now>deadline)return;
        if(prey.equals(player))outcome=Outcome.HUNTER_WON;else if(hunter.equals(player))outcome=Outcome.CANCELLED;
    }
    public void advance(long now) {
        if(outcome==Outcome.ACTIVE && now>=deadline)outcome=hunter==null?Outcome.FAILED:Outcome.PREY_WON;
    }
    public void cancel() { if(outcome==Outcome.ACTIVE)outcome=Outcome.CANCELLED; }
    public Set<UUID> winners() { return switch(outcome) {
        case COMPLETED -> Set.copyOf(contributors);case HUNTER_WON -> Set.of(hunter);case PREY_WON -> Set.of(prey);default -> Set.of();
    }; }
    public boolean claimReward(UUID player) { return winners().contains(player) && paid.add(player); }
    public int kills(){return victims.size();} public int goal(){return goal;} public long remaining(long now){return Math.max(0,deadline-now);}
    public Outcome outcome(){return outcome;}public Set<UUID> participants(){return participants;}public UUID hunter(){return hunter;}public UUID prey(){return prey;}
}
