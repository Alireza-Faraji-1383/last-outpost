# Project Exodus match map, devices, and final phase

## Intent and authorization

Provide a personal JourneyMap view of the current match, an always-visible match boss bar, claimable public teleporters, device-based respawn, and permanent death elimination after the first teleporter activates. The user approved the decisions below through a one-question-at-a-time design interview and authorized recording this design. Implementation is delivered in `codex/exodus-match-map`; real client and two-client acceptance remain pending. See the dated verification report.

These explicitly approved additions extend the foundation scope with map UI, device ownership, respawn rules, temporary damage protection, and final-phase elimination. Do not extend them into teams, faction membership, new structures, other victory conditions, or unrelated combat rules. All product text and logs remain English.

## Existing integration and compatibility

- Installed JourneyMap: `mods/journeymap-forge-1.20.1-6.0.6.jar`.
- `ArenaPreparationService` prepares public faction bases and camps; arena records and placement heights persist in `ExodusSavedData`. Bind map locations to the exact arena consumed by the current match. The existing location helper selects the latest record with placements and is not a sufficient match identity contract.
- `MatchManager.AllocationJob.commit` stores player base centers, origins, spawns, and owners before entering RUNNING. Use committed base data; failed allocation must not publish markers or reset time.
- The current city sampler records coverage counts rather than a dedicated city center. The separately approved fixed-city design defines one city per arena. Use its validated arena/city manifest when available; otherwise verify Lost Cities' real city-center data before recording a marker. Never treat the arena center or a coverage sample as an unverified city center. This feature does not implement or replace city generation.
- `RespawnService` already sets base respawn and resets to Overworld spawn. Extend this service rather than introduce a competing respawn authority.
- Existing teleporter inventory, match-bound components, unique rare items, countdown configuration, winner selection, slot locking, and cleanup remain authoritative.
- JourneyMap documentation describes server-managed waypoints in version 6 and server permissions. Exact API artifacts, UI overlays, normal/expanded player radar controls, operator exceptions, and dimension permissions must be verified against installed 6.0.6 before implementation. Do not infer compatibility from API examples for Minecraft 1.21.

## Personal map contract

The server decides which information each player receives. Do not transmit undiscovered private locations to clients and merely hide their icons.

| Feature | Visibility and lifetime |
| --- | --- |
| Match zone | Everyone associated with the match, from RUNNING until cleanup |
| `City` | One marker at the real city center, public from RUNNING |
| `Russian Base`, `American Base` | Public from RUNNING; no faction membership is introduced |
| Own original base | Private to its player, from RUNNING |
| Other players' bases | Discoverable; anonymous `Discovered Camp` |
| Arena camps | Discoverable; `Abandoned Camp` or `Occupied Camp` |
| Own claimed devices | Private `Claimed Device` markers; original base remains marked |
| Other incomplete devices | Discoverable; anonymous `Discovered Device` |
| Active teleporter | Prominent public marker immediately on activation |
| Three rare dropped components | Public live position while the current-match item entity is on the ground |

Markers appear on minimap and fullscreen map, without in-world beacons. Preserve the existing rare-item glow. Keep personal JourneyMap waypoints untouched. Faction-base public markers remain even when their devices are claimed; ownership does not rename the public base.

Draw the zone border in red. Tint the outside translucent red if the verified JourneyMap overlay supports this; retain readable terrain inside. Terrain remains unexplored until normal player exploration; do not reveal or pre-map terrain at match start.

Disable other-player radar on both map UIs through server-enforced JourneyMap permissions. The local player's navigation indicator remains. This applies to normal and expanded radar, including operator visibility paths where supported. Verify all relevant permission paths rather than rely on player settings. This contract concerns JourneyMap display, not preventing a modified client from observing normal Minecraft entity packets.

Rare item markers follow Alpha Key, Beta Key, and Dimensional Core item entities. Remove on pickup or removal, never follow their carrier, and exclude items not bound to this match. Chunk unload must not imply pickup: reconcile lifecycle without force-loading arbitrary chunks or publishing stale coordinates as live.

## Discovery and spectators

Discovery requires an active living match player within inclusive 50-block horizontal radius and inclusive 20-block vertical difference of a location's canonical center. Line of sight is not required. Persist discoveries per player and match; moving away, disconnecting, or transferring device ownership does not erase them. A removed device loses its marker. Original public POIs persist until match cleanup.

