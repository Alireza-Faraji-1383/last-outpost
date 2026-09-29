# Project Exodus Deterministic Surface Placement Design

## Goal

Place every arena-owned custom point of interest at its predetermined X/Z coordinates on a manufactured, supported surface in the Lost Cities dimension. Faction bases and camps must no longer appear at Y=0, underground beneath normal terrain, or floating over an unfilled void.

Player starter bases retain their existing allocation and burial behavior because that path is already working and is intentionally different from surface POIs.

## World-generation contract

- The playable dimension remains `lostcities:lostcity`.
- Exodus uses the Lost Cities `wasteland` profile with `landscapeType: "default"`.
- `floating`, `space`, `spheres`, and `cavernspheres` are not used. In particular, the arena must not generate glass-sphere islands.
- Exodus overrides the dimension biome source to fixed `minecraft:plains` while retaining the `minecraft:overworld` noise settings and the `lostcities:lostcity` dimension type.
- Fixed plains controls biome selection only; it does not make the terrain superflat and does not replace Lost Cities generation.
- The dimension/world-generation change is validated in a newly created world. Existing generated Lost Cities chunks are not rewritten or deleted.

## Scope

This design applies to arena-owned surface POIs:

- Russian faction base
- American faction base
- Abandoned camps
- Occupied camps

The player-dependent `exodus:starter_base` placement path is excluded. Loot-marker processing, structure contents, structure rotation, arena X/Z layout, and placement counts remain unchanged.

## Deterministic placement pipeline

### 1. Keep the layout X/Z

The arena layout remains the sole authority for each POI's horizontal origin. Placement does not search outward for a naturally flat location and does not move a POI to a different X/Z after the layout has been persisted.

### 2. Generate the required chunks

Before reading terrain height, Exodus computes the transformed bounding footprint for the complete logical POI. A composite faction base is treated as one footprint rather than four independent templates.

Every chunk intersecting the footprint plus a one-chunk margin must reach `ChunkStatus.FULL`. Chunk generation is advanced incrementally on the server thread with a persisted cursor so preparation can resume safely after a restart. Height sampling and terrain mutation cannot begin until this phase completes.

This dedicated POI chunk-generation phase is required even when broad arena pregeneration is disabled.

### 3. Sample actual surface support

For every X/Z column in the footprint, Exodus obtains an initial surface candidate with `MOTION_BLOCKING_NO_LEAVES`, converts the first-free height to a support height, and inspects the actual blocks around that height.

A valid sample must resolve to a non-air, non-fluid, non-vegetation support block inside the dimension build limits. A bounded downward check may skip leaves, plants, or similarly unsuitable top blocks. An unresolved column is recorded as unsupported rather than silently becoming Y=0.

### 4. Select a representative platform Y

Valid support heights are grouped into a histogram. For each candidate Y, Exodus counts samples within `Y +/- 2`. The selected platform height is the highest candidate whose band covers at least 50 percent of all footprint columns.

The range Y=55 through Y=70 is preferred as a tie-breaker, not enforced as a hard limit. A valid higher surface is accepted. A single floating block or small overhang cannot raise the platform because it cannot satisfy the 50-percent quorum.

Preparation fails clearly if no candidate satisfies the quorum, the result is implausibly close to the dimension minimum, or the structure would exceed the build height. It must never fall back to Y=0.

### 5. Manufacture the site

At the selected platform Y, Exodus prepares the exact footprint plus the existing bounded placement margin:

- Replace the platform layer with the configured solid foundation block, initially `minecraft:stone`.
- Clear the complete structure volume above the platform, using the logical POI's full transformed height.
- For every footprint column, fill air and fluid downward from `platformY - 1` until the first solid supporting block is reached.
- Stop foundation filling after a maximum depth of 96 blocks per column.
- If any required column finds no solid support within 96 blocks, fail the POI and arena preparation with an explicit error instead of leaving the structure floating.

Foundation filling affects only unsupported air/fluid cells. Existing solid terrain below the platform is retained. This produces a continuous supported base over caves, water, ravines, and uneven terrain without searching for a different site.

### 6. Place and verify the structure

