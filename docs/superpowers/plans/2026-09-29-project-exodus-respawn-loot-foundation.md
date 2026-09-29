# Project Exodus Respawn, Loot, and Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bind match players to their bases, reset respawns safely at cleanup, broaden and roughly double loot, distribute the six craftable teleporter components by chest tier, and move manufactured support below the structure-owned bottom layer.

**Architecture:** Add a small pure respawn policy plus a Minecraft-facing adapter used by `MatchManager`, keep loot data-driven through reusable tables and stable marker IDs while preserving rare-key routing, and make terrain preparation/verification share an explicit support-top calculation. Each subsystem is independently testable and lands in its own commit.

**Tech Stack:** Java 17, Minecraft 1.20.1, Forge 47.4.10, JUnit 5, Gson contract tests, Minecraft loot-table JSON, Lost Cities chest conditions.

**Spec:** `docs/superpowers/specs/2026-09-29-project-exodus-respawn-loot-foundation-design.md`

## Global Constraints

- All player-facing text and logs remain English.
- Do not add or remove third-party mods.
- Keep existing loot marker table IDs stable.
- Preserve Facility Alpha/Beta key faction distribution and unique-claim logic exactly.
- Exclude the Dimensional Core from ordinary chest loot.
- Reset cleanup respawns to the current default Overworld world spawn; do not restore previous beds or anchors.
- Preserve the structure Y coordinate and begin manufactured support exactly one block below its lowest layer.
- Real in-game multiplayer and death/respawn acceptance remains manual and must not be reported as verified before the user confirms it.

## Review Focus

- A startup failure before commit must leave player respawns untouched; exercise this through the pure assignment policy test.
- An offline player at cleanup must retain a one-shot pending Overworld respawn reset across save/load; exercise it in saved-data and lifecycle policy tests.
- A faction elite marker must keep its existing first-key routing and unique-claim behavior; exercise both faction IDs and both claim states.
- Loot JSON must reference only known curated TacZ item IDs, include all six craftable components with tier-scaled chances, and never contain the Dimensional Core; exercise exact resource strings in the contract test.
- Terrain preparation and verification must calculate the same shifted support row at minimum build height and normal terrain; exercise the extracted support-Y policy boundary tests.

---

### Task 1: Match Respawn Lifecycle

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/player/MatchRespawnPolicy.java`
- Create: `exodus-mod/src/main/java/dev/exodus/player/RespawnService.java`
- Create: `exodus-mod/src/test/java/dev/exodus/player/MatchRespawnPolicyTest.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/MatchManager.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/ExodusSavedData.java`
- Modify: `exodus-mod/src/test/java/dev/exodus/ExodusSavedDataTest.java`

**Interfaces:**
- Consumes: committed player association, `PlayerBaseData.spawn()`, match dimension, `MinecraftServer.overworld().getSharedSpawnPos()`, and persistent `pendingReturns`.
- Produces: `MatchRespawnPolicy.assignBase(Association, boolean committed, boolean hasBase)`, `RespawnService.setBase(ServerPlayer, ServerLevel, BlockPos)`, and `RespawnService.resetToOverworld(ServerPlayer, ServerLevel)`.

- [ ] **Step 1: Write failing policy and persistence tests**

```java
assertTrue(MatchRespawnPolicy.assignBase(Association.MATCH_PLAYER, true, true));
assertFalse(MatchRespawnPolicy.assignBase(Association.MATCH_PLAYER, false, true));
assertFalse(MatchRespawnPolicy.assignBase(Association.INITIAL_SPECTATOR, true, true));
assertFalse(MatchRespawnPolicy.assignBase(Association.MATCH_PLAYER, true, false));
```

Extend `ExodusSavedDataTest` with an offline `pendingReturns` entry, serialize and load, and assert its survival/reset intent remains present exactly once.

- [ ] **Step 2: Run focused tests and confirm red**

Run: `./gradlew test --tests dev.exodus.player.MatchRespawnPolicyTest --tests dev.exodus.ExodusSavedDataTest`

Expected: FAIL because `MatchRespawnPolicy` does not exist.

- [ ] **Step 3: Implement the pure policy and Minecraft adapter**

```java
public static boolean assignBase(Association association, boolean committed, boolean hasBase) {
    return committed && hasBase && association == Association.MATCH_PLAYER;
}

public static void setBase(ServerPlayer player, ServerLevel level, BlockPos spawn) {
    player.setRespawnPosition(level.dimension(), spawn, 0.0F, true, false);
}

