# Lost Cities 7.5.5 surface-placement research

Scope: Minecraft 1.20.1, Forge 47.4.10, Lost Cities 1.20-7.5.5. Sources are first-party Lost Cities source plus the mapped Minecraft 1.20.1 API surface.

## Conclusions for Exodus

1. Do not search for a naturally perfect site. Pick the deterministic X/Z already specified by the arena layout, fully generate every chunk touched by the template footprint, then derive and manufacture a build platform.
2. The current symptom (custom structures at Y=0 while vanilla/Lost Cities terrain is around Y=60) is consistent with reading a not-yet-generated/unprimed area or accepting a bad height sample. Height queries must happen only after all footprint chunks reach `ChunkStatus.FULL`.
3. For a normal open-sky Lost Cities world, use `landscapeType: "default"`. `wasteland` is a profile, not a landscape type; its built-in description is "Wasteland, no water, bare land" and its landscape type is `default`.
4. A fixed plains biome source is technically compatible with `lostcities:lostcity`: override the dimension JSON's `biome_source` with `minecraft:fixed` / `minecraft:plains`. Lost Cities attaches its generation feature to `#minecraft:is_overworld`, which includes plains. This must be tested in a fresh dimension/world because dimension generator JSON is bootstrap/worldgen state.
5. Recommended platform Y: sample the complete footprint with `MOTION_BLOCKING_NO_LEAVES`, discard implausible columns (air/fluid at the supporting block, or outside build limits), build a histogram of surface Y values, and choose the highest Y whose local horizontal support reaches at least 50% of the footprint (or a configurable quorum). Do not use the absolute maximum alone; one floating block can poison it.
6. Flatten the footprint at the selected Y, clear the template's complete volume above it, then fill every air/fluid column below the footprint with a foundation material down to the first solid supporting block (with a configurable safety depth). This is the same broad pattern Lost Cities uses: clear the building volume and fill unsupported underside columns.

## Lost Cities landscape types vs profiles

