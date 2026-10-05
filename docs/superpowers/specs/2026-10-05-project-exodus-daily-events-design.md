# Daily mission and airdrop scheduling

Approved in the October 5 conversation. Implement directly on main, then commit the verified release.

## Clock and eligibility

Day one has no automatic events. Use the match dimension's world day clock (the match starts at time zero). Morning draws run once before noon; mission draws run once from noon until night. A sleeping night advances the event day. Persist day/window claims so reconnect/restart or backward time changes cannot replay a draw. Objective durations continue using the existing elapsed match tick clock. The existing session component/respawn day rules are outside this change.

## Personal missions

Every definition has its own `minDay`, `priority`, `chancePercent`, and `chanceIncreasePercent`. Lower priority numbers run first. Chances belong to a definition/player pair, cap at 100%, and reset only on actual assignment, irrespective of subsequent success or failure. Only active living online match players at noon accumulate missed chances. Each can receive at most one automatic mission that noon. Receiving an earlier mission still counts as missing later unlocked definitions.

Manhunt unlocks on day 3, priority 1, base 30%, increase 30 percentage points. Roll each player independently, then randomly form the maximum number of disjoint pairs outside existing parties. An unmatched winner keeps their missed chance and proceeds to the next mission.

Zombie Hunt unlocks on day 2, priority 2, base 30%, increase 30 points. Roll remaining players independently; each winner receives a separate 50-kill, 90-second hunt with the existing 10-emerald reward. Counts, timeouts, and rewards are independent. Keep legacy shared saved objectives readable.

## Ordinary airdrops

At morning, check each unlocked type once in priority order until the cap is filled. No forced fill and no repeated type in one draw. Every eligible type not actually delivered increases its own global chance, including lower types skipped after the cap fills. A queued placement is not a delivery; only successful entity creation resets the type's chance. Failed placements retain the increased chance.

| Type | First day | Priority | Base percent | Miss increase points |
|---|---:|---:|---:|---:|
| RPG | 8 | 1 | 10 | 20 |
| Sniper | 7 | 2 | 10 | 20 |
| Heavy Armor | 6 | 3 | 10 | 20 |
| Combat Plus | 5 | 4 | 10 | 20 |
| Medical Plus | 4 | 5 | 10 | 20 |
| Combat | 3 | 6 | 20 | 20 |
| Equipment | 2 | 7 | 30 | 30 |
| Medical | 2 | 8 | 30 | 30 |

Configurable ordinary caps: days 2-3: 1; days 4-5: 2; days 6-7: 3; day 8 onward: 4. These are maximums, not guaranteed quantities. The guaranteed one-time day-5 Core is additional and keeps its retry/unique-claim behavior.

## Approved loot

- RPG: existing RPG-7 supply loot, six rockets.
- Sniper: existing M107 supply loot, 8x scope, 30 rounds.
- Heavy Armor: full Defender set, full durability, Protection IV and Unbreaking III.
- Combat Plus: existing combat event pools plus one existing HK416 or SCAR-H supply bundle with its corresponding ammunition.
- Medical Plus: three medical chest rolls, 6-8 extra golden apples, existing emerald reward, one enchanted golden apple.

Radio supply payment/quota/cooldown, ordinary chest quantities, and third-party mods remain outside the change.

## Verification

Pure tests cover ordered independent rolls, different chance increments, unmatched players, several disjoint legal pairs, online-only accumulation, caps, independent delivery outcomes, and persisted world windows. NBT tests cover chance/clock round trips and legacy active runs. Forge event GameTests cover five online players, independent kill progress, several Manhunts, party exclusions, and no replay after reload. Full Gradle test/build is required. Actual client UI, installed third-party loot/enchantment effects, and two-client acceptance remain manual.
