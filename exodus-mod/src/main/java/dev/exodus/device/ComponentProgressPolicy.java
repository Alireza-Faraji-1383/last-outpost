package dev.exodus.device;
import java.util.*;
public final class ComponentProgressPolicy {private ComponentProgressPolicy(){} public static int bestCount(int inventory,List<Integer> devices){int best=Integer.bitCount(inventory&511);for(int mask:devices)best=Math.max(best,Integer.bitCount((inventory|mask)&511));return best;}
 public static int partyCount(List<Integer> inventories,List<Integer> devices){int pooled=0;for(int mask:inventories)pooled|=mask;return bestCount(pooled,devices);}
}
