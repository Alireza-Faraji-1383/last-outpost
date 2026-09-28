# Project Exodus Teleporter Victory Design

## Purpose

Add the first escape and victory condition to Project Exodus. Players compete or cooperate to assemble nine match-bound components in an indestructible Exodus Teleporter. Completing a device exposes its coordinates and starts a 20-minute defense window. When the countdown expires, up to two active match players nearest the device within five blocks escape, win, and end the match.

This feature is an approved exception to the foundation-phase exclusion of escape and win conditions. It does not add teams, alliances, PvP rules, respawn rules, enemy content, the two facility structures, or the final event that distributes the three unique rare components.

All player-facing text, item names, UI copy, chat announcements, and logs remain English.

## Match Outcome

- A completed teleporter starts a countdown of 1,200 seconds by default.
- `teleporterCountdownSeconds` is configurable. Its value is captured when the device activates, so a config reload cannot change an active countdown.
- At expiry, only active Survival match players in the match dimension are eligible.
- Eligibility uses three-dimensional distance from the center of the teleporter. The default inclusive radius is five blocks.
- `teleporterVictoryRadius` is configurable and captured at activation.
- Eligible players are ordered by distance. A stable UUID ordering resolves an exact distance tie.
- The nearest players up to the captured capacity escape. The default capacity is two and `teleporterCapacity` is configurable from 1 through 8.
- Ownership and component contribution do not affect eligibility. A defender, collaborator, or attacker can win.
- Winners teleport to the match dimension's current world spawn. The match then ends, remaining associated players follow the existing return-to-spawn rules, and public match chat names the winners.
- If nobody is eligible when the countdown expires, the only escape opportunity is lost. The match ends without a winner and announces that nobody escaped.
- There are only three unique rare components in a match, so only one teleporter can complete. Activation atomically claims those components and prevents a second device from activating.

## Teleporter Block

Register `exodus:exodus_teleporter` as a block, block item, block entity, menu, and client screen.

- The block is unbreakable in Survival, is immune to explosions, and cannot be moved by pistons.
- Creative players may break it outside active match interaction restrictions so structure authors can edit structures.
- The mod does not place teleporters automatically. The operator places the block in the starter structure NBT.
- The device cannot accept hopper, pipe, or other item-handler automation.
- Only active match players in the match dimension and inside the current world border may open or modify it. Pending players, spectators, and unrelated players receive `Teleporter is inactive.`
- The inventory contains nine persistent slots. Closing the menu or unloading the chunk does not eject or lose installed items.
- Each slot accepts one exact component type. It rejects wrong items and stack insertion consumes only one item.
- Before completion, any eligible match player may insert or remove components.
- Inserting the ninth valid component atomically locks all nine slots, records the activation state, and starts the countdown. The locked inventory is read-only and cannot be cancelled.
- Installed components never drop. They are deleted when the match ends, stops, or recovers.
- An activated teleporter emits light, quiet ambient sound, and portal particles. The effects intensify in the final 60 seconds without moving or damaging entities.

The screen resembles a Crafting Table: a fixed 3x3 device grid above the player's inventory. Empty slots show a dim icon of the required component. It displays `N/9 Components Installed`. An activated device is read-only and shows the remaining time.

## Fixed Component Layout

The nine distinct item types and slots are:

| Row | Left | Center | Right |
| --- | --- | --- | --- |
| Top | `Reinforced Frame` | `Power Regulator` | `Phase Coil` |
| Middle | `Facility Alpha Key` | `Dimensional Core` | `Facility Beta Key` |
| Bottom | `Signal Processor` | `Spatial Lens` | `Containment Module` |

Registry IDs use lower snake case under the `exodus` namespace.

The six perimeter components are craftable and may exist in multiple copies. The three middle-row components are unique per match:

- `Facility Alpha Key` is intended for the first future facility structure.
- `Facility Beta Key` is intended for the second future facility structure.
- `Dimensional Core` is intended for a future event, special drop, or enemy reward.

The three distribution mechanisms are outside this implementation. The items, creative entries, template claiming, match validation, and uniqueness enforcement are included.

## Match-Bound Components

Every usable component stack records the active match UUID.

