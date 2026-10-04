# Project Exodus Agent Guide

## Project

Project Exodus is a Minecraft 1.20.1 Forge minigame foundation. The active game instance uses Forge 47.4.10 and KubeJS 2001.6.5-build.26. The authoritative core is implemented as a dedicated Forge Java mod in `exodus-mod/`; KubeJS remains available for lightweight future content but is not the match-state authority.

## Current phase

Build only the foundation: admin commands, match lifecycle, safe base allocation, structure placement, player/spectator tracking, persistence/recovery, border management, return-to-spawn behavior, status, and logging.

Supply drops and the Exodus Teleporter victory flow are the approved exceptions to this foundation phase, as specified in `docs/superpowers/specs/2026-09-27-project-exodus-supply-drops-design.md` and `docs/superpowers/specs/2026-09-28-project-exodus-teleporter-victory-design.md`. Do not add rockets, other escape/win conditions, other airdrops, NPCs, vehicles, guns, raids, teams, alliances, trading, PvP rules, respawn rules, other loot systems, enemy waves, day/night progression, or unrelated HUD/UI.

## Agreed behavior

Controlled enemy spawning is an additional approved foundation exception, specified in
docs/superpowers/specs/2026-09-30-project-exodus-enemy-spawning-design.md.
In the wasteland, automatic mob spawning is blocked outside the match-owned zombie and
Russian/American soldier system. Preserve administrator test spawns. Each active player
has an independent 30-enemy allocation, with a global cap of 200. Keep pressure rules
separate from placement/lifecycle for future difficulty changes; implement only the
approved zombie/soldier behavior. Real multiplayer performance remains manual acceptance.

- Only one global match may exist.
- `/exodus start`, `/exodus start <x> <z>`, and `/exodus start random` require permission level 2 and a player command source.
- `start` uses the executor's dimension. Nether and End are rejected; modded playable dimensions are allowed.
- Only Survival players already in that dimension become match players. Other game modes are spectators.
- The current cap is 8 match players; zero or too many players abort before world changes.
- Base allocation is preflighted before border/structure/teleport changes. It runs incrementally at 2 candidates per tick and times out after 120 seconds.
- Allocation is shuffled and seeded; centers must remain inside the safe border area, avoid unsuitable biomes/liquids/caves, have no more than 6 blocks of footprint height variation, be at least 250 blocks apart, and avoid persisted old Exodus bases.
- Match border is 2000x2000 around the chosen center. Preserve and restore the previous border on stop, failed commit, automatic ending, or restart recovery.
- Structure pool defaults to `exodus:starter_base`; missing entries warn, an empty pool aborts. Development fallback is explicit and disabled by default.
- Structure rotation is NONE. Track base center, structure origin, spawn position, owner UUID/name, and structure ID.
- Disconnect or leaving the match dimension starts a 120-second grace period. Returning within the period resumes play; after expiry the player has left and returns only as a spectator.
- New non-members entering the dimension mid-match become Spectator. When the match ends, match players and auto-spectators become Survival; initial spectators retain their game mode.
- When no active/pending players remain, end automatically. On stop/end, all associated online users return to the match dimension's current world spawn. Offline users receive a persistent pending return on next login.
- Structures are not deleted on stop. Persist a registry of placed Exodus bases so later matches do not overlap them.
- On server restart, an active/starting match is not resumed: recover to IDLE, restore the saved border, retain placed structures/base registry, and log clearly.
- All player-facing text and logs are English.
- Starting a committed match permanently clears match players' inventory, armor, offhand, and Ender Chest.
- The Exodus Teleporter uses nine fixed match-bound components, a configurable countdown/radius/capacity, and announces the nearest eligible winners at expiry.
- The Facility Alpha Key, Facility Beta Key, and Dimensional Core are registered and unique per match; their structure/event distribution remains future content.

## Engineering rules

- Work directly in the primary checkout on `main` and commit completed, verified changes there. Do not create another worktree for new work. This is the user's explicit project workflow preference.
- Match-owned two-player parties are an approved exception, specified in `docs/superpowers/specs/2026-10-04-project-exodus-parties-design.md`.
- Match-owned Zombie Hunt, Manhunt and event airdrops (including the unique day-5 Core) are approved exceptions, specified in `docs/superpowers/specs/2026-10-04-project-exodus-event-system-design.md`.

- Every delivered mod change must increment `mod_version`; produce a newly versioned JAR and keep distribution output versions consistent. Never overwrite a released version with changed contents.
- All installed gun attachments and Zero Contact items are approved chest-loot content. Higher actual scope zoom and greater net equipment benefits must be rarer. Preserve chest quantity.
- Sparse match-bound saddled horses are approved: two global daytime spawn opportunities per world day, with a configurable batch and global population cap separate from enemies.

- Do not guess Forge or Minecraft APIs. Verify against Forge 47.4.10/Minecraft 1.20.1 sources or compilation.
- Keep match, base allocation, structures, persistence, commands, and configuration modular.
- Avoid magic numbers; gameplay thresholds belong in `ExodusConfig`.
- Use tests for pure allocation/state logic and run the full Gradle test/build gates before reporting completion.
- Do not install or remove third-party mods without explicit approval.
- The real two-client multiplayer acceptance test is performed manually by the user; never describe it as verified until they report success.

## Runtime paths

- Mod source: `exodus-mod/`
- Built JAR destination: `mods/`
- Structure override path: `kubejs/data/exodus/structures/starter_base.nbt`
- Implementation plan: `docs/superpowers/plans/2026-09-27-project-exodus-foundation.md`
