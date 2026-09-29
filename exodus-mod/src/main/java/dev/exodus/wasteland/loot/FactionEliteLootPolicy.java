package dev.exodus.wasteland.loot;

public final class FactionEliteLootPolicy {
    private FactionEliteLootPolicy() {}

    public static Assignment resolve(LootMarker marker, String placementId, boolean uniqueKeyAlreadyAssigned) {
        if (marker == LootMarker.GENERAL_ELITE && !uniqueKeyAlreadyAssigned) {
            if ("russian_base_1".equals(placementId)) {
                return new Assignment("exodus:chests/faction/russian_key", true);
            }
            if ("american_base_1".equals(placementId)) {
                return new Assignment("exodus:chests/faction/american_key", true);
            }
        }
        return new Assignment(marker.lootTable(), false);
    }

    public record Assignment(String lootTable, boolean claimsUniqueKey) {}
}
