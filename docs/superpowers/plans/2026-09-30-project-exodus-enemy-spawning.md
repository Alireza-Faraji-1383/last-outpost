# Controlled Enemy Spawning Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan in this session. Steps use checkbox syntax for tracking.

**Goal:** Deliver version 0.4.0 with bounded, match-owned zombie and soldier spawning in the wasteland.

**Architecture:** Independent player allocations feed a configurable pressure policy and bounded placement service. Persistent entity tags connect spawn gates, combat, cleanup, and loot to match ownership. Reuse existing match, teleporter, and soldier loot authorities.

**Tech Stack:** Java 17, Minecraft 1.20.1, Forge 47.4.10, installed Simple Enemy Mod 0.1.6.

**Spec:** `docs/superpowers/specs/2026-09-30-project-exodus-enemy-spawning-design.md`

## Global Constraints

- Only `lostcities:lostcity` has the global mob gate; other dimensions retain their behavior.
- Preserve administrator command/egg exceptions and existing soldier weapons/ammunition.
- 30 enemies per independent player allocation, 200 global, loaded chunks only.
- No new third-party mods; English logs; real multiplayer acceptance remains manual.
- No main merge or push is authorized. Install only the verified Exodus JAR in the active instance.

## Review Focus

- Reconnect must not repeat consumed soldier opportunities: test retained per-day schedule state.
- Overlapping players must retain independent allocations: test owner/global population accounting.
- Unknown/direct mod spawns must not bypass the gate, but NBT summon commands must work: inspect Forge event lifecycle and command exceptions.
- Device reinforcement must replace ordinary budgets: test pressure policy outputs and nearest-player safety.
- Unloaded previous-match entities must not survive restart cleanup: test admission policy for stale persisted match tags.

## Task 1: Population, pressure, and scheduling policies

Files: create `enemy/EnemyPopulation.java`, `enemy/EnemyPressurePolicy.java`, `enemy/SoldierSchedule.java` and matching tests; modify `ExodusConfig.java`.

- [x] Write tests: two owner allocations can each reach 30; the global 200 cap still wins; ordinary zombie targets are 12/24; device target is remaining allocation capacity; schedule consumes opportunities even when placement/capacity fails and retains state on reconnect.
- [x] Run `.\gradlew.bat test --offline --tests 'dev.exodus.enemy.*'`, verify missing policy failure.
- [x] Implement `EnemyPopulation.remaining(UUID,int,int)`, configurable `EnemyPressurePolicy.evaluate(boolean,boolean,int,Settings)` and one-shot `SoldierSchedule.due(UUID,long,int)`.
- [x] Rerun focused tests and record results.

## Task 2: Admission, placement, lifecycle

Files: create `enemy/EnemyTags.java`, `enemy/EnemySpawnEvents.java`, `enemy/EnemyPlacement.java`, `enemy/EnemySpawnService.java`; modify `ExodusEvents.java` and match ending integration.

- [x] Test admission cases: administrator exceptions, managed current match, stale match, unauthorized loaded/direct spawn; test spawn distance and cleanup boundary policies.
- [x] Implement spawn-placement/position/finalization gates and final entity admission; persist administrator and match/allocation tags. Handle `/summon` with NBT through server command execution context.
- [x] Implement bounded loaded-chunk heightmap placement, border/collision/liquid checks, separate soldier distances, all-or-nothing group admission, and staggered independent player work.
- [x] Feed existing teleporter state into pressure and attraction; clean managed enemies on end and stale chunk admission, clear server-local state on stop/restart.
- [x] Run focused tests and compile.

## Task 3: Combat and loot

Files: create `enemy/ExodusZombie.java`, `enemy/EnemyRegistry.java`, `enemy/EnemyCombat.java`, `enemy/client/EnemyClient.java`, `enemy/ZombieLootPolicy.java`; integrate registration and existing drops handler.

- [x] Test independent zombie loot roll boundaries and 10/16, 32/48 target hysteresis.
- [x] Register a vanilla-looking adult zombie entity with sunlight immunity and wooden-door breaking at arena difficulty. Preserve environmental fire behavior.
- [x] Keep soldier player/faction goals; add bounded mutual zombie relationship and teleporter movement with combat precedence using verified selectors/events.
- [x] Replace managed zombie vanilla drops; retain soldier weapon/ammunition and existing single emerald roll.
- [x] Run focused tests and compile.

## Task 4: Release and verification

Files: `gradle.properties`, spec status, implementation evidence/acceptance documentation.

- [x] Set `mod_version=0.4.0`; run `.\gradlew.bat clean test build --offline`.
- [x] Review the entire change against the spec with a fresh reviewer; resolve actionable findings and rerun affected/full gates.
- [x] Verify JAR metadata and source/installed SHA256, replace the old Exodus JAR only; preserve third-party mods.
- [x] Report version, automated evidence, worktree source location, and manual gameplay/TPS boundaries.

## Execution ledger

- Ruling: user already authorized implementation; execute inline without another plan approval round.
- Ruling: native worktree created from HEAD; root untracked approved spec copied into it.
- Baseline attempt: missing ignored JourneyMap runtime JAR prevented API extraction; copy that existing dependency into worktree and rerun before implementation.

- Baseline rerun: test suite passed after copying the existing JourneyMap dependency.
- Final gates: Java 17, clean test build and focused enemy GameTest passed; 196 tests, zero failures/errors.
- Review: fixed reconnect backfill and target scan starvation; final independent review clean.
- Release: Exodus 0.4.0 installed with verified SHA256; previous artifact backed up; manual gameplay/TPS pending.
