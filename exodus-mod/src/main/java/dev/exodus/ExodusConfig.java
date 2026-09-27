package dev.exodus;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.List;

public final class ExodusConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue MATCH_RADIUS, BORDER_SIZE, BASE_MIN_DISTANCE,
            BORDER_SAFE_DISTANCE, MAX_LOCATION_ATTEMPTS, MAX_ALLOCATION_ROUNDS, MAX_RANDOM_CENTER_ATTEMPTS,
            RANDOM_CENTER_SEARCH_RADIUS, MAX_PLAYERS, MAX_HEIGHT_VARIATION, CHECKS_PER_TICK,
            STARTING_TIMEOUT_SECONDS, DISCONNECT_GRACE_SECONDS, SPAWN_SEARCH_RADIUS,
            BASE_PLAYER_OFFSET_X, BASE_PLAYER_OFFSET_Y, BASE_PLAYER_OFFSET_Z,
            RADIO_LINK_RANGE, DROP_SPAWN_HEIGHT, DROP_SPEED_MILLIBLOCKS,
            LANDING_CLEARANCE, LANDING_RETRY_SECONDS, EMPTY_CRATE_SECONDS;
    public static final ForgeConfigSpec.BooleanValue DEV_FALLBACK;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> STRUCTURES;

    static {
        var b = new ForgeConfigSpec.Builder();
        b.push("foundation");
        MATCH_RADIUS = b.defineInRange("matchRadius", 1000, 128, 30000);
        BORDER_SIZE = b.defineInRange("borderSize", 2000, 256, 60000);
        BASE_MIN_DISTANCE = b.defineInRange("baseMinDistance", 250, 32, 10000);
        BORDER_SAFE_DISTANCE = b.defineInRange("borderSafeDistance", 100, 0, 10000);
        MAX_LOCATION_ATTEMPTS = b.defineInRange("maxLocationAttempts", 100, 1, 10000);
        MAX_ALLOCATION_ROUNDS = b.defineInRange("maxAllocationRounds", 5, 1, 100);
        MAX_RANDOM_CENTER_ATTEMPTS = b.defineInRange("maxRandomCenterAttempts", 10, 1, 100);
        RANDOM_CENTER_SEARCH_RADIUS = b.defineInRange("randomCenterSearchRadius", 10000, 100, 1000000);
        MAX_PLAYERS = b.defineInRange("maxPlayers", 8, 1, 100);
        MAX_HEIGHT_VARIATION = b.defineInRange("maxBaseHeightVariation", 6, 0, 64);
        CHECKS_PER_TICK = b.defineInRange("locationChecksPerTick", 2, 1, 100);
        STARTING_TIMEOUT_SECONDS = b.defineInRange("startingTimeoutSeconds", 120, 10, 3600);
        DISCONNECT_GRACE_SECONDS = b.defineInRange("disconnectGraceSeconds", 120, 1, 3600);
        SPAWN_SEARCH_RADIUS = b.defineInRange("spawnSearchRadius", 5, 0, 32);
        BASE_PLAYER_OFFSET_X = b.defineInRange("basePlayerOffsetX", 0, -128, 128);
        BASE_PLAYER_OFFSET_Y = b.defineInRange("basePlayerOffsetY", 2, -64, 128);
        BASE_PLAYER_OFFSET_Z = b.defineInRange("basePlayerOffsetZ", 0, -128, 128);
        DEV_FALLBACK = b.define("enableDevFallback", false);
        STRUCTURES = b.defineList("starterStructures", List.of("exodus:starter_base"), o -> o instanceof String);
        b.pop();
        b.push("supplyDrops");
        RADIO_LINK_RANGE = b.defineInRange("radioLinkRange", 128, 1, 4096);
        DROP_SPAWN_HEIGHT = b.defineInRange("dropSpawnHeight", 80, 8, 512);
        DROP_SPEED_MILLIBLOCKS = b.defineInRange("dropSpeedMilliblocksPerTick", 125, 10, 1000);
        LANDING_CLEARANCE = b.defineInRange("landingClearance", 3, 0, 16);
        LANDING_RETRY_SECONDS = b.defineInRange("landingRetrySeconds", 10, 1, 60);
        EMPTY_CRATE_SECONDS = b.defineInRange("emptyCrateRemovalSeconds", 30, 1, 600);
        b.pop();
        SPEC = b.build();
    }
    private ExodusConfig() {}
}
