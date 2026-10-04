package dev.exodus.event.domain;
import java.util.*;
import java.util.function.BiPredicate;
public final class ManhuntPartyPolicy {
 private ManhuntPartyPolicy(){}
 public record Pair(UUID hunter,UUID prey){}
 public static List<Pair> pairs(List<UUID> players,BiPredicate<UUID,UUID> sameParty){List<Pair> pairs=new ArrayList<>();for(UUID first:players)for(UUID second:players)if(!first.equals(second)&&!sameParty.test(first,second))pairs.add(new Pair(first,second));return List.copyOf(pairs);}
}
