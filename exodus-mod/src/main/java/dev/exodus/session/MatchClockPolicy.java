package dev.exodus.session;
public final class MatchClockPolicy {
 private MatchClockPolicy(){}
 public static long day(long ticks,int length){if(ticks<0||length<=0)throw new IllegalArgumentException("Invalid clock");return 1+ticks/length;}
}
