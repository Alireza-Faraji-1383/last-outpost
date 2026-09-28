# Project Exodus Wasteland Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a persistent Lost Cities-backed, pre-generated, single-use arena system with guaranteed Exodus POIs, marker-driven loot, lobby access control, and restart-safe match integration.

**Architecture:** Pure Java domain classes own grid selection, state transitions, progress thresholds, composite layout, sampling, and placement policy. Forge adapters own Lost Cities IMC/API access, incremental chunk tickets, NBT templates, terrain mutation, chest/entity placement, commands, teleports, and SavedData. Arena preparation is an idempotent persisted state machine; match start only consumes a fully ready arena.

**Tech Stack:** Java 17, Minecraft 1.20.1, Forge 47.4.10, Lost Cities 1.20-7.5.5 API, ForgeGradle 6, JUnit 5, vanilla data packs and loot tables.

**Spec:** `docs/superpowers/specs/2026-09-28-project-exodus-wasteland-design.md`

## Global Constraints

- Pin Lost Cities runtime dependency to `1.20-7.5.5`; do not add other Lost Cities add-ons.
- Use `lostcities:lostcity` for arenas and the Overworld for lobby/returns.
- Preserve the existing `baseStructureDepth=6` contract and `Rotation.NONE` for player bases.
- Never delete generated chunks, old arenas, structures, or user-authored NBT files.
- Keep all logs and player-facing text in English.
- Do not distribute the public Lost Cities JAR inside the private Exodus JAR.
- Use TDD for every new production behavior and run the full suite after every task.
- Do not claim real two-client verification without the user's result.

## Review Focus

- Restart at every preparation phase must resume without duplicating chunks, composite pieces, marker chests, or template entities; Task 3 persistence tests and Task 6 idempotency tests own this.
- A changed arena size must recompute non-overlapping grid spacing and pregeneration bounds; Task 2 table-driven tests own this.
- Missing/wrong-sized composite files must prevent `READY` before terrain mutation; Task 5 validation tests own this.
- Unauthorized dimension entry must return normal players while preserving operator bypass game modes and match membership; Task 8 policy tests own this.
- Lost Cities API unavailability or expensive city sampling must degrade to an explicit skipped guard, never crash or silently report coverage; Task 4 adapter/policy tests own this.

---

### Task 1: Pin Lost Cities dependency and add the Exodus profile

