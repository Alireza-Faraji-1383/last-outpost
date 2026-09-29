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
import dev.exodus.wasteland.placement.FootprintChunkPlan;
import dev.exodus.wasteland.placement.SurfacePoi;
import dev.exodus.wasteland.placement.SurfacePoiCatalog;
import dev.exodus.wasteland.placement.PlacementBounds;
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
        ArenaRecord arena = data.arenas.active().orElseGet(() -> data.arenas.readyArena().orElse(null));
        if (arena == null) return false;
        data.arenas.cancel(arena.id());
        data.setDirty();
        announce(server, arena, "Arena abandoned. Generated chunks and placed blocks were retained.");
        return true;
    }

    public static List<String> status(MinecraftServer server) {
        List<ArenaRecord> records = ExodusSavedData.get(server).arenas.records();
        return records.isEmpty() ? List.of("No Exodus arena has been prepared.") : ArenaStatusFormatter.format(records.get(records.size() - 1));
    }

    public static List<ArenaLocation> locations(MinecraftServer server) {
        return locationCatalog(server).locations();
    }

    public static List<String> locationIds(MinecraftServer server) {
        return locationCatalog(server).ids();
    }

    public static java.util.Optional<ArenaLocation> location(MinecraftServer server, String placementId) {
        return locationCatalog(server).find(placementId);
    }

    private static ArenaLocationCatalog locationCatalog(MinecraftServer server) {
        List<ArenaRecord> records = ExodusSavedData.get(server).arenas.records();
        for (int i = records.size() - 1; i >= 0; i--) {
            ArenaRecord arena = records.get(i);
            if (!arena.checkpoint().placementY.isEmpty()) {
                return ArenaLocationCatalog.from(placements(arena), arena.checkpoint().placementY);
            }
        }
        return ArenaLocationCatalog.from(List.of(), java.util.Map.of());
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
                case POI_PLANNING -> arena.checkpoint().phase = ArenaPhase.POI_CHUNK_PREPARATION;
                case POI_CHUNK_PREPARATION, TERRAIN_PREPARATION, STRUCTURE_PLACEMENT, POI_VERIFICATION -> advancePoi(level, arena, server);
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
        int chunksPerTick = ExodusConfig.PREPARATION_CHUNKS_PER_TICK.get();
        if (!PregenerationPolicy.enabled(chunksPerTick)) {
            arena.checkpoint().phase = ArenaPhase.POI_PLANNING;
            announce(server, arena, "Arena pregeneration skipped by configuration; chunks will generate during play.");
            return;
        }
        ArenaGeometry geometry = ArenaGeometry.of(arena.arenaSize(), arena.buffer(), ExodusConfig.ARENA_SAFETY_GAP.get());
        ChunkPreparationCursor cursor = ChunkPreparationCursor.forArea(arena.centerX(), arena.centerZ(), geometry.generationSize(), arena.checkpoint().chunkCursor);
        int old = cursor.index();
        for (int count = 0; count < chunksPerTick && !cursor.complete(); count++) {
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

    private static void advancePoi(ServerLevel level, ArenaRecord arena, MinecraftServer server) {
        for (SurfacePoi poi : surfacePois(arena)) {
            FootprintChunkPlan chunks = FootprintChunkPlan.forBounds(poi.x(), poi.z(), poi.maxX(), poi.maxZ(), 1);
            PoiPreparationState state = arena.checkpoint().poiStates.computeIfAbsent(poi.id(), ignored -> new PoiPreparationState());
            PoiPreparationWorkflow.bindGeometry(state, poi.geometrySignature());
            if (state.totalChunks == 0) state.totalChunks = chunks.total();
            if (state.totalChunks != chunks.total()) throw new IllegalStateException("POI " + poi.id() + " persisted chunk plan no longer matches its footprint");
            PoiPreparationWorkflow.Action action = PoiPreparationWorkflow.next(state, chunks.total());
            if (action == PoiPreparationWorkflow.Action.DONE) continue;
            switch (action) {
                case GENERATE_CHUNK -> {
                    arena.checkpoint().phase = ArenaPhase.POI_CHUNK_PREPARATION;
                    for (int count = 0; count < ExodusConfig.POI_CHUNKS_PER_TICK.get() && state.chunkCursor < chunks.total(); count++) {
                        level.getChunk(chunks.chunkX(state.chunkCursor), chunks.chunkZ(state.chunkCursor),
                                net.minecraft.world.level.chunk.ChunkStatus.FULL, true);
                        state.chunkCursor++;
                    }
                    state.chunksComplete = state.chunkCursor == chunks.total();
                }
                case SELECT_SURFACE -> {
                    arena.checkpoint().phase = ArenaPhase.TERRAIN_PREPARATION;
                    var sample = TerrainPreparationService.sample(level, poi, ExodusConfig.SURFACE_QUORUM_PERCENT.get(),
                            ExodusConfig.SURFACE_HEIGHT_TOLERANCE.get(), ExodusConfig.PREFERRED_SURFACE_MIN_Y.get(),
                            ExodusConfig.PREFERRED_SURFACE_MAX_Y.get());
                    state.platformY = sample.selection().platformY();
                    state.quorumCount = sample.selection().quorumCount();
                    state.totalColumns = sample.selection().totalColumns();
                    state.validSamples = sample.validSamples();
                    state.unsupportedSamples = sample.unsupportedSamples();
                    state.minimumSupportY = sample.selection().minimumSupportY();
                    state.maximumSupportY = sample.selection().maximumSupportY();
                    state.surfaceSelected = true;
                    poi.parts().forEach(part -> arena.checkpoint().placementY.put(part.placementId(), state.platformY));
                    announce(server, arena, "POI " + poi.id() + " surface selected: size=" + poi.width() + "x" + poi.depth()
                            + " chunks=" + chunks.minimumChunkX() + "," + chunks.minimumChunkZ() + ".." + chunks.maximumChunkX() + "," + chunks.maximumChunkZ()
                            + " samples=" + state.validSamples + " unsupported=" + state.unsupportedSamples
                            + " range=" + state.minimumSupportY + ".." + state.maximumSupportY + " y=" + state.platformY
                            + " quorum=" + state.quorumCount + "/" + state.totalColumns + ".");
                }
                case PREPARE_TERRAIN -> {
                    arena.checkpoint().phase = ArenaPhase.TERRAIN_PREPARATION;
                    var report = TerrainPreparationService.prepare(level, poi, state.platformY,
                            ExodusConfig.POI_TERRAIN_MARGIN.get(), ExodusConfig.MAX_FOUNDATION_DEPTH.get());
                    state.clearedBlocks = report.clearedBlocks();
                    state.foundationBlocks = report.foundationBlocks();
                    state.deepestFill = report.deepestFill();
                    state.terrainPrepared = true;
                    announce(server, arena, "POI " + poi.id() + " terrain prepared: cleared=" + state.clearedBlocks
                            + " foundation=" + state.foundationBlocks + " deepestFill=" + state.deepestFill + ".");
                }
                case PLACE_STRUCTURE -> {
                    arena.checkpoint().phase = ArenaPhase.STRUCTURE_PLACEMENT;
                    for (PlacementPlan.Entry part : poi.parts()) {
                        TemplatePlacementService.placeOnce(level, part,
                                new net.minecraft.core.BlockPos(part.x(), state.platformY, part.z()),
                                arena.id().getMostSignificantBits(), arena.checkpoint().completedPlacements);
                    }
                    state.structurePlaced = poi.parts().stream()
                            .allMatch(part -> arena.checkpoint().completedPlacements.contains(part.placementId()));
                }
                case VERIFY -> {
                    arena.checkpoint().phase = ArenaPhase.POI_VERIFICATION;
                    TerrainPreparationService.verify(level, poi, state.platformY, ExodusConfig.MAX_FOUNDATION_DEPTH.get());
                    state.verified = true;
                    announce(server, arena, "POI " + poi.id() + " placement verified at Y=" + state.platformY + ".");
                }
                case DONE -> throw new IllegalStateException("Unexpected completed POI workflow action");
            }
            return;
        }
        arena.checkpoint().phase = ArenaPhase.MARKER_PROCESSING;
    }

    private static void processMarkers(ServerLevel level, ArenaRecord arena) {
        for (PlacementPlan.Entry entry : placements(arena)) {
            String id = "markers@" + entry.placementId();
            if (arena.checkpoint().completedPlacements.contains(id)) continue;
            int y = arena.checkpoint().placementY.get(entry.placementId());
            PlacementBounds bounds = PlacementBounds.from(entry);
            LootMarkerProcessor.process(level, new net.minecraft.core.BlockPos(bounds.minX(), y, bounds.minZ()),
                    new net.minecraft.core.BlockPos(bounds.maxX(), y + height(entry) - 1, bounds.maxZ()),
                    arena.id().getLeastSignificantBits(), arena.checkpoint().completedPlacements, entry.placementId());
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

    private static List<SurfacePoi> surfacePois(ArenaRecord arena) {
        return SurfacePoiCatalog.from(placements(arena), ExodusConfig.RUSSIAN_BASE_Y_OFFSET.get(),
                ExodusConfig.AMERICAN_BASE_Y_OFFSET.get());
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

    private static List<String> append(List<String> source, String value) { var copy = new ArrayList<>(source); copy.add(value); return copy; }
    private static void announce(MinecraftServer server, ArenaRecord arena, String message) {
        server.sendSystemMessage(Component.literal("[Exodus] " + message));
        ServerPlayer initiator = server.getPlayerList().getPlayer(arena.initiator());
        if (initiator != null) initiator.sendSystemMessage(Component.literal("[Exodus] " + message));
    }
}
