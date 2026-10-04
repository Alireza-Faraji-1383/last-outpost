package dev.exodus.map;
public final class TeammateProjectionPolicy {
 private TeammateProjectionPolicy(){}
 public static boolean visible(boolean recipientActive,boolean teammateActive,boolean teammateAlive,boolean sameDimension){return recipientActive&&teammateActive&&teammateAlive&&sameDimension;}
}