**Files:**
- Modify: `exodus-mod/build.gradle`
- Modify: `exodus-mod/gradle.properties`
- Modify: `exodus-mod/src/main/resources/META-INF/mods.toml`
- Create: `exodus-mod/src/main/resources/data/exodus/lostcities/profiles/exodus.json`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/profile/WastelandProfileContractTest.java`

**Interfaces:**
- Consumes: Lost Cities mod id `lostcities` and version `1.20-7.5.5`.
- Produces: compile-visible `mcjty.lostcities.api` interfaces, mandatory runtime dependency metadata, and a profile resource loaded by the Lost Cities profile callback.

- [ ] **Step 1: Write a failing profile contract test** that opens `data/exodus/lostcities/profiles/exodus.json`, asserts base profile `rarecities`, medium destruction values, and configured city-density inputs; also parse `mods.toml` and assert a mandatory `[7.5.5,7.5.6)` Lost Cities range.
- [ ] **Step 2: Run** `.\gradlew.bat test --tests dev.exodus.wasteland.profile.WastelandProfileContractTest` and confirm RED because the profile and dependency block do not exist.
- [ ] **Step 3: Add the exact development dependency** using a reproducible Maven coordinate or a documented `compileOnly` API artifact, add runtime dependency metadata, and add the profile JSON. Do not copy the public JAR into Exodus resources.
- [ ] **Step 4: Add `LostCitiesProfileRegistration`** using `InterModEnqueueEvent` and `ILostCities.GET_LOST_CITIES_PRE`; it calls `createProfile("exodus", "rarecities")` and applies only supported 7.5.5 profile setters verified by `javap`/source.
- [ ] **Step 5: Run the focused test and then** `.\gradlew.bat test`; both must pass before proceeding.
- [ ] **Step 6: Commit** `feat(wasteland): pin Lost Cities profile dependency`.

### Task 2: Arena geometry, grid allocation, and progress policy

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/domain/ArenaGeometry.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/domain/ArenaGrid.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/domain/ProgressMilestones.java`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/domain/ArenaGeometryTest.java`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/domain/ArenaGridTest.java`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/domain/ProgressMilestonesTest.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusConfig.java`

**Interfaces:**
- Produces: `ArenaGeometry.of(int arenaSize, int buffer, int safetyGap)`, `ArenaGrid.centerFor(long ordinal, int spacing)`, and `ProgressMilestones.crossed(int oldCompleted, int newCompleted, int total)`.

- [ ] **Step 1: Write failing table-driven tests** proving 2000/128/1024 yields 2256 generation size and 4096 spacing; 3000 yields 3256 and 5120; invalid negative values fail; sequential grid cells never overlap pregeneration bounds.
- [ ] **Step 2: Write failing milestone tests** proving 9-to-10 emits 10, 19-to-31 emits 20 and 30, repeated values emit nothing, and resume at 37 never re-emits 10/20/30.
- [ ] **Step 3: Run the three focused test classes** and confirm RED due to missing domain types.
- [ ] **Step 4: Implement immutable geometry/grid/progress types** with overflow-safe `long` intermediate math and deterministic square-spiral grid allocation away from the lobby origin.
- [ ] **Step 5: Add config values** `arenaSize=2000`, `pregenerationBuffer=128`, `arenaSafetyGap=1024`, `cityCoverageMinimumPercent=15`, `cityCoverageSampleStrideChunks=4`, `chunksPerTick`, camp count/distance ranges, terrain margin, faction-base Y offsets, and natural hostile-spawn switch.
- [ ] **Step 6: Run focused tests and the full suite**, then commit `feat(wasteland): add arena geometry policies`.

### Task 3: Persistent arena preparation state machine

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/arena/ArenaState.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/arena/ArenaPhase.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/arena/ArenaRecord.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/arena/ArenaRegistry.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/arena/PreparationCheckpoint.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusSavedData.java`
- Modify: `exodus-mod/src/test/java/dev/exodus/ExodusSavedDataTest.java`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/arena/ArenaRegistryTest.java`

**Interfaces:**
- Produces: one active preparation record, at most one ready arena, append-only consumed/failed/abandoned records, and NBT round trips with schema versioning.

- [ ] **Step 1: Write failing transition tests** for `PREPARING -> READY -> CONSUMED`, cancellation to `ABANDONED`, failure to `FAILED`, rejecting a second ready arena, and refusing consumption of incomplete records.
- [ ] **Step 2: Extend SavedData tests first** with old-save compatibility plus a fully populated arena/checkpoint round trip including phase, chunk cursor, sampled-city counters, placed component ids, last announced milestone, and initiator UUID.
- [ ] **Step 3: Run focused tests** and confirm expected RED failures.
- [ ] **Step 4: Implement state types and versioned NBT serialization**; unknown future enum values load the record as `FAILED` with a diagnostic rather than crashing the world.
- [ ] **Step 5: Implement restart recovery policy** that preserves preparation checkpoints, converts an interrupted placement sub-step to revalidation, and preserves existing match recovery behavior.
- [ ] **Step 6: Run focused and full tests**, then commit `feat(wasteland): persist arena preparation lifecycle`.

### Task 4: Lost Cities API bridge and city sampling

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/lostcities/LostCitiesBridge.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/lostcities/LostCitiesApiBridge.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/domain/CityCoverageSampler.java`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/domain/CityCoverageSamplerTest.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusMod.java`

**Interfaces:**
- Consumes: `ILostCities.getLostInfo(Level)` and `ILostChunkInfo.isCity()`.
- Produces: `LostCitiesBridge.cityChunk(ServerLevel level, int chunkX, int chunkZ)` returning `CITY`, `OUTSIDE`, or `UNAVAILABLE`; incremental `CityCoverageSampler` returns estimated percent and skip reason.

- [ ] **Step 1: Write failing pure sampler tests** for 15-percent acceptance, under-threshold rejection, deterministic stride coordinates, zero samples, API unavailability, and a configured operation-budget skip.
- [ ] **Step 2: Run focused tests** and confirm RED.
- [ ] **Step 3: Implement the pure sampler** without loading chunks and with a bounded number of API calls per tick.
- [ ] **Step 4: Implement the Forge bridge** acquired through Lost Cities IMC; report missing API/profile/dimension explicitly and never reflect into Lost Cities internals.
- [ ] **Step 5: Add an adapter contract test or compile fixture** proving the 7.5.5 methods used by the bridge resolve.
- [ ] **Step 6: Run focused/full tests and build**, then commit `feat(wasteland): sample Lost Cities arena coverage`.

### Task 5: Structure catalog and four-part faction-base geometry

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/structure/StructureCatalog.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/structure/CompositeDefinition.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/structure/CompositeValidator.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/domain/CompositeLayout.java`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/domain/CompositeLayoutTest.java`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/structure/CompositeValidatorTest.java`
- Modify: `exodus-mod/src/main/resources/data/exodus/structures/README.md`

