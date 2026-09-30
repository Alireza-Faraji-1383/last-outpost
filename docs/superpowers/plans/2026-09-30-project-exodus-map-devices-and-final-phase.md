# Match Map, Devices, and Final Phase Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans for native execution, or superpowers:subagent-driven-development if the user selects delegation. Steps use checkbox syntax for tracking.

**Goal:** Implement the approved personal JourneyMap view, match-wide boss bar, claimable devices, device respawn, and post-activation death elimination.

**Architecture:** Keep Forge match and teleporter services authoritative. Add a persistent match-session aggregate with pure policies, separate device/respawn/map services, and a recipient-filtered network projection. JourneyMap integration remains a client adapter with server permission enforcement; no hidden location is sent before authorization.

**Tech Stack:** Java 17, Minecraft 1.20.1, Forge 47.4.10, ForgeGradle, JUnit Jupiter 5.10.2, installed JourneyMap 6.0.6 with embedded JourneyMap API 2.0.0.

**Spec:** `docs/superpowers/specs/2026-09-30-project-exodus-map-devices-and-final-phase-design.md`

## Global Constraints

- All product text, logs, code, and documentation are English.
- Preserve existing unique components, slot layout/locking, countdown/radius/capacity snapshots, border restoration, placed structures, offline returns, and restart-to-IDLE recovery.
- Do not install/remove third-party mods or alter city generation in this work.
- Do not merge or commit to main without explicit user authorization. Use an isolated worktree for implementation; preserve the untracked approved design and this plan there.
- Gameplay thresholds belong in `ExodusConfig`: discovery horizontal 50, vertical 20; claim duration 20 seconds and radius 5; spawn exclusion radius 10; protection duration 10 seconds; match-day duration 24,000 ticks.
- Original-base devices cannot be claimed; activated devices cannot be claimed; device ownership never grants winner eligibility.
- Real two-client acceptance is manual and remains unverified until the user reports success.

## Review Focus

1. A new arena can be prepared while another runs: map coordinates must still come from the consumed arena (Task 2).
2. A device unloads or an inventory slot mutates without a menu click: progress must not lose installed components or force-load the world (Task 3).
3. A projectile launched during protection lands after it ends: player-attributed damage must follow the specified protection policy, with consistent treatment across damage hooks (Task 5).
4. Activation and claim completion occur in the same tick: activation wins and owner transfer cannot overwrite a locked device (Task 4).
5. Client disconnects and rejoins a new match: previous-match packets/overlays must not reveal or restore old markers (Tasks 6 and 7).

## File and interface ownership

New packages: `dev.exodus.session` (persistent session and clock), `dev.exodus.device` (identity, owners, claims, progress), `dev.exodus.map` (locations, discovery, projections), `dev.exodus.map.client` (JourneyMap rendering), and `dev.exodus.network` (map projection transport). Keep respawn/protection in the existing `dev.exodus.player` package.

The shared value contracts are:

```java
// session/MatchSessionState.java
UUID matchId(); UUID arenaId(); long elapsedTicks();
Set<UUID> roster(); Set<UUID> eliminated();
CompoundTag save(); static MatchSessionState load(CompoundTag tag);

// device/DeviceRecord.java -- immutable value; replacement is atomic
record DeviceRecord(UUID matchId, String dimension, long position,
                    UUID owner, UUID protectedOwner, int componentMask,
                    boolean active) {}

// map/MapLocation.java
record MapLocation(String id, String label, Kind kind, int x, int y, int z) {}
// Kind: CITY, RUSSIAN_BASE, AMERICAN_BASE, PLAYER_BASE,
// ABANDONED_CAMP, OCCUPIED_CAMP, DEVICE, ACTIVE_DEVICE, RARE_ITEM.

// map/MatchMapSnapshot.java -- no client-only Minecraft types
record MatchMapSnapshot(UUID matchId, long revision, String dimension,
                        int centerX, int centerZ, int borderSize,
                        boolean clear, List<MapLocation> locations) {}
```

