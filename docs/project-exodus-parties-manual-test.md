# Project Exodus 0.7.0 Party Acceptance

## Runtime and commands

Restart the Minecraft clients/server with the same Exodus 0.7.0 JAR. Map protocol is now 3; mixed Exodus versions are intentionally rejected. JourneyMap remains 6.0.6. No third-party mods were installed or removed.

Run an Exodus match with two Survival participants. An operator starts the match; normal players may use these commands:

```text
/exodus party create
/exodus party invite PlayerName
/exodus party accept PlayerName
/exodus party decline PlayerName
/exodus party status
/exodus party leave
/exodus party disband
```

`accept` and `decline` also work without a player name when exactly one invitation is pending. Only the creator can invite or disband. Only two players can belong to a party. An invitation expires after 30 seconds by default. No chat, health, or online-status HUD is added.

## Human two-client acceptance (pending)

1. As non-operators, create, invite, and accept. Check both receive English messages. Verify a third player cannot enter the full party and a spectator cannot join. Existing admin commands remain restricted.
2. Before acceptance, players can damage each other. After acceptance, try melee, ordinary TacZ bullets, armor-piercing bullets, incendiary bullets, and player-attributed explosives. Party damage must be blocked; the incendiary pre-hit must not ignite the teammate. Lava, environmental fire, falls, and enemy attacks remain harmful. Test any other installed weapon mod separately.
3. Verify the teammate name tag is green while unrelated player name tags remain hidden in the match dimension. JourneyMap minimap/fullscreen should show only the teammate's green marker, updating as the teammate moves. A third player must not see either private marker.
4. Give both players the same valid current-match teleporter component. Both boss bars must show 1/9, not 2/9. Give one player a different component: both show 2/9. Remove every copy of a type: the count falls. Invalid/unbound/previous-match components do not count. Cursor-held items count. Items installed in a single owned device remain counted; parts in separate devices are not falsely combined as one complete device. Active teleporter countdown still replaces component progress.
5. Use `leave` or creator `disband`. Both receive the separation warning immediately. Protection, shared progress, tags, and markers stay for 30 seconds. Duplicate leave cannot reset the countdown. At expiry both receive confirmation, private markers/tags disappear, progress becomes individual, and former teammates may hurt each other.
6. Disconnect and reconnect within grace: membership remains. Leave the dimension: the private position marker disappears immediately. Reenter within grace: it returns. Grace expiry or final elimination dissolves the party. Server stop/restart and match stop/new match remove all parties and invitations.
7. Start Manhunt on two unallied players with `/exodus event start exodus:manhunt`, then form a party between its hunter and prey. The event must immediately report `failed`, clear its private sidebar, and pay no emeralds to either player, including after the original deadline or a subsequent death. Existing teammates cannot be selected as hunter/prey. Changing other parties remains allowed.
8. Verify teaming leaves enemy allocation and individual teleporter winner eligibility unchanged. Each winner must be physically eligible at expiry.

## Automated evidence

- JUnit tests cover pure membership/expiry/departure, distinct component union and separate-device progress, legal Manhunt pairs, terminal no-reward failure, private tag state, and packet bounds/round-trip.
- Forge PartyGameTests use real server players/commands/damage events, inventory/cursor components, event reward state and lifecycle, and a simulated gun Pre event with the verified TacZ getter contract.
- Installed TacZ 1.1.8-hotfix bytecode verifies `ModDamageTypes.Sources.bullet`/`bulletVoid` carry the shooter and `EntityKineticBullet` posts the cancelable `EntityHurtByGunEvent.Pre` before ignition and ordinary damage. The optional adapter subscribes to this Pre event without requiring another dependency or installing another mod.
- These automated checks do not replace the real two-client visual/gun acceptance above.

## Workflow

Work continues directly on main per the user's instruction. Outstanding fixed-city and event sources were committed and merged into main before party implementation. Previous private Exodus JAR is backed up outside the active mods directory before installing the new version.
