# Project Exodus manual test

1. Put `starter_base.nbt` at `kubejs/data/exodus/structures/starter_base.nbt`, or enable the explicit development fallback in `config/exodus-common.toml`.
2. Start the 1.20.1 Forge instance and open a world as LAN/dedicated multiplayer.
3. Join with at least two Survival players in the same playable dimension.
4. Run `/exodus players`, then `/exodus start` (or `/exodus start <x> <z>` / `/exodus start random`).
5. While state is STARTING, check `/exodus status`; verify the server remains responsive.
6. Confirm the 2000-block border, distinct bases, safe teleports, and minimum spacing.
7. Disconnect or leave the dimension, return within two minutes, and verify membership resumes.
8. Repeat and wait longer than two minutes; verify the returning user is a spectator.
9. Run `/exodus stop`; verify the old border returns and associated online players are sent to the match dimension's world spawn.
10. Repeat with one associated player offline; verify the pending return happens on their next login.

Real two-client multiplayer behavior is not considered verified until this checklist is completed by the user.

## Dry Plains world generation (2026-09-30)

Restart Minecraft after installing the updated Exodus JAR and create a fresh world for world-generation verification. Existing saves and already-generated terrain are retained; their saved generator settings are not migrated.

The `lostcities:lostcity` dimension now uses the isolated `exodus:dry_plains` biome and noise settings. This biome copies vanilla Plains appearance, plants, animals, and cave carvers, but excludes water springs, dungeons, and geodes. It intentionally belongs to no vanilla biome tags, so vanilla structures (including villages, mineshafts, strongholds, and ruined portals) cannot select it. Lost Cities is added directly through an Exodus biome modifier.

Verify that the match dimension has rolling grass-covered terrain, dry caves, no natural water, and no vanilla structures, while Lost Cities buildings and Exodus bases/camps still appear. Water explicitly authored in a template or placed by a player is not removed. The normal Overworld, Nether, and End are unchanged.

Automated dedicated-server evidence and its limitations are recorded in `docs/research/2026-09-30-dry-plains-worldgen.md`. Visual gameplay and the two-client acceptance test remain manual.

## Fixed cities (Exodus 0.2.3)

Use a fresh world. The effective lostcities:lostcity profile is exodus; old generated domes are retained. Arena geometry must match the shipped 2000/128/1024 layout, with 64 finite slots. Building spawners are disabled while city chests remain enabled. Dedicated-server evidence and manual acceptance boundaries are in docs/research/2026-09-30-fixed-cities-verification.md. Verify the visual city, border, building distribution, and real two-client gameplay manually.
