# Project Exodus controlled enemy spawning

Date: 2026-09-30
Status: Approved and implemented in 0.4.0; automated verification passed, real multiplayer acceptance pending.

## Scope

This feature extends the existing foundation scope with controlled zombies and Russian/American Simple Enemy Mod soldiers. No additional enemy species, progression rules, or third-party mod installation/removal are included. Match authority remains in the Forge mod.

Control applies throughout `lostcities:lostcity`, including idle state, arena preparation, and running matches. Other dimensions retain their behavior. Block natural, spawner, and structure-generated mob spawning. Preserve administrator spawn eggs and `/summon` for testing. Spawn only managed enemies during a RUNNING match.

Remove pre-existing unauthorized mobs incrementally during preparation/chunk loading. Preserve administrator test mobs. Keep cleanup simple; the user may create a fresh world rather than requiring migration tooling. Spawn-source tracking and persisted management tags must distinguish managed enemies, administrator exceptions, and unauthorized mobs; do not infer authorization solely from entity type.

## Population and scheduling

- Each active match player has an independent allocation: at most 30 living managed enemies, including soldiers.
- Global match limit: 200 living managed enemies. All ordinary and device-related enemies share these limits.
- No player grouping/clustering algorithm. Nearby players retain separate allocations: two players can collectively have up to 60 enemies. Attribute each spawned enemy to one allocation, avoiding duplicate counting.
- Ordinary zombie targets per player: 12 by day, 24 by night. Living soldiers reduce available capacity under the 30-enemy allocation limit.
- Replenish gradually, at most 2 zombies per player allocation every 5 seconds, until its target or available capacity is reached.
- Zombies spawn 32–80 blocks from their allocation's active player, on valid terrain in already loaded chunks. Respect the match border. Never load new chunks for enemy spawning.
- Each active player's daily soldier schedule has two separate daytime opportunities: one group of 3 Russians and one group of 3 Americans, in random order, in the first and second halves of daytime. Stagger opportunities with bounded randomized timing.
- Daytime opportunities follow world time, not elapsed time per player. Starting after an opportunity does not backfill it. No new soldier groups at night; surviving daytime soldiers remain.
- Soldiers spawn 90–110 blocks from their allocation's player and at least 90 blocks from every active player. Each scheduled opportunity performs one bounded placement search. No suitable position means the opportunity is skipped permanently.
- Soldier groups require capacity for all three members. Insufficient local/global capacity skips the opportunity without retries or partial groups.
- Track consumed opportunities so disconnect/reconnect cannot grant additional groups in the same world day.

## Combat behavior

- Only ordinary adult zombies, without random equipment, are spawned in this version.
- Managed zombies do not burn from sunlight. Other fire behavior is not changed.
- Zombies may break wooden doors; they do not destroy walls or other blocks. Door-breaking must work under the arena's actual difficulty/settings, rather than assuming vanilla defaults suffice.
- Zombies acquire players within 32 blocks and stop pursuing beyond 48 blocks.
- Russian/American combat with players and opposing soldiers retains the installed mod's default range and behavior. Same-faction friendly fire/targeting is not introduced.
- Soldier–zombie combat is mutual: start within 10 blocks and stop pursuit beyond 16 blocks. Apply the range specifically to this target relationship.

## Active Exodus Teleporter

- Use the existing active teleporter state; do not introduce a separate device activation authority.
- Existing zombies and soldiers within 96 blocks are attracted toward the active device, using loaded chunks only.
- Immediate valid combat takes priority; after combat ends, attraction can resume.
- Device reinforcement is active only while at least one active player is within 96 blocks of the device.
- For each qualifying player allocation, raise the zombie target to the remaining capacity of its 30-enemy limit and replenish at most 4 zombies every 5 seconds. This replaces its ordinary target/rate rather than adding a second allocation.
- Reinforcements spawn toward/around the device while preserving the agreed 32-block minimum distance from active players and valid loaded terrain. Do not silently relax placement safety when no valid position is available.
- Device activation does not grant additional soldier opportunities, including at night. Existing soldiers may approach the device.

## Loot

Managed soldier deaths retain existing weapon/ammunition drops, plus an independent 10% chance for 1–3 emeralds. Integrate with the existing `SimpleEnemyDrops` handler; do not add a duplicate emerald roll.

Managed zombie deaths replace vanilla drops with three independent rolls:

| Item | Chance | Quantity |
| --- | --- | --- |
| Emerald | 1% | 1 |
| Gunpowder | 3% | 1 |
| Quartz | 3% | 1 |

Multiple successful rolls may drop multiple item types. Loot applies regardless of whether the killer is a player, another enemy, or the environment. Administrative despawning and lifecycle cleanup produce no loot. XP has not been overridden by this design; preserve existing behavior.

## Cleanup and lifecycle

- Enemies outside 128 blocks of every active player and the active device for 30 seconds are removed incrementally, provided they are not fighting.
- Stop/end removes all managed enemies without loot. Restart recovery invalidates previous-match enemies, including enemies encountered later when their chunks load.
- Use persistent match/allocation identifiers for cleanup and reconciliation. Do not resume a match after restart.
- Administrator test mobs are not included in managed match population or match-end cleanup.

## Implementation architecture

Separate spawn eligibility, population accounting, placement, soldier scheduling, combat integration, teleporter pressure, loot, and cleanup. Gameplay thresholds belong in `ExodusConfig`.

Expose a small pressure-policy input containing world day/time, active-player state, and active-device state, with outputs describing allowed enemy types, targets, and replenishment budgets. The initial policy implements only the agreed fixed rules. Future day progression or additional enemy types can replace/extend the policy and enemy definitions without rewriting placement and lifecycle handling; do not build a generic scripting engine or implement future progression now.

Reuse `MatchManager`, existing server tick/lifecycle integration, `TeleporterSavedState`, and `SimpleEnemyDrops`. Verify the installed Simple Enemy Mod's actual targeting/goals and entity identifiers before implementation; do not guess its APIs or globally change its behavior in other dimensions.

Bound placement candidates, target checks, navigation refreshes, and cleanup work per tick. Stagger player work, avoid whole-world scans, and use cached managed-entity bookkeeping with loaded-chunk reconciliation. Under pressure, defer zombie replenishment rather than exceeding budgets. Soldier opportunities remain one-shot. Log actionable placement/integration failures in English without per-tick spam.

## Verification

Test pure policies for independent player allocations, 30/200 caps, day/night targets, device replacement budgets, one-shot soldier opportunities, full-group capacity, range hysteresis, loot boundaries, and cleanup eligibility. Compile against Forge 47.4.10/Minecraft 1.20.1 and run the full Gradle test/build gates.

Manual in-game acceptance covers idle/preparation spawn blocking, administrator exceptions, soldier default combat, mutual zombie targeting, daylight protection, door breaking, loaded-chunk placement, device attraction, loot, cleanup/restart, and multiplayer performance near the 200-enemy limit. Automated tests/builds alone do not prove acceptable TPS or two-client behavior; the user performs final real multiplayer acceptance.