Initial and automatic spectators see public information only. Spectator movement cannot discover locations. A player permanently eliminated by death retains their previous private discoveries and owned-device markers, but cannot discover, claim, set spawn, or interact as an active participant.

## Match clock and boss bar

On successful match commit, set the match dimension's day time to 0 and initialize an independent elapsed-match tick counter. Preserve `gameTime`. Show `Day 1` immediately; advance the day every 24,000 elapsed server ticks. Do not reset the clock on failed preflight. This reset does not imply changing another dimension's time or introducing a day/night progression subsystem.

Show a boss bar to associated online match users from RUNNING onward. Before activation, an active player's title is `Day N â€” Components: K/9`, with progress K/9. Count the union of distinct valid current-match component types in the player's inventory and a single owned device. With multiple owned devices, select the device yielding the largest union with that inventory; never combine installed components across devices. Inventory-only progress is valid when no device is owned. Duplicate components do not increase K. Ender Chest, ordinary chests, other players' inventories, and unowned devices do not count. Dropping or transferring a component can reduce progress.

Spectators see only `Day N` with an empty bar before activation. Once the first device activates, all associated viewers see the day and public teleporter countdown, and the bar displays countdown progress. Preserve the existing public coordinate information alongside the countdown and the configured countdown/radius/capacity captured at activation.

## Device identity and ownership

Use persistent device identities scoped to the match and dimension/position; do not derive ownership solely from a block's current loaded state. Distinguish protected original-base devices from public devices and associate protected devices with committed base owners.

Each original-base device belongs to its base owner and cannot be claimed by another player. Public devices, including faction-base devices, are claimable. A player may own multiple devices. Ownership affects personal map/progress and spawn selection only; before activation, any otherwise eligible active participant may insert or remove components regardless of ownership. Ownership never determines winners.

Add `Claim Device` and `Set Spawnpoint` controls to the existing device screen. Validate every operation on the server against match, eligibility, device identity, range, and lifecycle. Button visibility alone is not authorization.

## Claim process

- A public incomplete device can transfer directly from its current owner to another eligible player, including while its owner is offline.
- `Claim Device` starts a 20-second server-timed claim. The claimant must stay within inclusive five-block three-dimensional radius of the device center.
- Only one claim can run on a device. A competing request receives `This device is being claimed.`
- Leaving range, dying, disconnecting, losing active eligibility, device removal, match ending, or device activation cancels the claim.
- The claimant may use the device inventory during claiming. If the device completes before ownership transfer, cancel the claim and preserve the existing owner.
- Display `Claiming Device: 12/20s` in the action bar rather than replace the match boss bar. Report success or cancellation in chat.
- Notify the current owner when claiming starts, including device coordinates, and after successful transfer, including the new owner's name. Queue notifications for an offline owner and deliver them on login.
- Transfer and claim completion must be atomic. If the previous owner's selected spawn uses the transferred device, reset it to the protected original-base device and notify them. Refresh personal markers and progress immediately.
- An activated device cannot be claimed. Other incomplete public devices remain claimable during the final phase.
- Owner death does not delete ownership or installed items. Public incomplete devices remain claimable; original-base and active devices retain protection.

## Device respawn

Before activation, `Set Spawnpoint` selects an owned device; original-base device is the default. Public devices require claiming first. Multiple ownership does not imply multiple simultaneously selected respawn targets.

Respawn one block above the selected indestructible device. Preserve that landing space as usable: indestructibility alone does not guarantee headroom, so verify the spawn footprint and prevent or safely handle obstruction without silently changing the agreed target. Do not reject the selection merely in favor of an unrelated search-based spawn policy.

If another active living match player is within inclusive ten-block three-dimensional radius of the selected non-original device at death/respawn resolution, use the original-base device for this respawn only. Spectators do not block it. Preserve the selection so a later death can use it again. The original-base device is exempt even when other players are nearby.

Deletion/removal of the selected device, or transfer to another owner, permanently resets the selection to the original-base device and sends an English notification, queued when necessary. Devices are indestructible for ordinary play; removal handling still covers administrative or lifecycle changes.

