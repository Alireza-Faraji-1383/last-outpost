# Project Exodus Event System Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans in this session. Track each task below.

**Goal:** Deliver an extensible server-authoritative event foundation, Zombie Hunt, Manhunt, and blue supply events with a unique day-5 Dimensional Core.

**Architecture:** Pure scheduling/objective policies drive Forge adapters. Persist match event history and event-owned drops in MatchSessionState. Supply delivery, map presentation, private vanilla scoreboard packets, and rewards have separate classes.

**Tech Stack:** Java 17, Minecraft 1.20.1, Forge 47.4.10, JUnit 5, existing JourneyMap API.

**Spec:** docs/superpowers/specs/2026-10-04-project-exodus-event-system-design.md

## Global Constraints
Preserve inherited local edits. English text/logs. No third-party mods. Version increment to a new unreleased JAR. Full clean test/build. Manual two-client acceptance remains unverified. User explicitly requested continuous implementation; implement inline, without repeated artifact approvals or unsolicited main commits.

## Review Focus
- Prey death versus deadline and final-phase elimination: death wins before eligibility cancellation.
- Multiple event sidebars: private Manhunt takes priority and the shared hunt returns afterward.
- Restart and chunk unload: event-owned entities/crates expire or clean up without duplicating core.
- Full inventories and cancelled/repeated death notifications: reward once, overflow remains collectible.
- Existing radio drops: appearance/payment/quota/cleanup unchanged.

## Files and interfaces
- event/domain/EventDefinition, EventSchedule, ObjectiveProgress: pure definition validation, weighted eligibility and idempotent objective resolution.
- event/EventCatalog and EventCatalogReloadListener: validated datapack definitions.
- event/EventSavedState: scheduling history and persistent owned drop records; stored in MatchSessionState.
- event/EventManager and EventHooks: lifecycle, concurrent run registry, objectives and admin commands.
- event/EventScoreboardService and EventRewardService: recipient-specific vanilla packets and emerald delivery/chat.
- supply/event/EventAirdropService: bounded terrain placement and existing falling entity/crate integration.
- SupplyDropEntity/SupplyCrateBlockEntity: optional event ownership, blue glow and unique payload, persistent lifetime/cleanup.
- MatchMapService/MapLocation/JourneyMapAdapter: public blue drop markers.
- ExodusConfig/ExodusCommands/ExodusEvents/MatchSessionService: wiring and configurable defaults.

### Task 1: Pure event policies
- [x] Write tests for day eligibility, weighted roll, one-time/cooldown/milestone rules, targeted exclusivity, kill deadline and contributor rewards, any-source prey death, timeout, duplicate reward resolution.
- [x] Run targeted tests and observe missing implementation failure.
- [x] Implement pure policies and run tests green.

### Task 2: Forge lifecycle and presentation
- [x] Add datapack catalog, persistence, config and scheduler/objective hooks using the pure policies.
- [x] Add private sidebar packets with timer/kills/distance, restore prior sidebar, choose private event before global.
- [x] Add permission-level-2 /exodus event start <id>, status and stop commands.
- [x] Deliver emeralds exactly once with English confirmation and inventory-overflow handling.
- [x] Compile and run focused tests.

### Task 3: Event airdrops and map
- [x] Add loaded/safe surface placement bounded by candidates-per-tick and attempt limits; queue day-5 retry.
- [x] Reuse existing SupplyDropEntity with optional event flag, blue getTeamColor, smoke, lifetime and land callback.
- [x] Add persistent event-owned crates and one bound core via rareClaims reservation after successful entity spawn.
- [x] Add public map marker and match cleanup/recovery.
- [x] Add three high-value loot tables: medical (2 existing medical-table rolls plus 3-5 golden apples and 8-12 emeralds), combat (2 rare weapon-table rolls, 4 native ammo rolls, 2 armor rolls and 2 attachment rolls), equipment (2 equipment rolls, 2 zero-contact rolls, 2 attachment rolls and 12-20 emeralds). Preserve existing chest tables/counts.
- [x] Compile and validate loot references and drop cleanup.

### Task 4: Verification and delivery
- [x] Add Forge GameTests for drop landing/core payload/blue state and reward/UI/objective routing where supported.
- [x] Self-review and fresh reviewer per executing-plans skill; fix material findings.
- [x] Bump authoritative version and version contract to 0.6.0 after checking availability.
- [x] Run gradlew.bat clean test build and applicable event GameTests.
- [x] Install new JAR to active mods while retaining old version in backup outside mods; do not touch third-party mods.
- [x] Write manual acceptance guide and report exact verification boundary.


## Delivered evidence
- 227 JUnit tests passed; clean test build passed.
- Event Forge GameTest passed (real server objects and simulated packet recipients).
- Release 0.6.0 installed in the active instance; previous private Exodus JAR backed up.
- Code remains in codex/exodus-event-system worktree; no main merge or commit.
- Human two-client and visual acceptance remains pending.


## Subsequent main integration
On 2026-10-04, the user requested outstanding worktree integration and all future work directly on main. Event source was saved in 65252d3 and merged into main in f3d696d, then preserved in the party release 0.7.0. The earlier worktree-only delivery note above describes the original delivery.
