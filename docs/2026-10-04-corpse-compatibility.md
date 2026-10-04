# Corpse compatibility (0.7.2)

Teleporter components leave Corpse 1.0.23 inventories when a corpse is created or loaded. Current-match stacks become protected glowing item entities beside the corpse, preserving their original match tag and count. Ordinary loot remains in the corpse. Stale or unbound components are removed without reissuing unique claims. A periodic loaded-entity check retries late initialization and rejected item spawns.

The optional bridge uses verified public Corpse inventory accessors through reflection; Exodus does not require Corpse. Main, armor, offhand and additional item lists are covered. Equipment display snapshots and the external death-history archive are not loot inventories and are not migrated.

Validation:
- Java 17: `gradlew.bat --offline test build compileGametestJava --console=plain` (265 unit tests).
- Optional installed-mod GameTest: `gradlew.bat --offline runGameTestServer -PcorpseRuntime -PgameTestNamespaces=exodus_corpse --console=plain`.
- GameTest covers normal loot preservation, current Core ejection, repeated recovery, saved-corpse reload and stale-stack removal using Corpse 1.0.23.
- Live two-client death/pickup and corpse GUI acceptance remains manual. Restart the game/server to load the new JAR.
