# Project Exodus Supply Drops Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add two claimable Supply Radios, a linked Drop Beacon, single-use Linking Tool, datapack-driven paid supply catalog, visible parachute drops, and persistent public loot crates to active Project Exodus matches.

**Architecture:** Pure Java policy and state types decide catalog validity, linking, quota, cooldown, payment, and landing outcomes. Focused Forge adapters register content, persist block/entity state, synchronize menus, execute server-authoritative requests, animate falling drops, and generate loot exactly once. `MatchManager` exposes only match identity/eligibility hooks and never becomes the owner of the supply subsystem.

**Tech Stack:** Java 17, Minecraft 1.20.1 official mappings, Forge 47.4.10, ForgeGradle 6, Gson, JUnit 5.

**Spec:** `docs/superpowers/specs/2026-09-27-project-exodus-supply-drops-design.md`

## Global Constraints

- Do not install or remove third-party mods.
- Use Forge 47.4.10 and Minecraft 1.20.1 APIs verified by local sources or compilation.
- All player-facing text and logs are English.
- Only active `MATCH_PLAYER` users in the active match dimension may claim, link, open, or request from radios.
- Radio and Beacon blocks are unbreakable outside Creative; Supply Crates are always unbreakable.
- Linking Tools have no recipe and are consumed exactly once after a successful link or takeover.
- Per-supply request count and cooldown belong to the physical radio and transfer on takeover.
- A fresh committed match resets stale radio state lazily through a persistent match UUID.
- Loot is generated exactly once from a standard loot table and landed crates survive match cleanup/restart.
- No rockets, win conditions, NPCs, vehicles, gun implementation, raids, teams, alliances, trading, PvP/respawn rules, enemy waves, day/night progression, or HUD work.
- Do not claim the real two-client acceptance test passed until the user reports it.

## Review Focus

- Two simultaneous request packets for one radio must produce one paid drop, one quota increment, and one cooldown transition.
- Breaking an endpoint in an unloaded-peer situation must not leave a usable ghost link when the peer later loads.
- A restart-recovered match must invalidate falling drops and in-flight flags without deleting landed crate contents.
- A datapack reload removing or changing a definition must not mutate an accepted falling drop or regenerate a landed crate.
- A blocked landing after payment must either place one crate or emit one set of loot after the bounded retry, never lose or duplicate loot.

---

### Task 1: Match Identity and Pure Supply Domain

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/supply/domain/RadioType.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/domain/SupplyCost.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/domain/SupplyDefinition.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/domain/SupplyDefinitionValidator.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/domain/RadioUsage.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/domain/RadioUsagePolicy.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/domain/LinkCandidate.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/domain/LinkPolicy.java`
- Create: `exodus-mod/src/test/java/dev/exodus/supply/domain/SupplyDefinitionValidatorTest.java`
- Create: `exodus-mod/src/test/java/dev/exodus/supply/domain/RadioUsagePolicyTest.java`
- Create: `exodus-mod/src/test/java/dev/exodus/supply/domain/LinkPolicyTest.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusSavedData.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/MatchManager.java`

**Interfaces:**
- Consumes: `MatchState.RUNNING`, `Association.MATCH_PLAYER`, active dimension/border state.
- Produces: `UUID ExodusSavedData.matchId`, `MatchManager.isActiveMatchPlayer(ServerPlayer)`, immutable supply definitions, `RadioUsagePolicy.evaluate(...)`, and `LinkPolicy.evaluate(...)` for later Forge adapters.

- [ ] **Step 1: Write failing match-identity and domain tests**

Create focused tests with wished-for APIs. The key assertions are:

```java
@Test void takeoverKeepsPhysicalRadioUsage() {
    UUID match = UUID.randomUUID();
    RadioUsage used = new RadioUsage(match, 1, 2_400L);
    assertEquals(2, RadioUsagePolicy.remaining(used, match, 3));
    assertEquals(2_400L, RadioUsagePolicy.normalize(used, match).cooldownUntilTick());
}