Beds and Respawn Anchors cannot change respawn during the match. Sleeping is disabled. End/stop/recovery reset associated participants' respawn to the Overworld's current world spawn, including persistent offline resets. Preserve the existing associated-player return and initial-spectator game-mode contracts.

## Temporary damage protection

Apply ten seconds of server-timed protection on successful match start and on every allowed match respawn. During protection, the player takes no damage from any source and cannot damage other players, including attacks attributed through projectiles or other indirect player damage. These are temporary protection rules, not a general PvP redesign. Do not apply this to spectator conversion as if it were a playable respawn. Cleanup protection on ending/recovery, and preserve its remaining time across normal player recreation rather than allowing reconnects to restart it.

## Final phase and outcomes

Only the first complete device activates. Activation atomically locks its slots, records the sole final destination, freezes its ownership, exposes its map marker, changes the boss bar, disables `Set Spawnpoint` everywhere, and disables playable respawn for all match players.

After activation, death permanently eliminates that player for this match and converts them to Spectator. Record elimination on the server; reconnecting cannot restore participation. Keep the match roster separate from the active-player/grace-period associations so death, pending return, and leaving cannot be confused.

A sole living player is not an automatic winner. Existing countdown-expiry eligibility remains active Survival status plus nearest eligible proximity within the captured radius and capacity, independent of device ownership or contributions.

If every roster participant has been eliminated by death, end immediately without a winner. Disconnection, dimension departure, and grace expiry do not count as death elimination. A participant in the 120-second return window prevents the all-dead condition unless already eliminated. If a roster participant left without dying, do not misreport an all-dead result; retain the existing automatic ending when there are no active or pending participants. Countdown expiry with no eligible winner still ends without a winner as before.

## Architecture and persistence

Keep responsibilities separated: arena location projection, recipient-specific map state and JourneyMap client adapter, discovery policy, device ownership/claim service, component-progress policy, match clock/boss bar, respawn/protection policy, and final-phase elimination. Extend existing match and teleporter authorities instead of maintaining duplicate lifecycle state.

Server persistence stores current arena identity/location snapshot, device classification and owners, selected spawn, discoveries, eliminated roster members, clock reference/counter, and queued notifications. Preserve device inventory semantics. Do not generate/load all chunks just to calculate progress; reconcile block-entity state with a server-side device index. Mutations of installed components must update that index.

Reconnect sends a fresh authorized map snapshot. Updates/removals carry match identity so stale packets cannot resurrect a previous match's markers. End, failed commit, stop, disconnect where applicable, and restart recovery release map displays, boss bars, claims, protection, and current-match data. Restart does not resume a match; restore IDLE and the saved border, preserve world structures, and apply pending respawn/return resets. Clear match discoveries, while preserving ordinary JourneyMap personal data.

Gameplay thresholds belong in `ExodusConfig`, with defaults matching this design. Capture in-progress claim/protection timing as needed so configuration changes cannot arbitrarily rewrite active timers. No third-party mod installation/removal is authorized by this design.

## Verification and delivery boundary

Pure policy tests cover recipient visibility, discovery boundary distances, unique component unions, best-owned-device selection, atomic claim races/cancellation, protected ownership, offline transfers, spawn fallback versus selection preservation, clock/day boundaries, protection timing, elimination versus grace/leave, sole-survivor non-victory, and all-dead ending.

Integration checks cover persistent round trips and old-save defaults, direct network request validation, teleporter slot/claim activation ordering, indirect damage attribution, player recreation, offline notifications/resets, old-match packet rejection, and cleanup on every ending/recovery path. Run the full Gradle test and build gates before reporting implementation complete.

Verify against the installed JourneyMap API and Forge 47.4.10/Minecraft 1.20.1. Exercise map overlays, player radar denial, reconnect snapshots, device UI, respawn location/headroom, and boss bars in a real client. The user performs real two-client multiplayer acceptance; do not describe it as verified before their success report.

## Primary references

- Installed JourneyMap 6.0.6 JAR and this repository's Forge source/build dependencies.
- https://teamjm.github.io/journeymap-docs/latest/server/multiplayer/
- https://github.com/TeamJM/journeymap-api
- Existing teleporter, respawn, wasteland, and fixed-city design documents in this directory. The current document supersedes their rules only where explicitly changed above.
