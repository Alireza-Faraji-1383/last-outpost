# Project Exodus Parties Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans inline. The user explicitly requested main development, integration, and commits without another worktree.

**Goal:** Deliver voluntary match-owned two-player parties with delayed betrayal, shared distinct component progress, private teammate identification, and Manhunt failure on alliance.

**Architecture:** A pure PartyState manages membership, invitations, and tick deadlines. PartyService validates Minecraft participation and emits notices and event callbacks. Existing recipient map snapshots carry only the recipient's teammate; existing combat and progress paths consult party authority.

**Tech Stack:** Java 17, Minecraft 1.20.1, Forge 47.4.10, JUnit 5, JourneyMap 6.0.6 API 2.0.0.

**Spec:** docs/superpowers/specs/2026-10-04-project-exodus-parties-design.md

## Global Constraints

- Work in main. Preserve all approved worktree features and current inventory/respawn/victory rules.
- English text and logs. No third-party mod installations or removals.
- Party limit two; default invite and betrayal duration 30 seconds. Capture deadlines at creation.
- Full Gradle test/build before installing an unused version greater than 0.6.0.
- Human two-client acceptance remains manual.

## Review Focus

- Permission inheritance: normal players can use party commands; start/stop/event/arena remain operator-only.
- Failed invitation acceptance cannot remove existing membership or create extra members.
- Departure deadlines are idempotent and continue across disconnects; spectators cannot receive private map positions.
- Same-tick Manhunt alliance is resolved before timeout/death rewards; no stale winner can claim a reward.
- Shared progress unions inventory types, not duplicate counts or separate device inventories.

### Task 1: Recover approved worktree deliveries

- [x] Commit source and documents in outstanding fixed-cities/events worktrees, excluding generated logs/caches.
- [x] Merge into main preserving current fixes, loot, horses, finite city manifest, and installed event behavior; resolve conflicts by responsibility rather than version age alone.
- [x] Run full test/build on merged baseline. Commit integration and main-only workflow rule.

### Task 2: Party authority and player commands

**Files:** party/PartyState.java, PartyService.java, PartyCommands.java; ExodusCommands.java, ExodusConfig.java, MatchSessionService.java, MatchManager.java; party/PartyStateTest.java.

**Interface:** `PartyState(UUID matchId)`; `create(UUID)`, `invite(UUID,UUID,long,long)`, `accept(UUID,UUID,long)`, `decline(UUID,UUID)`, `leave(UUID,long,long)`, `remove(UUID)`, `tick(long)`, `sameParty(UUID,UUID)`, `teammate(UUID)`. Transitions return explicit results for command feedback. `PartyService.sameParty(MinecraftServer,UUID,UUID)` is the shared authority for other modules.

- [x] Write tests and observe red for explicit acceptance, creator-only invites, capacity, concurrent invites, expiration, repeated leave, protection at tick 599 versus 600, match reset and final departure.
- [x] Implement pure transitions, then validate active players in Forge commands. Root `/exodus` is public but each existing administrative subtree retains permission level 2.
- [x] Add configuration defaults and lifecycle hooks before event/progress/map tick. Preserve membership during disconnect grace; remove permanently departed/eliminated members. Clear on start, stop, failed commit, recovery, server stop.
- [x] Run focused PartyState tests green.

### Task 3: Combat and shared progress

**Files:** party/PartyCombatEvents.java, party/PartyProgressService.java; session/MatchBossBarService.java; device/ComponentProgressPolicy.java; party/PartyProgressTest.java; gametest/PartyGameTests.java.

- [x] Write red tests for union `1 | 1 == 1`, `1 | 2 == 3`, invalid match components, and best progress across independent devices.
- [x] Resolve DamageSource attacker directly or through owned projectiles. Cancel friendly player damage at attack/hurt/damage boundaries, without cancelling environmental damage or self-damage. Inspect installed TacZ bytecode to confirm attribution and cancellation timing.
- [x] Scan both members' current inventories/cursor stacks and evaluate the pooled mask against each owned device separately. Keep active countdown unchanged.
- [x] Run progress tests green and add real server GameTest for melee/projectile-attributed damage, environmental damage and post-betrayal damage.

### Task 4: Private teammate tags and moving map marker

**Files:** map/MatchMapSnapshot.java, MatchMapService.java, MatchNameTagState.java; network/MatchMapPacket.java, ExodusNetwork.java; map/client/MatchNameTagClient.java, JourneyMapAdapter.java; map and network tests.

