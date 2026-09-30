package dev.exodus.map;
public final class DiscoveryPolicy {private DiscoveryPolicy(){} public static boolean discovered(double dx,double dy,double dz,boolean eligible,int horizontal,int vertical){return eligible&&dx*dx+dz*dz<=horizontal*(double)horizontal&&Math.abs(dy)<=vertical;}}
