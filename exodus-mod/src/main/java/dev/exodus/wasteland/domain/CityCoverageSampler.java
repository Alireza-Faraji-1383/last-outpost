package dev.exodus.wasteland.domain;
public final class CityCoverageSampler{
 public enum Result{CITY,OUTSIDE,UNAVAILABLE}
 private final int minimumPercent,budget;private int samples,cities;private String skipReason="";
 public CityCoverageSampler(int minimumPercent,int budget){if(minimumPercent<0||minimumPercent>100||budget<=0)throw new IllegalArgumentException();this.minimumPercent=minimumPercent;this.budget=budget;}
 public void record(Result result){if(!canSample())throw new IllegalStateException("Sampling budget exhausted");if(result==Result.UNAVAILABLE){skipReason="Lost Cities API unavailable";return;}samples++;if(result==Result.CITY)cities++;}
 public boolean canSample(){return !skipped()&&samples<budget;}public boolean skipped(){return !skipReason.isEmpty();}public String skipReason(){return skipReason;}
 public boolean accepted(){return skipped()||(samples>0&&(long)cities*100>=((long)minimumPercent*samples));}
 public int samples(){return samples;}public int cities(){return cities;}
}