- Craft output is valid only when taken by an active match player during `RUNNING`; the server stamps the current match UUID at that point.
- Supply-drop components and materials resolve against the active match.
- A component for another match, or an unbound Creative copy, cannot enter a teleporter.
- Creative copies of the three rare items placed manually in a structure NBT are template copies. The first player who removes a template copy from its loot container during an active match binds and claims it for that match.
- The server persists one claimed flag for each of the three unique component types. The first valid claim succeeds; later template copies are removed when taken and the player receives a concise English message.
- Six craftable components have no per-match quantity cap.
- Match-bound components cannot be placed into chests, barrels, shulker boxes, supply crates, crafting grids, furnaces, or modded item-handler containers. A player cursor is treated as part of player inventory.
- Components may be dropped manually and drop normally on death.
- A dropped component glows for the full active match, does not naturally despawn, and is immune to fire, cactus, and explosion damage.
- A component entering the void returns to its last safe on-ground position.
- When its match is no longer active, a dropped component removes itself.
- Current-match components are removed from online player inventories, Ender Chests, loaded item entities, and loaded teleporters on every match-ending path. Stale stacks in unloaded state are invalid and delete themselves when next encountered.

## Participant Inventory Isolation

After allocation and all other start preflight succeeds, but immediately before players are teleported and the match becomes `RUNNING`, permanently clear these inventories for every selected Survival match player:

- main inventory and hotbar;
- armor;
- offhand;
- Ender Chest.

There is no snapshot or restoration. Initial spectators and unrelated players are not cleared. A start that aborts before the final commit point does not clear anything.

## Crafting Recipes

Recipes are visible in the normal Recipe Book, but a valid output can only be taken by an active match player. The six shaped recipes are:

### Reinforced Frame

```text
Iron Block | Obsidian | Iron Block
Obsidian   | Diamond  | Obsidian
Iron Block | Obsidian | Iron Block
```

### Power Regulator

```text
Copper Block | Redstone | Copper Block
Redstone     | Blaze Rod| Redstone
Copper Block | Redstone | Copper Block
```

### Phase Coil

```text
Gold Ingot  | Ender Pearl  | Gold Ingot
Ender Pearl | Quartz Block | Ender Pearl
Gold Ingot  | Ender Pearl  | Gold Ingot
```

### Signal Processor

```text
Redstone      | Nether Quartz | Redstone
Nether Quartz | Comparator    | Nether Quartz
Redstone      | Nether Quartz | Redstone
```

### Spatial Lens

```text
Amethyst Shard | Glass        | Amethyst Shard
Glass          | Eye of Ender | Glass
Amethyst Shard | Glass        | Amethyst Shard
```

### Containment Module

```text
Obsidian   | Iron Block    | Obsidian
Iron Block | Diamond Block | Iron Block
Obsidian   | Iron Block    | Obsidian
```

## Special-Radio Material Drops

Add three deterministic material supplies visible only on the Special Supply Radio:

1. `Energy Materials`, costing 16 iron ingots.
2. `Optics Materials`, costing 12 copper ingots.
3. `Processing Materials`, costing 8 gold ingots.

Each supply ID has `max_requests: 1` per physical Special Supply Radio for each match. Basic Supply Radios do not show them. Together, one request of each supplies exactly the Nether-inaccessible ingredients needed for one complete set of the six recipes: the required Blaze Rods, Ender Pearls, and Nether Quartz. The three drops remain separate public crates and retain all existing supply-drop validation, descent, theft, persistence, and cleanup behavior.

Ordinary mineable ingredients are not included. The target for a knowledgeable solo player to gather and craft the six components is approximately 45 to 60 minutes; collaboration can reduce that time.

## Countdown, Boss Bar, and Chunk Lifetime

On activation, create one server-owned countdown record containing match UUID, dimension, block position, activation game time, duration, radius, and capacity.

- A server Boss Bar shows the exact device coordinates and formatted remaining time.
- Its progress decreases continuously from full to empty.
- It is visible to online active players, players who reconnect within grace, expired automatic spectators, and initial spectators associated with that match. Unrelated players do not see it.
- A reconnecting associated player receives the current Boss Bar state immediately, not a restarted timer.
- A player returning within the existing 120-second grace period resumes active eligibility.
- A player whose grace expired remains a spectator. They see the bar but cannot win.
- An offline player at expiry cannot win.
- The teleporter area stays loaded with a temporary chunk ticket for the countdown. The ticket covers every chunk intersecting the victory radius.
- The ticket and Boss Bar are released on victory, no-winner expiry, admin stop, automatic ending, failed cleanup, and restart recovery.