@Test void aNewMatchResetsUsageLazily() {
    RadioUsage old = new RadioUsage(UUID.randomUUID(), 2, 9_000L);
    RadioUsage reset = RadioUsagePolicy.normalize(old, UUID.randomUUID());
    assertEquals(0, reset.acceptedRequests());
    assertEquals(0L, reset.cooldownUntilTick());
}

@Test void rejectsASecondEndpointAlreadyLinkedElsewhere() {
    LinkCandidate candidate = new LinkCandidate(true, true, true, true, 64.0, 128, false, true);
    assertEquals(LinkPolicy.Result.ENDPOINT_OCCUPIED, LinkPolicy.evaluate(candidate));
}
```

Add validator cases for invalid resource IDs, empty/unknown radio types, `max_requests` of `0` or below `-1`, negative cooldown, invalid cost, invalid RGB, defaults, and deterministic ordering.

- [ ] **Step 2: Run tests and verify RED**

Run: `cd exodus-mod; .\gradlew.bat test --tests "dev.exodus.supply.domain.*"`

Expected: compilation fails because the supply-domain types do not exist.

- [ ] **Step 3: Implement minimal pure domain types**

Use records/enums without Minecraft imports except string resource IDs so tests remain fast:

```java
public record RadioUsage(UUID matchId, int acceptedRequests, long cooldownUntilTick) {}