Only after terrain preparation succeeds may the templates be placed. Composite faction-base parts retain their existing relative offsets and commit as one logical POI.

After placement, Exodus verifies that the expected bounding box is inside build limits, the platform exists across the footprint, and no footprint column remains unsupported within the foundation contract. A successful `StructureTemplate.placeInWorld` return value alone is insufficient.

## State, persistence, and recovery

Arena preparation gains resumable per-POI progress for:

1. required chunks calculated,
2. chunks generated to `FULL`,
3. surface selected,
4. terrain prepared,
5. templates placed,
6. placement verified.

Persisted records include the deterministic X/Z, transformed footprint, selected platform Y, and phase completion. Resume revalidates completed mutations before advancing and must not duplicate template entities, loot markers, or composite parts.

Cancellation retains already generated chunks and terrain changes, consistent with the existing abandoned-arena behavior. A failed mandatory surface POI prevents the arena from becoming `READY`.

## Configuration

Gameplay thresholds belong in `ExodusConfig`. The initial defaults are:

- surface quorum: 50 percent
- surface height tolerance: 2 blocks
- preferred minimum surface Y: 55
- preferred maximum surface Y: 70
- maximum foundation depth: 96 blocks
- foundation block: `minecraft:stone`
- POI chunks generated per tick: a bounded positive value chosen to avoid a long server-tick stall

The per-tick generation rate may be tuned during implementation based on the existing arena scheduler, but disabling broad arena pregeneration must not disable required POI chunk generation.

## Commands and observability

Existing `/exodus arena locations` and `/exodus arena tp <placementId>` commands continue to expose the persisted final placement coordinates, including the selected platform Y.

For each POI, English logs and operator status output record:

- placement ID and structure ID
- transformed footprint and chunk range
- generated chunk progress
- valid and unsupported sample counts
- accepted surface minimum and maximum
- selected Y, quorum count, and quorum percentage
- cleared block count
- foundation block count and deepest fill
- verification result or explicit failure reason

These diagnostics must distinguish chunk generation, surface selection, terrain preparation, structure placement, and verification failures.

## Failure behavior

- Missing or invalid templates retain their current mandatory-placement failure behavior.
- No valid surface quorum: fail the POI; do not move it and do not use Y=0.
- Structure exceeds build height: fail before terrain mutation.
- No foundation support within 96 blocks: fail the POI and arena preparation.
- Restart during preparation: resume from persisted progress and revalidate the last completed phase.
- Existing generated worlds: report that the world-generation profile change requires a fresh world for reliable validation.

No failure path deletes generated chunks, old arenas, or previously placed structures.

## Verification

Automated verification includes:

- pure tests for histogram/quorum selection, highest qualifying band, tie-breaking toward 55-70, outlier rejection, and no-valid-surface failure;
- footprint and one-chunk-margin chunk-range tests, including composite bases and rotated camps;
- foundation-policy tests for solid terrain, water, caves, bounded depth, and unsupported-depth failure;
- state/recovery tests proving surface work waits for all chunks to be `FULL` and completed phases do not duplicate placement;
- dimension resource tests proving fixed plains, Overworld noise settings, Lost Cities dimension type, `wasteland` profile, and `default` landscape selection;
- full `gradlew clean test build`.

Manual acceptance uses a fresh world and remains separate from automated evidence:

1. prepare an arena with broad pregeneration disabled;
2. inspect both faction bases and several camps through `/exodus arena locations` and the TP action;
3. confirm each surface POI is above the actual terrain, has a cleared structure volume, and has no unsupported void below its footprint;
4. confirm terrain is plains-biome Lost Cities wasteland using the `default` landscape, without glass-sphere islands;
5. confirm player starter bases retain their existing working placement behavior.

Real in-game acceptance is not claimed until the user reports the result.

## Out of scope

- Repositioning POIs away from their deterministic layout coordinates
- Superflat generation
- Deleting or regenerating existing Lost Cities chunks
- Changing player starter-base burial/allocation behavior
- Changing POI counts, rotations, loot, entities, or faction-base composition
- Adding gameplay systems outside the approved foundation scope
