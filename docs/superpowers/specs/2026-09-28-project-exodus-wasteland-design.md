# Project Exodus Wasteland Dimension Design

## Goal

Run every Project Exodus match inside a persistent Lost Cities dimension while keeping arena selection, preparation, unique POIs, player bases, loot, lifecycle, and recovery authoritative in the Exodus Forge mod.

## Dependencies and ownership

- Minecraft 1.20.1, Forge 47.4.10, and Lost Cities `1.20-7.5.5` are pinned runtime dependencies.
- The playable dimension is `lostcities:lostcity`; the Overworld is the restricted lobby and return destination.
- Lost Cities owns terrain, roads, normal city buildings, and city chests.
- Exodus owns arena lifecycle, match borders, player bases, unique bases, camps, marker processing, loot tables, access control, and recovery.
- No Autogen, Autoloader, DevTool, Modern Tweaks, or other Lost Cities add-on is required.

## World and arena lifecycle

- The Lost Cities dimension is persistent. It is not created or deleted per match.
- Each arena is square, single-use, and moves to a fresh grid cell after consumption.
- Default playable size is 2000 blocks. Pregeneration adds a 128-block buffer on every side.
- Grid spacing is computed as `ceil((arenaSize + 2 * pregenerationBuffer + safetyGap) / 1024) * 1024`, with a default safety gap of 1024. Default spacing is therefore 4096; a 3000-block arena produces 5120.
- Exactly one prepared arena is kept ready. Consumed arenas remain on disk and are never deleted automatically.
- Arena states are `PREPARING`, `READY`, `CONSUMED`, `FAILED`, and `ABANDONED`.
- `/exodus arena prepare` runs incrementally, has no global timeout, survives restarts, and revalidates uncertain steps.
- `/exodus arena status` reports state, phase, progress, coordinates, consumed count, and an approximate generated-chunk total.
- `/exodus arena cancel` stops safely and marks the partial arena `ABANDONED`; generated chunks are retained.
- Progress is sent at 10-percent boundaries to the initiating operator, online permission-level-2 operators, and console only.
- `/exodus start` never starts a preparation job. It requires a `READY` arena.

## City profile and validation

- Exodus supplies a Lost Cities profile derived from `rarecities` with medium destruction and a target city density of roughly 40-45 percent.
- City chest count is not reduced.
- Before full pregeneration, Exodus uses the Lost Cities API to sample city chunks incrementally.
- A candidate grid cell is accepted when estimated city coverage is at least 15 percent. If the API path proves to generate chunks or exceed its configured per-tick budget, the guard is skipped with an explicit status/log message and profile density remains the fallback.
- Exact density is intentionally not normalized; some matches may have less loot than others.
- The city should normally be off-center. This is a preference used for candidate scoring, not a reason to reject every otherwise valid arena.

## Structure catalog

### Player bases

- The existing `exodus:starter_base` pool remains player-dependent and uses rotation `NONE`.
- Placement keeps the existing six-block burial contract: structure origin Y is `surfaceY - 6`.
- Exodus aggressively clears the exact player-base placement volume because the base is primarily underground.
- Player bases remain at least 250 blocks apart, inside the safe border, and outside Lost Cities city chunks and POI exclusion bounds.

### Unique faction bases

- Every arena contains exactly one Russian base and exactly one American base.
- Each base is a four-template composite with rotation `NONE`, final dimensions 57x20x60, and a separately configurable vertical offset.
- Files are `russian_base_1.nbt` through `russian_base_4.nbt` and `american_base_1.nbt` through `american_base_4.nbt`.
- Part 1 is the anchor. With the builder's orientation, image-up is +Z and image-right is -X. Template origins are placed at offsets: part 1 `(0,0,0)`, part 2 `(+29,0,0)`, part 3 `(0,0,+30)`, and part 4 `(+29,0,+30)`.
- Expected part sizes are: part 1 29x20x30, part 2 28x20x30, part 3 29x20x30, and part 4 28x20x30.
- The four pieces are validated and committed as one logical POI. Missing or wrong-sized pieces prevent `READY`.
- Russian and American bases occupy different arena halves, remain far apart, and receive roughly comparable player access without forced mirror symmetry.

