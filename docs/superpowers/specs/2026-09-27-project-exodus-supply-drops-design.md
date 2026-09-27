# Project Exodus Supply Drops Design

## Purpose

Add a competitive, server-authoritative supply-drop system to Project Exodus. During an active match, players use a linked Supply Radio and Drop Beacon to buy configured loot crates. Drops descend visibly by parachute and colored smoke, land on the first surface above the beacon, and become public 54-slot containers.

This feature is an explicit exception to the foundation-phase exclusions for airdrops and loot. Rockets, escape and win conditions, NPCs, vehicles, guns themselves, raids, teams, alliances, trading, PvP rules, respawn rules, enemy waves, day/night progression, and HUD work remain out of scope.

## Player-Facing Content

The mod registers four placeable/usable objects:

- `exodus:basic_supply_radio`: displays supply definitions that include the `basic` radio type.
- `exodus:special_supply_radio`: displays supply definitions that include the `special` radio type.
- `exodus:drop_beacon`: marks the vertical drop line for one linked radio.
- `exodus:linking_tool`: a single-use item that claims and links one radio and one beacon.

There are no crafting recipes and no automatic match-start distribution. Operators distribute these objects manually. All player-facing names, messages, UI text, and logs are English.

Supply Radios and Drop Beacons cannot be broken in Survival, including by their owner. A Creative player can break them. Breaking either endpoint invalidates the link. Supply Crates cannot be broken in any game mode and remove themselves 30 seconds after their inventory becomes empty.

## Ownership and Linking

Linking is a two-step server-authoritative interaction:

1. A match player right-clicks a Supply Radio with a Linking Tool. The tool records the radio dimension and position without being consumed.
2. The same player right-clicks a Drop Beacon with that same tool. The server validates and completes the operation atomically.

A successful operation consumes exactly one Linking Tool in all cases: claiming unowned blocks, linking blocks already owned by that player, or taking a linked pair from another player. A failed first or second interaction does not consume the item.

Validation requires:

- Match state is `RUNNING`.
- The actor is an active `MATCH_PLAYER`, not pending, expired, or a spectator.
- Both blocks are in the active match dimension and within the current world border.
- Radio and beacon are no more than `128` blocks apart by default. The distance is configurable in `ExodusConfig`.
- Each radio is linked to at most one beacon and each beacon to at most one radio.
- If either endpoint is already linked, the selected endpoints must be the same existing pair. A linked endpoint cannot be silently rebound to another block.

On success, both endpoints receive the actor's UUID and name as owner while preserving the existing link. Taking another player's pair is immediate and requires no permission from the old owner. The old and new owners receive a message when online. There is no limit on how many radio/beacon pairs one player may own.

Ownership and links are valid only for the match identifier under which they were claimed. At match end they remain stored on the blocks but are inactive. Claiming them during a later match replaces stale ownership/link state and resets the radio's per-match usage state. Radio or beacon blocks outside the active match dimension or border cannot be activated or claimed.

## Supply Catalog

Supply definitions are server datapack JSON resources under:

`data/<namespace>/exodus_supply_drops/<name>.json`

They reload through the normal server `/reload` flow. The server validates every definition. An invalid definition is excluded from the catalog and produces a precise English warning naming its resource ID and invalid field. A reload never corrupts an already-falling drop or an already-landed crate; those retain the resolved definition data captured when the request was accepted.

The initial schema is:

```json
{
  "display_name": "AK-47 Supply Drop",
  "icon": "minecraft:crossbow",
  "loot_table": "exodus:supply_drops/ak47",
  "radio_types": ["special"],
  "max_requests": 3,
  "cooldown_seconds": 120,
  "cost": {
    "item": "minecraft:emerald",
    "count": 12
  },
  "drop": {
    "smoke_color": "#D94841"
  },
  "enabled": true,
  "sort_order": 10
}
```

Rules:

- The resource ID of the JSON is the stable supply ID; no duplicate `id` field is stored in the file.
- `display_name` is a literal English name for this phase.
- `icon`, `loot_table`, and `cost.item` are namespaced resource IDs. Modded item and loot-table IDs are allowed when present.
- `radio_types` is a non-empty set containing only `basic` and/or `special`. A definition is shown only by the listed radio types; Special does not implicitly include Basic entries.
- `max_requests` is `-1` for unlimited or a positive integer. Zero is invalid; disable entries with `enabled: false` instead.
- `cooldown_seconds` is zero or greater.
- `cost` is optional. When absent, the request is free. `cost.count` must be positive.
- `drop.smoke_color` is a six-digit RGB hex color.
- `enabled` defaults to `true`; `sort_order` defaults to `0`.

