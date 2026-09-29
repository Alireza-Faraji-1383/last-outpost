# Project Exodus Deterministic Surface Placement Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Generate a fixed-plains Lost Cities wasteland with the `default` landscape and place every faction base and camp on a deterministic, cleared, fully supported surface instead of Y=0, underground, or over a void.

**Architecture:** Keep the existing arena X/Z layout, but group composite faction-base parts into logical surface POIs. For each POI, incrementally generate its footprint plus one chunk of margin to `ChunkStatus.FULL`, select a representative Y using a pure histogram/quorum policy, manufacture and verify a stone-supported site, then place the existing templates. Persist per-POI progress so restart recovery is idempotent and broad arena pregeneration may remain disabled.

**Tech Stack:** Java 17, Minecraft 1.20.1, Forge 47.4.10, Lost Cities 1.20-7.5.5 API, JUnit 5, Gradle

**Spec:** `docs/superpowers/specs/2026-09-29-project-exodus-surface-placement-design.md`

## Global Constraints

- The playable dimension remains `lostcities:lostcity`.
- Use the Lost Cities `wasteland` profile with `landscapeType: "default"`; do not use `floating`, `space`, `spheres`, or `cavernspheres`.
- Use fixed `minecraft:plains` biome selection with `minecraft:overworld` noise settings; this is not superflat terrain.
- Do not change player `exodus:starter_base` allocation or burial behavior.
- Do not move arena POIs away from their persisted deterministic X/Z coordinates.
- Surface quorum defaults to 50 percent with height tolerance 2; Y=55 through Y=70 is a preference, not a hard limit.
- Foundation fill defaults to `minecraft:stone` and fails when support is not found within 96 blocks.
- Disabling broad arena pregeneration must not disable required per-POI chunk generation.
- All player-facing messages and logs remain English.
- Do not install or remove third-party mods.
- Automated tests and builds are evidence for code behavior only; real in-game acceptance remains manual.

## Review Focus

- A one-block tower or floating leaf above otherwise level ground must not raise the selected platform Y; Task 2 pins this with an outlier test.
- A composite faction base must use one 57x60 surface and one selected Y across all four parts; Task 3 pins grouping and shared-height behavior.
- Broad pregeneration set to zero must still generate every POI footprint chunk to `FULL` before sampling; Task 6 pins this state-machine transition.
- A ravine or void deeper than 96 blocks below any required footprint column must fail loudly without marking terrain complete; Task 4 pins bounded-foundation failure.
- Loading an older save without new checkpoint fields must resume safely from POI chunk preparation instead of treating the placement as complete; Task 5 pins backward-compatible persistence.

---

### Task 1: Lock the Lost Cities world-generation contract