public static void resetToOverworld(ServerPlayer player, ServerLevel overworld) {
    player.setRespawnPosition(overworld.dimension(), overworld.getSharedSpawnPos(), 0.0F, true, false);
}
```

Verify the exact Forge/Minecraft 1.20.1 method signature by compilation; do not substitute an unverified API.

- [ ] **Step 4: Wire assignment and every cleanup boundary**

After successful match commit, look up each `MATCH_PLAYER` base and call `setBase` using the stored spawn. In cleanup, reset online match players before teleporting them; preserve the existing pending entry for offline players and apply `resetToOverworld` on login before removing it. During restart recovery, enqueue/reset associated match players before associations are cleared. Initial spectators remain excluded from base assignment.

- [ ] **Step 5: Run focused and lifecycle tests**

Run: `./gradlew test --tests 'dev.exodus.*' --tests 'dev.exodus.player.*'`

Expected: PASS, including saved-data round trip and policy boundaries.

- [ ] **Step 6: Commit the respawn slice**

```powershell
git add exodus-mod/src/main/java/dev/exodus/player exodus-mod/src/main/java/dev/exodus/MatchManager.java exodus-mod/src/main/java/dev/exodus/ExodusSavedData.java exodus-mod/src/test/java/dev/exodus/player exodus-mod/src/test/java/dev/exodus/ExodusSavedDataTest.java
git commit -m "feat(match): bind respawns to player bases"
```

### Task 2: Composable Generous Loot and Craftable Component Distribution

**Files:**
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/bonus/survival.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/bonus/blocks.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/bonus/weapons_common.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/bonus/weapons_valuable.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/bonus/weapons_elite.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/bonus/components_common.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/bonus/components_standard.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/bonus/components_valuable.json`
- Create: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/bonus/components_elite.json`
- Modify: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/general/common.json`
- Modify: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/general/standard.json`
- Modify: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/general/valuable.json`
- Modify: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/general/elite.json`
- Modify: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/food.json`
- Modify: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/medical.json`
- Modify: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/weapons.json`
- Modify: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/utility.json`
- Modify: `exodus-mod/src/main/resources/data/exodus/loot_tables/chests/tech.json`
- Modify: `exodus-mod/src/main/resources/data/lostcities/lostcities/conditions/chestloot.json`
- Modify: `exodus-mod/src/test/java/dev/exodus/wasteland/loot/LootTableContractTest.java`

**Interfaces:**
- Consumes: stable `LootMarker.lootTable()` IDs and vanilla `minecraft:loot_table` entry composition.
- Produces: reusable bonus tables, curated TacZ gun rarity bands, and tier-scaled tables containing exactly the six craftable teleporter components.

- [ ] **Step 1: Replace old assertions with failing contracts**

Assert that all marker and bonus JSON files parse; no ordinary table contains `facility_alpha_key`, `facility_beta_key`, or `dimensional_core`; the existing faction key resources still contain only their own guaranteed key; every component bonus contains all six craftable component IDs; component entry weights or table-selection chances increase from common through elite; weapon resources include at least two IDs in every approved family; useful Vanilla blocks are present; total roll maxima are approximately twice their old budgets; and Lost Cities mapping includes general valuable, general elite, weapons, utility, and tech with elite weighted below valuable and both below common.

- [ ] **Step 2: Run focused loot tests and confirm red**

Run: `./gradlew test --tests dev.exodus.wasteland.loot.LootTableContractTest --tests dev.exodus.wasteland.loot.FactionEliteLootPolicyTest`

Expected: FAIL on missing bonus/component tables, old roll budgets, and absent Lost Cities categories.

- [ ] **Step 3: Preserve rare-component behavior**

Run `FactionEliteLootPolicyTest` unchanged and verify that the first Russian/American elite marker still assigns its faction table, later markers return ordinary elite loot, and unique-key claims remain one per faction placement. Do not modify `FactionEliteLootPolicy`, `LootMarkerProcessor`, either faction key table, `RareClaimPolicy`, or Dimensional Core behavior.

- [ ] **Step 4: Add reusable survival and block bonuses**

Build `bonus/survival.json` from weighted food, medicine, utility, and ammunition table references. Build `bonus/blocks.json` from modest stack ranges of cobblestone/stone, stone bricks, planks/logs, glass, iron bars, ladders, scaffolding, and torches. Reference these bonus tables from all four general tiers with tier-appropriate roll/chance settings.

- [ ] **Step 5: Add curated TacZ gun bands**

Use verified default-pack IDs. Common examples: `tacz:glock_17`, `tacz:m1911`, `tacz:cz75`, `tacz:hk_mp5a5`, `tacz:uzi`, `tacz:ump45`. Valuable examples: `tacz:ak47`, `tacz:m4a1`, `tacz:hk416d`, `tacz:scar_l`, `tacz:m870`, `tacz:m1014`. Elite examples: `tacz:kar98`, `tacz:m700`, `tacz:ai_awp`, `tacz:m107`, `tacz:scar_h`, `tacz:mk14`. Confirm every chosen ID exists in the installed TacZ default pack before adding it. Keep ammo weight above guns and exclude launcher/minigun IDs.

- [ ] **Step 6: Add tier-scaled craftable component bonuses**

Create four component bonus tables containing exactly `exodus:reinforced_frame`, `exodus:power_regulator`, `exodus:phase_coil`, `exodus:signal_processor`, `exodus:spatial_lens`, and `exodus:containment_module`. Reference them from every general and specialized chest family. Configure low chance for common and specialized loot, then increasing standard, valuable, and elite chances; never add `exodus:dimensional_core`, `exodus:facility_alpha_key`, or `exodus:facility_beta_key` to these tables.

- [ ] **Step 7: Expand specialized and Lost Cities tables**

Raise useful roll budgets to roughly twice their previous output, add item variety without removing each table's identity, and map Lost Cities weights to all categories. Use a 100-point distribution where common/standard remain dominant, food/medical remain meaningful, weapons/utility/tech are present, and elite is rarer than valuable.

- [ ] **Step 8: Run focused contracts**

Run: `./gradlew test --tests dev.exodus.wasteland.loot.LootTableContractTest --tests dev.exodus.wasteland.loot.FactionEliteLootPolicyTest`

Expected: PASS with all resources parseable, faction key behavior preserved, all six craftable components tier-scaled, and the Dimensional Core absent from ordinary loot.

- [ ] **Step 9: Commit the loot slice**

```powershell
git add exodus-mod/src/main/java/dev/exodus/wasteland/loot exodus-mod/src/main/resources/data/exodus/loot_tables exodus-mod/src/main/resources/data/lostcities exodus-mod/src/test/java/dev/exodus/wasteland/loot
git commit -m "feat(loot): broaden wasteland chest rewards"
```

### Task 3: Shift Manufactured Foundation Below the Structure

**Files:**
- Create: `exodus-mod/src/main/java/dev/exodus/wasteland/placement/FoundationHeightPolicy.java`
- Create: `exodus-mod/src/test/java/dev/exodus/wasteland/placement/FoundationHeightPolicyTest.java`
- Modify: `exodus-mod/src/main/java/dev/exodus/wasteland/placement/TerrainPreparationService.java`

**Interfaces:**
- Consumes: structure placement/bottom-layer Y.
- Produces: `FoundationHeightPolicy.supportTopY(int structureBottomY)` and `FoundationHeightPolicy.fillStartY(int structureBottomY)` used identically by preflight, mutation, and verification.

- [ ] **Step 1: Write failing height-policy tests**

```java
assertEquals(63, FoundationHeightPolicy.supportTopY(64));
assertEquals(62, FoundationHeightPolicy.fillStartY(64));
assertEquals(-1, FoundationHeightPolicy.supportTopY(0));
```

Also assert the structure-owned Y is never returned as a foundation coordinate.

- [ ] **Step 2: Run the focused test and confirm red**

Run: `./gradlew test --tests dev.exodus.wasteland.placement.FoundationHeightPolicyTest`

Expected: FAIL because the policy does not exist.

- [ ] **Step 3: Implement and wire one shared coordinate policy**

```java
public static int supportTopY(int structureBottomY) {
    return Math.subtractExact(structureBottomY, 1);
}