Lost Cities 7.5.5 has exactly six `LandscapeType` enum values: `default`, `floating`, `space`, `cavern`, `spheres`, and `cavernspheres`. The profile helpers define their operational grouping: `cavern` and `cavernspheres` are cavern modes; `spheres` and `cavernspheres` are sphere modes. [`LandscapeType.java`](https://github.com/McJtyMods/LostCities/blob/4e2be659167f9344b47a93403b1ecf8fd19e18c6/src/main/java/mcjty/lostcities/config/LandscapeType.java), [`LostCityProfile.java`](https://github.com/McJtyMods/LostCities/blob/4e2be659167f9344b47a93403b1ecf8fd19e18c6/src/main/java/mcjty/lostcities/config/LostCityProfile.java#L736-L756)

- `default`: normal Overworld-style terrain with Lost Cities generation layered into it.
- `floating`: cities on floating islands; the built-in profile recommends Lost Worlds' islands world type.
- `space`: cities in floating glass bubbles, intended with Lost Worlds' spheres/void-style world type.
- `cavern`: cities inside a cavern terrain type, intended with Lost Worlds' caves world type.
- `spheres`: cities/terrain constrained by city spheres while retaining an outside landscape/profile.
- `cavernspheres`: spheres inside large caverns, intended with Lost Worlds' cavespheres type.

These are algorithms, not the user-facing presets. The bundled 7.5.5 profiles include: `default`, `cavern`, `nodamage`, `floating`, `space`, `biosphere_caves`, `biosphere`, `onlycities`, `tallbuildings`, `safe`, `ancient`, `wasteland`, `atlantis`, `rarecities`, and `largecities`, plus internal outside profiles such as `void_outside` and `bio_wasteland`. The owning source supplies the exact descriptions and settings. [`ProfileSetup.java`](https://github.com/McJtyMods/LostCities/blob/4e2be659167f9344b47a93403b1ecf8fd19e18c6/src/main/java/mcjty/lostcities/config/ProfileSetup.java)

Important distinction: most profiles, including `wasteland`, `rarecities`, `onlycities`, and `atlantis`, still use `landscapeType: "default"`; they mainly alter city frequency, water/ground levels, damage, ruins, and related settings.

## Fixed-plains Lost Cities dimension

The stock `lostcities:lostcity` dimension is a normal `minecraft:noise` generator using `minecraft:overworld` noise settings and an Overworld `minecraft:multi_noise` biome source. [`lostcity.json`](https://github.com/McJtyMods/LostCities/blob/4e2be659167f9344b47a93403b1ecf8fd19e18c6/src/main/resources/data/lostcities/dimension/lostcity.json)

A datapack/mod resource with the same path can replace only the biome source:

```json
{
  "type": "lostcities:lostcity",
  "generator": {
    "type": "minecraft:noise",
    "seed": 0,
    "settings": "minecraft:overworld",
    "biome_source": {
      "type": "minecraft:fixed",
      "biome": "minecraft:plains"
    }
  },
  "forge:use_server_seed": true
}
```

Lost Cities' own Forge biome modifier adds its raw-generation feature to every biome in `#minecraft:is_overworld`, so fixing the biome source to plains does not inherently disable Lost Cities generation. [`lostcities.json biome modifier`](https://github.com/McJtyMods/LostCities/blob/4e2be659167f9344b47a93403b1ecf8fd19e18c6/src/main/resources/data/lostcities/forge/biome_modifier/lostcities.json)

This yields plains biome selection, not superflat terrain. The `minecraft:overworld` noise settings still create hills, caves, aquifers, etc. Therefore deterministic surface preparation is still required.

## Correct generation-before-height pattern

Minecraft's 1.20.1 chunk API exposes `getChunk(x, z, leastStatus, create)` and `ServerChunkCache#getChunkFuture(...)`; `ChunkStatus.FULL` is the appropriate completed runtime chunk boundary. [`ChunkSource`](https://mappings.dev/1.20.1/net/minecraft/world/level/chunk/ChunkSource.html), [`ServerChunkCache`](https://mappings.dev/1.20.1/net/minecraft/server/level/ServerChunkCache.html)

For a bounded structure footprint:

```java
for (ChunkPos chunk : footprintChunksWithOneChunkMargin) {
    level.getChunk(chunk.x, chunk.z, ChunkStatus.FULL, true);
}
// Only now sample level.getHeight(...) and inspect supporting blocks/fluids.
```

For multiple chunks, schedule/batch this on the server thread (for example one or a few chunks per tick) rather than synchronously generating a large arena in one tick. `ForgeChunkManager.forceChunk` is for persistent chunk tickets; it is not a substitute for explicitly obtaining the chunk at `FULL` before querying it.

Use a one-chunk margin when placement or neighbor updates can touch outside the nominal bounding box. Place the template only after every touched chunk is full. `StructureTemplate` exposes the template size and `placeInWorld(...)`, so the transformed bounding footprint should be computed before generation and terrain work. [`StructureTemplate`](https://mappings.dev/1.20.1/net/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate.html)

## Robust footprint surface selection

Recommended deterministic algorithm:

1. Compute transformed template bounds first.
2. Generate all intersecting chunks to `FULL`.
3. For every X/Z column (or a dense regular grid for very large templates), query `MOTION_BLOCKING_NO_LEAVES` and convert the returned first-free Y to support Y (`topY - 1`).
4. Reject a sample if the support block is air, replaceable vegetation, or fluid; optionally walk down a small bounded distance to find the real solid support.
5. Count valid support heights in a histogram. For each candidate Y, count columns whose support is within a small tolerance of Y (for example ±1 or ±2). Select the highest candidate meeting the configured quorum, such as 50%. This implements the user's "at least half this row/level has blocks" idea and ignores isolated floating blocks.
6. Clamp/fail if the chosen platform plus template height exceeds world build limits. A preferred band such as 55–70 may be a tie-breaker, not a hard rule; tall valid terrain remains acceptable.
7. Flatten the exact footprint to platform Y, clear `[platformY + 1, platformY + templateHeight]`, fill unsupported columns downward, then place the template.

Lost Cities itself does more than sample one point. Its `ChunkHeightmap#calculateAccurateHeight` samples multiple points per chunk and records minimum/maximum heights; scattered multibuildings aggregate minimum, maximum, and average across the entire chunk footprint and can reject excessive height differences. [`ChunkHeightmap.java`](https://github.com/McJtyMods/LostCities/blob/4e2be659167f9344b47a93403b1ecf8fd19e18c6/src/main/java/mcjty/lostcities/worldgen/ChunkHeightmap.java), [`Scattered.java`](https://github.com/McJtyMods/LostCities/blob/4e2be659167f9344b47a93403b1ecf8fd19e18c6/src/main/java/mcjty/lostcities/worldgen/gen/Scattered.java#L270-L309)

## Foundation and clearing precedent

Lost Cities' `makeRoomForBuilding` explicitly clears the building interior and repairs unsupported foundations. In normal terrain it adds border support down toward sampled terrain and places a filler block when the building base cell is air. In floating/space modes it fills underside columns downward until terrain is encountered, with a bounded depth. [`LostCityTerrainFeature.makeRoomForBuilding`](https://github.com/McJtyMods/LostCities/blob/4e2be659167f9344b47a93403b1ecf8fd19e18c6/src/main/java/mcjty/lostcities/worldgen/LostCityTerrainFeature.java#L2255-L2375)

For Exodus, the stronger user-requested variant is appropriate: across the complete custom-template footprint, fill air/fluid from `platformY - 1` downward to the first solid block. Keep a configurable maximum depth to prevent pathological work in ravines/void; if no support is found within that depth, either build a full pier to a safe lower bound or fail loudly rather than leaving a floating structure.

## Implementation guardrails

- Record/log for every placement: ID, template size, chunk range, sample count, selected Y, histogram/quorum result, minimum/maximum accepted surface, blocks cleared, and foundation blocks filled.
- Treat Y=0 (or near dimension minimum) as an explicit error for a normal open-sky arena unless the sampled terrain genuinely supports it.
- Do not infer correctness from a successful `placeInWorld` return alone; verify at least the template bounding box and foundation after placement.
- Validate in a fresh world/dimension after changing the dimension JSON, and keep real in-game inspection separate from automated build/test evidence.