**Files:**
- Create: `exodus-mod/src/main/resources/data/lostcities/dimension/lostcity.json`
- Modify: `exodus-mod/src/main/java/dev/exodus/wasteland/profile/WastelandProfileDefinition.java`
- Modify: `exodus-mod/src/test/java/dev/exodus/wasteland/profile/WastelandProfileDefinitionTest.java`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/profile/LostCityDimensionResourceTest.java`

**Interfaces:**
- Consumes: Lost Cities profile setup API and Forge mod resources.
- Produces: `WastelandProfileDefinition.LANDSCAPE_TYPE == "default"` and a packaged `data/lostcities/dimension/lostcity.json` contract.

- [ ] **Step 1: Write failing profile and resource contract tests**

Add assertions that the Exodus profile derives from `wasteland` and declares the expected landscape constant:

```java
@Test
void usesWastelandWithDefaultLandscape() {
    assertEquals("wasteland", WastelandProfileDefinition.BASE_PROFILE);
    assertEquals("default", WastelandProfileDefinition.LANDSCAPE_TYPE);
}
```

Create `LostCityDimensionResourceTest` and parse the packaged JSON with Gson:

```java
@Test
void packagesFixedPlainsLostCityDimension() throws Exception {
    try (var stream = getClass().getResourceAsStream("/data/lostcities/dimension/lostcity.json")) {
        assertNotNull(stream);
        var root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals("lostcities:lostcity", root.get("type").getAsString());
        var generator = root.getAsJsonObject("generator");
        assertEquals("minecraft:noise", generator.get("type").getAsString());
        assertEquals("minecraft:overworld", generator.get("settings").getAsString());
        var biome = generator.getAsJsonObject("biome_source");
        assertEquals("minecraft:fixed", biome.get("type").getAsString());
        assertEquals("minecraft:plains", biome.get("biome").getAsString());
        assertTrue(root.get("forge:use_server_seed").getAsBoolean());
    }
}
```

- [ ] **Step 2: Run the focused tests and verify the new contract fails**

Run:

```powershell
cd exodus-mod
.\gradlew test --tests dev.exodus.wasteland.profile.WastelandProfileDefinitionTest --tests dev.exodus.wasteland.profile.LostCityDimensionResourceTest
```

Expected: FAIL because `LANDSCAPE_TYPE` and the dimension resource do not exist.

- [ ] **Step 3: Add the explicit profile constant and dimension JSON**

Add to `WastelandProfileDefinition`:

```java
public static final String LANDSCAPE_TYPE = "default";
```

Create the resource:

```json
{
  "type": "lostcities:lostcity",
  "generator": {
    "type": "minecraft:noise",
    "seed": 0,
    "settings": "minecraft:overworld",
    "biome_source": {
      "type": "minecraft:fixed",
      "biome": "minecraft:plains"
    }
  },
  "forge:use_server_seed": true
}
```

Keep `BASE_PROFILE = "wasteland"`. The landscape constant documents and tests the inherited `default` contract; do not call an unverified Lost Cities setter.

- [ ] **Step 4: Run the focused tests**

Run the Step 2 command.

Expected: PASS.

- [ ] **Step 5: Commit the world-generation contract**

```powershell
git add -- exodus-mod/src/main/resources/data/lostcities/dimension/lostcity.json exodus-mod/src/main/java/dev/exodus/wasteland/profile/WastelandProfileDefinition.java exodus-mod/src/test/java/dev/exodus/wasteland/profile/WastelandProfileDefinitionTest.java exodus-mod/src/test/java/dev/exodus/wasteland/profile/LostCityDimensionResourceTest.java
git commit -m "feat(wasteland): use default plains lost city terrain"
```

### Task 2: Implement pure surface selection and footprint chunk planning

**Files:**
- Replace: `exodus-mod/src/main/java/dev/exodus/wasteland/placement/SurfacePlacementPolicy.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/placement/FootprintChunkPlan.java`
- Replace: `exodus-mod/src/test/java/dev/exodus/wasteland/placement/SurfacePlacementPolicyTest.java`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/placement/FootprintChunkPlanTest.java`

**Interfaces:**
- Consumes: integer support heights collected from actual world columns.
- Produces: `SurfacePlacementPolicy.Selection select(List<Integer> supports, int totalColumns, int quorumPercent, int tolerance, int preferredMinY, int preferredMaxY)` and `FootprintChunkPlan.forBounds(int minX, int minZ, int maxX, int maxZ, int marginChunks)`.

- [ ] **Step 1: Write failing quorum-policy tests**

Cover the exact decisions:

```java
@Test void rejectsSingleHighOutlier() {
    var supports = new ArrayList<Integer>();
    for (int i = 0; i < 99; i++) supports.add(64);
    supports.add(120);
    var result = SurfacePlacementPolicy.select(supports, 100, 50, 2, 55, 70);
    assertEquals(64, result.platformY());
    assertEquals(99, result.quorumCount());
}

@Test void choosesHighestBandMeetingQuorum() {
    var result = SurfacePlacementPolicy.select(
        Stream.concat(Collections.nCopies(50, 62).stream(), Collections.nCopies(50, 74).stream()).toList(),
        100, 50, 2, 55, 70);
    assertEquals(74, result.platformY());
}

@Test void preferredBandBreaksEqualScoreTieOnly() {
    var result = SurfacePlacementPolicy.select(List.of(60, 60, 72, 72), 4, 50, 0, 55, 70);
    assertEquals(60, result.platformY());
}

@Test void unsupportedColumnsCountAgainstQuorum() {
    assertThrows(IllegalStateException.class,
        () -> SurfacePlacementPolicy.select(Collections.nCopies(49, 64), 100, 50, 2, 55, 70));
}
```

Also test invalid percentages/tolerances, empty supports, and world-minimum rejection supplied by the caller.

- [ ] **Step 2: Write failing footprint-to-chunk tests**