public final class RadioUsagePolicy {
    public static RadioUsage normalize(RadioUsage usage, UUID activeMatchId) { /* reset on mismatch */ }
    public static int remaining(RadioUsage usage, UUID activeMatchId, int maximum) { /* -1 is unlimited */ }
    public static Decision evaluate(RadioUsage usage, UUID matchId, int maximum, long nowTick) { /* quota/cooldown */ }
    public static RadioUsage accept(RadioUsage usage, UUID matchId, long cooldownUntilTick) { /* increment */ }
}
```

`LinkCandidate` contains only resolved booleans/distance; `LinkPolicy.Result` enumerates `ALLOW`, `NO_MATCH`, `NOT_ACTIVE_PLAYER`, `WRONG_DIMENSION`, `OUTSIDE_BORDER`, `TOO_FAR`, and `ENDPOINT_OCCUPIED`.

- [ ] **Step 4: Add persistent match UUID lifecycle**

Serialize optional `matchId` in `ExodusSavedData`. Assign `UUID.randomUUID()` only after allocation commit succeeds and state becomes `RUNNING`. Clear it on stop, abort, and restart recovery. Add:

```java
public static boolean isActiveMatchPlayer(ServerPlayer player) {
    ExodusSavedData data = ExodusSavedData.get(player.server);
    return data.state == MatchState.RUNNING
        && data.matchId != null
        && data.associations.get(player.getUUID()) == Association.MATCH_PLAYER
        && !data.pendingPlayers.containsKey(player.getUUID())
        && player.serverLevel().dimension().location().toString().equals(data.dimension);
}
```

- [ ] **Step 5: Run domain and full tests**

Run: `cd exodus-mod; .\gradlew.bat test --tests "dev.exodus.supply.domain.*"`

Expected: all new domain tests pass.

Run: `cd exodus-mod; .\gradlew.bat test`

Expected: all existing and new tests pass.

- [ ] **Step 6: Commit**

```powershell
git add exodus-mod/src/main/java/dev/exodus/supply/domain exodus-mod/src/test/java/dev/exodus/supply/domain exodus-mod/src/main/java/dev/exodus/ExodusSavedData.java exodus-mod/src/main/java/dev/exodus/MatchManager.java
git commit -m "feat(exodus): add supply domain policies"
```

### Task 2: Forge Registries, Configuration, and Static Resources

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/supply/ExodusSupplyRegistry.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/block/SupplyRadioBlock.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/block/DropBeaconBlock.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/block/SupplyCrateBlock.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/item/LinkingToolItem.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/entity/SupplyDropEntity.java`
- Create: `exodus-mod/src/main/resources/assets/exodus/blockstates/basic_supply_radio.json`
- Create: `exodus-mod/src/main/resources/assets/exodus/blockstates/special_supply_radio.json`
- Create: `exodus-mod/src/main/resources/assets/exodus/blockstates/drop_beacon.json`
- Create: `exodus-mod/src/main/resources/assets/exodus/blockstates/supply_crate.json`
- Create: `exodus-mod/src/main/resources/assets/exodus/models/block/basic_supply_radio.json`
- Create: `exodus-mod/src/main/resources/assets/exodus/models/block/special_supply_radio.json`
- Create: `exodus-mod/src/main/resources/assets/exodus/models/block/drop_beacon.json`
- Create: `exodus-mod/src/main/resources/assets/exodus/models/block/supply_crate.json`
- Create: `exodus-mod/src/main/resources/assets/exodus/models/item/basic_supply_radio.json`
- Create: `exodus-mod/src/main/resources/assets/exodus/models/item/special_supply_radio.json`
- Create: `exodus-mod/src/main/resources/assets/exodus/models/item/drop_beacon.json`
- Create: `exodus-mod/src/main/resources/assets/exodus/models/item/supply_crate.json`
- Create: `exodus-mod/src/main/resources/assets/exodus/models/item/linking_tool.json`
- Create: `exodus-mod/src/main/resources/assets/exodus/lang/en_us.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/blocks/basic_supply_radio.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/blocks/special_supply_radio.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/blocks/drop_beacon.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/blocks/supply_crate.json`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusMod.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusConfig.java`

**Interfaces:**
- Consumes: radio types from Task 1.
- Produces: registry objects for four blocks, four block items, Linking Tool, three block-entity types, Supply Drop entity type, two menu types, config values, and resource models used by later tasks.

- [ ] **Step 1: Add config declarations before registry code**

Add exact server/common settings under a `supplyDrops` config section:

```java
RADIO_LINK_RANGE = b.defineInRange("radioLinkRange", 128, 1, 4096);
DROP_SPAWN_HEIGHT = b.defineInRange("dropSpawnHeight", 80, 8, 512);
DROP_SPEED_MILLIBLOCKS = b.defineInRange("dropSpeedMilliblocksPerTick", 125, 10, 1000);
LANDING_CLEARANCE = b.defineInRange("landingClearance", 3, 0, 16);
LANDING_RETRY_SECONDS = b.defineInRange("landingRetrySeconds", 10, 1, 60);
EMPTY_CRATE_SECONDS = b.defineInRange("emptyCrateRemovalSeconds", 30, 1, 600);
```

- [ ] **Step 2: Register content on the mod event bus**

`ExodusSupplyRegistry` owns `DeferredRegister<Block>`, `Item`, `BlockEntityType<?>`, `EntityType<?>`, and `MenuType<?>`. Give Basic and Special radio block instances an immutable `RadioType`. Use block strengths with an override in `getDestroyProgress`/`canEntityDestroy` so only Creative players can remove radio/beacon blocks. Return no drops for protected blocks and crate.

Initialize from `ExodusMod` using `FMLJavaModLoadingContext.get().getModEventBus()`; do not put client-only classes in common initialization.

- [ ] **Step 3: Add complete resource JSON**

Use vanilla textures so no placeholder bitmap is needed. For example, the Basic model uses `minecraft:block/iron_block`, Special uses `minecraft:block/gold_block`, Beacon uses `minecraft:block/redstone_block`, and Crate uses `minecraft:block/barrel_side`/`barrel_top`. Item models parent their corresponding block models; Linking Tool parents `minecraft:item/handheld` with `minecraft:item/spyglass` as its texture. Block loot tables are empty because distribution is operator-controlled and breaking is administrative.

- [ ] **Step 4: Compile against Forge APIs**

Run: `cd exodus-mod; .\gradlew.bat compileJava processResources`

Expected: `BUILD SUCCESSFUL`; registry generics and all resource JSON parse during processing.

- [ ] **Step 5: Commit**

```powershell
git add exodus-mod/src/main/java/dev/exodus/ExodusMod.java exodus-mod/src/main/java/dev/exodus/ExodusConfig.java exodus-mod/src/main/java/dev/exodus/supply/ExodusSupplyRegistry.java exodus-mod/src/main/java/dev/exodus/supply/block exodus-mod/src/main/java/dev/exodus/supply/item exodus-mod/src/main/java/dev/exodus/supply/entity/SupplyDropEntity.java exodus-mod/src/main/resources/assets/exodus exodus-mod/src/main/resources/data/exodus/loot_tables/blocks
git commit -m "feat(exodus): register supply drop content"
```

### Task 3: Persistent Radio/Beacon State and Single-Use Linking

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/supply/blockentity/SupplyRadioBlockEntity.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/blockentity/DropBeaconBlockEntity.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/link/LinkingService.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/link/LinkingSelection.java`
- Create: `exodus-mod/src/test/java/dev/exodus/supply/link/LinkingSelectionTest.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/supply/block/SupplyRadioBlock.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/supply/block/DropBeaconBlock.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/supply/item/LinkingToolItem.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/supply/ExodusSupplyRegistry.java`

