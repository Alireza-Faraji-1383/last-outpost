# Project Exodus Teleporter Victory Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a persistent nine-component Exodus Teleporter that starts a visible 20-minute countdown and ends the global match by teleporting and announcing the nearest eligible players.

**Architecture:** A pure `teleporter.domain` layer owns slot validity, unique claims, activation, countdown, and winner selection. Forge adapters under `teleporter` own registries, block/menu UI, item lifecycle, persistence, Boss Bar and chunk tickets; `MatchManager` exposes narrow lifecycle hooks and remains the authority for player association and match cleanup.

**Tech Stack:** Java 17, Minecraft 1.20.1 official mappings, Forge 47.4.10, Forge registries/events/menus, JUnit 5, JSON recipes/models/loot tables.

**Spec:** `docs/superpowers/specs/2026-09-28-project-exodus-teleporter-victory-design.md`

## Global Constraints

- Keep Forge 47.4.10 and Minecraft 1.20.1; do not add or remove third-party mods.
- All player-facing text and logs are English.
- One global match remains authoritative through `ExodusSavedData` and `MatchManager`.
- Never claim the real two-client multiplayer acceptance test passed until the user reports it.
- Do not implement the facility structures or final enemy/event that distributes the three rare components.
- Keep the existing `kubejs/data/exodus/structures/starter_base.nbt` untouched; the user places the teleporter manually.
- Use exact config defaults: 1,200 seconds, 5.0 blocks, capacity 2.

## Review Focus

- Two players inserting the ninth component on different teleporters in the same tick must produce exactly one active countdown; Task 6 adds an activation-race policy test.
- A stale or unbound stack must never become valid merely by moving through a container; Tasks 2 and 5 test explicit bind transitions and rejection.
- Stop/recovery with an unloaded teleporter must clear Boss Bar/tickets immediately and clear block contents lazily; Tasks 2 and 6 test persistence decisions and idempotent cleanup.
- Exact-radius and exact-distance ties must be deterministic; Task 1 tests inclusive radius and UUID tie-breaking.
- Inventory clearing must occur only after successful placement preflight; Task 2 introduces and tests a pure commit-order policy before wiring the destructive clear.

---

### Task 1: Pure Teleporter Domain Policies

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/domain/TeleporterComponent.java`
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/domain/ComponentStackState.java`
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/domain/ComponentPolicy.java`
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/domain/RareClaimPolicy.java`
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/domain/ActivationPolicy.java`
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/domain/EscapeCandidate.java`
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/domain/WinnerPolicy.java`
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/domain/CountdownSnapshot.java`
- Test: `exodus-mod/src/test/java/dev/exodus/teleporter/domain/ComponentPolicyTest.java`
- Test: `exodus-mod/src/test/java/dev/exodus/teleporter/domain/RareClaimPolicyTest.java`
- Test: `exodus-mod/src/test/java/dev/exodus/teleporter/domain/ActivationPolicyTest.java`
- Test: `exodus-mod/src/test/java/dev/exodus/teleporter/domain/WinnerPolicyTest.java`
- Test: `exodus-mod/src/test/java/dev/exodus/teleporter/domain/CountdownSnapshotTest.java`

**Interfaces:**
- Produces: `TeleporterComponent.forSlot(int)`, `ComponentPolicy.accepts(...)`, `RareClaimPolicy.claim(...)`, `ActivationPolicy.evaluate(...)`, `WinnerPolicy.select(...)`, and `CountdownSnapshot.progress(long)`.
- Consumes: only Java records, collections, UUID, and primitive values.

- [ ] **Step 1: Write failing component and claim tests**

```java
@Test void rejectsAValidComponentBoundToAnotherMatch() {
    UUID current = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UUID stale = UUID.fromString("00000000-0000-0000-0000-000000000002");
    assertFalse(ComponentPolicy.accepts(0, TeleporterComponent.REINFORCED_FRAME,
            new ComponentStackState(stale, false), current));
}

@Test void onlyFirstTemplateClaimOfEachRareTypeSucceeds() {
    var first = RareClaimPolicy.claim(false, true);
    var second = RareClaimPolicy.claim(true, true);
    assertEquals(RareClaimPolicy.Result.CLAIM, first);
    assertEquals(RareClaimPolicy.Result.DELETE_DUPLICATE, second);
}
```

