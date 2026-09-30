package dev.exodus.wasteland.loot;

/** Pure bounds and chance rules shared by the SEM drop adapter and its tests. */
public final class EnemyDropPolicy {
    private EnemyDropPolicy() {}
    public static int amount(int first,int second,int offset){
        int minimum=Math.min(first,second),maximum=Math.max(first,second);
        return minimum+Math.floorMod(offset,maximum-minimum+1);
    }
    public static boolean roll(double sample,double chance){return sample<chance;}
}
