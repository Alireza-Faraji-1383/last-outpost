# Project Exodus Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a stable Forge 1.20.1 foundation for one Project Exodus match with admin commands, incremental safe base allocation, structure placement, lifecycle tracking, recovery, and persistent returns.

**Architecture:** A dedicated server-side Forge mod owns authoritative state. Pure Java policy classes handle allocation and lifecycle decisions; Forge adapters handle worlds, structures, players, commands, ticks, events, SavedData, and configuration. World mutations happen only after preflight succeeds.

**Tech Stack:** Java 17, Minecraft 1.20.1, Forge 47.4.10, ForgeGradle 6, JUnit 5.

**Spec:** `AGENTS.md` plus the accepted handoff decisions in the initiating conversation.

## Global Constraints

- Do not add or remove third-party mods.
- Keep all messages and logs English.
- Preserve existing worlds and structures; never perform destructive reset behavior.
- Development fallback is explicit and off by default.
- Do not claim two-client multiplayer verification; the user performs it manually.

## Review Focus

- Abort before commit when one player cannot receive a valid base.
- Restore the exact previous border on every exit path including restart recovery.
- Do not turn initial creative/spectator observers into Survival at cleanup.
- Expired disconnected players must return as spectators and still receive pending world-spawn return.
- Old persisted base centers must participate in future distance rejection.

---

### Task 1: Forge project and pure domain model

**Files:** Create `exodus-mod` build metadata, match/base records, configuration constants, allocation geometry, and JUnit tests.

**Interfaces:** Produces deterministic candidate validation and match membership/grace decisions consumed by runtime services.

- [ ] Write failing tests for distance, border margin, slope, player eligibility, grace expiry, and persisted-base rejection.
- [ ] Run tests and confirm missing domain types cause RED.
- [ ] Implement the smallest pure Java domain model and policies.
- [ ] Run the complete test suite and confirm GREEN.

### Task 2: Persistent state and recovery

**Files:** Create Forge `SavedData` models and serialization tests.

**Interfaces:** Consumes domain records; produces match snapshots, placed-base registry, pending returns, and saved border state.

- [ ] Write failing round-trip and recovery-policy tests.
- [ ] Implement NBT serialization and IDLE recovery rules.
- [ ] Run the complete test suite.

### Task 3: Commands and incremental start pipeline

**Files:** Create command registration, match manager, allocation job, location finder, and status formatting.

**Interfaces:** Consumes policies/persistence; produces `/exodus start|stop|status|players` behavior and tick-driven preflight.

- [ ] Write failing command/parser and status-format tests.
- [ ] Implement command tree and preflight queue with two checks per tick and 120-second timeout.
- [ ] Compile against Forge 47.4.10 and run all tests.

### Task 4: Structure, border, player lifecycle, and cleanup adapters

**Files:** Create structure service, border snapshot adapter, event subscribers, teleport safety, and cleanup logic.

**Interfaces:** Consumes successful preflight; produces committed bases, safe teleports, grace transitions, auto-stop, and pending login returns.

- [ ] Write failing policy tests for cleanup categories and pending returns.
- [ ] Implement Forge adapters and event handlers.
- [ ] Compile and run all tests.

### Task 5: Packaging and runtime verification

**Files:** Add mod metadata, default config, structure placeholder instructions, operator test guide, and install built JAR into `mods/`.

**Interfaces:** Produces a launchable instance artifact and an explicit manual multiplayer checklist.

- [ ] Run `gradlew test` and `gradlew build` with Java 17.
- [ ] Install only the Project Exodus JAR into the instance.
- [ ] Launch the client/server far enough to verify Forge loads the mod and KubeJS reports no new script errors.
- [ ] Record any unverified multiplayer or missing-structure boundary honestly.