**Interfaces:**
- Consumes: `LinkPolicy`, active match UUID/eligibility, registered blocks and block entities.
- Produces: versioned NBT state for both endpoints and `LinkingService.useOnRadio/useOnBeacon` used by block interactions.

- [ ] **Step 1: Write failing selection serialization tests**

Test dimension/position round trip, a second radio click replacing the selection, malformed NBT clearing safely, and success clearing selection:

```java
@Test void selectionRoundTripsWithoutOwnerState() {
    LinkingSelection value = new LinkingSelection("minecraft:overworld", 12, 70, -4);
    assertEquals(value, LinkingSelection.decode(value.encode()).orElseThrow());
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run: `cd exodus-mod; .\gradlew.bat test --tests "dev.exodus.supply.link.LinkingSelectionTest"`

Expected: compilation fails because `LinkingSelection` does not exist.

- [ ] **Step 3: Implement block-entity persistence**

Radio NBT stores radio type implicitly from its block plus `ownerId`, `ownerName`, `matchId`, peer dimension/position, `Map<ResourceLocation, RadioUsage>`, and optional active drop UUID. Beacon NBT stores owner identity, match ID, and peer dimension/position. Loading malformed UUIDs/resource IDs skips only the malformed field and logs once.

Expose narrow methods:

```java
public void claim(UUID matchId, UUID ownerId, String ownerName, GlobalPos beacon);
public RadioUsage usage(ResourceLocation supplyId, UUID matchId);
public void recordAccepted(ResourceLocation supplyId, UUID matchId, long cooldownUntilTick, UUID dropId);
public void rollbackAccepted(ResourceLocation supplyId, RadioUsage previous);
public void clearInFlight(UUID expectedDropId);
public boolean hasValidPeer(ServerLevel level);
```

- [ ] **Step 4: Implement atomic link/takeover interaction**

The first radio click writes only dimension/position to the held tool and sends `Supply Radio selected.` The beacon click loads both endpoints, constructs `LinkCandidate`, validates the exact existing-pair rule, claims both endpoints, notifies online old/new owners, clears selection, and shrinks the held stack by one. Lock the operation to the server thread and re-read both block entities immediately before mutation.

Creative breaking calls a common unlink helper. If the peer chunk is unloaded, local removal is sufficient; `hasValidPeer` rejects the counterpart lazily on its next use.

- [ ] **Step 5: Run focused tests, full tests, and compile**

Run: `cd exodus-mod; .\gradlew.bat test --tests "dev.exodus.supply.link.*"`

Expected: selection tests pass.

Run: `cd exodus-mod; .\gradlew.bat test compileJava`

Expected: all tests pass and Forge interaction APIs compile.

- [ ] **Step 6: Commit**

```powershell
git add exodus-mod/src/main/java/dev/exodus/supply/blockentity exodus-mod/src/main/java/dev/exodus/supply/link exodus-mod/src/test/java/dev/exodus/supply/link exodus-mod/src/main/java/dev/exodus/supply/block exodus-mod/src/main/java/dev/exodus/supply/item/LinkingToolItem.java exodus-mod/src/main/java/dev/exodus/supply/ExodusSupplyRegistry.java
git commit -m "feat(exodus): add radio beacon linking"
```

### Task 4: Datapack Catalog, Network Snapshot, and Radio UI

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/supply/catalog/SupplyCatalog.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/catalog/SupplyCatalogReloadListener.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/catalog/SupplyJsonParser.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/network/ExodusSupplyNetwork.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/network/SupplyCatalogEntryDto.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/menu/SupplyRadioMenu.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/client/SupplyRadioScreen.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/client/ExodusSupplyClient.java`
- Create: `exodus-mod/src/test/java/dev/exodus/supply/catalog/SupplyJsonParserTest.java`
- Create: `exodus-mod/src/main/resources/data/exodus/exodus_supply_drops/basic_food.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/supply_drops/basic_food.json`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusEvents.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusMod.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/supply/block/SupplyRadioBlock.java`

**Interfaces:**
- Consumes: immutable `SupplyDefinition`, radio type/state, registry menu type.
- Produces: atomic server catalog snapshots, reload registration, menu-open payload, paged Radio screen, and request packet DTO consumed by Task 5.

- [ ] **Step 1: Write failing JSON parser tests**

Parse strings with Gson and assert exact defaults/filtering. Include modded IDs, free requests, unlimited `-1`, Basic-only, Special-only, disabled definitions, unknown fields tolerated, and each invalid field returning a validation error containing resource ID and field path.

- [ ] **Step 2: Run parser test and verify RED**

Run: `cd exodus-mod; .\gradlew.bat test --tests "dev.exodus.supply.catalog.SupplyJsonParserTest"`

Expected: compilation fails because parser/catalog classes do not exist.

- [ ] **Step 3: Implement reload listener and immutable snapshot**

Use `SimpleJsonResourceReloadListener` for directory `exodus_supply_drops`. Parse to a temporary map, warn and skip invalid entries, then atomically replace `SupplyCatalog` with an unmodifiable sorted snapshot. Register through `AddReloadListenerEvent`.

- [ ] **Step 4: Implement SimpleChannel and menu payloads**

Register a protocol-versioned `SimpleChannel`. The server sends only filtered DTO fields needed by the screen: ID, name, icon item ID, cost, maximum, accepted count, cooldown end tick, enabled state, and disabled reason. The client request packet contains dimension, radio position, and supply ID only. Enqueue all handlers on the logical server/client thread.

- [ ] **Step 5: Implement radio menu and screen**

The block validates owner/active-player state before `NetworkHooks.openScreen`. The screen renders a scrollable catalog with icon, English name, cost, remaining/unlimited, cooldown seconds, and request button. It has no HUD and uses vanilla widgets/fonts. Rejected server requests display the returned English reason without optimistic quota mutation.

- [ ] **Step 6: Add one valid bundled sample**

`basic_food.json` is enabled for both radio types, costs four emeralds, permits three requests per radio per match, uses a 30-second cooldown, brown/orange smoke, and references a loot table containing bread, cooked beef, and golden carrots. It proves reload without pretending to implement guns.

- [ ] **Step 7: Run tests and compile both logical sides**

Run: `cd exodus-mod; .\gradlew.bat test --tests "dev.exodus.supply.catalog.*"`

Expected: all parser/catalog tests pass.

Run: `cd exodus-mod; .\gradlew.bat test compileJava processResources`

Expected: all tests pass and common/client classes compile without dedicated-server-only client imports.

- [ ] **Step 8: Commit**

```powershell
git add exodus-mod/src/main/java/dev/exodus/supply/catalog exodus-mod/src/main/java/dev/exodus/supply/network exodus-mod/src/main/java/dev/exodus/supply/menu exodus-mod/src/main/java/dev/exodus/supply/client exodus-mod/src/test/java/dev/exodus/supply/catalog exodus-mod/src/main/resources/data/exodus/exodus_supply_drops exodus-mod/src/main/resources/data/exodus/loot_tables/supply_drops exodus-mod/src/main/java/dev/exodus/ExodusEvents.java exodus-mod/src/main/java/dev/exodus/ExodusMod.java exodus-mod/src/main/java/dev/exodus/supply/block/SupplyRadioBlock.java
git commit -m "feat(exodus): add supply catalog and radio ui"
```

### Task 5: Atomic Request Acceptance and Payment

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/supply/request/SupplyRequestService.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/request/RequestContext.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/request/RequestResult.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/request/PaymentService.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/request/AcceptedDrop.java`
- Create: `exodus-mod/src/test/java/dev/exodus/supply/request/SupplyRequestPolicyTest.java`
- Create: `exodus-mod/src/test/java/dev/exodus/supply/request/AtomicRequestTransactionTest.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/supply/network/ExodusSupplyNetwork.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/supply/blockentity/SupplyRadioBlockEntity.java`

