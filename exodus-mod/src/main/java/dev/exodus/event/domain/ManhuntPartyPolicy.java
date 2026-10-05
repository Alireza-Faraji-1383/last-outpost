package dev.exodus.event.domain;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.random.RandomGenerator;
public final class ManhuntPartyPolicy {
 private ManhuntPartyPolicy(){}
 public record Pair(UUID hunter,UUID prey){}
 public static List<Pair> pairs(List<UUID> players,BiPredicate<UUID,UUID> sameParty){List<Pair> pairs=new ArrayList<>();for(UUID first:players)for(UUID second:players)if(!first.equals(second)&&!sameParty.test(first,second))pairs.add(new Pair(first,second));return List.copyOf(pairs);}
 /** Maximum matching for disjoint party groups (a complete multipartite graph). */
 public static List<Pair> disjointPairs(List<UUID> players,BiPredicate<UUID,UUID> sameParty,RandomGenerator random){
  List<UUID> shuffled=new ArrayList<>(new LinkedHashSet<>(players));
  for(int i=shuffled.size()-1;i>0;i--){int j=random.nextInt(i+1);Collections.swap(shuffled,i,j);}
  List<List<UUID>> groups=new ArrayList<>();
  for(UUID player:shuffled){var group=groups.stream().filter(g->sameParty.test(player,g.get(0))).findFirst().orElse(null);if(group==null){group=new ArrayList<>();groups.add(group);}group.add(player);}
  List<Pair> result=new ArrayList<>();
  while(groups.size()>1){
   groups.sort(Comparator.<List<UUID>>comparingInt(List::size).reversed());
   var first=groups.get(0);var second=groups.get(1);UUID a=first.remove(first.size()-1),b=second.remove(second.size()-1);
   result.add(random.nextBoolean()?new Pair(a,b):new Pair(b,a));groups.removeIf(List::isEmpty);
  }
  return List.copyOf(result);
 }
}