The UI orders entries by `sort_order`, then `display_name`, then resource ID for deterministic ties.

## Radio State and Match Reset

Each Supply Radio block entity stores, per supply ID:

- accepted request count for the current match;
- the game-time tick when its cooldown expires.

This state belongs to the physical radio, not the player. Taking a radio transfers its remaining request counts and active cooldowns unchanged. Re-linking the same pair does not reset usage. Owning multiple radios grants the independent quota of each radio.

Each radio also stores the match identifier associated with its usage state. The match manager creates a fresh persistent match identifier when a start operation commits successfully. On the first valid claim or access under a different match identifier, the radio clears request counts and cooldowns before proceeding. This lazy reset handles placed blocks without scanning all loaded and unloaded chunks.

## Radio UI and Requests

Only the current owner, while an active match player, can open and use a Supply Radio. Everyone else receives a short denial message. The server opens a menu backed by the radio block entity and sends the currently valid filtered catalog to the client.

For each entry, the UI shows:

- icon and display name;
- item cost or `Free`;
- remaining requests for that radio, or `Unlimited`;
- remaining cooldown;
- an enabled request button only when all known preconditions are met.

The server revalidates every button request; client state is never authoritative. Acceptance requires the same owner and active match, an intact valid link, both endpoints still inside the match dimension/border and configured range, no drop already in flight for that pair, a catalog entry allowed by that radio type, remaining quota, expired cooldown, sufficient payment items, and a viable spawn height above the beacon.

Payment accepts exact item IDs without matching custom NBT. Items are removed across the player's main inventory and hotbar. Creative match players receive no exemption; however, under current match rules Creative players are spectators and therefore cannot request drops.

After all validation succeeds, the server atomically records the accepted request, sets its cooldown, removes payment, marks the link as having a drop in flight, and creates the drop entity. If entity creation fails in that transaction, payment, usage, cooldown, and in-flight state are rolled back. After the entity exists, destruction, theft, or loss of the resulting public crate never refunds payment or quota.

## Falling Drop

An accepted drop spawns centered directly above its beacon at the lesser of:

- beacon Y plus the configurable spawn-height offset, default `80` blocks; or
- the highest legal entity position below the dimension build ceiling.

If no legal spawn position exists above the beacon, the request is rejected before payment or quota consumption. The server temporarily keeps the drop path's chunks active while the entity exists and releases that ticket after landing or removal.

The entity descends vertically at a configurable constant speed. It renders a Supply Crate suspended beneath a simple parachute and emits colored smoke using the definition's captured RGB value. It does not damage or push entities and cannot be attacked. Horizontal movement is not introduced in this phase.

The server checks the vertical collision path continuously so high tick speed cannot skip thin surfaces. The drop lands on the upper surface of the first blocking collision shape encountered from above. Therefore, a beacon under a roof produces a crate on top of that roof. Leaves and other blocks with a blocking collision shape can catch the drop. Fluids alone do not count as a landing surface.

At landing, the entity places a Supply Crate in the first replaceable block space immediately above the hit surface. If that space becomes obstructed during descent, it searches upward within a small configurable landing clearance, default `3` blocks. If no legal crate position exists, the entity remains at the collision point and retries for a bounded configurable period, default `10` seconds; after that it drops its generated contents as item entities, logs a warning, clears the in-flight state, and removes itself. This fallback prevents a permanently stuck link without silently deleting paid loot.

## Supply Crate

The Supply Crate block entity has 54 slots and records the accepted supply ID and display name. The server resolves and fills its inventory from the referenced Minecraft loot table exactly once, when landing succeeds. Reopening, chunk reload, server restart, ownership transfer, or datapack reload cannot regenerate contents.

Loot generation uses normal Minecraft loot-table behavior with the requesting player as the luck/context player and the landing position as origin. Output is inserted into the 54 slots. If generated stacks exceed capacity, the overflow is spawned once as item entities at the crate and an English warning is logged. This is runtime-safe because a loot table's possible output size cannot be reliably proven during datapack reload.

Any player can open and loot a landed crate, including spectators if normal Minecraft interaction permits them. The crate is intentionally not owner-locked. The crate cannot be moved by pistons and has no hopper automation in this phase, preventing unattended extraction and movement from bypassing the intended public interaction.