Define all listed contracts before consumers use them; constructors validate non-null identity and bounded collection sizes. Persist only server-domain values, not JourneyMap display objects.

### Task 1: Verified integration boundary and persistent session

**Files:** Create `session/MatchSessionState.java`, `session/MatchClockPolicy.java`, `device/DeviceRecord.java`; modify `ExodusSavedData.java`, `ExodusConfig.java`, `build.gradle`; create tests under `src/test/java/dev/exodus/session/` and extend `ExodusSavedDataTest.java`.

**Consumes:** Existing `ExodusSavedData`, `TeleporterSavedState`, arena records, and match UUIDs.
**Produces:** Contracts above, defaults-safe session load/save, and a version-pinned JourneyMap compile dependency without bundling the third-party mod.

- [ ] Write failing session persistence and clock tests, including old saves and offline notices surviving session cleanup:

```java
@Test void dayChangesAfterExactly24000Ticks() {
    assertEquals(1, MatchClockPolicy.day(0, 24000));
    assertEquals(1, MatchClockPolicy.day(23999, 24000));
    assertEquals(2, MatchClockPolicy.day(24000, 24000));
}
@Test void legacySaveHasNoInventedCurrentMatch() {
    assertNull(ExodusSavedData.load(new CompoundTag()).session.matchId());
}
```

- [ ] Run `./gradlew.bat test --tests '*MatchClockPolicyTest' --tests '*ExodusSavedDataTest'` and confirm a missing-contract failure before implementing.
- [ ] Implement day calculation `1 + elapsedTicks / dayTicks`; reject negative elapsed time or nonpositive day length. Add session NBT with schema version, roster/eliminations, exact arena identity, device index, selected spawns, discoveries, protection deadlines, and monotonic projection revision. Keep pending notifications separately so match cleanup cannot delete undelivered notices.
- [ ] Inspect installed API classes with `javap` before selecting coordinates. The embedded API is `META-INF/jarjar/journeymap-api-forge-1.20.1-2.0.0.jar`; extract it to the build directory using a Gradle task, or resolve the exact matching published artifact. Use ForgeGradle deobfuscation where required, and verify the resulting runtime JAR does not contain JourneyMap classes. Add no external mod to `mods/`.
- [ ] Compile an adapter against verified signatures: `@JourneyMapPlugin(apiVersion="2.0.0")`, `IClientPlugin.initialize(IClientAPI)`, `getModId()`, `IClientAPI.show(Displayable)`, `remove(Displayable)`, `removeAll(String)`. Verify default annotation parameters and `Context.UI` constants by `javap`, not guesswork.
- [ ] Run the focused tests and `./gradlew.bat compileJava`. Record the dependency choice and actual radar permission path in the implementation notes.

### Task 2: Exact arena locations and committed device registration

**Files:** Create `map/MapLocation.java`, `map/ArenaMapCatalog.java`, `device/DeviceIndex.java`; modify `ArenaPreparationService.java`, `ArenaLocationCatalog.java`, `MatchManager.java`, `ExodusTeleporterBlockEntity.java`, `TemplatePlacementService.java`; test `map/ArenaMapCatalogTest.java` and `device/DeviceIndexTest.java`.

**Consumes:** `ArenaRecord`, placement plan/heights, committed `PlayerBaseData`, Task 1 session.
**Produces:** `ArenaMapCatalog.locations(ArenaRecord)` and `DeviceIndex.register(DeviceRecord)`; `DeviceIndex.devices(UUID owner)` returns current-match records without chunk loads.

