# Exodus Wasteland operator guide

## Required structure files

Place these templates in `kubejs/data/exodus/structures/` before preparing an arena:

- `starter_base.nbt`
- `russian_base_1.nbt` (29x20x30), `russian_base_2.nbt` (28x20x30), `russian_base_3.nbt` (29x20x30), `russian_base_4.nbt` (28x20x30)
- `american_base_1.nbt` through `american_base_4.nbt`, using the same numbered sizes
- `abandoned_camp_01.nbt` and `occupied_camp_01.nbt` (11x11x16); additional numbered variants can be added later

All faction-base pieces must face the same direction. Exodus places parts 1/2 on the lower row and parts 3/4 on the upper row with no rotation. Save occupied camps with entities enabled.

## Loot markers

Use a structure block in DATA mode at the chest position. Set its metadata to one of:

`exodus:loot/general/common`, `exodus:loot/general/standard`, `exodus:loot/general/valuable`, `exodus:loot/general/elite`, `exodus:loot/food`, `exodus:loot/weapons`, `exodus:loot/medical`, `exodus:loot/utility`, or `exodus:loot/tech`.

During preparation the marker becomes a one-time shared chest. Unknown `exodus:loot/` markers fail preparation; foreign DATA markers are left alone.

## Workflow

1. Run `/exodus arena prepare` from an operator account.
2. Watch 10-percent updates or use `/exodus arena status`.
3. Use `/exodus arena cancel` if necessary. Generated chunks and placed structures are intentionally retained.
4. When status is `READY`, put Survival players at the Overworld lobby and run `/exodus start`.
5. Use `/exodus stop` to end the match and return associated users to the Overworld spawn.

`/exodus dimension enter` and `/exodus dimension leave` provide an operator inspection bypass without changing the operator's game mode. Normal non-members are rejected from `lostcities:lostcity`.

Preparation resumes from its persisted phase and chunk cursor after a restart. A consumed arena is never reused. Arena spacing scales automatically with `borderSize`, `pregenerationBuffer`, and `arenaSafetyGap`.