Once the inventory first becomes empty, the crate starts a 30-second removal timer. Adding items back does not cancel the timer; the crate rejects insertion by players and automation, so it cannot be used as permanent storage. At expiry it plays a short smoke effect and removes itself. A crate that still contains loot persists through match end and restart; ending a match does not delete it or its inventory.

## Persistence and Recovery

Radio and Beacon ownership, link coordinates, link match identifier, per-supply usage, cooldown deadlines, and in-flight identity are stored in their block entities. The falling entity serializes its resolved supply ID, display name, loot table ID, smoke color, requester, source radio, source beacon, and whether loot has already been generated.

The active match's persistent match identifier is stored in `ExodusSavedData`. Existing save data without this field loads safely with no active identifier. It is assigned only when a start successfully commits, cleared when recovery or cleanup returns the match to `IDLE`, and never reused.

On server restart, the existing foundation rule recovers active matches to `IDLE`; it does not resume them. Any serialized falling supply entity from the abandoned match removes itself without generating loot when it next ticks, and clears the source radio's stale in-flight flag if the radio is loaded. Stale flags are also cleared lazily when a radio is claimed in a later match. Landed crates remain intact.

Breaking a Radio or Beacon in Creative clears its own block entity and attempts to clear the counterpart link if that chunk is currently loaded. The counterpart also validates its peer lazily on use, so an unloaded broken endpoint cannot leave a usable ghost link.

## Architecture

Keep the subsystem outside the existing monolithic match flow except for narrow lifecycle hooks:

- Registry module: blocks, items, block entities, entity type, menu type, serializers, client screen/renderer registration.
- Catalog module: datapack JSON parsing, validation, deterministic ordering, immutable snapshots, and client synchronization DTOs.
- Linking module: selection stored on the Linking Tool, ownership policy, one-to-one validation, and ownership notifications.
- Radio module: persistent link/usage state, server menu, request validation, atomic payment/quota/cooldown transitions.
- Drop module: falling entity, collision/landing service, chunk ticket lifecycle, parachute/smoke rendering, crate placement.
- Crate module: inventory, one-time loot filling, public menu, extraction-only rules, empty-removal lifecycle.
- Match integration: match UUID creation/clear, active-player eligibility query, and stale-state invalidation.

Pure Java policy types must cover catalog validation, link validation, usage/cooldown transitions, and request acceptance inputs. Forge adapters own registries, worlds, menus, packets, entities, loot tables, block entities, and events.

No third-party mod is installed or removed. Forge 47.4.10 and Minecraft 1.20.1 APIs must be verified through compilation or local dependency sources rather than guessed.

## Failure Handling and Logging

Expected player errors use concise English messages: no active match, not an active player, not owner, invalid/stale link, wrong dimension, outside border, too far apart, occupied endpoint, unknown supply, quota exhausted, cooldown active, insufficient payment, drop already in flight, or no legal spawn height.

Unexpected server failures log supply ID, requester UUID, dimension, radio position, beacon position, and the operation stage without logging unrelated player data. Operations must fail closed: no duplicate entity, duplicated loot, free accepted request, or consumed payment without either a live falling entity or the explicit landing fallback.

## Testing and Acceptance

Automated pure tests cover:

- JSON defaults and every invalid field class;
- Basic/Special filtering and deterministic sorting;
- one-to-one link rules, distance, border, dimension, match membership, stale match IDs, and takeover behavior;
- Linking Tool consumption only after success;
- per-radio quota and cooldown state, unlimited quota, match reset, takeover preservation, and multiple-radio independence;
- atomic payment/state rollback when spawn creation fails;
- first-surface collision and obstructed-landing fallback decisions;
- one-time loot generation and empty-removal timing;
- persistence round trips and restart invalidation.

Forge compilation and build gates are `gradlew.bat test` and `gradlew.bat build` under `exodus-mod/`. A development client/server run must verify that registries load, sample datapack definitions reload, menus open, and no new Forge/KubeJS errors appear. The built Project Exodus JAR is copied to `mods/` only after both Gradle gates pass.

The user performs the real two-client multiplayer acceptance test. Until the user reports it passing, the following remain explicitly unverified: ownership takeover notifications across two clients, simultaneous request races, remote observation of parachute/smoke, public crate looting, and multiplayer match-to-match resets.