- [ ] Write a failing test constructing two arena records with distinct centers; assert a catalog requested for the older consumed arena never uses newer coordinates. Test composite faction parts produce one base marker rather than one marker per part. Test original-base devices keep protected owners on index reload.
- [ ] Run `./gradlew.bat test --tests '*ArenaMapCatalogTest' --tests '*DeviceIndexTest'` and confirm failure.
- [ ] Add an explicit-arena location helper rather than use the latest-record helper. Persist a canonical location snapshot at successful commit. City marker center comes from the verified fixed-city manifest when that feature is present; otherwise resolve real center data from installed Lost Cities API. Do not implement fixed-city generation or assume center offsets for legacy arenas. Record a real center before publishing `City`.
- [ ] Register devices while actual structure blocks are placed and during chunk/block-entity load reconciliation. Register starter-base devices with their committed owner before exposing RUNNING. Limit any reconciliation scan to known structure bounds, not arbitrary world chunks. Device indexing does not add or relocate structure blocks.
- [ ] Reconcile component masks on mutation and block-entity load. Use immutable `DeviceRecord` replacement keyed by current match/dimension/position. Verify unload keeps the last valid server-owned inventory mask; verified removal removes the record and discovery marker.
- [ ] Run focused tests and compile. Inspect a prepared structure location listing against the catalog without modifying the existing save.

### Task 3: Personal component progress, match clock, and unified boss bar

**Files:** Create `device/ComponentProgressPolicy.java`, `session/MatchBossBarService.java`; modify `TeleporterService.java`, `MatchManager.java`, `ExodusTeleporterBlockEntity.java`; test `device/ComponentProgressPolicyTest.java`, `session/MatchBossBarPolicyTest.java`.

**Consumes:** Current-match inventory components, indexed owned devices, elapsed match ticks, active countdown.
**Produces:** `ComponentProgressPolicy.bestCount(int inventoryMask, List<Integer> ownedDeviceMasks)` and `MatchBossBarService.tick(MinecraftServer)` / `cleanup(MinecraftServer)`.

- [ ] Write failing unique-union tests:

```java
@Test void neverCombinesTwoDevices() {
    assertEquals(2, ComponentProgressPolicy.bestCount(1, List.of(2, 4)));
    assertEquals(3, ComponentProgressPolicy.bestCount(1, List.of(1, 6)));
}
@Test void duplicatesDoNotIncreaseProgress() {
    assertEquals(1, ComponentProgressPolicy.bestCount(1, List.of(1)));
}
```

Add stale-match exclusion at inventory-to-mask conversion, inventory-only counting, offhand counting, and unchanged cached masks across chunk unload.

- [ ] Run focused tests and confirm failure; implement `max(bitCount(inventoryMask), bitCount(inventoryMask | eachOwnedMask))`, restricting bits to the nine component types and ownership to the current match.
- [ ] At successful commit set `level.setDayTime(0)` and elapsed counter 0; leave `gameTime` unchanged. Increment once per running server tick. Create one personal boss bar per associated viewer; reconcile audiences without removing/readding every viewer every tick.
- [ ] Before activation show personal K/9 or spectator empty-day bar. After activation show day, coordinates, and the existing captured countdown; remove the old independent teleporter boss bar. Owner transfer, death, inventory mutation, login, and cleanup trigger refresh. Boss bar construction never activates a device.
- [ ] Run tests and compile; check failed allocation does not reset time or expose a boss bar.

### Task 4: Atomic claims, persistent ownership, notifications, and device controls

**Files:** Create `device/ClaimPolicy.java`, `device/DeviceOwnershipService.java`; modify `ExodusTeleporterMenu.java`, `ExodusTeleporterScreen.java`, `TeleporterService.java`; test `device/ClaimPolicyTest.java`, `device/DeviceOwnershipServiceTest.java`.

**Consumes:** Device index, active-player eligibility, server tick, match identity.
**Produces:** `DeviceOwnershipService.requestClaim(ServerPlayer, BlockPos)`, `setSpawn(ServerPlayer, BlockPos)`, `tick(MinecraftServer)`, `cancelFor(UUID)`; menu button IDs 0=`Claim Device`, 1=`Set Spawnpoint` validated server-side.