If the server restarts during `STARTING` or `RUNNING`, preserve the existing foundation rule: do not resume the match. Recovery returns to `IDLE`, restores the border, removes the Boss Bar and chunk tickets, invalidates the countdown, and lazily resets the teleporter to empty when its chunk loads.

## Match Lifecycle Integration

The teleporter subsystem integrates through narrow hooks rather than placing device logic in `MatchManager`:

- final start commit clears participant and Ender Chest inventories;
- active-player and association queries authorize menus and determine Boss Bar viewers;
- a teleporter service owns activation, countdown, chunk tickets, winner selection, and match-end requests;
- all stop, automatic-end, abort-after-commit, and recovery paths call one idempotent teleporter cleanup method;
- cleanup removes current-match components and countdown resources even if the device chunk is unloaded;
- the existing return-to-spawn and pending-return behavior remains authoritative after winners are selected.

The match UUID must be assigned before components can be stamped and before the state becomes `RUNNING`. Start commit ordering must ensure inventory clearing cannot occur during allocation or an earlier preflight failure.

## Creative Tab

Register a `Project Exodus` creative-mode tab containing all player-usable Exodus content:

- Basic Supply Radio;
- Special Supply Radio;
- Drop Beacon;
- Linking Tool;
- Supply Crate;
- Exodus Teleporter;
- all nine teleporter components.

Internal entities, block entities, menus, and match-bound variants are not shown. Creative component copies are intentionally unbound.

## Persistence

Extend `ExodusSavedData` with version-tolerant fields for:

- rare-component claim flags for the current match UUID;
- active teleporter countdown identity and captured settings;
- enough cleanup identity to invalidate stale stacks after unloaded chunks return.

The teleporter block entity stores its nine slots, local match UUID, activation state, and the captured countdown identity. Old saves without these fields load safely. A teleporter from an older match clears itself lazily and cannot reactivate from stale contents.

## Architecture

Keep the feature under a dedicated `dev.exodus.teleporter` package:

- registry: blocks, items, block entity type, menu type, creative tab, client screen and rendering registration;
- domain: component-slot mapping, component validity, rare-claim decisions, activation decisions, winner ordering, and countdown snapshot;
- block and block entity: persistence, interaction, fixed inventory, light/particle state, and automation rejection;
- menu and screen: server-authoritative slot rules and client presentation;
- item lifecycle: match stamping, container rejection, glowing drops, damage/despawn protection, void recovery, and stale cleanup;
- service: activation transaction, Boss Bar membership, chunk tickets, tick processing, winner selection, announcements, and lifecycle cleanup.

Pure Java policy types must not depend on Forge runtime types where practical. Forge adapters own worlds, players, menus, events, recipes, saved data, chunk tickets, and registries.

## Failure Handling

Expected player failures use concise English messages: inactive teleporter, not an active player, wrong dimension, outside border, wrong slot, wrong match, stale component, duplicate unique component, locked teleporter, or invalid recipe actor.

Activation is atomic. A simultaneous ninth-slot insertion cannot create two countdowns, consume components without an active countdown, or leave two devices active. Cleanup is idempotent so repeated stop/recovery hooks do not duplicate announcements, teleportation, or item removal.

Unexpected failures log the match UUID, dimension, teleporter position, operation stage, and exception without unrelated player data. They fail closed: no winner is declared without an expiry decision and no stale Boss Bar or chunk ticket may survive match cleanup.

## Testing and Acceptance

Pure JUnit tests cover:

- exact component-to-slot mapping and rejection of wrong/stale/unbound items;
- rare component single-claim policy and duplicate rejection;
- activation only on nine valid components and atomic rejection of a second active device;
- nearest-player ordering, inclusive radius, capacity, tie-breaking, spectator/pending/offline exclusion, and no-winner expiry;
- captured config values remaining stable during a countdown;
- countdown progress and expiry boundaries;
- lifecycle cleanup and restart invalidation decisions;
- all six recipe shapes and the combined guaranteed material totals of the three special supplies.

Forge build verification must include `gradlew.bat test` and `gradlew.bat build` under `exodus-mod/`. A development client/server run must verify registry loading, the creative tab, menu opening and persistence, slot restrictions, recipe stamping, drop protections, Special Radio filtering, Boss Bar join/leave behavior, particles/sound, and cleanup logs.

The user performs the real two-client multiplayer acceptance test. Until the user reports it passing, simultaneous GUI interaction, remote Boss Bar synchronization, attacker victory selection, reconnect/grace behavior, and multiplayer countdown completion remain explicitly unverified.