```java
@Test void includesAllIntersectingChunksAndOneChunkMargin() {
    var plan = FootprintChunkPlan.forBounds(16, 31, 72, 90, 1);
    assertEquals(0, plan.minimumChunkX());
    assertEquals(0, plan.minimumChunkZ());
    assertEquals(6, plan.maximumChunkX());
    assertEquals(6, plan.maximumChunkZ());
    assertEquals(49, plan.total());
}

@Test void handlesNegativeBlockCoordinatesWithFloorDivision() {
    var plan = FootprintChunkPlan.forBounds(-17, -1, -1, 15, 1);
    assertEquals(-3, plan.minimumChunkX());
    assertEquals(-2, plan.minimumChunkZ());
}
```

- [ ] **Step 3: Run the focused tests and verify failure**

Run:

```powershell
cd exodus-mod
.\gradlew test --tests dev.exodus.wasteland.placement.SurfacePlacementPolicyTest --tests dev.exodus.wasteland.placement.FootprintChunkPlanTest
```

Expected: FAIL on missing types/signatures and the old maximum-height behavior.

- [ ] **Step 4: Implement immutable policy results and cursor-friendly chunk plans**

Use these public shapes:

```java
public record Selection(int platformY, int quorumCount, int totalColumns,
                        int minimumSupportY, int maximumSupportY) {
    public int quorumPercent() { return quorumCount * 100 / totalColumns; }
}

public static Selection select(List<Integer> supports, int totalColumns,
                               int quorumPercent, int tolerance,
                               int preferredMinY, int preferredMaxY)
```

Candidate ranking is: qualifying candidates only; prefer the 55-70 band when equal quorum counts compete; then choose the greater Y. Count quorum against `totalColumns`, not only valid samples.

Use this chunk-plan shape:

```java
public record FootprintChunkPlan(int minimumChunkX, int minimumChunkZ,
                                 int maximumChunkX, int maximumChunkZ) {
    public static FootprintChunkPlan forBounds(int minX, int minZ, int maxX, int maxZ, int marginChunks);
    public int total();
    public int chunkX(int index);
    public int chunkZ(int index);
}
```

Validate bounds, margin, and index explicitly.

- [ ] **Step 5: Run focused tests and commit**

Run the Step 3 command; expect PASS.

```powershell
git add -- exodus-mod/src/main/java/dev/exodus/wasteland/placement/SurfacePlacementPolicy.java exodus-mod/src/main/java/dev/exodus/wasteland/placement/FootprintChunkPlan.java exodus-mod/src/test/java/dev/exodus/wasteland/placement/SurfacePlacementPolicyTest.java exodus-mod/src/test/java/dev/exodus/wasteland/placement/FootprintChunkPlanTest.java
git commit -m "feat(wasteland): select supported poi surface heights"
```

### Task 3: Model logical surface POIs and configuration

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/placement/SurfacePoi.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/placement/SurfacePoiCatalog.java`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/placement/SurfacePoiCatalogTest.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusConfig.java`

**Interfaces:**
- Consumes: the existing deterministic `List<PlacementPlan.Entry>` and configured faction Y offsets.
- Produces: `SurfacePoi` logical bounds shared by chunk generation, surface selection, terrain preparation, placement, marker processing, locations, and verification.

- [ ] **Step 1: Write failing logical-grouping tests**

```java
@Test void groupsRussianPartsIntoOneCompositeFootprint() {
    var pois = SurfacePoiCatalog.from(entriesWithRussianCompositeAndOneCamp(), 0, 0);
    var russian = pois.stream().filter(p -> p.id().equals("russian_base")).findFirst().orElseThrow();
    assertEquals(57, russian.width());
    assertEquals(60, russian.depth());
    assertEquals(20, russian.height());
    assertEquals(4, russian.parts().size());
    assertTrue(russian.parts().stream().allMatch(p -> p.kind() == PlacementPlan.Kind.RUSSIAN_BASE));
}

@Test void keepsEachCampAsItsOwnLogicalPoi() {
    var camp = SurfacePoiCatalog.from(List.of(campEntry("abandoned_camp_0", 40, -80)), 0, 0).get(0);
    assertEquals("abandoned_camp_0", camp.id());
    assertEquals(11, camp.width());
    assertEquals(16, camp.depth());
    assertEquals(11, camp.height());
}

@Test void appliesOneConfiguredYOffsetToWholeFactionComposite() {
    var russian = SurfacePoiCatalog.from(russianParts(), -3, 4).get(0);
    assertEquals(-3, russian.yOffset());
}
```

- [ ] **Step 2: Run the test and verify missing-model failure**

```powershell
cd exodus-mod
.\gradlew test --tests dev.exodus.wasteland.placement.SurfacePoiCatalogTest
```

Expected: FAIL because `SurfacePoi` and `SurfacePoiCatalog` do not exist.

