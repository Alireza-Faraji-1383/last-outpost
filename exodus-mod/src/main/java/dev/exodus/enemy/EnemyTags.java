package dev.exodus.enemy;

import net.minecraft.world.entity.Mob;
import java.util.UUID;

public final class EnemyTags {
    public static final String MATCH = "ExodusEnemyMatch";
    public static final String OWNER = "ExodusEnemyOwner";
    public static final String KIND = "ExodusEnemyKind";
    public static final String ADMIN = "ExodusAdminTestMob";
    public static boolean managed(Mob mob) { return mob.getPersistentData().hasUUID(MATCH); }
    public static UUID match(Mob mob) { return mob.getPersistentData().getUUID(MATCH); }
    public static UUID owner(Mob mob) { return mob.getPersistentData().getUUID(OWNER); }
    public static EnemyKind kind(Mob mob) {
        try { return EnemyKind.valueOf(mob.getPersistentData().getString(KIND)); }
        catch (IllegalArgumentException e) { return null; }
    }
    public static boolean valid(Mob mob) {
        return managed(mob) && mob.getPersistentData().hasUUID(OWNER) && kind(mob) != null;
    }
    public static void bind(Mob mob, UUID match, UUID owner, EnemyKind kind) {
        var tag = mob.getPersistentData();
        tag.putUUID(MATCH, match); tag.putUUID(OWNER, owner); tag.putString(KIND, kind.name());
        mob.setPersistenceRequired();
    }
}
