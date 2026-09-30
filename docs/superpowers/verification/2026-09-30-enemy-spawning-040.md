# Exodus 0.4.0 enemy spawning verification

## Release

- Source: isolated enemy-spawning-040 Codex worktree.
- Installed artifact: active instance mods/exodus-0.4.0.jar.
- Built and installed SHA256: D12E69D9EF231A8E01F816DDD051247F1528B2DCD8E6653098538BD0A3FEA58C.
- Previous Exodus 0.3.0 artifact preserved in the worktree's release-backups/.
- Third-party mods unchanged.

## Automated evidence

Java 17; Minecraft 1.20.1; Forge 47.4.10.

Command from exodus-mod/: gradlew.bat clean test build runGameTestServer --offline -PgameTestNamespaces=exodus_enemy

Result: BUILD SUCCESSFUL, 196 unit/contract tests, zero failures/errors, and one required enemy GameTest passed.

Pure tests cover independent player allocations, global limits, day/night/device pressure, schedule consumption and reconnect behavior, range hysteresis, loot roll boundaries, cleanup distance/combat, idempotent population admission, and fair work queues under sustained 200-enemy demand.

The real Forge enemy GameTest covers unauthorized direct entity insertion, spawn egg exception persistence, administrator execute/summon with custom NBT, current/stale match admission, adult/unequipped zombies, installed door-breaking goal, no daylight ignition, preserved environmental fire, match-end discard, and preservation of test mobs.

Independent review found and resolved reconnect backfill and unfair scan-budget starvation. Final review reported no remaining actionable findings.

The original all-namespace GameTest harness was also attempted. Its existing lifecycle test fails in GameTestServer (JourneyMap dedicated-side classification when present; a null respawn assertion without it). This release does not claim that unrelated harness passed. The new gameTestNamespaces selector isolates the enemy integration test. The minimal fixture does not include the complete live modpack, so external loot tables referencing TaCZ/LR items log missing-item errors in the test runtime; no live third-party mods or loot definitions were changed to silence them.

## Manual acceptance remains pending

Restart Minecraft fully before testing; reload does not replace Java code.

1. In a fresh wasteland, confirm no automatic animals/hostiles during idle and arena preparation. Check spawners/structure-generated mobs are blocked; administrator eggs and summon still work across chunk reload.
2. Start a match: ordinary adult zombies appear 32–80 blocks away, up to 12/day or 24/night per independent player allocation. Nearby players retain summed allocations, with 30/player and 200/global limits.
3. Confirm Russian and American groups each have three members near one another, 90–110 blocks away and at least 90 blocks from every active player. Two opportunities/player/day, no night groups, no failed-opportunity retries or reconnect backfill.
4. Check soldier default player/opposing-faction combat; mutual soldier/zombie acquisition at 10 and release beyond 16; zombie/player acquisition at 32 and release beyond 48.
5. In sunlight, zombies remain unburned. Verify they actually break wooden doors at the live difficulty and cannot break walls/other blocks. The automated test checks installed goals, not completed door destruction.
6. Activate the teleporter: enemies within 96 approach it with combat precedence; reinforcement replaces ordinary pressure only for nearby active players, never adds soldier groups, and respects all caps.
7. Sample soldier weapons/ammo and 10% 1–3 emerald roll; zombie independent 1% emerald, 3% gunpowder, 3% quartz rolls. Environmental/NPC deaths can drop loot; administrative cleanup cannot.
8. Test 30-second far-enemy cleanup beyond 128 blocks, chunk unload/reload accounting, stop/end, and restart recovery with later-loaded stale enemies.
9. With real clients, observe TPS/path behavior near the 200-enemy limit. No claim of lag-free multiplayer is made from unit tests or this small server fixture.

## Default work budgets

One allocation performs placements per tick; 24 terrain candidates/tick, eight entity maintenance steps/tick, eight FIFO target scans/tick, and four FIFO device path requests/tick. Navigation advances in local 16-block legs and never adds chunk tickets. Gameplay and work thresholds are in ExodusConfig under enemySpawning.
