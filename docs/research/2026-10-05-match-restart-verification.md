# Match restart verification — Exodus 0.7.3

The user approved resuming committed matches after closing/restarting a world or
server, replacing the former unconditional recovery to IDLE. They subsequently
cancelled reconstruction/migration of the already-cleared live match. No save
reconstruction was performed.

RUNNING retains its match identity, player/base associations, session time,
inventory, respawn/device ownership, discovery, elimination, rare claims, party
membership/invitations/separation deadlines, objective progress/reward journal,
and event deliveries. Enemy and horse population reservations also survive chunk
unloads and restart. Horse shutdown detaches runtime references without deleting
entities. STARTING and ENDING retain the previous cleanup/recovery behavior.

The restarted match waits for its first active returning player. Session time,
world day, teleporter countdown, event-drop expiration, and enemy maintenance
pause during this wait. Remaining players receive the configured reconnect grace
from the first active return. Missing RUNNING identity/dimension aborts recovery
without deliberately clearing saved match data. Ordinary logout grace and explicit
stop continue to apply after play resumes.

## Automated evidence

- `gradlew.bat --offline test build`: passed, 269 tests, zero failures.
- `gradlew.bat --offline runGameTestServer -PgameTestNamespaces=exodus_events`:
  passed both event and party integration tests (the party test uses the events
  template namespace).
- `gradlew.bat --offline runGameTestServer -PgameTestNamespaces=exodus_party,exodus_restart`:
  passed the restart integration test. It serializes/reloads match data, simulates
  5000 world ticks before login, checks paused time/countdown/drop expiration and
  preserved enemy reservations, then checks mocked login, inventory, party,
  reconnect grace, resumed clock, and explicit stop.
- Read-only independent review found the waiting countdown and enemy-maintenance
  issues; both were corrected and covered by the restart integration test.
- Installed `mods/exodus-0.7.3.jar` matches the built JAR SHA-256:
  `C96BC5550A0DB1AB383BC3390ED6D0974194FD4B40010A7E68E10B5E7ECEB8C1`.

The restart fixture now captures the actual original border before exercising
stop, avoiding pollution of subsequent test runs. The event overflow test places
its random hunter in an already-ticking chunk before checking spilled rewards;
production reward behavior was not changed for that fixture correction.

## Acceptance and rollback

This evidence is a serialized reload plus real Forge server integration with mocked
players, not a real two-client shutdown/restart result. Follow EXODUS_TESTING.md
for manual acceptance. Crash recovery retains the last completed Minecraft save;
it cannot recover unsaved game ticks.

The pre-install live-world copy and prior Exodus JAR are under
`backups/pre-exodus-0.7.3-20261005/`. A rollback to 0.7.2 must pair the old JAR with
that world copy: 0.7.2 intentionally clears active match state during startup.
