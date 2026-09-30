package dev.exodus.player;
public final class DeviceRespawnPolicy {private DeviceRespawnPolicy(){} public static boolean fallback(boolean primary,double squaredDistance,boolean otherActive,int radius){return !primary&&otherActive&&squaredDistance<=radius*(double)radius;}}