- [ ] **Step 2: Run the focused tests and confirm RED**

Run: `gradlew.bat test --tests "dev.exodus.teleporter.domain.ComponentPolicyTest" --tests "dev.exodus.teleporter.domain.RareClaimPolicyTest"`

Expected: compilation failure because the domain types do not exist.

- [ ] **Step 3: Implement the fixed slot enum and validity/claim policies**

```java
public enum TeleporterComponent {
    REINFORCED_FRAME(0, false), POWER_REGULATOR(1, false), PHASE_COIL(2, false),
    FACILITY_ALPHA_KEY(3, true), DIMENSIONAL_CORE(4, true), FACILITY_BETA_KEY(5, true),
    SIGNAL_PROCESSOR(6, false), SPATIAL_LENS(7, false), CONTAINMENT_MODULE(8, false);
    // immutable slot lookup; duplicate slots fail construction
}
```

`ComponentPolicy.accepts` must require exact slot/type, current match UUID, and a non-template bound stack. `RareClaimPolicy` must return `NOT_RARE`, `CLAIM`, `ALREADY_BOUND`, or `DELETE_DUPLICATE` from explicit inputs.

- [ ] **Step 4: Run the focused tests and confirm GREEN**

Run the command from Step 2. Expected: all focused tests pass.

- [ ] **Step 5: Write failing activation, countdown, and winner tests**

```java
@Test void selectsNearestTwoInsideInclusiveRadiusWithStableTieBreak() {
    UUID a=UUID.fromString("00000000-0000-0000-0000-000000000001");
    UUID b=UUID.fromString("00000000-0000-0000-0000-000000000002");
    UUID c=UUID.fromString("00000000-0000-0000-0000-000000000003");
    var candidates=List.of(new EscapeCandidate(c, 25.0, true),
            new EscapeCandidate(b, 4.0, true), new EscapeCandidate(a, 4.0, true));
    assertEquals(List.of(a,b), WinnerPolicy.select(candidates, 5.0, 2));
}

@Test void expiryWithoutEligiblePlayersProducesNoWinners() {
    assertTrue(WinnerPolicy.select(List.of(new EscapeCandidate(UUID.randomUUID(), 1.0, false)), 5, 2).isEmpty());
}

@Test void capturedSettingsDoNotChangeWhenExternalConfigChanges() {
    var snapshot=new CountdownSnapshot(100, 1200, 5.0, 2);
    assertEquals(0.5, snapshot.progress(12_100), 0.0001);
    assertFalse(snapshot.expired(12_099));
    assertTrue(snapshot.expired(24_100));
}
```

- [ ] **Step 6: Run the new focused tests and confirm RED**

Run: `gradlew.bat test --tests "dev.exodus.teleporter.domain.ActivationPolicyTest" --tests "dev.exodus.teleporter.domain.WinnerPolicyTest" --tests "dev.exodus.teleporter.domain.CountdownSnapshotTest"`

Expected: compilation failure for the missing policies.

- [ ] **Step 7: Implement activation, countdown, and winner policies**

`ActivationPolicy.evaluate` returns `INCOMPLETE`, `ACTIVATE`, or `REJECT_SECOND_DEVICE`. `WinnerPolicy` filters `eligible`, compares squared distance to `radius * radius` inclusively, orders by distance then UUID, and limits by capacity. `CountdownSnapshot` uses server ticks and clamps progress to `[0,1]`.

- [ ] **Step 8: Run the complete test suite and commit**

Run: `gradlew.bat test`

Commit: `feat(exodus): define teleporter victory policies`

---

### Task 2: Saved State, Configuration, and Safe Match Commit

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/domain/StartCommitPolicy.java`
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/TeleporterSavedState.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusConfig.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusSavedData.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/MatchManager.java`
- Test: `exodus-mod/src/test/java/dev/exodus/teleporter/domain/StartCommitPolicyTest.java`
- Test: `exodus-mod/src/test/java/dev/exodus/ExodusSavedDataTest.java`

**Interfaces:**
- Consumes: Task 1 component enum and countdown snapshot.
- Produces: saved rare-claim set, active teleporter record, invalidated match UUIDs, `MatchManager.endWithWinners(...)`, and `MatchManager.associatedOnlinePlayers(...)`.

