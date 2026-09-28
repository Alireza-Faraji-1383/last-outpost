package dev.exodus.wasteland.loot;

import java.util.Arrays;
import java.util.Optional;

public enum LootMarker {
    GENERAL_COMMON("general/common"), GENERAL_STANDARD("general/standard"),
    GENERAL_VALUABLE("general/valuable"), GENERAL_ELITE("general/elite"),
    FOOD("food"), WEAPONS("weapons"), MEDICAL("medical"), UTILITY("utility"), TECH("tech");

    private final String path;

    LootMarker(String path) { this.path = path; }
    public String metadata() { return "exodus:loot/" + path; }
    public String lootTable() { return "exodus:chests/" + path; }

    public static Optional<LootMarker> parse(String value) {
        if (value == null || !value.startsWith("exodus:loot/")) return Optional.empty();
        return Arrays.stream(values()).filter(marker -> marker.metadata().equals(value)).findFirst();
    }

    public static LootMarker requireKnown(String value) {
        return parse(value).orElseThrow(() -> new IllegalArgumentException("Unknown Exodus loot marker: " + value));
    }
}