- [ ] **Step 3: Implement the logical POI model**

Use one immutable record:

```java
public record SurfacePoi(String id, PlacementPlan.Kind kind,
                         int x, int z, int width, int depth, int height,
                         int yOffset, List<PlacementPlan.Entry> parts) {
    public SurfacePoi { parts = List.copyOf(parts); }
    public int maxX() { return x + width - 1; }
    public int maxZ() { return z + depth - 1; }
}
```

`SurfacePoiCatalog.from(entries, russianYOffset, americanYOffset)` must validate that each faction has exactly the four expected part IDs and derive the 57x60 union from actual bounds. Camps remain one POI each with zero Y offset. Do not infer grouping from list order.

- [ ] **Step 4: Add exact configuration keys**

Add these `IntValue` fields under `wasteland`:

```java
SURFACE_QUORUM_PERCENT = b.defineInRange("surfaceQuorumPercent", 50, 1, 100);
SURFACE_HEIGHT_TOLERANCE = b.defineInRange("surfaceHeightTolerance", 2, 0, 32);
PREFERRED_SURFACE_MIN_Y = b.defineInRange("preferredSurfaceMinY", 55, -64, 319);
PREFERRED_SURFACE_MAX_Y = b.defineInRange("preferredSurfaceMaxY", 70, -64, 319);
MAX_FOUNDATION_DEPTH = b.defineInRange("maxFoundationDepth", 96, 1, 384);
POI_CHUNKS_PER_TICK = b.defineInRange("poiChunksPerTick", 2, 1, 64);
```

Foundation material remains the deliberately fixed initial `Blocks.STONE`; do not add a string parser until multiple materials are required.

- [ ] **Step 5: Run focused tests and commit**

Run the Step 2 command; expect PASS.

```powershell
git add -- exodus-mod/src/main/java/dev/exodus/wasteland/placement/SurfacePoi.java exodus-mod/src/main/java/dev/exodus/wasteland/placement/SurfacePoiCatalog.java exodus-mod/src/test/java/dev/exodus/wasteland/placement/SurfacePoiCatalogTest.java exodus-mod/src/main/java/dev/exodus/ExodusConfig.java
git commit -m "feat(wasteland): model logical surface pois"
```

### Task 4: Inspect, manufacture, and verify supported terrain

**Files:**
- Replace: `exodus-mod/src/main/java/dev/exodus/wasteland/placement/TerrainPreparationService.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/placement/SurfaceSample.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/placement/TerrainPreparationReport.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/placement/TerrainColumnPolicy.java`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/placement/TerrainColumnPolicyTest.java`

**Interfaces:**
- Consumes: `SurfacePoi`, FULL chunks, surface-policy configuration, margin, and maximum foundation depth.
- Produces: `SurfaceSample sample(ServerLevel, SurfacePoi, ...)`, `TerrainPreparationReport prepare(ServerLevel, SurfacePoi, int platformY, int margin, int maxDepth)`, and `void verify(ServerLevel, SurfacePoi, int platformY, int maxDepth)`.

- [ ] **Step 1: Write failing pure column-policy tests**

Extract block-state decisions behind `TerrainColumnPolicy` so they can be tested without a server:

```java
@Test void airFluidLeavesAndReplaceablePlantsAreNotSurfaceSupport() {
    assertFalse(TerrainColumnPolicy.isSurfaceSupport(Blocks.AIR.defaultBlockState()));
    assertFalse(TerrainColumnPolicy.isSurfaceSupport(Blocks.WATER.defaultBlockState()));
    assertFalse(TerrainColumnPolicy.isSurfaceSupport(Blocks.OAK_LEAVES.defaultBlockState()));
    assertFalse(TerrainColumnPolicy.isSurfaceSupport(Blocks.TALL_GRASS.defaultBlockState()));
}

@Test void stoneDirtAndLostCityBuildingBlocksAreSupport() {
    assertTrue(TerrainColumnPolicy.isSurfaceSupport(Blocks.STONE.defaultBlockState()));
    assertTrue(TerrainColumnPolicy.isSurfaceSupport(Blocks.DIRT.defaultBlockState()));
    assertTrue(TerrainColumnPolicy.isSurfaceSupport(Blocks.BRICKS.defaultBlockState()));
}