**Interfaces:**
- Produces: exact Russian/American four-part definitions, camp-pool discovery, combined 57x20x60 bounds, and pre-mutation validation results.

- [ ] **Step 1: Write failing layout tests** asserting part offsets `(0,0,0)`, `(29,0,0)`, `(0,0,30)`, `(29,0,30)`, no gaps/overlaps, and combined 57x20x60 bounds.
- [ ] **Step 2: Write failing validator tests** for missing parts, each wrong expected dimension, an empty camp pool, and valid numbered camp discovery sorted numerically.
- [ ] **Step 3: Run focused tests** and confirm RED.
- [ ] **Step 4: Implement definitions/validation** with exact resource ids and readable English diagnostics; perform all validation before any block mutation.
- [ ] **Step 5: Document exact filenames, sizes, orientation, entity inclusion, and marker commands** in the structure README.
- [ ] **Step 6: Run focused/full tests**, then commit `feat(wasteland): validate composite faction structures`.

### Task 6: Idempotent terrain, template, entity, and marker placement

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/placement/PlacementPlan.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/placement/PlacementPolicy.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/placement/TerrainPreparationService.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/placement/TemplatePlacementService.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/loot/LootMarker.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/loot/LootMarkerProcessor.java`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/placement/PlacementPolicyTest.java`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/loot/LootMarkerTest.java`

**Interfaces:**
- Consumes: validated templates and persisted placement ids.
- Produces: deterministic POI plans, bounded terrain repairs, one-time entity placement, recognized marker replacement, and idempotent resume results.

- [ ] **Step 1: Write failing policy tests** for camp 2-block natural slope, bounded repair fallback, player-base aggressive clearing, POI overlap rejection, 120/150-block camp exclusions, opposing faction halves, and deterministic seeded counts/rotations.
- [ ] **Step 2: Write failing marker tests** for all nine exact metadata values, unknown Exodus marker rejection, non-Exodus marker preservation, and loot-table resource mapping.
- [ ] **Step 3: Write failing idempotency tests** proving completed composite parts, marker replacements, and entity batches are not repeated after a checkpoint reload.
- [ ] **Step 4: Implement pure planning and marker parsing**, run RED-to-GREEN focused tests, then implement Forge terrain/template adapters.
- [ ] **Step 5: For entities, validate every template entity id against the server registry before commit** and persist the entity-batch completion bit only after successful spawn; on restart, inspect the placement id before retrying.
- [ ] **Step 6: Run focused/full tests and build**, then commit `feat(wasteland): place restart-safe arena POIs`.

### Task 7: Data-driven chest loot and Lost Cities chest rebalance

**Files:**
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/general/common.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/general/standard.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/general/valuable.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/general/elite.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/food.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/weapons.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/medical.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/utility.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/tech.json`
- Create: `exodus-mod/src/main/resources/data/exodus/lostcities/conditions/chestloot.json`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/loot/LootTableContractTest.java`

**Interfaces:**
- Produces: nine valid vanilla loot tables and a Lost Cities condition override mapping ordinary buildings to bounded Exodus quality without changing chest density.

- [ ] **Step 1: Write a failing resource contract test** that parses every JSON file, resolves every item id/tag against a checked-in expected registry list, rejects teleporter unique-component ids, enforces elite absence from Lost Cities mappings, and checks weapons weights: ammunition greater than attachments, attachments greater than guns.
- [ ] **Step 2: Run focused tests** and confirm RED because resources do not exist.
- [ ] **Step 3: Inventory the installed pack's actual food, medical, utility, tech, ammunition, gun, and attachment registry ids** using a server data dump or existing authoritative TaCZ data; do not invent ids.
- [ ] **Step 4: Author the nine tables and Lost Cities condition resource** with shared one-time vanilla chest semantics and faction/camp tier assignments.
- [ ] **Step 5: Run resource tests, data generation validation if applicable, full tests, and build**, then commit `feat(wasteland): add balanced arena loot tables`.

### Task 8: Arena preparation service, commands, and operator progress

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/arena/ArenaPreparationService.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/arena/ChunkPreparationCursor.java`
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/arena/ArenaStatusFormatter.java`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/arena/ChunkPreparationCursorTest.java`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/arena/ArenaStatusFormatterTest.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusCommands.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusEvents.java`

**Interfaces:**
- Produces: `/exodus arena prepare|status|cancel`, incremental server-tick work, restart resume, 10-percent operator/console notifications, and a ready arena only after all gates pass.

- [ ] **Step 1: Write failing cursor tests** for complete inclusive buffered chunk coverage, deterministic resume cursor, no duplicate coordinates, arena-size changes, and completion count.
- [ ] **Step 2: Write failing formatter/notification tests** for every state/phase, skipped city guard, failed diagnostics, consumed counts, and milestone recipients.
- [ ] **Step 3: Run focused tests** and confirm RED.
- [ ] **Step 4: Implement the tick state machine** in this order: dependency/profile gate, candidate selection, city sampling, catalog validation, incremental pregeneration, POI planning, terrain preparation, templates/entities, markers, final validation, `READY`.
- [ ] **Step 5: Implement commands with permission level 2**; cancellation persists `ABANDONED`, status never mutates state, and prepare refuses a second active/ready arena.
- [ ] **Step 6: Run focused/full tests and build**, then commit `feat(wasteland): prepare arenas incrementally`.

### Task 9: Match consumption, lobby returns, and dimension access control

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/access/DimensionAccessPolicy.java`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/access/DimensionAccessPolicyTest.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/MatchManager.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusCommands.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusEvents.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusSavedData.java`

**Interfaces:**
- Consumes: one `READY` arena.
- Produces: atomic ready-to-consumed match start, Overworld lobby returns, unauthorized-entry bounce, and `/exodus dimension enter|leave` operator bypass preserving game mode.

- [ ] **Step 1: Write failing access-policy tests** covering idle normal entry rejection, active member admission, authorized spectator admission, unrelated player rejection, operator visit bypass, and operator non-membership.
- [ ] **Step 2: Write failing start-policy tests** proving no-ready abort, atomic `READY -> CONSUMED`, exact arena center/border use, and no arena consumption when player-base allocation fails.
- [ ] **Step 3: Run focused tests** and confirm RED.
- [ ] **Step 4: Refactor `MatchManager.start`** to always target `lostcities:lostcity`, allocate within the ready arena, aggressively clear player-base bounds, commit the arena only after successful preflight, and preserve the existing inventory/Ender Chest clearing contract.
- [ ] **Step 5: Change cleanup/pending returns to the Overworld lobby spawn** and implement dimension enter/leave plus unauthorized bounce without altering operator game modes.
- [ ] **Step 6: Run focused/full tests and build**, then commit `feat(wasteland): run matches in prepared arenas`.

### Task 10: Runtime packaging and bounded server verification

**Files:**
- Modify: `exodus-mod/tools/build-curseforge-pack.ps1`
- Modify: `exodus-mod/tools/test-curseforge-pack.ps1`
- Create: `docs/exodus-wasteland-operator-guide.md`
- Create: `docs/exodus-wasteland-manual-acceptance.md`

**Interfaces:**
- Produces: a private Exodus JAR plus a public CurseForge Lost Cities reference, operator instructions, and evidence for automated/runtime boundaries.

- [ ] **Step 1: Update pack tests first** to require exactly the pinned public Lost Cities project/file reference and to reject a bundled Lost Cities JAR in overrides.
- [ ] **Step 2: Run the pack test** and confirm RED before changing the builder.
- [ ] **Step 3: Update the builder/manifest** and document the exact NBT filenames, marker commands, arena workflow, progress, cancellation, failure recovery, and disk-retention behavior.
- [ ] **Step 4: Run** `.\gradlew.bat clean test build` and require exit code 0; inspect the produced JAR for profile, loot, metadata, and no embedded Lost Cities JAR.
- [ ] **Step 5: Run the dedicated server with a reduced test arena config** and verify Lost Cities/Exodus load, commands register, prepare reaches `READY`, restart resumes an interrupted preparation, one start consumes the arena, stop returns users to lobby, and logs contain no stack traces or duplicate-placement warnings.
- [ ] **Step 6: Build and validate the CurseForge pack**, copy only the private Exodus JAR to the instance `mods/`, and record that real two-client acceptance remains pending.
- [ ] **Step 7: Commit** `test(wasteland): verify packaged arena workflow`.

## Self-review record

- Spec coverage: every accepted decision maps to Tasks 1-10; exact loot contents are resolved from installed registry ids in Task 7 rather than guessed.
- Placeholder scan: no implementation step delegates unspecified error handling or contains an unresolved design choice.
- Type consistency: geometry feeds registry/cursors; registry feeds preparation; catalog/placement feeds readiness; readiness feeds match consumption.
- Review-focus coverage: restart idempotency, dynamic sizing, composite validation, access control, and API degradation each have an owning failing test step.
