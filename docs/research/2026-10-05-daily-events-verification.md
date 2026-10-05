# Daily events release verification

Release: Exodus 0.8.0, Minecraft 1.20.1 / Forge 47.4.10. Primary checkout on main.

## Automated evidence

- Full `./gradlew.bat --offline test build`: 281 unit/resource/NBT tests, zero failures/errors/skips; versioned reobfuscated JAR built.
- `./gradlew.bat --offline runGameTestServer -PgameTestNamespaces=exodus_events`: all three required server tests passed, including the new five-player personal noon scheduling/reload test and existing objective/delivery/party integration tests.
- Final `./gradlew.bat --offline test build runGameTestServer -PgameTestNamespaces=exodus_restart`: successful; the required committed-match restart recovery test passed.
- Read-only independent code review found no actionable behavioral/persistence/integration issues.
- Development logs are in `exodus-mod/build/daily-events-gametest.log` and `exodus-mod/build/daily-events-final-verification.log` (ignored build outputs).
- The earlier version test failed because it hardcoded 0.7.3; it now validates a semantic version while checking distribution tools read gradle.properties. The initial new GameTest fixture omitted borderSize and failed map login validation; setting the test session's normal 2000-block border fixed the fixture. Final gates were rerun successfully.

The development runtime does not load the installed TacZ/LR/Zero Contact mods and logs missing third-party loot item references, including existing loot tables. This environment proves scheduling, state, native Exodus delivery, packet projection, and persistence; it does not prove third-party weapon/armor effects. Existing validated supply tables are reused for RPG/M107/HK416/SCAR-H, and all four Defender model IDs were confirmed in the installed LR armor JAR.

## Installed artifact

- `mods/exodus-0.8.0.jar` is the only active Exodus JAR. The previous 0.7.3 JAR was retained with a `.pre-0.8.0-<timestamp>.bak` suffix. No worlds or third-party mods were changed.
- Built and installed SHA256: `4BF4DD929CA8C31A5BC47284888C7967AE21A2A2F6F520AF82074DF248A10393`.
- Installed metadata says 0.8.0, contains eleven event definitions (two missions, eight ordinary drops, one Core), and excludes GameTest classes.
- Pack build tools derive their versions from mod_version; previously released packs were not overwritten.

## Manual acceptance

Not yet verified with real clients: morning/noon daylight experience, UI/map visibility, full installed-mod loot and protection/enchantment effects, simultaneous personal missions/rewards, offline-player pity, and two-client restart behavior. Follow `docs/project-exodus-event-system-manual-test.md`. Existing RUNNING saves retain active objectives; old shared objectives remain readable and finish under their captured rules, while future draws use the new personal scheduling.