@Test void foundationStopsAtFirstSolidAndHonorsDepthLimit() {
    assertEquals(4, TerrainColumnPolicy.fillCount(List.of(AIR, WATER, AIR, AIR, STONE), 96));
    assertThrows(IllegalStateException.class,
        () -> TerrainColumnPolicy.fillCount(Collections.nCopies(97, AIR), 96));
}
```

Use actual `BlockState` constants in the test rather than the shorthand names above.

- [ ] **Step 2: Run the focused test and verify failure**

```powershell
cd exodus-mod
.\gradlew test --tests dev.exodus.wasteland.placement.TerrainColumnPolicyTest
```

Expected: FAIL because the policy does not exist.

- [ ] **Step 3: Implement sampling with actual block validation**

Define:

```java
public record SurfaceSample(SurfacePlacementPolicy.Selection selection,
                            int validSamples, int unsupportedSamples) {}
```

For every footprint column, start at `level.getHeight(MOTION_BLOCKING_NO_LEAVES, x, z) - 1`, inspect the actual block and fluid state, and walk downward by at most 8 blocks to skip unsuitable vegetation/leaves. Pass valid support heights and the complete footprint column count to `SurfacePlacementPolicy.select`. Reject a selected platform at or below `level.getMinBuildHeight() + 4`, and reject `platformY + poi.height() + 2 >= level.getMaxBuildHeight()` before mutation. Apply `poi.yOffset()` only after representative terrain selection, then repeat build-limit checks.

- [ ] **Step 4: Implement bounded terrain mutation and reporting**

Define:

```java
public record TerrainPreparationReport(int clearedBlocks, int foundationBlocks,
                                       int deepestFill) {}
```

`prepare` must first pre-scan every footprint column and prove support exists within `maxDepth`; if any column fails, throw before changing blocks. Then set the platform at `platformY - 1`, fill only air/fluid downward to the first solid support, and clear from `platformY` through `platformY + poi.height() + 1` across footprint plus configured margin. Count actual changes in the report.

`verify` must assert platform solidity across the footprint and ensure walking down at most `maxDepth` encounters no air/fluid gap that reaches the limit. Error messages include POI ID and X/Y/Z.

- [ ] **Step 5: Run focused placement tests and commit**

Run:

```powershell
cd exodus-mod
.\gradlew test --tests dev.exodus.wasteland.placement.SurfacePlacementPolicyTest --tests dev.exodus.wasteland.placement.TerrainColumnPolicyTest
```

Expected: PASS.

```powershell
git add -- exodus-mod/src/main/java/dev/exodus/wasteland/placement/TerrainPreparationService.java exodus-mod/src/main/java/dev/exodus/wasteland/placement/SurfaceSample.java exodus-mod/src/main/java/dev/exodus/wasteland/placement/TerrainPreparationReport.java exodus-mod/src/main/java/dev/exodus/wasteland/placement/TerrainColumnPolicy.java exodus-mod/src/test/java/dev/exodus/wasteland/placement/TerrainColumnPolicyTest.java
git commit -m "feat(wasteland): manufacture supported poi terrain"
```

### Task 5: Persist resumable per-POI preparation state

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/arena/PoiPreparationState.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/wasteland/arena/PreparationCheckpoint.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/wasteland/arena/ArenaRegistry.java`
- Modify: `exodus-mod/src/test/java/dev/exodus/wasteland/arena/ArenaRegistryTest.java`

**Interfaces:**
- Consumes: logical POI ID and its incremental progress.
- Produces: `PreparationCheckpoint.poiStates` keyed by POI ID, persisted through `ArenaRegistry.save/load`.

- [ ] **Step 1: Write failing round-trip and legacy-load tests**

Pin all persisted fields:

```java
@Test void roundTripsPoiPreparationProgress() {
    var state = new PoiPreparationState();
    state.chunkCursor = 7;
    state.chunksComplete = true;
    state.surfaceSelected = true;
    state.platformY = 66;
    state.terrainPrepared = true;
    state.structurePlaced = true;
    state.verified = false;
    arena.checkpoint().poiStates.put("russian_base", state);
    var restored = roundTrip(registry).records().get(0).checkpoint().poiStates.get("russian_base");
    assertEquals(7, restored.chunkCursor);
    assertEquals(66, restored.platformY);
    assertTrue(restored.terrainPrepared);
    assertFalse(restored.verified);
}

@Test void legacySaveWithoutPoiStatesRestartsRequiredPoiPreparation() {
    var restored = ArenaRegistry.load(legacyArenaTagWithoutPoiStates());
    assertTrue(restored.records().get(0).checkpoint().poiStates.isEmpty());
    assertEquals(ArenaPhase.POI_CHUNK_PREPARATION, restored.records().get(0).checkpoint().phase);
}
```

