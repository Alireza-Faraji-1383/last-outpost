package dev.exodus.device;
public final class ClaimPolicy {
 private ClaimPolicy(){}
 public static boolean canClaim(boolean protectedDevice,boolean active,boolean eligible){return eligible&&!protectedDevice&&!active;}
 public static boolean complete(long start,long now,long duration,boolean eligible,boolean active){return eligible&&!active&&now-start>=duration;}
}
