package dev.exodus.enemy;

public final class EnemyAdmission {
    public static boolean allowed(boolean managed, boolean currentMatch, boolean administrator) {
        return managed ? currentMatch : administrator;
    }
}