- [ ] **Step 1: Write failing persistence and commit-order tests**

```java
@Test void inventoryClearIsAllowedOnlyAfterEveryPreflightStep() {
    assertFalse(StartCommitPolicy.mayClearInventories(true,true,false));
    assertTrue(StartCommitPolicy.mayClearInventories(true,true,true));
}

@Test void oldSaveWithoutTeleporterFieldsLoadsEmptyState() {
    ExodusSavedData data=ExodusSavedData.load(new CompoundTag());
    assertNull(data.teleporter.active());
    assertTrue(data.teleporter.rareClaims().isEmpty());
}
```

- [ ] **Step 2: Run focused tests and confirm RED**

Run: `gradlew.bat test --tests "dev.exodus.teleporter.domain.StartCommitPolicyTest" --tests "dev.exodus.ExodusSavedDataTest"`

- [ ] **Step 3: Add config and version-tolerant NBT state**

Add `TELEPORTER_COUNTDOWN_SECONDS` (`1200`, range `1..86400`), `TELEPORTER_VICTORY_RADIUS_MILLIBLOCKS` (`5000`, range `100..64000`) and `TELEPORTER_CAPACITY` (`2`, range `1..8`) under `teleporter`. Persist match UUID, dimension, packed block position, activation/deadline ticks, captured radius/capacity, rare claim names, and invalidated match UUIDs.

- [ ] **Step 4: Move match UUID assignment before final commit and clear inventories once**

In `AllocationJob.commit`, perform all structure placement/spawn validation first. Then assign a fresh UUID, clear each selected player's inventory/armor/offhand via `getInventory().clearContent()` and Ender Chest via `getEnderChestInventory().clearContent()`, teleport, and set `RUNNING`. Do not clear during allocation or any earlier failure branch.

Expose an idempotent end method that accepts winner UUIDs and announcement text, teleports winners through the existing spawn helper, broadcasts names to associated players, then runs ordinary cleanup exactly once.

- [ ] **Step 5: Run tests and compile**

Run: `gradlew.bat test`

Run: `gradlew.bat compileJava`

- [ ] **Step 6: Commit**

Commit: `feat(exodus): persist teleporter match state`

---