- [ ] Write failing tests for protected device rejection, two claimants/one device, 399 versus 400 ticks, inclusive radius, offline transfer, queued notices, death/logout/range cancellation, and activation in the completion tick. Model transitions with record inputs rather than mock client UI.
- [ ] Run focused tests, implement claims on the server thread. Re-check eligibility, exact indexed identity, protected owner, radius, and active state immediately before transfer. Process activation before claim completion. A no-op claim of one's own device does not notify a fictitious owner transfer.
- [ ] Notify owner on start and successful transfer. Store undelivered notices outside session teardown data. On transfer, reset the former owner's selected spawn only if it selected this device; preserve all other owned devices and inventory masks.
- [ ] Use vanilla menu button transport (`handleInventoryButtonClick` / `clickMenuButton`) with correct container ID and server menu validation. Add synchronized button flags via menu data slots. Keep the existing 3x3 slot coordinates and textures; place controls outside slot hitboxes. Fix client `stillValid` so its server-player check does not invalidate the client menu.
- [ ] Show action-bar elapsed claim seconds and English chat outcome. Allow inventory use during claims. Disable Claim on protected/active devices and Spawnpoint after any teleporter activation, with server rejection even for forged button requests.
- [ ] Run tests and compile; check no new screen/network dependency enters the dedicated-server classloader.

### Task 5: Device respawn, obstruction invariants, and damage protection

**Files:** Create `player/DeviceRespawnPolicy.java`, `player/MatchPlayerEvents.java`, `player/DamageProtectionPolicy.java`; modify `RespawnService.java`, `MatchManager.java`, `ExodusEvents.java`; test the two policies and player-state recreation/persistence.

**Consumes:** Selected owned device, protected original-base device, active-player positions, protection deadlines, final-phase flag.
**Produces:** `RespawnService.selectMatchSpawn(ServerPlayer)`, `DamageProtectionPolicy.blocks(long now, long protectedUntil)`, event handlers that preserve session state across player recreation.

- [ ] Write failing tests for an occupied selected device, original-base exemption, spectator exclusion, distance exactly 10 versus beyond 10, fallback preserving selected device, ownership loss resetting selection, and protection expiring exactly at 200 ticks:

```java
@Test void protectionExpiresAtItsDeadline() {
    assertTrue(DamageProtectionPolicy.blocks(199, 200));
    assertFalse(DamageProtectionPolicy.blocks(200, 200));
}
```

- [ ] Verify Forge event signatures from mapped 1.20.1 sources: death, player recreation/respawn, sleeping, spawn-setting, incoming attacks/hurt, and projectile/player attribution. Select the device target at death before vanilla dimension/spawn resolution; do not simply teleport after an incorrect cross-dimension respawn.
- [ ] Implement respawn exactly above the chosen device, with default protected original target and a per-death occupancy fallback. Enforce usable headroom during match interaction; do not equate indestructibility with air above. Reject placements/fluids that obstruct reserved respawn headroom, and safely clear any already-obstructed reserved spawn cells when resolving a respawn, without deleting the device.
- [ ] Block sleeping and bed/anchor spawn changes only for associated match users while RUNNING; preserve ordinary-world behavior. Use an internal service guard so these events do not reject Exodus's own intentional spawn assignment/reset.
- [ ] Apply protection at commit and playable respawn; cancel all incoming damage and player-attributed damage to others while protection is active. Check both attacker and victim deadlines at impact; projectiles do not retain a permanent protection tag after the deadline. Death-to-spectator recreation receives no playable protection.
- [ ] Keep deadlines on server session state across logout/recreation; login never grants a new window. Run tests and compile, including direct, projectile, explosion-attributed, and environmental damage cases.

### Task 6: Permanent final-phase elimination and complete lifecycle cleanup

**Files:** Create `session/FinalPhasePolicy.java`; modify `MatchManager.java`, `TeleporterService.java`, `MatchPlayerEvents.java`, `ExodusSavedData.java`; test `session/FinalPhasePolicyTest.java` and extend lifecycle/persistence tests.

