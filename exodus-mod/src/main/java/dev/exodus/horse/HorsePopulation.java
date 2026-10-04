package dev.exodus.horse;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** A chunk unload is deliberately not a removal from the population. */
public final class HorsePopulation {
    private final Set<UUID> ids=new HashSet<>();
    public void add(UUID id) { ids.add(id); }
    public void remove(UUID id) { ids.remove(id); }
    public int remaining(int cap) { return Math.max(0,cap-ids.size()); }
    public void clear() { ids.clear(); }
}
