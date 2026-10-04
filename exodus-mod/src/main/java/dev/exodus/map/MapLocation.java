package dev.exodus.map;
import java.util.Objects;
public record MapLocation(String id,String label,Kind kind,int x,int y,int z) {
 public enum Kind{CITY,RUSSIAN_BASE,AMERICAN_BASE,PLAYER_BASE,ABANDONED_CAMP,OCCUPIED_CAMP,DEVICE,ACTIVE_DEVICE,RARE_ITEM,AIRDROP,TEAMMATE}
 public MapLocation {Objects.requireNonNull(id);Objects.requireNonNull(label);Objects.requireNonNull(kind);if(id.length()>160||label.length()>160)throw new IllegalArgumentException("Map text too long");}
}
