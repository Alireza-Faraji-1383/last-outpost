# Project Exodus fixed arena cities

## Intent and agreed appearance

Replace glass-domed, fragmented city generation with one large connected open-sky city per arena. A city occupies approximately 40–45% of the 2000-by-2000 playable area, lies slightly off-center, and stays inside the border. Most buildings remain enterable, with moderate destruction. Residential neighborhoods target 3–6 floors, a small central tower population targets 8–12 floors, and the outskirts target 1–3 floors. These are visual distribution targets, not a requirement that every building have those heights.

The user approved fixed city locations, procedurally varied buildings, and a finite expandable arena collection, and delegated routine design choices. Lost Cities owns city terrain, streets, and buildings; Exodus owns arena selection, major POIs, match lifecycle, and its existing loot markers. Preserve Lost Cities chest quantity and the current dry-plains terrain work.

## Chosen approach

Ship 64 predefined city assets paired with the first 64 positions of the existing ArenaGrid. Preserve the existing grid order. At default geometry (2000 arena, 128 buffer, 1024 safety gap), spacing is 4096 blocks. Asset coordinates and arena coordinates must come from the same validated manifest. Generate assets at build time with deterministic tooling; do not insert cities into a registry after world generation starts.

Alternatives rejected: random city profiles plus repeated candidate checks do not meet the user's no-search requirement; a runtime infinite city grid introduces a custom generation integration beyond the needed scope. The finite manifest is sufficient and can be expanded before its unused regions are generated.

Only the requested arena is generated or prepared. Defining 64 cities must not pre-generate 64 arenas. Exhaustion produces an explicit English error before allocating a record or changing the world; it never silently selects a random region or overwrites a used arena.

## Geometry and profile

Use landscapeType `default`, the existing dry-plains dimension generator, and an Exodus-owned world style. Disable random city centers (`cityChance=0`), random spheres, and sphere-based landscapes. Reconcile common.toml's existing `lostcities:lostcity=biosphere` mapping with Exodus's API registration so there is one consistent effective profile. Merely creating exodus.json is insufficient: the mod currently creates an API profile with that name, so profile loading and precedence must be verified against the installed 7.5.5 implementation.

Initial city geometry: center offset (0, -160) blocks relative to each arena, predefined influence radius 928 blocks, and cityThreshold 0.2. With a single center and a neutral biome multiplier, the ideal effective city radius is 742.4 blocks, giving approximately 43.3% coverage. This is an analytical initial target, not measured acceptance evidence. Chunk discretization, terrain, roads, parks, POIs, and generation filters affect the realized area.

Set cityMaxRadius high enough to include each predefined center in Lost Cities' neighborhood scan, even with cityChance zero. Keep height eligibility compatible with the dry-plains terrain and use neutral city-chance multipliers for its biome. Do not use the sphere `onlyPredefined` setting as a replacement for disabling random cities.

Provide coherent center, residential, and outskirts styles using supported style selection and floor constraints. Verify that the radius/style-threshold mechanism actually supports the intended zones; if it does not, define deterministic neighborhood anchors inside the single connected urban footprint rather than inventing an unsupported JSON field. Buildings remain procedural, not a hand-authored copy of one city.

Large radius values increase Lost Cities' neighborhood scanning cost. Measure chunk generation on the installed version before accepting this single-center implementation. If it is impractical, retain the same urban footprint through several predefined neighborhood anchors with smaller scan bounds. This must still appear as one connected city and preserve the no-search contract.

## Preparation and POIs

ArenaPreparationService selects the next unused manifest entry. Replace the city-coverage candidate sampling phase with bounded manifest/profile validation; retain dependency, template, chunk preparation, terrain, structure, marker, and final validation stages. Older persisted CITY_SAMPLING checkpoints must have an explicit compatibility path, not become unreadable when changing phases.

Validate manifest availability and geometry before creating an arena record. Geometry configuration incompatible with the shipped manifest must produce an actionable error before changes, rather than moving arena centers away from city assets. Store or validate a generation-layout version for new records. Existing arena records keep their coordinates and must never be relabeled as newly generated fixed-city arenas.

Keep major bases in reserved open land. For default geometry, initial base centers are (-500, 800) and (500, 800) relative to the arena, outside the urban footprint and inside the playable border with structure margins. Camp placement excludes the city footprint where necessary to preserve open-land POIs. Retain existing template orientation, composite geometry, surface recovery, marker loot, player inventory rules, and match lifecycle. This change removes city-location search, not the existing independent safety requirements for player bases or camp placement.

## Existing worlds and failures

Previously generated glass domes cannot be removed by a profile change. Do not delete saves, dimension folders, bases, or structures. Validate on a fresh test world and report that old generated chunks remain unchanged. Before preparing a catalog entry in an existing world, reject known incompatible generated regions or require a verified fresh-layout initialization; do not claim that profile changes repair old chunks.

Missing city assets, invalid styles, wrong profile binding, manifest mismatch, or exhausted catalog are explicit failures. No fallback to biosphere, random city placement, or a city-free READY arena is allowed. Ordinary chunk-generation work remains necessary and can continue incrementally under the existing configured budgets.

## Acceptance and evidence

1. Pure tests: manifest/grid alignment, finite exhaustion, bounds, city/border and base separation, geometry mismatch, and legacy checkpoint compatibility.
2. Resource checks: 64 valid 7.5.5 predefined cities, referenced styles, disabled random cities/spheres, correct dimension/profile, dry-plains compatibility, and no missing dependencies.
3. Full Gradle test and build gates pass. Inspect the built JAR for all generated resources before installing the Exodus JAR; do not install/remove third-party mods.
4. Fresh-world server smoke: first and distant catalog entries resolve their cities without candidate search; inspect city coverage, connected footprint, chunk-generation performance, and English status/error reporting. Do not mark an arena READY with missing city data.
5. Fresh-world visual verification: no glass domes, city wholly within border, readable streets, target height distribution, moderate ruins, and bases outside the city. Measured coverage must be reported separately from the analytical 43.3% estimate.
6. Real two-client multiplayer acceptance remains manual and unverified until the user reports success.

## Verified sources

- Installed mods/lostcities-1.20-7.5.5.jar: javap confirmed PredefinedCity's dimension/coordinate/radius/style accessors and City.isCityCenter/getCityRadius precedence for predefined assets, plus height filters and cityMaxRadius neighborhood scan.
- https://mcjty.eu/docs/mods/lost-cities/asset_structure#predefined-cities
- https://github.com/McJtyMods/LostCities/blob/1.20/src/main/java/mcjty/lostcities/worldgen/lost/City.java
- https://github.com/McJtyMods/LostCities/blob/1.20/src/main/java/mcjty/lostcities/worldgen/gen/Spheres.java

## Scope exclusions

No new combat, victory, loot system, HUD, third-party mods, save deletion, or unrelated terrain redesign. Preserve in-progress workspace changes. This document records the design; it does not claim implementation or runtime verification.