### Task 3: Registry, Components, Creative Tab, Models, and Recipes

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/ExodusTeleporterRegistry.java`
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/item/TeleporterComponentItem.java`
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/item/ComponentStacks.java`
- Create: `exodus-mod/src/main/resources/data/exodus/recipes/{reinforced_frame,power_regulator,phase_coil,signal_processor,spatial_lens,containment_module}.json`
- Create: `exodus-mod/src/main/resources/assets/exodus/models/item/*.json`
- Create: `exodus-mod/src/main/resources/assets/exodus/lang/en_us.json` additions
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusMod.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/supply/ExodusSupplyRegistry.java`
- Test: `exodus-mod/src/test/java/dev/exodus/teleporter/item/ComponentStacksTest.java`
- Test: `exodus-mod/src/test/java/dev/exodus/teleporter/domain/RecipeBillTest.java`

**Interfaces:**
- Consumes: Task 1 component enum; Task 2 current match UUID/claim state.
- Produces: registry objects for nine component items and a `Project Exodus` creative tab; `ComponentStacks.bind`, `matchId`, `isTemplate`, and `isCurrent`.

- [ ] **Step 1: Write failing NBT transition and recipe-bill tests**

Test unbound Creative stacks, bound current/stale stacks, and template rare stacks. Add a literal recipe bill asserting one full set requires two Blaze Rods total (one direct and one converted for an Eye), five Ender Pearls, and fourteen Nether Quartz equivalents.

- [ ] **Step 2: Run focused tests and confirm RED**

Run: `gradlew.bat test --tests "dev.exodus.teleporter.item.ComponentStacksTest" --tests "dev.exodus.teleporter.domain.RecipeBillTest"`

- [ ] **Step 3: Register items and dedicated creative tab**

Use a separate deferred register for blocks, items, block entities, menus and `CreativeModeTab`. Populate the tab in the agreed order with existing supply objects followed by teleporter and all nine components. Remove additions to vanilla tabs to avoid duplicates.

- [ ] **Step 4: Add the six exact shaped recipes and item models**

Encode the approved 3x3 ingredient layouts literally. Use `minecraft:item/generated` models backed by distinct existing vanilla textures for this phase, so no binary asset-generation dependency is introduced. Add all English display names.

- [ ] **Step 5: Stamp crafted output server-side**

Handle `PlayerEvent.ItemCraftedEvent`: reject/remove output unless `MatchManager.isActiveMatchPlayer`, otherwise bind it to the current match. Only the six craftable component items use this route.

- [ ] **Step 6: Run tests/build-resource processing and commit**

Run: `gradlew.bat test`

Run: `gradlew.bat processResources compileJava`

Commit: `feat(exodus): register teleporter components and recipes`

---

### Task 4: Persistent Teleporter Block, Menu, and Screen

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/block/ExodusTeleporterBlock.java`
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/blockentity/ExodusTeleporterBlockEntity.java`
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/menu/ExodusTeleporterMenu.java`
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/client/ExodusTeleporterScreen.java`
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/client/ExodusTeleporterClient.java`
- Create: blockstate, block/item model, block loot-table JSON files for `exodus_teleporter`
- Modify: `exodus-mod/src/main/java/dev/exodus/teleporter/ExodusTeleporterRegistry.java`
- Test: `exodus-mod/src/test/java/dev/exodus/teleporter/domain/TeleporterInventoryPolicyTest.java`

**Interfaces:**
- Consumes: Task 1 slot policy and Task 3 registry/NBT helpers.
- Produces: nine-slot persistent block entity, `install`, `remove`, `lock`, `resetIfStale`, menu synchronization fields, and no-automation item capability.

- [ ] **Step 1: Write failing inventory-policy tests**

Cover exact slot, one-item insertion, wrong component, stale match, removal before activation, and immutable slots after activation.

- [ ] **Step 2: Run the focused test and confirm RED**

Run: `gradlew.bat test --tests "dev.exodus.teleporter.domain.TeleporterInventoryPolicyTest"`

- [ ] **Step 3: Implement block and block entity**

Use `BaseEntityBlock`, `EntityBlock`, `MenuProvider`, a nine-element `NonNullList<ItemStack>`, `ContainerHelper` save/load, `PushReaction.BLOCK`, negative destroy time, explosion resistance, and no exposed `IItemHandler`. Stale match IDs clear slots on load/use.

- [ ] **Step 4: Implement server-authoritative menu**

Create nine custom `Slot` instances whose `mayPlace` delegates to `ComponentPolicy`, whose max stack size is one, and whose `mayPickup` is false once locked. Revalidate active-player, dimension and border conditions in `stillValid` and every mutating operation. Quick-move must insert only into the exact destination slot and never move components into ordinary containers.

- [ ] **Step 5: Implement screen and visual block state**

Render the 3x3 grid, dim required-item icons, installed count, locked countdown text, and player inventory. Register client screen safely on the mod event bus. Add `ACTIVE` blockstate with light level and vanilla-based inactive/active models.

- [ ] **Step 6: Run tests and compilation, then commit**

Run: `gradlew.bat test`

Run: `gradlew.bat compileJava processResources`

Commit: `feat(exodus): add persistent teleporter interface`

---

### Task 5: Component Claiming, Container Guard, and Dropped-Item Safety

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/ComponentLifecycleEvents.java`
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/domain/DroppedComponentPolicy.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusEvents.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/teleporter/item/TeleporterComponentItem.java`
- Test: `exodus-mod/src/test/java/dev/exodus/teleporter/domain/DroppedComponentPolicyTest.java`

**Interfaces:**
- Consumes: Task 2 claims/current match; Task 3 item helpers.
- Produces: template-on-pickup binding, duplicate deletion, menu/container rejection, glowing/invulnerable/no-despawn item behavior, last-safe-position tracking, and void recovery.

- [ ] **Step 1: Write failing dropped-item transition tests**

Test active/current item persistence, stale removal, damage immunity, last-safe-position update only while grounded, and void return coordinates.

- [ ] **Step 2: Run the focused test and confirm RED**

Run: `gradlew.bat test --tests "dev.exodus.teleporter.domain.DroppedComponentPolicyTest"`

- [ ] **Step 3: Implement rare template claim and generic container rejection**

On pickup, bind unbound rare template copies only after `RareClaimPolicy.CLAIM`; delete duplicates with an English message. Cancel container clicks/quick-moves that would place a component outside player inventory or an Exodus Teleporter. Reject Forge item-handler insertion from the component side and cover vanilla menus through `PlayerContainerEvent`/menu click validation.

- [ ] **Step 4: Implement item entity protection**

On join/tick, set glowing and unlimited lifetime for current-match components. Cancel fire, cactus and explosion damage. Store the last safe `BlockPos` on the item entity persistent data while on ground; teleport back when below the dimension minimum. Remove stale-match entities.

- [ ] **Step 5: Run tests and compilation, then commit**

Run: `gradlew.bat test`

Run: `gradlew.bat compileJava`

Commit: `feat(exodus): protect match-bound components`

---

### Task 6: Activation Service, Boss Bar, Chunk Tickets, and Victory

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/TeleporterService.java`
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/TeleporterChunkTickets.java`
- Create: `exodus-mod/src/main/java/dev/exodus/teleporter/domain/ActivationTransaction.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/MatchManager.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusEvents.java`
- Test: `exodus-mod/src/test/java/dev/exodus/teleporter/domain/ActivationTransactionTest.java`
- Test: `exodus-mod/src/test/java/dev/exodus/teleporter/domain/BossBarAudiencePolicyTest.java`

**Interfaces:**
- Consumes: Tasks 1-5 policies, persistence, block entity, associations.
- Produces: `tryActivate`, `tick`, `onLogin`, `cleanup`, `recover`, and idempotent match-ending behavior.

- [ ] **Step 1: Write failing activation-race and audience tests**

```java
@Test void secondCompletedDeviceCannotActivateAfterFirstClaimsCountdown() {
    var tx=new ActivationTransaction();
    assertEquals(ACTIVATED, tx.tryActivate(true,false));
    assertEquals(REJECTED_SECOND_DEVICE, tx.tryActivate(true,true));
}
```

Audience cases cover active, pending reconnect, auto spectator, initial spectator and unrelated player.

- [ ] **Step 2: Run focused tests and confirm RED**

Run: `gradlew.bat test --tests "dev.exodus.teleporter.domain.ActivationTransactionTest" --tests "dev.exodus.teleporter.domain.BossBarAudiencePolicyTest"`

- [ ] **Step 3: Implement atomic activation and captured state**

Run only on the server thread. Confirm nine current-match stacks, no active teleporter record, and the three rare claim flags; persist the record before locking the block entity. Roll back the saved record if locking fails. Update the block's `ACTIVE` state and mark data dirty.

- [ ] **Step 4: Implement Boss Bar and reconnect handling**

Create `ServerBossEvent` text formatted as `Exodus Teleporter — X: %d Y: %d Z: %d — %02d:%02d`. Recompute progress on server ticks, reconcile the audience set idempotently, and attach immediately on login. Never persist the Boss Bar object itself.

- [ ] **Step 5: Implement chunk tickets and visual effects**

Use Forge chunk forcing with a stable Exodus ticket owner for every chunk intersecting captured radius. Release the exact set on every cleanup path. Send portal particles and low ambient sound from the server, increasing cadence only during the final 60 seconds.

- [ ] **Step 6: Implement expiry and match end**

At deadline, gather online active match players in the recorded dimension, pass squared distances to `WinnerPolicy`, teleport winners through `MatchManager.endWithWinners`, announce names or no escape, then call idempotent cleanup. A player offline, pending, expired, spectator, or exactly outside the radius is excluded.

- [ ] **Step 7: Wire stop, auto-end, abort-after-commit, recovery and server-stop cleanup**

All routes remove Boss Bar, release tickets, invalidate match-bound components, clear loaded teleporters, and leave unloaded devices for lazy reset. Repeated cleanup performs no second announcement or teleport.

- [ ] **Step 8: Run full tests and compile, then commit**

Run: `gradlew.bat test`

Run: `gradlew.bat compileJava`

Commit: `feat(exodus): resolve teleporter countdown victories`

---

### Task 7: Special-Radio Material Supplies

**Files:**
- Create: `exodus-mod/src/main/resources/data/exodus/exodus_supply_drops/teleporter_energy_materials.json`
- Create: `exodus-mod/src/main/resources/data/exodus/exodus_supply_drops/teleporter_optics_materials.json`
- Create: `exodus-mod/src/main/resources/data/exodus/exodus_supply_drops/teleporter_processing_materials.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/supply_drops/teleporter_{energy,optics,processing}_materials.json`
- Test: `exodus-mod/src/test/java/dev/exodus/supply/catalog/TeleporterMaterialSupplyTest.java`

**Interfaces:**
- Consumes: existing supply catalog JSON schema and Task 3 literal recipe bill.
- Produces: three Special-only, one-request-per-radio supplies whose combined deterministic output covers exactly the non-zone ingredients.

- [ ] **Step 1: Write a failing catalog-and-loot contract test**

Load the three real JSON definitions and loot-table JSON resources. Assert `radio_types == ["special"]`, `max_requests == 1`, costs of 16 iron/12 copper/8 gold, and combined fixed entries of two Blaze Rods, five Ender Pearls, one Quartz Block plus five Nether Quartz.

- [ ] **Step 2: Run the focused test and confirm RED**

Run: `gradlew.bat test --tests "dev.exodus.supply.catalog.TeleporterMaterialSupplyTest"`

- [ ] **Step 3: Add deterministic definitions and loot tables**

Give each entry a distinct smoke color and stable `sort_order`. Keep cooldown zero; the existing one-drop-in-flight constraint already serializes requests per linked pair. Use fixed-count loot entries without random ranges.

- [ ] **Step 4: Run full tests/resource processing and commit**

Run: `gradlew.bat test`

Run: `gradlew.bat processResources`

Commit: `feat(exodus): supply teleporter crafting materials`

---

### Task 8: Project Rules, Status, Full Verification, and Runtime Artifact

**Files:**
- Modify: `AGENTS.md`
- Modify: `docs/superpowers/specs/2026-09-28-project-exodus-teleporter-victory-design.md` only if implementation evidence requires a clarified ruling
- Modify: `exodus-mod/src/main/java/dev/exodus/MatchManager.java` status output
- Create/modify: `exodus-mod/src/main/resources/assets/exodus/lang/en_us.json`
- Output: `mods/exodus-<version>.jar`

**Interfaces:**
- Consumes: all earlier tasks.
- Produces: documented approved phase exception, operator-visible status, verified build, and installed runtime JAR.

- [ ] **Step 1: Update project rules and status output**

Record Teleporter Victory as the second approved exception beside Supply Drops. Add active teleporter coordinates, remaining time and capacity to `/exodus status`; keep `IDLE` output unchanged.

- [ ] **Step 2: Audit all English copy and resources**

Search for missing translation keys, accidental non-English strings, absent model references and missing loot/recipe resource IDs.

Run: `rg -n "Teleporter|teleporter" exodus-mod/src/main AGENTS.md docs/superpowers/specs/2026-09-28-project-exodus-teleporter-victory-design.md`

- [ ] **Step 3: Run the complete unit-test gate**

Run: `gradlew.bat test`

Expected: exit 0 with no failed tests.

- [ ] **Step 4: Run the complete build gate**

Run: `gradlew.bat build`

Expected: exit 0 and a reobfuscated JAR under `exodus-mod/build/libs/`.

- [ ] **Step 5: Copy the verified JAR into the instance**

Resolve the exact built filename and the existing Exodus JAR in `mods/`. Replace only that exact mod artifact; do not delete or modify other mods. Verify file size and SHA-256 after copying.

- [ ] **Step 6: Perform bounded development runtime checks**

Start the Forge development client or server long enough to verify registry/resource loading and absence of new Exodus errors. Exercise creative-tab presence, menu persistence, one recipe, and `/exodus status` if the local environment permits. Label two-client behaviors unverified.

- [ ] **Step 7: Re-read the spec line by line and record acceptance evidence**

Map every spec section to a test, build output, or explicitly unverified manual boundary. Do not substitute compilation for client UI or multiplayer evidence.

- [ ] **Step 8: Commit final integration**

Commit: `feat(exodus): deliver teleporter victory flow`

