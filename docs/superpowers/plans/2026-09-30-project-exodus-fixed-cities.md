# Project Exodus Fixed Cities Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan task-by-task. The user explicitly said to start and delegated implementation choices; execute inline without another approval round.

**Goal:** Give each of 64 fixed arenas one open-sky procedural city without searching for city locations or generating building spawners.

**Architecture:** Generate Lost Cities predefined-city resources from one versioned manifest at build time. Validate the manifest, profile binding, and unused region before allocating an arena. Preserve the existing incremental preparation workflow and dry-plains work.

**Tech Stack:** Java 17, Minecraft 1.20.1, Forge 47.4.10, Lost Cities 1.20-7.5.5, Gradle, JUnit 5.

**Spec:** docs/superpowers/specs/2026-09-30-project-exodus-fixed-cities-design.md

## Global constraints

- English logs and player text; no third-party mod changes or deletion of worlds.
- Keep city chests and existing loot behavior. Set Lost Cities GENERATE_SPAWNERS=false.
- Preserve all in-progress source changes copied into the isolated worktree.
- No merge/commit to main, push, or manual two-client acceptance claim.

## Review focus

- Existing biosphere mapping wins over registerDimension: replace only this dimension's mapping and clear its profile cache before binding.
- Larger predefined radius than neighborhood scan: cityMaxRadius must cover the influence radius.
- Old generated/loaded chunks: reject a used catalog region before creating a new record.
- Legacy persisted preparation: retain enum readability but fail explicitly before changing old terrain.
- Manifest exhaustion/config mismatch/missing assets: fail before allocation, never random fallback.

## Task 1: Versioned finite layout and build resources

Files: FixedCityLayout.java; data/exodus/city_layout.json; build.gradle; FixedCityLayoutTest.java; FixedCityResourcesTest.java.

- [x] Add tests for all 64 grid positions, bounds/coverage, exhausted ordinals, and incompatible geometry. Example: `assertThrows(IllegalStateException.class, () -> layout.arena(64));`.
- [x] Add resource acceptance tests for first/distant cities and referenced styles; run `gradlew.bat test --tests '*FixedCity*'` and observe missing layout/resources.
- [x] Implement immutable manifest parsing and build-time city generation. 69 neighborhood anchors, radius=240, extent=576, neighborhoodSpacing=128, offsetZ=-160, threshold=0.2, spacing=4096, arenaSize=2000, buffer=128, safetyGap=1024.
- [x] Supply residential/outskirts styles and a small deterministic set of tower anchors. Inherit existing palettes/parts; do not author unsupported fields.
- [x] Re-run focused tests and inspect generated resources.

## Task 2: Effective profile and no spawners

Files: WastelandProfileDefinition.java/test; LostCitiesIntegration.java; new DimensionProfileBinding.java/test.

- [x] Assert `CITY_CHANCE=0`, default landscape, zero spheres, compatible height range, disabled spawners, and unchanged chest/loot flags.
- [x] Assert mapping replacement preserves unrelated dimensions and removes duplicate mappings for lostcities:lostcity.
- [x] Run focused tests, implement direct verified 7.5.5 profile fields and config/cache binding, then re-run.
- [x] Log effective profile binding. Preserve delayed registration after Forge configuration is attached.

## Task 3: Safe preparation and open-land POIs

Files: RegionGenerationGuard.java/test; ArenaPreparationService.java; PreparationCheckpoint.java; ArenaRegistry.java/test; PlacementPolicy.java/test.

- [x] Test region headers for generated chunks inside/outside requested bounds without generating chunks.
- [x] Test layout-version persistence and legacy preparation rejection.
- [x] Validate manifest geometry, assets, active profile, loaded/disk chunks and finite ordinal before begin(). Replace candidate city sampling with fixed-layout verification.
- [x] Persist layout version; retain CITY_SAMPLING enum for compatibility and reject old preparations without moving their placements.
- [x] Move only new-layout faction bases to x=±500,z=800; exclude new-layout camps from the city disk plus structure margin. Preserve legacy positions.
- [x] Re-run targeted layout, region, persistence, and placement tests.

## Task 4: Runtime evidence, packaging and delivery

Files: EXODUS_TESTING.md; dedicated opt-in smoke source/config; plan progress ledger.

- [x] Run `gradlew.bat clean test build`.
- [x] Launch isolated fresh-world Forge server with exactly the existing Lost Cities dependency; verify profile, first/distant city assets, building heights, no spheres/spawners, and generation timing.
- [x] Inspect built JAR city/style/manifest resources. Fix any runtime defects with focused regression tests and repeat relevant gates.
- [x] Copy the verified Exodus JAR into the live mods directory with a recoverable backup of the previous Exodus JAR; reconcile the live dimension mapping without changing unrelated settings.
- [x] Report implemented behavior, concrete automated evidence, and missing visual/two-client verification. Link the isolated source and installed JAR; do not merge to main without a request.

## Execution ledger

Ruling: proceed directly from the user's explicit “start” instruction after writing this plan — repeated permission is contrary to the user's delegated scope; all mutations remain local and reversible.

Ruling: use an isolated native worktree and copy the existing uncommitted source snapshot into it — preserves dry-plains/placement work without changing main.

Baseline: `gradlew.bat test` passed before city implementation on the copied snapshot.


Implementation evidence: initial manifest/resource/profile/region tests failed before implementation and passed after. A new height-contract test failed against the inherited dimension type before adding the matching Exodus type. Runtime smoke first revealed unloaded heightmap sampling; reloading prepared footprints resolved minimum-height placement. The initial radius-928 layout was replaced with the documented neighborhood fallback after actual generation timing. Independent review found anchor styles did not propagate in 7.5.5; supported characteristics-event radial zoning and actual non-anchor style/floor assertions resolved it. Follow-up review reported no confirmed defect.

Fresh smoke6 passed both first/distant layout checks, 43.3107% sampled coverage each, 16 full chunks with no enabled sphere or spawner, used-region and legacy rejection, and real READY with four verified POIs/92 parts and markers. Broad pregeneration was disabled only for this diagnostic. The normal clean build excludes the opt-in subscriber. VersionContractTest caught pack-script version drift after the 0.2.3 bump; matching script references were updated, without building/uploading a public pack.

Delivery: 162 tests passed with zero failures/errors; normal Gradle test/build passed after the clean rebuild. JAR has 4416 city assets and zero smoke classes. Installed exodus-0.2.3.jar SHA256 921A71D23317F7546A5D6BCEE49A7918C8847DFB8320A1D002592C4EA561EA60. Previous JAR and Lost Cities common config backed up under live .exodus-backups/2026-09-30-fixed-cities. Live dimension mapping now uses exodus and preserves the abyss entry. Visual/full-modpack/two-client acceptance remains manual. No merge or commit to main.