**Interfaces:**
- Consumes: catalog snapshot, radio/link state, `RadioUsagePolicy`, active match identity, payment inventory, entity registry.
- Produces: `RequestResult SupplyRequestService.request(ServerPlayer, BlockPos, ResourceLocation)` and immutable `AcceptedDrop` captured by the falling entity.

- [ ] **Step 1: Write failing transaction tests**

Use small interfaces (`PaymentPort`, `DropSpawnPort`, `RadioStatePort`) so pure tests exercise real transaction logic. Pin these cases: insufficient items changes nothing; exhausted quota changes nothing; cooldown changes nothing; two sequential calls after the first acceptance reject the second as in-flight; spawn failure restores exact inventory/state; success consumes exact cost once.

```java
@Test void spawnFailureRollsBackPaymentUsageCooldownAndFlight() {
    FakeTransactionPorts ports = FakeTransactionPorts.withItems(12).failingSpawn();
    RequestResult result = service.request(validContext(), ports);
    assertEquals(RequestResult.Code.SPAWN_FAILED, result.code());
    assertEquals(12, ports.items());
    assertEquals(new RadioUsage(matchId, 0, 0L), ports.usage());
    assertNull(ports.inFlightDrop());
}
```

- [ ] **Step 2: Run request tests and verify RED**

