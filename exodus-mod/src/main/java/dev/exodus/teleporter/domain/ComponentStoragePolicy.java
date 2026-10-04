package dev.exodus.teleporter.domain;

public final class ComponentStoragePolicy {
    private ComponentStoragePolicy() {}
    public static boolean shouldEject(boolean playerInventory, boolean teleporter) {
        return shouldEject(playerInventory, teleporter, false);
    }
    public static boolean shouldEject(boolean playerInventory, boolean teleporter, boolean craftingResult) {
        return !playerInventory && !teleporter && !craftingResult;
    }
}