public static int fillStartY(int structureBottomY) {
    return Math.subtractExact(structureBottomY, 2);
}
```

Replace inline `platformY - 1` and `platformY - 2` foundation calculations in preflight, mutation, `validateFoundation`, and `verify`. Keep structure placement Y unchanged. Keep clearing anchored at the structure bottom (`platformY`) so it cannot overwrite the support row at `platformY - 1`.

- [ ] **Step 4: Run placement tests**

Run: `./gradlew test --tests 'dev.exodus.wasteland.placement.*' --tests 'dev.exodus.wasteland.arena.*'`

Expected: PASS; preparation and verification agree on shifted coordinates.

- [ ] **Step 5: Commit the foundation slice**

```powershell
git add exodus-mod/src/main/java/dev/exodus/wasteland/placement exodus-mod/src/test/java/dev/exodus/wasteland/placement
git commit -m "fix(wasteland): start foundations below structures"
```

### Task 4: Full Verification and Runtime Installation

**Files:**
- Modify only if a verification failure proves a scoped defect in Tasks 1-3.
- Build output: `exodus-mod/build/libs/*.jar`
- Install destination: `mods/`

**Interfaces:**
- Consumes: all prior task commits.
- Produces: tested release JAR installed into the active Prism instance.

- [ ] **Step 1: Run the entire test suite**

Run: `./gradlew test`

Expected: PASS with no failed tests.

- [ ] **Step 2: Run the clean build gate**

Run: `./gradlew clean build`

Expected: `BUILD SUCCESSFUL` and a current non-sources JAR under `exodus-mod/build/libs/`.

- [ ] **Step 3: Install and hash the JAR**

Copy the built non-sources JAR over the existing Exodus JAR in `mods/`, then compare SHA-256 hashes of source and destination. Do not remove or modify unrelated mods.

- [ ] **Step 4: Inspect final repository state**

Run: `git status --short` and `git log -4 --oneline`.

Expected: only the pre-existing untracked `exodus-mod/logs/` remains; the spec, plan, and implementation commits are present.

- [ ] **Step 5: Report automated versus manual evidence**

Report test count, build result, installed JAR name/hash, and commits. Explicitly leave these for user verification: death respawns at the personal base, cleanup resets to Overworld spawn, custom/Lost Cities chest quality, and foundation appearance in a newly prepared arena.