Run: `cd exodus-mod; .\gradlew.bat test --tests "dev.exodus.supply.request.*"`

Expected: compilation fails because request transaction types do not exist.

- [ ] **Step 3: Implement server-authoritative validation order**

Under a per-radio server-thread critical section, re-read: active player/match, block type, owner, peer existence/exact reverse link, dimension, border, distance, radio type, current catalog entry, quota, cooldown, in-flight flag, payment count, and legal drop spawn height. Return one stable `RequestResult.Code` for every expected denial.

- [ ] **Step 4: Implement payment and rollback**

Count/remove exact item IDs across main inventory and hotbar without NBT matching. Snapshot affected slots and prior radio usage before mutation. Spawn only after payment and state mutation; if `ServerLevel.addFreshEntity` returns false or throws, restore affected slots and prior state in the same server task and log the operation stage.

- [ ] **Step 5: Connect request packet response to UI**

The packet handler ignores client-provided prices/counts. It invokes the service and returns the code plus refreshed DTO for the selected entry. The client updates only from this response.

- [ ] **Step 6: Run focused, full, and compile gates**

Run: `cd exodus-mod; .\gradlew.bat test --tests "dev.exodus.supply.request.*"`

Expected: all transaction cases pass.

Run: `cd exodus-mod; .\gradlew.bat test compileJava`

Expected: the entire suite passes and request/network adapters compile.

- [ ] **Step 7: Commit**

```powershell
git add exodus-mod/src/main/java/dev/exodus/supply/request exodus-mod/src/test/java/dev/exodus/supply/request exodus-mod/src/main/java/dev/exodus/supply/network/ExodusSupplyNetwork.java exodus-mod/src/main/java/dev/exodus/supply/blockentity/SupplyRadioBlockEntity.java
git commit -m "feat(exodus): accept paid supply requests atomically"
```

