# Exodus dry Plains world generation

## Approved behavior

The user requested natural, gently uneven Plains terrain without natural water, and without vanilla structures such as villages. Preserve the existing `lostcities:lostcity` dimension, Lost Cities buildings/loot, Exodus structures, and ordinary generation in other dimensions. Do not delete or migrate existing worlds.

## Implementation

- Derive `exodus:dry_plains` noise settings and biome data from the installed Minecraft 1.20.1 `client-extra.jar`. No global vanilla world-generation resources are overridden.
- Retain the Overworld vertical range, stone default block, noise caves, cave carvers, ore veins, bedrock, and surface rules.
- Set the noise default fluid to air, sea level to -64, and disable aquifers. This combination prevents natural water filling; disabling aquifers alone would not. Vanilla deep lava and lava features remain.
- Replace the height offset with `-0.46875 + 0.045 * continentalness_noise`, sampled horizontally at scale 0.75. With the vanilla depth gradient, the offset centers the reference terrain around Y=68. Use a fixed terrain factor of 5 and omit mountain jaggedness. Retain base 3D noise and vanilla cave density functions. These are world-generation asset parameters, not new match rules.
- Copy Plains colors, temperature, vegetation, animals, and cave carvers. Remove `spring_water`, `monster_room`, `monster_room_deep`, and `amethyst_geode` features. The latter features are not structure starts, so blocking structures alone would not exclude them.
- Do not add the new biome to vanilla biome tags. All 33 bundled Minecraft 1.20.1 structure definitions select tagged biomes, so the new biome is ineligible. This restriction is confined to the new biome.
- Explicitly add `lostcities:lostcities` at the `raw_generation` step through a biome modifier scoped to `exodus:dry_plains`. The existing Exodus profile inherits the Lost Cities `wasteland` profile, whose `avoidWater` is already true. No city, ruin, loot, or placement parameters were changed.

Custom biome isolation is necessary to remove Plains water springs without changing the normal Overworld. It retains the Plains visual identity but has the registry ID `exodus:dry_plains`.

## Verification

`gradlew.bat clean test build --console=plain` passed with 150 tests, zero failures, and zero errors. Resource regression tests first failed against the original dimension and absent dry-world resources, then passed after implementation.

A Forge 47.4.10 / Lost Cities 1.20-7.5.5 development server ran with Java 17 on localhost port 25577. It used a fresh world (seed 734981), stored separately under `exodus-mod/build/dry-plains-smoke`. Required world-generation registries loaded and the server reached `Done`.

Four 8x8-chunk regions were force-loaded around block coordinates (0,0), (1024,1024), (-1024,2048), and (4096,0). After `save-all flush`, actual region-file NBT was decoded and block palettes counted. Neighboring generation produced 576 complete match-dimension chunks:

| Observation | Result |
| --- | --- |
| Biome IDs | Only `exodus:dry_plains` |
| Water blocks | 0 |
| Waterlogged blocks | 0 |
| Valid vanilla structure starts | 0 |
| Chunks containing city construction materials | 125 |
| Chest blocks | 6 |
| Non-city grass-block Y range | 49 to 85, 37 distinct elevations |
| Air blocks below Y=48 in non-city chunks | 631,788 |
| Lava blocks | 16,832 |

`locate structure` found no Plains village, stronghold, mineshaft, pillager outpost, or standard ruined portal in the match dimension. The lack of biome eligibility supplies the general exclusion; these commands and generated chunks are additional runtime checks.

The same server's normal Overworld retained 220,447 water blocks, 5,303 waterlogged blocks, three mineshaft starts, and one Savanna village start across 529 complete chunks. This verifies dimension isolation at the generated-world boundary. The server was stopped and saved normally after inspection.

The development runtime contains Exodus and Lost Cities, but not the full installed client modpack. Existing TaCZ-dependent loot tables logged missing-item errors in this minimal runtime. This test verifies terrain, biome isolation, structure exclusion, and actual city placement; it does not verify weapon loot, visual gameplay, every seed, or real two-client multiplayer.

## Existing saves

World-generation definitions are saved during world creation. Restart the game and create a fresh world to reliably receive these settings. Already-generated areas are not rebuilt, and existing saves are not edited or deleted. Water explicitly authored in a template or placed by a player remains allowed.
