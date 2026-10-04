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

## Committed match restart (Exodus 0.7.3)

Start a new match with two clients. Keep identifiable loot, create a party, and record
the match day, base locations, selected respawn, event progress, and teleporter timer.
Close the world/server normally, restart it, and join again. `/exodus status` must
show RUNNING with the original match and bases; inventory must remain intact and
players already in the match dimension must retain their saved positions. Session
time, party invitations/separation, active event progress, rare claims, event drops,
and the active teleporter must survive. Shutdown time must not count as match time.

After restarting a dedicated server, leave it empty longer than two minutes before
joining. The match, enemies, teleporter countdown, and event-drop lifetime must wait
for the first returning active player. Once that player returns, the other members
have the configured disconnect grace (normally 120 seconds); later arrivals become
spectators. Initial spectators keep their game mode. `/exodus stop` still ends the
resumed match and restores the previous border. STARTING/ENDING interruptions still
recover to IDLE. Already-cleared legacy matches are not reconstructed.

Automated checks: full Gradle test/build and isolated `exodus_restart` Forge GameTest
cover serialized reload, return/login, inventory preservation, party continuity,
reconnect grace, paused countdown/drop expiry, enemy preservation while waiting,
and explicit stop. This is not a real two-client shutdown/restart acceptance result.

## Dry Plains world generation (2026-09-30)

Restart Minecraft after installing the updated Exodus JAR and create a fresh world for world-generation verification. Existing saves and already-generated terrain are retained; their saved generator settings are not migrated.

The `lostcities:lostcity` dimension now uses the isolated `exodus:dry_plains` biome and noise settings. This biome copies vanilla Plains appearance, plants, animals, and cave carvers, but excludes water springs, dungeons, and geodes. It intentionally belongs to no vanilla biome tags, so vanilla structures (including villages, mineshafts, strongholds, and ruined portals) cannot select it. Lost Cities is added directly through an Exodus biome modifier.

Verify that the match dimension has rolling grass-covered terrain, dry caves, no natural water, and no vanilla structures, while Lost Cities buildings and Exodus bases/camps still appear. Water explicitly authored in a template or placed by a player is not removed. The normal Overworld, Nether, and End are unchanged.

Automated dedicated-server evidence and its limitations are recorded in `docs/research/2026-09-30-dry-plains-worldgen.md`. Visual gameplay and the two-client acceptance test remain manual.

## Fixed cities (Exodus 0.2.3)

Use a fresh world. The effective lostcities:lostcity profile is exodus; old generated domes are retained. Arena geometry must match the shipped 2000/128/1024 layout, with 64 finite slots. Building spawners are disabled while city chests remain enabled. Dedicated-server evidence and manual acceptance boundaries are in docs/research/2026-09-30-fixed-cities-verification.md. Verify the visual city, border, building distribution, and real two-client gameplay manually.
