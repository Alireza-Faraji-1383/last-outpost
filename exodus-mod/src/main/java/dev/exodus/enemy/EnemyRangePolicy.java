package dev.exodus.enemy;

public final class EnemyRangePolicy {
    public static boolean allowed(double distanceSquared, boolean pursuing, double acquire, double release) {
        double radius = pursuing ? release : acquire;
        return distanceSquared <= radius * radius;
    }
    public static boolean farForCleanup(double nearestSquared, double radius, boolean fighting) {
        return !fighting && nearestSquared > radius * radius;
    }
}