- [ ] **Step 2: Run the registry tests and verify failure**

```powershell
cd exodus-mod
.\gradlew test --tests dev.exodus.wasteland.arena.ArenaRegistryTest
```

Expected: FAIL on missing state and phase.

- [ ] **Step 3: Add the state and backward-compatible NBT schema**

Use:

```java
public final class PoiPreparationState {
    public int chunkCursor;
    public boolean chunksComplete;
    public boolean surfaceSelected;
    public int platformY;
    public boolean terrainPrepared;
    public boolean structurePlaced;
    public boolean verified;
}
```

Add `POI_CHUNK_PREPARATION` and `POI_VERIFICATION` to `ArenaPhase`. Add `Map<String, PoiPreparationState> poiStates = new LinkedHashMap<>()` to the checkpoint. Save it as a compound keyed by logical POI ID. On load, if a PREPARING arena is in or beyond old `TERRAIN_PREPARATION` but lacks `poiStates`, set its phase to `POI_CHUNK_PREPARATION`, clear old `placementY`, and set `placementNeedsRevalidation = true`. Preserve terminal READY/FAILED/ABANDONED records without reopening them.

- [ ] **Step 4: Run registry tests and commit**

Run the Step 2 command; expect PASS.

```powershell
git add -- exodus-mod/src/main/java/dev/exodus/wasteland/arena/PoiPreparationState.java exodus-mod/src/main/java/dev/exodus/wasteland/arena/PreparationCheckpoint.java exodus-mod/src/main/java/dev/exodus/wasteland/arena/ArenaPhase.java exodus-mod/src/main/java/dev/exodus/wasteland/arena/ArenaRegistry.java exodus-mod/src/test/java/dev/exodus/wasteland/arena/ArenaRegistryTest.java
git commit -m "feat(wasteland): persist poi surface preparation"
```

### Task 6: Integrate incremental generation, shared placement heights, verification, and diagnostics

**Files:**
- Modify: `exodus-mod/src/main/java/dev/exodus/wasteland/arena/ArenaPreparationService.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/wasteland/arena/ArenaStatusFormatter.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/wasteland/arena/ArenaLocationCatalog.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/arena/PoiPreparationWorkflow.java`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/arena/PoiPreparationWorkflowTest.java`
- Modify: `exodus-mod/src/test/java/dev/exodus/wasteland/arena/ArenaStatusFormatterTest.java`
- Modify: `exodus-mod/src/test/java/dev/exodus/wasteland/arena/ArenaLocationCatalogTest.java`

**Interfaces:**
- Consumes: `SurfacePoiCatalog`, `FootprintChunkPlan`, `TerrainPreparationService`, and `PoiPreparationState`.
- Produces: a tick-bounded POI workflow whose next action is explicit and testable independently of Minecraft world mutation.

- [ ] **Step 1: Write failing workflow transition tests**

Model decisions in a pure coordinator:

```java
@Test void requiredPoiChunksRunWhenBroadPregenerationIsDisabled() {
    var state = new PoiPreparationState();
    assertEquals(PoiPreparationWorkflow.Action.GENERATE_CHUNK,
        PoiPreparationWorkflow.next(state, 49));
}

@Test void samplingWaitsUntilEveryRequiredChunkIsFull() {
    var state = new PoiPreparationState();
    state.chunkCursor = 48;
    assertEquals(PoiPreparationWorkflow.Action.GENERATE_CHUNK,
        PoiPreparationWorkflow.next(state, 49));
    state.chunkCursor = 49;
    state.chunksComplete = true;
    assertEquals(PoiPreparationWorkflow.Action.SELECT_SURFACE,
        PoiPreparationWorkflow.next(state, 49));
}

