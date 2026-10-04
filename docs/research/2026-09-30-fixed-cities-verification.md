# Fixed arena cities verification — 2026-09-30

## Delivered behavior

Exodus 0.2.3 defines 64 fixed arena layouts with 69 overlapping Lost Cities neighborhood anchors each. Cities need no location search. The effective exodus profile replaces the prior biosphere mapping, disables spheres, random cities and building spawners, and preserves inherited chest/loot behavior. Urban floor targets are residential 3–6, outskirts 1–3, and five central towers 8–12. A supported characteristics event applies radial zoning before floor selection.

Only unused catalog regions can be prepared. Previously generated chunks and legacy preparation checkpoints are rejected before new layout work; saves are never deleted. An exhausted catalog or incompatible arena geometry fails explicitly. Existing generated domes remain in old worlds: create a fresh world for world-generation acceptance.

## Dedicated-server evidence

Fresh isolated world `city-smoke-20260930-6`, Forge 47.4.10, Java 17, installed Lost Cities 1.20-7.5.5:

- First and distant catalog entries (0 and 63) validated binding, all referenced assets, and fresh regions.
- Both measured 191 urban samples out of 441: 43.3107% grid coverage. This is sampled evidence, not an exact block-area measurement.
- Sixteen FULL chunks generated in approximately 9.10 seconds total. All checked chunk palettes contained no spawner blocks and no enabled spheres. Actual city-style checks passed, including a non-anchor outskirts probe and outer building floors 1–3; central towers passed 8–12.
- Used-region and legacy-checkpoint rejection passed.
- Real arena 1 reached READY, with both faction bases and two camps verified: four POIs and 92 completed structure parts/markers. This probe used zero broad pregeneration and one camp of each type to exercise real preparation without generating the entire arena.
- Smoke finished PASS and stopped normally after about 41.43 seconds of probe execution.

The existing dry-plains noise span (-64, 384) now has a matching Exodus dimension type preserving Lost Cities behavior flags. Separately, runtime found unloaded footprint chunks causing Level.getHeight to return minimum build height. Reloading already-prepared chunks before terrain sampling fixed placement; the existing surface recovery policy is retained.

## Boundaries

The isolated server includes Lost Cities and Exodus, not the full live modpack. Missing TACZ loot and simpleenemymod entity warnings are expected in this scope; NPC/weapon behavior was not verified. No third-party mods were installed or removed. No full-arena performance benchmark, visual gameplay review, or real two-client acceptance is claimed. The user performs those acceptance checks.

Smoke code is opt-in via `-PcitySmoke`; the delivered normal-build JAR must exclude the smoke subscriber. Source lives in the managed `codex/exodus-fixed-cities` worktree; it has not been merged or committed to main.

## Packaging result

162 tests passed, zero failures/errors; normal Gradle test/build passed after a clean rebuild. Delivered JAR includes all 4416 predefined neighborhood assets and excludes FixedCitySmoke. Installed into the live mods directory as exodus-0.2.3.jar; previous JAR and common configuration are recoverable in .exodus-backups/2026-09-30-fixed-cities. Live mapping is lostcities:lostcity=exodus; unrelated abyss mapping is preserved. SHA256: 921A71D23317F7546A5D6BCEE49A7918C8847DFB8320A1D002592C4EA561EA60.

