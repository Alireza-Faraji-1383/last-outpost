# Project Exodus Respawn, Loot, and Foundation Design

## Goal

Finish the playable match loop by binding every match player to their allocated base, making loot substantially more generous and varied, distributing the six craftable teleporter components through ordinary loot, and ensuring manufactured foundations begin below the structure instead of competing with its bottom layer.

## Scope

This change covers match-player respawn targets, cleanup recovery for online and offline players, Exodus structure loot, Lost Cities chest mapping, craftable teleporter-component distribution, and the existing terrain-manufacturing depth. It does not add new items, mods, weapon mechanics, victory conditions, or loot UIs.

## Match Respawn Lifecycle

- After a match has committed successfully and each player has an allocated `PlayerBaseData.spawn`, set that player's forced respawn position to the same base spawn in the Exodus wasteland dimension.
- Do not change respawn positions during allocation or before commit succeeds.
- Death during an active match therefore returns a match player to their own base.
- Every cleanup path resets affected match players to the current default Overworld world spawn. This includes operator stop, automatic match end, failed committed startup cleanup, and restart recovery.
- Online players are reset immediately. Offline associated players retain a persistent pending-return entry; login applies the Overworld respawn reset before clearing that entry.
- Initial spectators who never received a match base are not assigned or reset as if they were match players.
- The previous bed or anchor position is intentionally not restored.

## Loot Architecture

- Keep the existing marker-facing loot-table IDs stable.
- General tier tables remain the tier authority but compose reusable category bonus tables so common, standard, valuable, and elite chests may also contain food, medicine, weapons, ammunition, utility, technology, resources, and useful building blocks.
- Target approximately twice the current useful item volume without guaranteeing a gun in every chest.
- Higher tiers increase the chance and quality of guns and valuable supplies.
- Specialized food, medical, weapons, utility, and tech tables are expanded rather than replaced.
- Useful Vanilla blocks include a curated construction/survival set such as stone or cobblestone, planks/logs, stone bricks, glass, iron bars, ladders, scaffolding, and torches. Quantities must be useful without filling an entire chest with bulk blocks.
- TacZ loot includes actual guns as well as ammunition and attachments. Curate stable `tacz:` default-pack IDs across pistols, SMGs, rifles, shotguns, and precision rifles.
- Ammunition remains more common than guns. Pistols and SMGs are lower-tier; rifles and shotguns are mid/high-tier; precision rifles are rare/high-tier.
- RPGs, miniguns, launchers, and other explosive/heavy weapons are excluded from ordinary loot.
- Lost Cities chest selection gains weapons, utility, tech, valuable, and elite mappings. Valuable and elite remain uncommon but are no longer impossible.
- All ordinary chest families may yield the six craftable teleporter components: Reinforced Frame, Power Regulator, Phase Coil, Signal Processor, Spatial Lens, and Containment Module.
- Component chance rises with chest quality: low in common and specialized tables, higher in standard and valuable, and highest in elite. A component is never guaranteed by an ordinary chest.

## Rare Teleporter Components

- Keep the Facility Alpha Key and Facility Beta Key logic, faction-base guaranteed distribution, unique-claim behavior, and loot tables unchanged.
- Keep the center-slot Dimensional Core completely unchanged and exclude it from all ordinary loot tables.
- Keep all match binding, storage, crafting, rarity, and teleporter inventory rules unchanged.

## Foundation Ownership

- Structure placement retains its current Y coordinate.
- The structure owns its entire lowest layer.
- Manufactured stone support begins exactly one block below that lowest structure layer and fills air or fluid downward until solid support, within the existing configured maximum depth.
- Clearing remains above/in the structure volume as required by placement; the support pass must not replace any block belonging to the structure's lowest layer.
- Preparation and verification must use the same shifted support Y so retries remain idempotent.

## Failure and Persistence Rules

- A failed preflight must not change respawn positions.
- A failed commit that has assigned a respawn target must run the normal cleanup reset.
- Pending offline resets survive save/load and are applied once on login.
- Existing loot marker IDs and chest replacement idempotency remain compatible with already prepared arenas.
- Existing prepared structures are not retroactively regenerated; the new foundation rule applies when terrain is prepared again or in a new arena/world.

## Verification

- Unit/contract tests cover base-spawn assignment eligibility, cleanup reset eligibility, pending offline reset behavior, unchanged faction key routing, exclusion of the Dimensional Core, tier-scaled distribution of all six craftable components, expanded TacZ gun families, useful blocks, approximately doubled roll budgets, and Lost Cities valuable/elite availability.
- Terrain tests prove support starts one block below the structure-owned bottom layer and preparation/verification agree.
- Run the full Gradle test and build gates, then install the built JAR in `mods/`.
- Manual acceptance remains the user's responsibility: start a fresh match, die and respawn at the personal base, stop/end and confirm later respawn uses default Overworld spawn, sample custom-structure and Lost Cities chests, and inspect a freshly prepared foundation.
