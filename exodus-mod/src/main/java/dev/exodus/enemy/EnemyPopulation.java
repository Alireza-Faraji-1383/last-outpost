package dev.exodus.enemy;

import java.util.*;

/** Each entity belongs to exactly one player's allocation, regardless of proximity. */
public final class EnemyPopulation {
    private record Member(UUID owner, EnemyKind kind) {}
    private final Map<UUID, Member> members = new HashMap<>();
    public void add(UUID entity, UUID owner, EnemyKind kind) { members.put(entity, new Member(owner, kind)); }
    public void remove(UUID entity) { members.remove(entity); }
    public void clear() { members.clear(); }
    public int size() { return members.size(); }
    public int count(UUID owner) { return (int) members.values().stream().filter(m -> m.owner.equals(owner)).count(); }
    public int zombies(UUID owner) { return (int) members.values().stream().filter(m -> m.owner.equals(owner) && m.kind == EnemyKind.ZOMBIE).count(); }
    public int remaining(UUID owner, int localLimit, int globalLimit) {
        return Math.max(0, Math.min(localLimit - count(owner), globalLimit - size()));
    }
    public boolean canAdd(UUID owner, int amount, int localLimit, int globalLimit) {
        return amount > 0 && remaining(owner, localLimit, globalLimit) >= amount;
    }
}