@Test void placementCannotPrecedeTerrainAndVerificationCannotPrecedePlacement() {
    var state = selectedSurface(64);
    assertEquals(PoiPreparationWorkflow.Action.PREPARE_TERRAIN, PoiPreparationWorkflow.next(state, 9));
    state.terrainPrepared = true;
    assertEquals(PoiPreparationWorkflow.Action.PLACE_STRUCTURE, PoiPreparationWorkflow.next(state, 9));
    state.structurePlaced = true;
    assertEquals(PoiPreparationWorkflow.Action.VERIFY, PoiPreparationWorkflow.next(state, 9));
}
```

Also test `DONE` only after verification and reject inconsistent persisted states.

- [ ] **Step 2: Run workflow/status/location tests and verify failure**

```powershell
cd exodus-mod
.\gradlew test --tests dev.exodus.wasteland.arena.PoiPreparationWorkflowTest --tests dev.exodus.wasteland.arena.ArenaStatusFormatterTest --tests dev.exodus.wasteland.arena.ArenaLocationCatalogTest
```

Expected: FAIL because the coordinator and logical shared-height behavior do not exist.

- [ ] **Step 3: Implement the pure workflow coordinator**

```java
public final class PoiPreparationWorkflow {
    public enum Action { GENERATE_CHUNK, SELECT_SURFACE, PREPARE_TERRAIN, PLACE_STRUCTURE, VERIFY, DONE }
    public static Action next(PoiPreparationState state, int totalChunks) {
        if (totalChunks <= 0 || state.chunkCursor < 0 || state.chunkCursor > totalChunks) {
            throw new IllegalStateException("Invalid POI chunk progress");
        }
        if (state.chunksComplete && state.chunkCursor != totalChunks) {
            throw new IllegalStateException("POI chunks marked complete before cursor completion");
        }
        if ((state.terrainPrepared || state.structurePlaced || state.verified) && !state.surfaceSelected) {
            throw new IllegalStateException("POI progress depends on an unselected surface");
        }
        if ((state.structurePlaced || state.verified) && !state.terrainPrepared) {
            throw new IllegalStateException("POI structure progress depends on unprepared terrain");
        }
        if (state.verified && !state.structurePlaced) {
            throw new IllegalStateException("POI verification depends on structure placement");
        }
        if (state.verified) return Action.DONE;
        if (state.structurePlaced) return Action.VERIFY;
        if (state.terrainPrepared) return Action.PLACE_STRUCTURE;
        if (state.surfaceSelected) return Action.PREPARE_TERRAIN;
        if (state.chunksComplete) return Action.SELECT_SURFACE;
        return Action.GENERATE_CHUNK;
    }
}
```

Validation rejects cursor outside `[0,totalChunks]`, surface-dependent flags without `surfaceSelected`, placement without terrain, and verification without placement.

- [ ] **Step 4: Replace the old arena-wide terrain and placement loops**

After `POI_PLANNING`, transition to `POI_CHUNK_PREPARATION`. On each server tick:

1. Rebuild deterministic placements and `SurfacePoiCatalog`.
2. Select the first logical POI whose workflow action is not `DONE`.
3. For `GENERATE_CHUNK`, call `level.getChunk(x, z, ChunkStatus.FULL, true)` for at most `POI_CHUNKS_PER_TICK` chunks and persist the cursor after each success.
4. For `SELECT_SURFACE`, call `TerrainPreparationService.sample`, persist one shared `platformY`, and copy that Y to every part ID in `checkpoint.placementY` so existing location/TP output remains compatible.
5. For `PREPARE_TERRAIN`, mutate the whole logical footprint once and persist its report summary for logs before marking complete.
6. For `PLACE_STRUCTURE`, call `TemplatePlacementService.placeOnce` for every part at `new BlockPos(part.x(), platformY, part.z())`; only set `structurePlaced` after all part IDs are in the idempotency set.
7. Keep marker processing after structure placement, but process each existing part bounding box exactly once.
8. For `VERIFY`, call terrain verification for the logical footprint and then mark the POI verified.
9. Move to `FINAL_VALIDATION` only when every logical POI returns `DONE` and marker processing is complete.

Remove the old `placementY(ServerLevel, Entry)` and max-height sampling. Preserve configured Russian/American Y offsets through `SurfacePoi.yOffset()`.

- [ ] **Step 5: Add precise English status and logs**

Status reports the current logical POI, action, and chunk progress, for example:

```text
phase=POI_CHUNK_PREPARATION poi=russian_base chunks=12/49
phase=TERRAIN_PREPARATION poi=abandoned_camp_0 y=64 quorum=87/176
```

Successful sampling/preparation logs include POI ID, dimensions, chunk range, valid/unsupported samples, min/max support, chosen Y, quorum, cleared blocks, foundation blocks, deepest fill, and verification result. Exceptions already flow to the arena failure message; ensure messages name the POI and failing coordinate.

- [ ] **Step 6: Update location tests for a shared composite height**

Assert all four Russian part IDs resolve to the same platform Y and `[TP]` uses `platformY + 1`. Camps retain their own heights. Do not expose synthetic logical IDs as teleport targets unless a real placement entry exists.

- [ ] **Step 7: Run focused integration tests and commit**

Run the Step 2 command plus registry tests:

```powershell
cd exodus-mod
.\gradlew test --tests dev.exodus.wasteland.arena.PoiPreparationWorkflowTest --tests dev.exodus.wasteland.arena.ArenaRegistryTest --tests dev.exodus.wasteland.arena.ArenaStatusFormatterTest --tests dev.exodus.wasteland.arena.ArenaLocationCatalogTest
```

Expected: PASS.

```powershell
git add -- exodus-mod/src/main/java/dev/exodus/wasteland/arena/ArenaPreparationService.java exodus-mod/src/main/java/dev/exodus/wasteland/arena/ArenaStatusFormatter.java exodus-mod/src/main/java/dev/exodus/wasteland/arena/ArenaLocationCatalog.java exodus-mod/src/main/java/dev/exodus/wasteland/arena/PoiPreparationWorkflow.java exodus-mod/src/test/java/dev/exodus/wasteland/arena/PoiPreparationWorkflowTest.java exodus-mod/src/test/java/dev/exodus/wasteland/arena/ArenaStatusFormatterTest.java exodus-mod/src/test/java/dev/exodus/wasteland/arena/ArenaLocationCatalogTest.java
git commit -m "feat(wasteland): prepare surface pois incrementally"
```

### Task 7: Verify, package, deploy, and hand off manual acceptance

**Files:**
- Potential correction scope after a failing gate: exact files already named in Tasks 1-6
- Build output: `exodus-mod/build/libs/exodus-0.2.2.jar`
- Deploy: `mods/exodus-0.2.2.jar`

**Interfaces:**
- Consumes: the completed implementation and current instance configuration.
- Produces: a verified JAR installed into the active PrismLauncher instance plus an exact manual test script.

- [ ] **Step 1: Run the complete automated gate**

```powershell
cd exodus-mod
.\gradlew clean test build
```

Expected: `BUILD SUCCESSFUL` with all tests passing.

- [ ] **Step 2: Inspect the built JAR contracts**

```powershell
jar tf build/libs/exodus-0.2.2.jar | Select-String 'data/lostcities/dimension/lostcity.json|SurfacePoi|SurfacePlacementPolicy|PoiPreparationWorkflow'
```

Expected: all named resource/class entries are present.

- [ ] **Step 3: Copy the verified artifact and compare hashes**

```powershell
Copy-Item -LiteralPath 'build/libs/exodus-0.2.2.jar' -Destination '..\mods\exodus-0.2.2.jar' -Force
$built = (Get-FileHash -Algorithm SHA256 'build/libs/exodus-0.2.2.jar').Hash
$deployed = (Get-FileHash -Algorithm SHA256 '..\mods\exodus-0.2.2.jar').Hash
if ($built -ne $deployed) { throw 'Built and deployed JAR hashes differ' }
```

Expected: no exception and identical SHA-256 values.

- [ ] **Step 4: Confirm runtime configuration keeps broad pregeneration optional**

Inspect `../config/exodus-common.toml` and confirm `preparationChunksPerTick = 0` is allowed while `poiChunksPerTick` is a positive value. Do not overwrite unrelated user configuration. If the new key is absent, Forge will materialize its default on the next launch.

- [ ] **Step 5: Perform static diff review**

```powershell
git diff --check
git status --short
git log --oneline -8
```

Expected: no whitespace errors; only intended source/tests/resources plus pre-existing user-owned/untracked runtime logs remain.

- [ ] **Step 6: Provide the manual fresh-world acceptance script**

Tell the user to create a new world, then run:

```text
/exodus arena prepare
/exodus arena status
/exodus arena locations
/exodus arena tp russian_base_1
/exodus arena tp russian_base_4
/exodus arena tp american_base_1
/exodus arena tp abandoned_camp_0
/exodus arena tp occupied_camp_0
```

Acceptance requires: preparation reaches READY; the biome is plains-based Lost Cities wasteland with the `default` landscape and no glass spheres; every inspected faction part shares its composite platform; every camp is on its own platform; structure interiors are clear; stone support reaches solid terrain with no visible void; and starter bases still use their current working placement behavior.

- [ ] **Step 7: Commit any final test-only corrections, if and only if Step 1 required them**

Stage exact corrected files and use:

```powershell
git commit -m "fix(wasteland): satisfy surface placement verification"
```

Do not create an empty commit. Report automated evidence separately from the user's pending real-game acceptance.
