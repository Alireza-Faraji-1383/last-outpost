package dev.exodus.enemy;

public final class ZombieLootPolicy {
    public record Drops(int emerald, int gunpowder, int quartz) {}
    public static Drops roll(double emerald, double powder, double quartz, double emeraldChance, double powderChance, double quartzChance) {
        return new Drops(emerald < emeraldChance ? 1 : 0, 1 + (powder < powderChance ? 1 : 0), quartz < quartzChance ? 1 : 0);
    }
}
