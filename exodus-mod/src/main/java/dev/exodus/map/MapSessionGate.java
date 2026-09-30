package dev.exodus.map;
public final class MapSessionGate {
 private long epoch=-1,revision=-1;
 public boolean accept(long incomingEpoch,long incomingRevision){if(incomingEpoch<0||incomingRevision<0||incomingEpoch<epoch||incomingEpoch==epoch&&incomingRevision<=revision)return false;epoch=incomingEpoch;revision=incomingRevision;return true;}
 public void reset(){epoch=-1;revision=-1;}
}