- [x] Write red tests for teammate-only tag visibility, stranger concealment, dimension mismatch, clear/disconnect, packet round-trip and recipient position filtering.
- [x] Include at most one teammate UUID in recipient snapshots and a TEAMMATE marker only for eligible co-located match players. Use configurable bounded refresh, remove immediately on membership/lifecycle change, and avoid public location lists.
- [x] Render teammate name in green using RenderNameTagEvent; show green JourneyMap marker using the existing overlay API. Bump protocol when changing packet layout.
- [x] Run privacy, projection, tag and network tests green; compile against installed API.

### Task 5: Manhunt alliance failure

**Files:** event/EventManager.java, event/domain/ObjectiveProgress.java, event/domain/ManhuntPartyPolicy.java; event tests, gametest/PartyGameTests.java.

- [x] Write red tests for pair selection excluding teammates, available players with no legal opponent, alliance before deadline/death, terminal FAILED with empty winners and rejected reward claims.
- [x] Choose from legal distinct ordered pairs, not an unchecked shuffled player list. Notify EventManager synchronously after successful party acceptance; fail affected Manhunts and clear sidebar through existing terminal lifecycle.
- [x] Keep a tick-time guard as defense and preserve other event outcomes/rewards.
- [x] Run focused event tests and real server GameTest for no emerald payout after alliance.

### Task 6: Verify, install and commit

- [x] Run fresh-context whole-change review per executing-plans; address material findings with regression tests.
- [x] Bump to available version 0.7.0 and update VersionContractTest; distribution builders derive version from gradle.properties.
- [x] Run `gradlew.bat clean test build` with Java 17 and applicable Forge GameTests. Verify version/assets and JAR hash.
- [x] Back up the previous private Exodus JAR outside mods and install newly versioned JAR. Preserve third-party mods.
- [x] Write manual two-client steps and commit all source/docs on main, excluding generated logs/caches. Report commits and unverified client boundary.

## Execution ledger

- User instruction overrides default worktree and artifact-approval gates; execute on main continuously.
- Fixed-city and event source were previously left outside main; integrate both before party changes, preserving newer terrain sampling and current balance.

## Delivered evidence

- Outstanding fixed-city work saved as e1d85ff and integrated into main in b2cefc4; event work saved as 65252d3 and integrated into main in f3d696d. Preserve newer terrain recovery, loot/equipment, horses and component acquisition fixes.
- Pure membership, progress and Manhunt tests observed missing-implementation failures before implementation; subsequent full suites passed.
- Fresh read-only reviewer found no actionable defects in party commands/state, component union, map privacy, Manhunt and integration changes. TacZ attribution was then verified against installed bytecode; the additional pre-hit bridge protects incendiary hits before ignition.
- Forge fixture corrections: use templateNamespace for cross-namespace templates; remove vanilla initial spawn immunity and enable PvP in simulated players. Environmental damage and post-separation hits then demonstrate real damage rather than false-positive immunity checks.
- Fresh Java 17 `gradlew.bat clean test build`: 263 JUnit tests, zero failures/errors/skips, BUILD SUCCESSFUL.
- Forge `runGameTestServer -PgameTestNamespaces=exodus_party,exodus_events`: both required tests passed, including commands, damage/progress, actual recipient map packets and no-reward Manhunt alliance. Runtime evidence remains in exodus-mod/run/logs/latest.log.
- Optional TacZ ABI contract reads the actual installed 1.1.8-hotfix JAR and verifies the cancelable Pre class, getters and constructor. Actual Pre construction posts KubeJS and requires launched Forge; runtime behavior is tested through the simulated equivalent in GameTests. Real client shooting remains manual.
- Installed exodus-0.7.0.jar, SHA256 65C3C84ACC791F79929F60BCD19D7D8500C69D4EFCD79182E9DF4CBF258602F4. Release contains 4416 predefined-city assets, party/event classes and no GameTest/smoke classes.
- Previous 0.6.0 JAR preserved outside mods in .exodus-backups/2026-10-04-parties-0.7.0. No third-party mod changes.
- Generated logs/cache are no longer tracked; their local files remain intact. Work continues on main and is committed locally.
- Human two-client visual/gun/JourneyMap acceptance is pending; see docs/project-exodus-parties-manual-test.md.