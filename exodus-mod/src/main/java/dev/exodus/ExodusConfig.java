package dev.exodus;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.List;

public final class ExodusConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue ENEMY_AMMO_MIN, ENEMY_AMMO_MAX, ENEMY_EMERALD_MIN, ENEMY_EMERALD_MAX;
    public static final ForgeConfigSpec.DoubleValue ENEMY_EMERALD_CHANCE;
    public static final ForgeConfigSpec.IntValue BASIC_RADIO_COOLDOWN_SECONDS, SPECIAL_RADIO_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.IntValue MATCH_RADIUS, BORDER_SIZE, BASE_MIN_DISTANCE,
            BORDER_SAFE_DISTANCE, MAX_LOCATION_ATTEMPTS, MAX_ALLOCATION_ROUNDS, MAX_RANDOM_CENTER_ATTEMPTS,
            RANDOM_CENTER_SEARCH_RADIUS, MAX_PLAYERS, MAX_HEIGHT_VARIATION, CHECKS_PER_TICK,
            STARTING_TIMEOUT_SECONDS, DISCONNECT_GRACE_SECONDS, SPAWN_SEARCH_RADIUS,
            BASE_PLAYER_OFFSET_X, BASE_PLAYER_OFFSET_Y, BASE_PLAYER_OFFSET_Z, BASE_STRUCTURE_DEPTH,
            PREGENERATION_BUFFER, ARENA_SAFETY_GAP, CITY_COVERAGE_MINIMUM_PERCENT,
            CITY_COVERAGE_SAMPLE_STRIDE_CHUNKS, PREPARATION_CHUNKS_PER_TICK,
            ABANDONED_CAMPS_MIN, ABANDONED_CAMPS_MAX, OCCUPIED_CAMPS_MIN, OCCUPIED_CAMPS_MAX,
            CAMP_MIN_DISTANCE, CAMP_PLAYER_BASE_DISTANCE, POI_TERRAIN_MARGIN,
            RUSSIAN_BASE_Y_OFFSET, AMERICAN_BASE_Y_OFFSET,
            SURFACE_QUORUM_PERCENT, SURFACE_HEIGHT_TOLERANCE,
            PREFERRED_SURFACE_MIN_Y, PREFERRED_SURFACE_MAX_Y,
            MAX_FOUNDATION_DEPTH, MAX_SURFACE_CLEAR_DEPTH, POI_CHUNKS_PER_TICK,
            RADIO_LINK_RANGE, DROP_SPAWN_HEIGHT, DROP_SPEED_MILLIBLOCKS,
            LANDING_CLEARANCE, LANDING_RETRY_SECONDS, EMPTY_CRATE_SECONDS,
            TELEPORTER_COUNTDOWN_SECONDS, TELEPORTER_VICTORY_RADIUS_MILLIBLOCKS, TELEPORTER_CAPACITY;
    public static final ForgeConfigSpec.BooleanValue DEV_FALLBACK, NATURAL_HOSTILE_SPAWNS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> STRUCTURES;

    static {
        var b = new ForgeConfigSpec.Builder();
        b.push("enemyLoot");
        ENEMY_AMMO_MIN=b.defineInRange("ammoMinimum",16,1,64);
        ENEMY_AMMO_MAX=b.defineInRange("ammoMaximum",32,1,64);
        ENEMY_EMERALD_MIN=b.defineInRange("emeraldMinimum",1,1,64);
        ENEMY_EMERALD_MAX=b.defineInRange("emeraldMaximum",3,1,64);
        ENEMY_EMERALD_CHANCE=b.defineInRange("emeraldChance",.10,0.0,1.0);
        b.pop();
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
        BASE_STRUCTURE_DEPTH = b.defineInRange("baseStructureDepth", 6, 0, 128);
        DEV_FALLBACK = b.define("enableDevFallback", false);
        STRUCTURES = b.defineList("starterStructures", List.of("exodus:starter_base", "exodus:starter_base_2"), o -> o instanceof String);
        b.pop();
        b.push("wasteland");
        PREGENERATION_BUFFER = b.defineInRange("pregenerationBuffer", 128, 0, 4096);
        ARENA_SAFETY_GAP = b.defineInRange("arenaSafetyGap", 1024, 0, 30000);
        CITY_COVERAGE_MINIMUM_PERCENT = b.defineInRange("cityCoverageMinimumPercent", 15, 0, 100);
        CITY_COVERAGE_SAMPLE_STRIDE_CHUNKS = b.defineInRange("cityCoverageSampleStrideChunks", 4, 1, 32);
        PREPARATION_CHUNKS_PER_TICK = b.comment("Set to 0 to skip arena pregeneration and generate chunks during play instead.")
                .defineInRange("preparationChunksPerTick", 2, 0, 64);
        ABANDONED_CAMPS_MIN = b.defineInRange("abandonedCampsMin", 6, 0, 100);
        ABANDONED_CAMPS_MAX = b.defineInRange("abandonedCampsMax", 10, 0, 100);
        OCCUPIED_CAMPS_MIN = b.defineInRange("occupiedCampsMin", 3, 0, 100);
        OCCUPIED_CAMPS_MAX = b.defineInRange("occupiedCampsMax", 5, 0, 100);
        CAMP_MIN_DISTANCE = b.defineInRange("campMinDistance", 120, 0, 10000);
        CAMP_PLAYER_BASE_DISTANCE = b.defineInRange("campPlayerBaseDistance", 150, 0, 10000);
        POI_TERRAIN_MARGIN = b.defineInRange("poiTerrainMargin", 2, 0, 32);
        RUSSIAN_BASE_Y_OFFSET = b.defineInRange("russianBaseYOffset", 0, -128, 128);
        AMERICAN_BASE_Y_OFFSET = b.defineInRange("americanBaseYOffset", 0, -128, 128);
        SURFACE_QUORUM_PERCENT = b.defineInRange("surfaceQuorumPercent", 50, 1, 100);
        SURFACE_HEIGHT_TOLERANCE = b.defineInRange("surfaceHeightTolerance", 2, 0, 32);
        PREFERRED_SURFACE_MIN_Y = b.defineInRange("preferredSurfaceMinY", 55, -64, 319);
        PREFERRED_SURFACE_MAX_Y = b.defineInRange("preferredSurfaceMaxY", 70, -64, 319);
        MAX_FOUNDATION_DEPTH = b.defineInRange("maxFoundationDepth", 96, 1, 384);
        MAX_SURFACE_CLEAR_DEPTH = b.comment("Maximum top-down clearing depth when a POI surface fails its quorum.")
                .defineInRange("maxSurfaceClearDepth", 32, 0, 384);
        POI_CHUNKS_PER_TICK = b.defineInRange("poiChunksPerTick", 2, 1, 64);
        NATURAL_HOSTILE_SPAWNS = b.define("naturalHostileSpawns", true);
        b.pop();
        b.push("supplyDrops");
        BASIC_RADIO_COOLDOWN_SECONDS=b.defineInRange("basicRadioCooldownSeconds",30,0,86400);
        SPECIAL_RADIO_COOLDOWN_SECONDS=b.defineInRange("specialRadioCooldownSeconds",90,0,86400);
        RADIO_LINK_RANGE = b.defineInRange("radioLinkRange", 128, 1, 4096);
        DROP_SPAWN_HEIGHT = b.defineInRange("dropSpawnHeight", 80, 8, 512);
        DROP_SPEED_MILLIBLOCKS = b.defineInRange("dropSpeedMilliblocksPerTick", 125, 10, 1000);
        LANDING_CLEARANCE = b.defineInRange("landingClearance", 3, 0, 16);
        LANDING_RETRY_SECONDS = b.defineInRange("landingRetrySeconds", 10, 1, 60);
        EMPTY_CRATE_SECONDS = b.defineInRange("emptyCrateRemovalSeconds", 30, 1, 600);
        b.pop();
        b.push("teleporter");
        TELEPORTER_COUNTDOWN_SECONDS=b.defineInRange("teleporterCountdownSeconds",1200,1,86400);
        TELEPORTER_VICTORY_RADIUS_MILLIBLOCKS=b.defineInRange("teleporterVictoryRadiusMilliblocks",5000,100,64000);
        TELEPORTER_CAPACITY=b.defineInRange("teleporterCapacity",2,1,8);
        b.pop();
        SPEC = b.build();
    }
    private ExodusConfig() {}
}