**Consumes:** Persistent roster/eliminations, existing associations/pending grace, first active teleporter, claims/protection/map cleanup hooks.
**Produces:** `FinalPhasePolicy.allDead(Set<UUID> roster, Set<UUID> eliminated)` and a single server-authoritative transition to final phase.

- [ ] Write failing tests:

```java
@Test void leavingIsNotDeathElimination() {
    UUID a = UUID.randomUUID(), b = UUID.randomUUID();
    assertFalse(FinalPhasePolicy.allDead(Set.of(a, b), Set.of(a)));
    assertTrue(FinalPhasePolicy.allDead(Set.of(a, b), Set.of(a, b)));
    assertFalse(FinalPhasePolicy.allDead(Set.of(), Set.of()));
}
```

Also test sole-survivor non-victory, pending return, eliminated reconnect, duplicate death delivery, and death coinciding with activation. Use server-thread event order: deaths before activation use pre-final respawn; deaths after committed activation eliminate.

- [ ] Preserve the immutable original roster even when grace expiry changes/removes active associations. Elimination removes grace eligibility, keeps spectator association for cleanup/map audience, cancels claiming, and persists before player recreation. Login and dimension events cannot reinstate an eliminated participant.
- [ ] Trigger immediate no-winner ending only for all-dead roster. Keep existing no-active/no-pending and countdown-no-winner endings distinct. Do not award victory because only one player remains.
- [ ] Centralize cleanup called by end/stop/failed commit/recovery: release bars, claims, protection, overlays, devices and session-private state; reset respawn to current Overworld spawn online or once on next login. Preserve placed structures, arena history, personal JourneyMap waypoints, and undelivered owner notices.
- [ ] Run policy and persistence tests; simulate save/restart of final-phase state and confirm recovery to IDLE rather than resumption.

### Task 7: Recipient projections, discovery, dropped items, and JourneyMap rendering

**Files:** Create `map/DiscoveryPolicy.java`, `map/MatchMapSnapshot.java`, `map/MapProjectionPolicy.java`, `map/MatchMapService.java`, `network/ExodusNetwork.java`, `network/MatchMapPacket.java`, `map/client/JourneyMapAdapter.java`, `map/JourneyMapRadarPolicy.java`; modify `ExodusMod.java`, `ComponentLifecycleEvents.java`, lifecycle hooks; add map/network tests and icon resources under `assets/exodus/textures/map/`.

**Consumes:** Exact arena catalog, device owners, persistent discoveries, eligible viewers, indexed live rare-item entities, Task 6 lifecycle.
**Produces:** `MapProjectionPolicy.project(UUID viewer, boolean active, Set<String> discovered, List<MapLocation> publicLocations, List<MapLocation> privateLocations)`; `MatchMapService.tick(MinecraftServer)` / `clear(MinecraftServer)`; `JourneyMapAdapter.apply(MatchMapSnapshot)`.

