package dev.exodus.wasteland.arena;

import dev.exodus.ExodusConfig;
import dev.exodus.ExodusSavedData;
import dev.exodus.wasteland.domain.ArenaGeometry;
import dev.exodus.wasteland.domain.ArenaGrid;
import dev.exodus.wasteland.domain.ProgressMilestones;
import dev.exodus.wasteland.lostcities.LostCitiesIntegration;
import dev.exodus.wasteland.structure.CompositeDefinition;
import dev.exodus.wasteland.structure.CompositeValidator;
import dev.exodus.wasteland.placement.PlacementPlan;
import dev.exodus.wasteland.placement.PlacementPolicy;
import dev.exodus.wasteland.placement.TemplatePlacementService;
import dev.exodus.wasteland.placement.TerrainPreparationService;
import dev.exodus.wasteland.loot.LootMarkerProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class ArenaPreparationService {
    private ArenaPreparationService() {}

    public static ArenaRecord prepare(MinecraftServer server, UUID initiator) {
        ExodusSavedData data = ExodusSavedData.get(server);
        ArenaGeometry geometry = ArenaGeometry.of(ExodusConfig.BORDER_SIZE.get(), ExodusConfig.PREGENERATION_BUFFER.get(), ExodusConfig.ARENA_SAFETY_GAP.get());
        long ordinal = data.arenas.records().size();
        ArenaGrid.Center center = ArenaGrid.centerFor(ordinal, geometry.gridSpacing());
        ArenaRecord arena = data.arenas.begin(initiator, ordinal, center.x(), center.z(), geometry.arenaSize(), geometry.pregenerationBuffer());
        data.setDirty();
        announce(server, arena, "Arena preparation started at " + center.x() + ", " + center.z() + ".");
        return arena;
    }

    public static boolean cancel(MinecraftServer server) {
        ExodusSavedData data = ExodusSavedData.get(server);
        var active = data.arenas.active();
        if (active.isEmpty()) return false;
        data.arenas.cancel(active.get().id());
        data.setDirty();
        announce(server, active.get(), "Arena preparation cancelled. Generated chunks and placed blocks were retained.");
        return true;
    }

    public static List<String> status(MinecraftServer server) {
        List<ArenaRecord> records = ExodusSavedData.get(server).arenas.records();
        return records.isEmpty() ? List.of("No Exodus arena has been prepared.") : ArenaStatusFormatter.format(records.get(records.size() - 1));
    }

    public static void tick(MinecraftServer server) {
        ExodusSavedData data = ExodusSavedData.get(server);
        ArenaRecord arena = data.arenas.active().orElse(null);
        if (arena == null) return;
        ServerLevel level = server.getLevel(LostCitiesIntegration.WASTELAND_DIMENSION);
        try {
            switch (arena.checkpoint().phase) {
                case DEPENDENCY_CHECK -> {
                    if (level == null) throw new IllegalStateException("Lost Cities dimension lostcities:lostcity is unavailable");
                    if (LostCitiesIntegration.bridge().isEmpty()) throw new IllegalStateException("Lost Cities API is unavailable");
                    arena.checkpoint().phase = ArenaPhase.CITY_SAMPLING;
                }
                case CITY_SAMPLING -> sampleCity(level, arena);
                case TEMPLATE_VALIDATION -> validateTemplates(level, arena);
                case PREGENERATION -> pregenerate(level, arena, server);
                case POI_PLANNING -> arena.checkpoint().phase = ArenaPhase.TERRAIN_PREPARATION;
                case TERRAIN_PREPARATION -> prepareTerrain(level, arena);
                case STRUCTURE_PLACEMENT -> placeStructure(level, arena);
                case MARKER_PROCESSING -> processMarkers(level, arena);
                case FINAL_VALIDATION -> {
                    data.arenas.ready(arena.id());
                    announce(server, arena, "Arena preparation completed and the arena is READY.");
                }
                case COMPLETE -> {}
            }
            data.setDirty();
        } catch (Exception exception) {
            data.arenas.fail(arena.id(), exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage());
            data.setDirty();
            announce(server, arena, "Arena preparation failed: " + arena.failure());
        }
    }

    private static void sampleCity(ServerLevel level, ArenaRecord arena) {
        var bridge = LostCitiesIntegration.bridge().orElseThrow();
        int target = 100;
        int perTick = 8;
        int radiusChunks = arena.arenaSize() / 32;
        while (perTick-- > 0 && arena.checkpoint().citySamples < target) {
            int index = arena.checkpoint().citySamples;
            int x = Math.toIntExact(arena.centerX() >> 4) - radiusChunks + (index % 10) * Math.max(1, radiusChunks * 2 / 9);
            int z = Math.toIntExact(arena.centerZ() >> 4) - radiusChunks + (index / 10) * Math.max(1, radiusChunks * 2 / 9);
            var result = bridge.cityChunk(level, x, z);
            if (result == dev.exodus.wasteland.domain.CityCoverageSampler.Result.UNAVAILABLE) {
                arena.checkpoint().phase = ArenaPhase.TEMPLATE_VALIDATION;
                return;
            }
            arena.checkpoint().citySamples++;
            if (result == dev.exodus.wasteland.domain.CityCoverageSampler.Result.CITY) arena.checkpoint().cityChunks++;
        }
        if (arena.checkpoint().citySamples >= target) {
            int percent = arena.checkpoint().cityChunks * 100 / arena.checkpoint().citySamples;
            if (percent < ExodusConfig.CITY_COVERAGE_MINIMUM_PERCENT.get()) {
                throw new IllegalStateException("City coverage " + percent + "% is below the configured minimum");
            }
            arena.checkpoint().phase = ArenaPhase.TEMPLATE_VALIDATION;
        }
    }

    private static void validateTemplates(ServerLevel level, ArenaRecord arena) {
        List<String> errors = CompositeValidator.validate(id -> {
            StructureTemplate template = level.getStructureManager().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("exodus", id)).orElse(null);
            if (template == null) return null;
            var size = template.getSize();
            return new CompositeDefinition.Size(size.getX(), size.getY(), size.getZ());
        });
        for (String camp : List.of("abandoned_camp_01", "occupied_camp_01")) {
            if (level.getStructureManager().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("exodus", camp)).isEmpty()) errors = append(errors, camp + " is missing");
        }
        if (!errors.isEmpty()) throw new IllegalStateException(String.join("; ", errors));
        arena.checkpoint().phase = ArenaPhase.PREGENERATION;
    }

    private static void pregenerate(ServerLevel level, ArenaRecord arena, MinecraftServer server) {
        ArenaGeometry geometry = ArenaGeometry.of(arena.arenaSize(), arena.buffer(), ExodusConfig.ARENA_SAFETY_GAP.get());
        ChunkPreparationCursor cursor = ChunkPreparationCursor.forArea(arena.centerX(), arena.centerZ(), geometry.generationSize(), arena.checkpoint().chunkCursor);
        int old = cursor.index();
        for (int count = 0; count < ExodusConfig.PREPARATION_CHUNKS_PER_TICK.get() && !cursor.complete(); count++) {
            level.getChunk(cursor.chunkX(), cursor.chunkZ(), net.minecraft.world.level.chunk.ChunkStatus.FULL, true);
            cursor = cursor.advance();
        }
        arena.checkpoint().chunkCursor = cursor.index();
        for (int milestone : ProgressMilestones.crossed(old, cursor.index(), cursor.total())) {
            arena.checkpoint().lastAnnouncedPercent = milestone;
            announce(server, arena, "Arena preparation " + milestone + "% complete.");
        }
        if (cursor.complete()) arena.checkpoint().phase = ArenaPhase.POI_PLANNING;
    }

    private static void prepareTerrain(ServerLevel level, ArenaRecord arena) {
        if (arena.checkpoint().placementY.isEmpty()) {
            for (PlacementPlan.Entry entry : placements(arena)) {
                arena.checkpoint().placementY.put(entry.placementId(), placementY(level, entry));
            }
        }
        for (PlacementPlan.Entry entry : placements(arena)) {
            String id = "terrain@" + entry.placementId();
            if (arena.checkpoint().completedPlacements.contains(id)) continue;
            int y = arena.checkpoint().placementY.get(entry.placementId());
            TerrainPreparationService.prepare(level, entry, y, height(entry), ExodusConfig.POI_TERRAIN_MARGIN.get());
            arena.checkpoint().completedPlacements.add(id);
            return;
        }
        arena.checkpoint().phase = ArenaPhase.STRUCTURE_PLACEMENT;
    }

    private static void placeStructure(ServerLevel level, ArenaRecord arena) {
        for (PlacementPlan.Entry entry : placements(arena)) {
            if (arena.checkpoint().completedPlacements.contains(entry.placementId())) continue;
            int y = arena.checkpoint().placementY.get(entry.placementId());
            TemplatePlacementService.placeOnce(level, entry, new net.minecraft.core.BlockPos(entry.x(), y, entry.z()),
                    arena.id().getMostSignificantBits(), arena.checkpoint().completedPlacements);
            return;
        }
        arena.checkpoint().phase = ArenaPhase.MARKER_PROCESSING;
    }

    private static void processMarkers(ServerLevel level, ArenaRecord arena) {
        for (PlacementPlan.Entry entry : placements(arena)) {
            String id = "markers@" + entry.placementId();
            if (arena.checkpoint().completedPlacements.contains(id)) continue;
            int y = arena.checkpoint().placementY.get(entry.placementId());
            LootMarkerProcessor.process(level, new net.minecraft.core.BlockPos(entry.x(), y, entry.z()),
                    new net.minecraft.core.BlockPos(entry.maxX(), y + height(entry) - 1, entry.maxZ()),
                    arena.id().getLeastSignificantBits(), arena.checkpoint().completedPlacements);
            arena.checkpoint().completedPlacements.add(id);
            return;
        }
        arena.checkpoint().phase = ArenaPhase.FINAL_VALIDATION;
    }

    private static List<PlacementPlan.Entry> placements(ArenaRecord arena) {
        int cx = Math.toIntExact(arena.centerX()), cz = Math.toIntExact(arena.centerZ()), quarter = arena.arenaSize() / 4;
        List<PlacementPlan.Entry> entries = new ArrayList<>();
        addFaction(entries, "russian", cx - quarter - 28, cz - 30);
        addFaction(entries, "american", cx + quarter - 28, cz - 30);
        java.util.Random counts = new java.util.Random(arena.id().getLeastSignificantBits());
        int abandoned = randomInclusive(counts, ExodusConfig.ABANDONED_CAMPS_MIN.get(), ExodusConfig.ABANDONED_CAMPS_MAX.get());
        int occupied = randomInclusive(counts, ExodusConfig.OCCUPIED_CAMPS_MIN.get(), ExodusConfig.OCCUPIED_CAMPS_MAX.get());
        PlacementPlan camps = PlacementPolicy.plan(arena.id().getMostSignificantBits(), cx, cz, arena.arenaSize(),
                abandoned, occupied, ExodusConfig.CAMP_MIN_DISTANCE.get(),
                List.of("exodus:abandoned_camp_01"), List.of("exodus:occupied_camp_01"));
        entries.addAll(camps.entries().stream().filter(entry -> entry.kind() == PlacementPlan.Kind.ABANDONED_CAMP
                || entry.kind() == PlacementPlan.Kind.OCCUPIED_CAMP).toList());
        return entries;
    }

    private static void addFaction(List<PlacementPlan.Entry> entries, String nation, int x, int z) {
        CompositeDefinition definition = CompositeDefinition.faction(nation);
        PlacementPlan.Kind kind = nation.equals("russian") ? PlacementPlan.Kind.RUSSIAN_BASE : PlacementPlan.Kind.AMERICAN_BASE;
        for (CompositeDefinition.Part part : definition.parts()) {
            entries.add(new PlacementPlan.Entry(part.id(), "exodus:" + part.id(), kind, x + part.offsetX(), z + part.offsetZ(),
                    part.size().x(), part.size().z(), PlacementPlan.Rotation.NONE, true));
        }
    }

    private static int height(PlacementPlan.Entry entry) {
        return entry.kind() == PlacementPlan.Kind.ABANDONED_CAMP || entry.kind() == PlacementPlan.Kind.OCCUPIED_CAMP ? 11 : 20;
    }

    private static int randomInclusive(java.util.Random random, int minimum, int maximum) {
        if (maximum < minimum) throw new IllegalStateException("Camp maximum cannot be smaller than minimum");
        return minimum + random.nextInt(maximum - minimum + 1);
    }

    private static int placementY(ServerLevel level, PlacementPlan.Entry entry) {
        if (entry.kind() == PlacementPlan.Kind.ABANDONED_CAMP || entry.kind() == PlacementPlan.Kind.OCCUPIED_CAMP) {
            return TerrainPreparationService.surfaceY(level, entry);
        }
        int anchorX = entry.x() - (entry.placementId().endsWith("_2") || entry.placementId().endsWith("_4") ? 29 : 0);
        int anchorZ = entry.z() - (entry.placementId().endsWith("_3") || entry.placementId().endsWith("_4") ? 30 : 0);
        PlacementPlan.Entry combined = new PlacementPlan.Entry("combined", "", entry.kind(), anchorX, anchorZ,
                57, 60, PlacementPlan.Rotation.NONE, true);
        int offset = entry.kind() == PlacementPlan.Kind.RUSSIAN_BASE
                ? ExodusConfig.RUSSIAN_BASE_Y_OFFSET.get() : ExodusConfig.AMERICAN_BASE_Y_OFFSET.get();
        return TerrainPreparationService.surfaceY(level, combined) + offset;
    }

    private static List<String> append(List<String> source, String value) { var copy = new ArrayList<>(source); copy.add(value); return copy; }
    private static void announce(MinecraftServer server, ArenaRecord arena, String message) {
        server.sendSystemMessage(Component.literal("[Exodus] " + message));
        ServerPlayer initiator = server.getPlayerList().getPlayer(arena.initiator());
        if (initiator != null) initiator.sendSystemMessage(Component.literal("[Exodus] " + message));
    }
}
