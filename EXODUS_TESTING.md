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