### Task 6: Falling Parachute, First-Surface Landing, and Public Crate

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/supply/domain/LandingPolicy.java`
- Create: `exodus-mod/src/test/java/dev/exodus/supply/domain/LandingPolicyTest.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/blockentity/SupplyCrateBlockEntity.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/menu/SupplyCrateMenu.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/client/SupplyCrateScreen.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/client/SupplyDropRenderer.java`
- Create: `exodus-mod/src/main/java/dev/exodus/supply/loot/SupplyLootService.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/supply/entity/SupplyDropEntity.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/supply/block/SupplyCrateBlock.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/supply/client/ExodusSupplyClient.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/supply/ExodusSupplyRegistry.java`

**Interfaces:**
- Consumes: `AcceptedDrop`, config values, loot manager, source radio in-flight identity.
- Produces: serialized falling entity, collision/landing loop, rendered crate/parachute/smoke, one-time 54-slot loot container, and bounded obstruction fallback.

- [ ] **Step 1: Write failing landing-policy tests**

Model a vertical list of collision/replaceability samples. Assert highest first collision wins, fluids alone are ignored, clearance selects the first replaceable space from `surface + 1` through configured clearance, and a fully blocked column returns `RETRY` until deadline then `DROP_ITEMS`.

- [ ] **Step 2: Run landing tests and verify RED**

Run: `cd exodus-mod; .\gradlew.bat test --tests "dev.exodus.supply.domain.LandingPolicyTest"`

Expected: compilation fails because `LandingPolicy` does not exist.

- [ ] **Step 3: Implement falling entity persistence and collision**

Persist accepted supply ID/name, resolved loot-table ID, smoke RGB, requester UUID, match UUID, radio/beacon global positions, source radio/drop UUID, landing retry deadline, and loot-generated flag. Each server tick ray/sweeps from previous to next Y against block collision shapes, ignoring fluids, then applies `LandingPolicy`. Maintain a region ticket for the entity chunk and beacon column; remove tickets on landing/removal.

- [ ] **Step 4: Implement renderer and particles**

Render the registered Supply Crate block below a simple four-sided white canopy using `PoseStack`/`VertexConsumer`; no external texture asset is required beyond a small solid-color material reference. Emit server-visible colored dust particles at a bounded interval. Register renderer only through `EntityRenderersEvent.RegisterRenderers` on the client mod bus.

- [ ] **Step 5: Implement crate inventory and one-time loot**

Use a `Container`/`RandomizableContainerBlockEntity`-compatible 54-slot block entity with a 9x6 menu. Resolve the loot table from the captured ID and unpack once using requester/luck and landing origin context. Persist the actual inventory and `lootGenerated=true` before any later tick can repeat generation. Overflow spawns once and logs a warning.

Reject insertion via menu shift-click and sided capability/item-handler exposure while allowing extraction. Start `emptySinceTick` on the first empty state and never cancel it. At configured expiry, emit smoke, clear the source radio in-flight flag if it still matches, and remove the block.

- [ ] **Step 6: Implement obstruction fallback**

Retry placement for the configured 10 seconds. At expiry, call the same one-time loot generator into a temporary 54-slot container, spawn each resulting stack once at collision position, clear the matching in-flight flag, release tickets, log context, and discard the entity.

- [ ] **Step 7: Run focused, full, and build gates**

Run: `cd exodus-mod; .\gradlew.bat test --tests "dev.exodus.supply.domain.LandingPolicyTest"`

Expected: landing tests pass.

Run: `cd exodus-mod; .\gradlew.bat test build`

Expected: complete suite and reobfuscated JAR build pass.

- [ ] **Step 8: Commit**

```powershell
git add exodus-mod/src/main/java/dev/exodus/supply/domain/LandingPolicy.java exodus-mod/src/test/java/dev/exodus/supply/domain/LandingPolicyTest.java exodus-mod/src/main/java/dev/exodus/supply/blockentity/SupplyCrateBlockEntity.java exodus-mod/src/main/java/dev/exodus/supply/menu/SupplyCrateMenu.java exodus-mod/src/main/java/dev/exodus/supply/client exodus-mod/src/main/java/dev/exodus/supply/loot exodus-mod/src/main/java/dev/exodus/supply/entity/SupplyDropEntity.java exodus-mod/src/main/java/dev/exodus/supply/block/SupplyCrateBlock.java exodus-mod/src/main/java/dev/exodus/supply/ExodusSupplyRegistry.java
git commit -m "feat(exodus): land parachute supply crates"
```

### Task 7: Recovery, Race Regression, Packaging, and Runtime Evidence

**Files:**
- Create: `exodus-mod/src/test/java/dev/exodus/supply/persistence/SupplyStateCodecTest.java`
- Create: `exodus-mod/src/test/java/dev/exodus/supply/request/ConcurrentRequestRegressionTest.java`
- Create: `docs/project-exodus-supply-drop-manual-test.md`
- Modify: `exodus-mod/src/main/java/dev/exodus/MatchManager.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusEvents.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/supply/entity/SupplyDropEntity.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/supply/blockentity/SupplyRadioBlockEntity.java`
- Modify: `AGENTS.md`

**Interfaces:**
- Consumes: all prior subsystem interfaces.
- Produces: safe restart invalidation, regression proof for likely races/persistence failures, operator instructions, installed JAR, and explicit manual acceptance boundary.

- [ ] **Step 1: Write failing persistence/recovery tests**

Round-trip owner/link/match/usage/cooldown/in-flight state through NBT codecs. Assert malformed optional fields degrade safely. Add a request test that schedules two requests against one radio state and proves exactly one accepted result, one cost removal, and one active drop ID.

- [ ] **Step 2: Run regression tests and verify RED**

Run: `cd exodus-mod; .\gradlew.bat test --tests "dev.exodus.supply.persistence.*" --tests "dev.exodus.supply.request.ConcurrentRequestRegressionTest"`

Expected: at least the new recovery/race assertions fail before hooks and synchronization are completed.

- [ ] **Step 3: Complete restart and stale-link recovery**

On `MatchManager.recover`, clear the active match ID after border restoration. A falling entity compares its serialized match ID with current active state on first server tick; mismatch clears its matching radio flag if loaded, releases tickets, and discards without loot. Radio access normalizes stale usage and clears a stale in-flight UUID whose entity is absent from a running current match.

- [ ] **Step 4: Update project scope documentation**

Change `AGENTS.md` Current phase to record supply drops as the sole approved exception, link the spec/plan, and retain every other exclusion. Add a manual guide covering `/give` commands, sample datapack reload, Basic/Special filtering, linking/tool consumption, paid request, roof landing, takeover with preserved quota/cooldown, match reset, public looting, restart behavior, and two-client checks.

- [ ] **Step 5: Run clean verification gates**

Run: `cd exodus-mod; .\gradlew.bat clean test`

Expected: all tests pass from clean outputs.

Run: `cd exodus-mod; .\gradlew.bat build`

Expected: `BUILD SUCCESSFUL` and exactly one current `exodus-0.1.0.jar` production artifact under `exodus-mod/build/libs/` (excluding sources/dev artifacts).

- [ ] **Step 6: Install only the verified JAR**

Resolve the exact built JAR and exact destination under the instance `mods/`. Replace only the prior Project Exodus JAR, leaving every third-party mod untouched. Verify SHA-256 of source and installed files match.

- [ ] **Step 7: Perform local runtime smoke**

Launch the configured Forge development client or instance far enough to confirm Project Exodus registers all new content, the bundled catalog reloads, the Radio menu opens for a valid owner, and logs contain no new Forge registry, model, packet, datapack, or KubeJS errors. Do not describe this as the real two-client test.

- [ ] **Step 8: Commit**

```powershell
git add AGENTS.md docs/project-exodus-supply-drop-manual-test.md exodus-mod/src/test/java/dev/exodus/supply/persistence exodus-mod/src/test/java/dev/exodus/supply/request/ConcurrentRequestRegressionTest.java exodus-mod/src/main/java/dev/exodus/MatchManager.java exodus-mod/src/main/java/dev/exodus/ExodusEvents.java exodus-mod/src/main/java/dev/exodus/supply/entity/SupplyDropEntity.java exodus-mod/src/main/java/dev/exodus/supply/blockentity/SupplyRadioBlockEntity.java
git commit -m "test(exodus): verify supply drop recovery"
```

## Completion Boundary

Implementation is complete only when Tasks 1-7 are committed on the feature branch, `clean test` and `build` pass, the installed JAR hash matches the built artifact, and the local single-client/runtime smoke has no new relevant errors. Two-client ownership, simultaneous multiplayer behavior, remote parachute rendering, public looting, and match-to-match multiplayer reset remain pending until the user executes the manual guide and reports the result.

