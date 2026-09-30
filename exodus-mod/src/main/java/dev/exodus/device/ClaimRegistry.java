package dev.exodus.device;
import java.util.*;
public final class ClaimRegistry {
 public record Claim(UUID match,UUID player,long start,long duration,int radius){public Claim{Objects.requireNonNull(match);Objects.requireNonNull(player);if(start<0||duration<=0||radius<=0)throw new IllegalArgumentException("Invalid claim");}}
 private final Map<Long,Claim> claims=new HashMap<>();
 public boolean start(long device,Claim claim){return claims.putIfAbsent(device,claim)==null;}
 public void cancel(UUID player){claims.entrySet().removeIf(e->e.getValue().player().equals(player));}
 public Map<Long,Claim> entries(){return claims;}
}
