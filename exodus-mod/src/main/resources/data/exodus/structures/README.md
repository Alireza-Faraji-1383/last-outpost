# Starter base structure

Place the production structure at `kubejs/data/exodus/structures/starter_base.nbt` in the instance, or copy an NBT here as `starter_base.nbt` before building.

The default structure ID is `exodus:starter_base`. With no structure present, `/exodus start` aborts safely unless `enableDevFallback=true` in `config/exodus-common.toml`.

Faction bases are four-piece composites. Provide `russian_base_1` through `_4` and `american_base_1` through `_4`. Expected sizes are 29x20x30, 28x20x30, 29x20x30, and 28x20x30. Part 1 is the anchor; part offsets are `(0,0,0)`, `(29,0,0)`, `(0,0,30)`, and `(29,0,30)`.

Camp variants use `abandoned_camp_01`, `occupied_camp_01`, and increasing two-digit suffixes. Save occupied camps with entities enabled.
