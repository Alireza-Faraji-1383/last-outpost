package dev.exodus.wasteland.profile;

import com.mojang.logging.LogUtils;
import dev.exodus.wasteland.lostcities.LostCitiesIntegration;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.varia.ChunkCoord;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Opt-in diagnostics built only with -PcitySmoke; stops its isolated server when finished. */
@Mod.EventBusSubscriber(modid="exodus")
public final class FixedCitySmoke {
    private static final org.slf4j.Logger LOG = LogUtils.getLogger();
    private static final int[] ORDINALS = {0,63};
    private static int phase, arenaIndex, samples, cities, chunks;
    private static long started;
    private static double totalMillis;
    private static java.util.UUID preparedId;
    private static int preparationTicks;
    private static final int[][] PROBES = {{0,0},{-8,-8},{8,-8},{-8,8},{8,8},{20,0},{35,1},{50,0}};
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || phase == 6) return;
        var server = event.getServer();
        try {
            var level = server.getLevel(LostCitiesIntegration.WASTELAND_DIMENSION);
            if (level == null) throw new IllegalStateException("Missing city dimension");
            var layout = FixedCityLayout.load();
            var provider = Registration.LOSTCITY_FEATURE.get().getDimensionInfo(level);
            if (provider == null) throw new IllegalStateException("Missing city provider");
            if (phase == 0) {
                started = System.nanoTime();
                for (int ordinal : ORDINALS) {
                    FixedCityRuntime.validate(level,layout,ordinal);
                    FixedCityRuntime.requireFresh(level,layout,ordinal);
                }
                LOG.info("[CITY_SMOKE] Binding/assets/fresh regions passed; mode={} spawners={} profile={}",
                        provider.getProfile().LANDSCAPE_TYPE,provider.getProfile().GENERATE_SPAWNERS,provider.getProfile().getName());
                phase=1;
            } else if (phase == 1) {
                var center = layout.arena(ORDINALS[arenaIndex]);
                for (int budget=0; budget<8 && samples<441; budget++) {
                    int x = Math.toIntExact(center.x())-960+(samples%21)*96;
                    int z = Math.toIntExact(center.z())-960+(samples/21)*96;
                    if (BuildingInfo.isCityRaw(new ChunkCoord(level.dimension(),x>>4,z>>4),provider,provider.getProfile())) cities++;
                    samples++;
                }
                if (samples==441) {
                    double coverage = cities * 100.0 / samples;
                    if (coverage < 38 || coverage > 48) throw new IllegalStateException("Unexpected sampled coverage: "+coverage);
                    LOG.info("[CITY_SMOKE] Arena {} measured grid coverage {}% ({}/{})",ORDINALS[arenaIndex],coverage,cities,samples);
                    arenaIndex++; samples=0; cities=0;
                    if (arenaIndex==ORDINALS.length) {arenaIndex=0;phase=2;}
                }
            } else if (phase == 2) {
                var center = layout.arena(ORDINALS[arenaIndex]);
                int[] probe = PROBES[chunks % PROBES.length];
                int x = Math.toIntExact((center.x()+layout.offsetX())>>4)+probe[0];
                int z = Math.toIntExact((center.z()+layout.offsetZ())>>4)+probe[1];
                long before = System.nanoTime();
                var chunk = level.getChunk(x,z);
                double elapsed = (System.nanoTime()-before)/1_000_000.0;
                totalMillis += elapsed;
                var info = BuildingInfo.getBuildingInfo(new ChunkCoord(level.dimension(),x,z),provider);
                if (info.getSphere()!=null && info.getSphere().isEnabled()) throw new IllegalStateException("Sphere generated");
                for (var section : chunk.getSections()) {
                    if (section.getStates().maybeHas(state -> state.is(Blocks.SPAWNER))) throw new IllegalStateException("Spawner generated");
                }
                if (probe[0]==0 && probe[1]==0 && (!info.isCity() || info.getNumFloors()<8 || info.getNumFloors()>12)) {
                    throw new IllegalStateException("Missing or incorrectly sized central tower");
                }
                if (info.isCity()) {
                    String expected = FixedCityZoning.styleAt((long)x*16,(long)z*16);
                    if (!expected.equals(info.getCityStyle().getName())) throw new IllegalStateException("Incorrect radial style: "+info.getCityStyle().getName());
                    if (info.getBuildingType()!=null && "exodus:outskirts".equals(expected) && (info.getNumFloors()<1 || info.getNumFloors()>3)) throw new IllegalStateException("Incorrect outskirts floors");
                }
                LOG.info("[CITY_SMOKE] Arena {} chunk {},{} city={} building={} floors={} sphere=none spawners=0 generationMs={}",
                        ORDINALS[arenaIndex],x,z,info.isCity(),info.getBuildingType(),info.getNumFloors(),elapsed);
                if (!info.isCity()) LOG.info("[CITY_SMOKE] Outside terrain y={} dimensionMinY={} dimensionHeight={}",
                        level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,x*16,z*16),
                        level.getMinBuildHeight(),level.getHeight());
                chunks++;
                if (chunks % PROBES.length == 0) arenaIndex++;
                if (arenaIndex==ORDINALS.length) phase=3;
            } else if (phase == 3) {
                boolean rejected = false;
                try { FixedCityRuntime.requireFresh(level,layout,0); }
                catch (IllegalStateException expected) { rejected=expected.getMessage().contains("generated chunks"); }
                if (!rejected) throw new IllegalStateException("Used region was accepted as fresh");
                var registry=dev.exodus.ExodusSavedData.get(server).arenas;
                var first=layout.arena(0);
                var legacy=registry.begin(java.util.UUID.randomUUID(),0,first.x(),first.z(),layout.arenaSize(),layout.buffer());
                legacy.checkpoint().phase=dev.exodus.wasteland.arena.ArenaPhase.CITY_SAMPLING;
                dev.exodus.wasteland.arena.ArenaPreparationService.tick(server);
                if (legacy.state()!=dev.exodus.wasteland.arena.ArenaState.FAILED) throw new IllegalStateException("Legacy preparation was not rejected");
                dev.exodus.ExodusConfig.PREPARATION_CHUNKS_PER_TICK.set(0);
                dev.exodus.ExodusConfig.ABANDONED_CAMPS_MIN.set(1);
                dev.exodus.ExodusConfig.ABANDONED_CAMPS_MAX.set(1);
                dev.exodus.ExodusConfig.OCCUPIED_CAMPS_MIN.set(1);
                dev.exodus.ExodusConfig.OCCUPIED_CAMPS_MAX.set(1);
                var prepared=dev.exodus.wasteland.arena.ArenaPreparationService.prepare(server,java.util.UUID.randomUUID());
                preparedId=prepared.id();
                LOG.info("[CITY_SMOKE] Used-region/legacy rejection passed; preparing real arena {} with existing templates",prepared.ordinal());
                phase=4;
            } else if (phase == 4) {
                preparationTicks++;
                var prepared=dev.exodus.ExodusSavedData.get(server).arenas.records().stream().filter(record->record.id().equals(preparedId)).findFirst().orElseThrow();
                if (prepared.state()==dev.exodus.wasteland.arena.ArenaState.FAILED) throw new IllegalStateException("Real arena preparation failed: "+prepared.failure());
                if (preparationTicks>4000) throw new IllegalStateException("Preparation exceeded smoke tick budget");
                if (prepared.state()==dev.exodus.wasteland.arena.ArenaState.READY) {
                    if (prepared.checkpoint().poiStates.size()!=4 || prepared.checkpoint().poiStates.values().stream().anyMatch(state->!state.verified)) {
                        throw new IllegalStateException("POIs were not all verified");
                    }
                    LOG.info("[CITY_SMOKE] Real arena {} READY with {} verified POIs and {} structure parts/markers",prepared.ordinal(),prepared.checkpoint().poiStates.size(),prepared.checkpoint().completedPlacements.size());
                    phase=5;
                }
            } else {
                LOG.info("[CITY_SMOKE] PASS fullChunks={} totalGenerationMs={} elapsedSeconds={}",chunks,totalMillis,(System.nanoTime()-started)/1_000_000_000.0);
                phase=6;
                server.halt(false);
            }
        } catch (Exception exception) {
            LOG.error("[CITY_SMOKE] FAIL",exception);
            phase=6;
            server.halt(false);
        }
    }
}

