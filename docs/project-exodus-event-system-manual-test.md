# Event System: Runtime Acceptance

Version: 0.6.0. Install the same version on server and clients; map protocol is now 2 because airdrop markers have a new type. Restart Minecraft after changing the JAR.

## First test
Start a normal match with at least two Survival participants. The normal scheduler rolls once per match day (60% default) and uses minDay/weight/cooldown from datapacks. To test immediately:

```
/exodus event status
/exodus event stop
/exodus event start exodus:zombie_hunt
/exodus event start exodus:manhunt
/exodus event start exodus:airdrop_medical
/exodus event start exodus:airdrop_combat
/exodus event start exodus:airdrop_equipment
/exodus event start exodus:dimensional_core
```

Commands require permission level 2 and an active match. Admin starts bypass day windows and cooldowns for testing; unique events remain one-time. Drop starts queue safe placement, so check event status and the start announcement. At least two eligible players are required for Manhunt. Disconnection or departure cancels a personal event without reward; the match itself still uses its existing grace period. Admin stop clears current objectives and event drops; it does not erase scheduling history or unique-core claims. Test destructive admin stop on a fresh match.

## Objective checks
- Zombie Hunt shows `Kills: 0 / 50` and `Time: 01:30`. Kill 50 zombies before expiry. Kills must be attributed to participating active players in the match dimension. Each living eligible contributor receives 10 emeralds and an English reward message; noncontributors and spectators receive none. A timeout pays nobody.
- Manhunt lasts five minutes. Hunter sees target name, current distance in blocks and countdown. Prey sees only survival countdown. Other players see their own active event or no event sidebar.
- Prey death from any source pays the living eligible hunter 16 emeralds. Prey survival until expiry pays prey 8 emeralds. Confirm the payment message in chat and no repeated payment. Hunter death cancels the event.
- A Manhunt sidebar takes priority over the shared hunt; after it ends the shared hunt reappears if still active. A player may not enter more than one personal event by default. Confirm prior sidebar restoration after all events finish.
- Repeat with full inventory: reward overflow should drop at the recipient instead of disappearing.

## Airdrop checks
- Existing radio requests keep their normal appearance, payment and quotas.
- Event falling drops have a blue outline and blue smoke. The landed crate does not glow.
- Map/minimap displays a public blue airdrop marker and start chat supplies coordinates.
- Medical: two rolls of the current medical chest, 3-5 additional golden apples, 8-12 emeralds.
- Combat: two elite weapon rolls, four native ammunition rolls, two armor rolls and two attachment rolls. Existing weights preserve rare high-power guns/scopes.
- Equipment: two equipment rolls, two Zero Contact rolls, two attachment rolls and 12-20 emeralds. Existing net-benefit rarity remains unchanged.
- Ordinary drops expire after ten minutes by default. Empty crates use the existing cleanup delay. Expired/removed crate markers disappear.
- The day-5 Core milestone runs independently of daily randomness. Exactly one bound Dimensional Core is delivered. It has no ordinary ten-minute expiry while waiting for collection. A previously reserved/claimed core prevents another delivery.
- Restart or stop the match while a drop is falling and while its crate is unloaded; revisit the chunks and confirm event-owned resources are cleared. Existing radio drops and unrelated blocks must not be removed.

## Extending events
Add JSON at `data/<namespace>/exodus_events/<id>.json` and `/reload`. The shipped examples are in the mod resources. Supported scope/selector pairs: GLOBAL/ALL_ACTIVE and TARGETED/RANDOM_PAIR. Supported objectives: KILL_ENTITY, KILL_PLAYER, WORLD_DROP. New objective interactions require a new Java handler; TEAM/ZONE and other future modes are not implemented.

Fields: title, scope, objective, participantSelector, minDay, maxDay (0 = no end), weight, oneTime, cooldownDays, milestoneDay (0 = random), lootTable, core. Optional per-definition durationSeconds, targetCount, targetEntity, rewardEmeralds, hunterEmeralds and preyEmeralds override the corresponding `events` defaults in exodus-common.toml. Invalid definitions log warnings and are excluded. Zero random weight leaves an event admin-only unless it has a milestone.

Example custom zombie event:

```json
{
  "title": "Quick Zombie Hunt",
  "scope": "GLOBAL",
  "participantSelector": "ALL_ACTIVE",
  "objective": "KILL_ENTITY",
  "targetEntity": "minecraft:zombie",
  "targetCount": 25,
  "durationSeconds": 60,
  "rewardEmeralds": 5,
  "minDay": 3,
  "weight": 10,
  "cooldownDays": 2
}
```

## Verification boundary
Pure policy/resource tests and Forge server GameTests provide automated evidence. Test packet recipients are simulated connections, not human-operated clients. Actual client rendering, full installed-mod loot behavior, and two-client gameplay acceptance must be checked manually.
