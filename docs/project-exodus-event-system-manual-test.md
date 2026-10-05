# Event System: Runtime Acceptance

Version: 0.8.0. Install the same version on server and clients and restart Minecraft after changing the JAR.

## Automatic world-clock schedule

Start a normal match with active Survival participants. Day one has no automatic events. Morning ordinary airdrop caps are 1 on days 2-3, 2 on days 4-5, 3 on days 6-7, and 4 from day 8. These are maximums; a failed roll can leave any day's slots empty. Each ordinary type is checked once in priority order, with no forced fill. The guaranteed unique day-5 Core is additional.

The match dimension's daylight controls morning and noon: default day length 24000 ticks, noon 6000, night 12000. Sleep advances the event day. Window claims survive restart and backward time changes cannot replay an old day. Other component/respawn session-day rules retain their existing elapsed-clock behavior.

At noon, Manhunt (day 3 onward) rolls each player's independent chance first, then forms as many legal disjoint pairs as possible outside parties. Remaining players roll personal Zombie Hunt (day 2 onward). Every automatic participant gets at most one mission that noon. A selected player without a legal partner proceeds to Zombie Hunt and does not lose their Manhunt chance.

Each mission has base 30% and a 30-point daily increase by default. Each unlocked definition not received increases its own chance for active living online players present at noon, including those assigned an earlier mission. Offline players do not accumulate missed chances. Actual assignment resets only that definition's chance; completion/failure does not affect the reset.

## Manual schedule checks

In the match dimension, use `/time set 24000` for day-2 morning and `/time set 30000` for day-2 noon; day-3 morning/noon are 48000/54000; day-5 morning is 96000. These commands are test tools; use a new match to repeat already-claimed windows. Do not confuse a random failure with scheduler failure. For deterministic mission checks, override the datapack's chancePercent to 100, then `/reload`.

- Day one: no automatic objectives or drops.
- Day two: morning checks Medical and Equipment with cap 1; noon can grant several separate hunts, never Manhunt.
- Day three: several pairs can get Manhunt; party members never pair together; an odd/unmatched winner can get a personal hunt. No player gets both automatically.
- Have two players kill different zombies: their hunt counts stay independent. Fifty attributed kills within 90 seconds pay the owner 10 emeralds; timeout pays nothing.
- Finish/fail a mission, then tick again at the same noon: no second automatic assignment.
- Miss a definition on consecutive eligible days: its chance rises by its configured increment, capped at 100. Receiving another definition preserves/increases the missed definition. Receive it: its own chance returns to base.
- Keep a player offline across noon: their chances stay unchanged. Reconnect before a later noon: saved chances resume.
- Restart a committed running match after morning/noon. Existing mission counts, deadlines, chance counters and window claims resume; no duplicate draw. Stop/end clears match-owned objectives/drops.

Admin commands bypass automatic day/chance rolls for content testing:

```
/exodus event status
/exodus event stop
/exodus event start exodus:zombie_hunt
/exodus event start exodus:manhunt
/exodus event start exodus:airdrop_medical
/exodus event start exodus:airdrop_equipment
/exodus event start exodus:airdrop_combat
/exodus event start exodus:airdrop_medical_plus
/exodus event start exodus:airdrop_combat_plus
/exodus event start exodus:airdrop_armor
/exodus event start exodus:airdrop_sniper
/exodus event start exodus:airdrop_rpg
```

## Objective/UI checks

- Hunt sidebar shows personal count and 01:30 timer. Spectators receive no objective or reward.
- Hunter sees prey name, distance, and 05:00 timer. Prey sees only survival countdown. Other pairs cannot see one another's private target.
- Prey death from any source pays the eligible living hunter 16 emeralds; surviving the timer pays prey 8. Hunter death cancels. Forming a party with the target fails the mission without reward.
- Repeated death callbacks never duplicate payment. Full-inventory rewards fall as collectible emeralds.
- Sidebar restores the previous scoreboard when the objective finishes. Saved legacy shared hunts remain readable; new automatic hunts are personal.

## Airdrop checks

| Priority | Type | First day | Base percent | Miss increase points |
|---|---|---:|---:|---:|
| 1 | RPG | 8 | 10 | 20 |
| 2 | Sniper | 7 | 10 | 20 |
| 3 | Heavy Armor | 6 | 10 | 20 |
| 4 | Combat Plus | 5 | 10 | 20 |
| 5 | Medical Plus | 4 | 10 | 20 |
| 6 | Combat | 3 | 20 | 20 |
| 7 | Equipment | 2 | 30 | 30 |
| 8 | Medical | 2 | 30 | 30 |

- Failed rolls and types skipped after the cap fills increase their independent global chance. Queue acceptance alone does not reset it; actual entity spawn does. Failed placement leaves the increased chance for later mornings.
- Medical: two current medical chest rolls, 3-5 extra golden apples, 8-12 emeralds.
- Equipment and Combat retain their prior pools and quantities.
- Medical Plus: three medical chest rolls, 6-8 extra golden apples, 8-12 emeralds, one enchanted golden apple.
- Combat Plus: normal Combat pools plus one HK416 or SCAR-H supply bundle with matching ammunition.
- Heavy Armor: complete full-durability Defender armor, Protection IV and Unbreaking III on all pieces. Verify installed-mod protection effects in actual gameplay.
- Sniper: M107, 8x scope, 30 .50 BMG rounds. RPG: RPG-7 and six rockets.
- Falling event drops have a blue outline/smoke. Landed crates do not glow. Public map markers/chat coordinates remain visible and disappear on cleanup.
- Ordinary event drops expire after ten minutes. The unique day-5 bound Core has no ordinary expiry, retries unavailable placement, and never duplicates a reserved/claimed Core.
- Radio appearance, payment, quotas and cooldowns retain their behavior. Stop/end must remove only owned event resources, including falling drops/unloaded crates, without spilling the Core.

## Extending definitions

Add JSON at `data/<namespace>/exodus_events/<id>.json` and `/reload`. Lower `priority` numbers roll first. Fields `minDay`, `maxDay` (0 = unlimited), `chancePercent`, and `chanceIncreasePercent` control unlock/chance independently. GLOBAL/ALL_ACTIVE is used for world drops and supported legacy shared hunts; TARGETED/SINGLE_ACTIVE for personal kill hunts; TARGETED/RANDOM_PAIR for Manhunt. New objective interactions still require a Java handler.

Optional durationSeconds, targetCount, targetEntity, rewardEmeralds, hunterEmeralds and preyEmeralds override the events config defaults. Fixed milestoneDay/oneTime/core remain separate from ordinary draws. Legacy weight/cooldownDays fields remain readable for saved definitions and milestone/admin history, but ordinary daily scheduling now uses independent chance fields.

```json
{
  "title": "Quick Zombie Hunt",
  "scope": "TARGETED",
  "participantSelector": "SINGLE_ACTIVE",
  "objective": "KILL_ENTITY",
  "targetEntity": "minecraft:zombie",
  "targetCount": 25,
  "durationSeconds": 60,
  "rewardEmeralds": 5,
  "minDay": 3,
  "priority": 3,
  "chancePercent": 50,
  "chanceIncreasePercent": 10
}
```

## Verification boundary

Pure policy/resource/NBT tests and Forge server GameTests provide automated evidence. GameTest connections are simulated, not human-operated clients. Actual client rendering, full installed-mod loot/enchantment effects, and two-client gameplay acceptance remain manual.