- [ ] Write failing discovery tests at horizontal 50 and vertical 20 boundaries, diagonal horizontal distance, deep tunnels, and spectator exclusion. Write privacy tests proving an undiscovered private location never enters a snapshot and an eliminated player retains only previous discoveries/owned data.
- [ ] Add snapshot decode bounds, revision/match identity, disconnect clearing, and delayed prior-session packet tests. Protect session establishment with an explicit ordered reset/epoch packet before incremental snapshots; UUID comparison alone cannot decide which different match is newer.
- [ ] Implement map projections server-side. Update discovering eligible players on a bounded tick schedule; keep markers when they leave range. Track rare item entity lifecycle by UUID and current match, sending live positions while ground item entities exist; remove on pickup/discard/unload and re-publish only on verified entity load. Do not force-load unloaded item chunks.
- [ ] Implement one Forge SimpleChannel registered during mod initialization, using S2C map snapshots with recipient delivery and bounded payloads. Register client handlers through a client-only bridge; decode types contain no JourneyMap/client references. Send no original private catalog to clients.
- [ ] Implement API v2 client markers using verified `MarkerOverlay`, `MapImage`, and `Overlay.setActiveUIs(...)`. Use stable Exodus IDs, typed icon colors, anonymous discovered labels, and a prominent active-device marker. Build the red border polygon and a translucent exterior polygon with a zone hole over the finite arena mapping extent. Use `IClientAPI.removeAll("exodus")` only for Exodus-owned overlays; never delete ordinary waypoint data. Reconcile equal snapshots without recreating unchanged overlays.
- [ ] Verify normal and expanded radar permission evaluation using installed JourneyMap bytecode/source. Set dimension-level `playerRadarEnabled=false` and name radar false, apply global/operator paths if necessary, and send refreshed permissions through verified `PermissionsManager.sendPermissions(ServerPlayer)`. Preserve unrelated map options. Tests must include operator and reconnect paths, not only non-op normal radar.
- [ ] On stop/reset/disconnect release client overlays; on reconnect deliver a fresh authorized snapshot. If JourneyMap is missing, log a clear optional-integration warning while keeping game lifecycle functional. Run map tests and compile the dedicated-server path.

### Task 8: Full verification, package inspection, and manual acceptance handoff

**Files:** Modify this plan's checkboxes; create `docs/verification/2026-09-30-map-devices-final-phase.md` containing actual evidence and remaining manual checks.

**Consumes:** Completed Tasks 1–7 and the approved design.
**Produces:** Tested Exodus runtime artifact and honest verification report.

- [ ] Run `./gradlew.bat test build` in `exodus-mod`; inspect all output and resolve failures before repeating only affected tests and final gate.
- [ ] Inspect the built JAR for map icons, mod metadata, network classes, and absence of embedded JourneyMap mod/API copies. Verify no accidental third-party mod changes or world modifications in the diff.
- [ ] Run a fresh test-world dedicated-server smoke with installed Forge/JourneyMap: arena identity, one active destination, claims, offline notices, death/grace distinctions, stop/restart cleanup, and saved border/respawn reset. Record precise commands and observed results.
- [ ] Run a real client check for personal markers, fogged terrain, zone overlay, rare-item movement/pickup, two buttons, action bar, permanent boss bar, respawn above devices, obstruction handling, and radar settings. A one-client check cannot prove cross-player privacy or multiplayer outcomes.
- [ ] Review the whole diff against the spec, emphasizing no hidden-location packets, no active-owner race, unchanged winner proximity, no rejoin bypass, and persistent offline cleanup. Use independent review only if the user chooses/authorizes delegation; otherwise review inline.
- [ ] Install only the newly built Exodus JAR in `mods/` when its implementation checkout is ready for user testing; retain third-party mods. Do not claim a launched game loaded the replacement until a fresh launch confirms it. Do not merge/commit to main without explicit authorization.
- [ ] Give the user a concise two-client checklist: independently discover a camp; claim/steal a public device; confirm protected base rejection; choose/contest spawn; test 10-second damage symmetry; activate; die and reconnect as spectator; confirm sole survivor still needs teleport; all-dead ending; stop and verify world-spawn reset. Label these pending until the user's report.

## Plan self-review and execution handoff

All design sections map to Tasks 1–8. The fixed-city generation feature remains separate; this work consumes its verified center when available. Actual JourneyMap 6.0.6 inspection confirmed embedded API 2.0.0, client overlays, per-player server waypoints, server polygons, and dimension permission fields. Do not confuse the server polygon-only overlay API with the client marker API.

Recommended execution is native in an isolated worktree because the services share session/device interfaces and one authoritative lifecycle. Preserve the user's approval of the design; the next gate is review of this concrete plan and selection of native versus delegated execution, as required by the invoked writing-plans skill.