### Camps

- Abandoned camp files are `abandoned_camp_01.nbt`, `abandoned_camp_02.nbt`, and so on.
- Occupied camp files are `occupied_camp_01.nbt`, `occupied_camp_02.nbt`, and so on.
- Current camp templates are 11x11x16 and may rotate by 0, 90, 180, or 270 degrees.
- Each arena receives 6-10 abandoned camps and 3-5 occupied camps, seeded deterministically.
- Camps keep at least 120 blocks from each other and 150 blocks from player bases.
- Placement first seeks terrain with at most two blocks of height variation. If necessary, bounded flattening, filling, vegetation clearing, and entrance repair are allowed.
- Occupied-camp entities are copied from the template once. They do not respawn. Missing entity types fail arena preparation clearly, and resume must not duplicate entities.

## Placement and terrain mutation

- Preparation prefers natural placement but may clear and flatten exact POI bounds plus a small configured margin.
- Major POI placement is guaranteed by Exodus rather than trusted to random Lost Cities generation.
- Terrain mutation may not overlap another registered POI, player base, arena, or the protected city exclusion selected for that POI.
- Every multi-step placement stores an idempotency record before advancing so restart recovery cannot place a part, marker result, chest, or entity twice.

## Loot markers and loot behavior

- Structure templates use structure blocks with `mode:"DATA"` and one of these metadata values:
  - `exodus:loot/general/common`
  - `exodus:loot/general/standard`
  - `exodus:loot/general/valuable`
  - `exodus:loot/general/elite`
  - `exodus:loot/food`
  - `exodus:loot/weapons`
  - `exodus:loot/medical`
  - `exodus:loot/utility`
  - `exodus:loot/tech`
- Placement replaces every recognized marker with a chest whose unopened loot is backed by `data/exodus/loot_tables/chests/...`.
- Unknown Exodus markers fail preparation. Non-Exodus data markers remain untouched for their owning system.
- Loot is shared, public, generated once on first open, never refilled, and remains empty after looting.
- Lost Cities keeps its original chest density. Exodus overrides loot quality rather than removing chests.
- Ordinary city buildings primarily produce common/standard loot. Valuable loot is rare, elite loot is absent, and city weapons are mostly ammunition with a very small gun/attachment chance.
- Faction bases are the main source of weapons, valuable loot, and limited elite loot. Occupied camps are stronger than abandoned camps but weaker than faction bases.
- Match-unique teleporter components are never random entries in ordinary loot tables; their guaranteed distribution remains a separate authoritative system.

## Match and access integration

- The Overworld is the lobby and return destination. Starting a match teleports eligible players to bases in the ready Lost Cities arena.
- The match border remains the configured square around the consumed arena center and is restored on every cleanup/recovery path.
- Normal players may not enter `lostcities:lostcity` outside an active authorized match. Unauthorized arrivals return to the Overworld lobby.
- Permission-level-2 operators receive `/exodus dimension enter` and `/exodus dimension leave`. Their current game mode is preserved and they never join match accounting.
- Match cleanup returns associated players to the Overworld lobby spawn, not the Lost Cities world spawn. Offline users receive the existing persistent pending return.
- Natural hostile spawning and vanilla day/night/weather remain enabled for the first version. The natural-spawn switch is configurable so a later enemy system can replace it.

## Commands and failure behavior

- Arena and dimension commands require permission level 2.
- All player-facing text and logs are English.
- Missing Lost Cities, the wrong pinned version, missing templates, invalid composite dimensions, unknown markers, failed mandatory placement, or incomplete validation prevent `READY` and `/exodus start`.
- Cosmetic terrain problems trigger bounded repair rather than rejecting the match.
- No operation deletes old arena chunks or structures.

## Verification boundary

- Pure layout, state, grid, progress, persistence, sampling, marker, and placement-policy logic receives JUnit coverage.
- The full Gradle `clean test build` gate must pass.
- A dedicated-server smoke must prove dependency loading, profile binding, command registration, save/resume, and one arena preparation through a bounded test configuration.
- Real two-client multiplayer acceptance remains manual and cannot be claimed until the user reports it.
