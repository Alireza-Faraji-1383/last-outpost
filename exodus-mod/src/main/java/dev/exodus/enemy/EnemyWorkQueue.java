package dev.exodus.enemy;

import java.util.*;

/** Deduplicated FIFO: refreshing a request never moves it ahead of waiting enemies. */
public final class EnemyWorkQueue<K,V> {
    private final LinkedHashMap<K,V> pending=new LinkedHashMap<>();
    public void request(K key,V value) { pending.put(key,value); }
    public void cancel(K key) { pending.remove(key); }
    public void clear() { pending.clear(); }
    public V poll() {
        var iterator=pending.entrySet().iterator();
        if (!iterator.hasNext()) return null;
        var entry=iterator.next();V value=entry.getValue();iterator.remove();return value;
    }
}
