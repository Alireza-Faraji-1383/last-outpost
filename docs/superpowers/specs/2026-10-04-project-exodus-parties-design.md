# Project Exodus Match Parties

## Approved intent

Add voluntary two-player parties during a running match. Parties belong to the current match only. Preserve individual enemy allocations, inventory ownership, respawn rules, and teleporter winner eligibility. No team chat, health display, or online-status HUD is added. All commands, notifications, and logs are English.

## Membership and commands

Use `/exodus party create`, `invite <player>`, `accept <inviter>`, `decline <inviter>`, `leave`, `disband`, and `status`. Creation makes a one-member party; its creator may invite one eligible active player. Acceptance must be explicit. Each player may belong to only one party. Invitations expire after 30 seconds and are revalidated on acceptance. A full party cannot invite. Pending departures block invitations and acceptance for affected members. Commands validate current match identity and participation server-side; spectators and non-members cannot join.

Leaving or disbanding a two-member party immediately notifies both members and begins a 30-second departure countdown. Membership, friendly-fire protection, shared progress, name tags, and map visibility remain in force until expiry. At expiry the party dissolves and both players may damage one another. The countdown uses server ticks and is not reset by disconnecting. A singleton can leave immediately. Disconnect grace preserves membership; final participant departure dissolves the party immediately. Match stop, normal ending, failed start, and restart recovery clear memberships, invitations, departure timers, and client projections. Old-match state never applies to a new match.

## Combat and identification

Block player-attributed damage between party members, including melee, owned projectiles, gun damage, and player-attributed explosions. Environmental damage remains unchanged. Verify installed gun damage attribution against source or actual integration tests; do not assume vanilla scoreboard protection covers it. Keep server membership authoritative and avoid replacing unrelated scoreboard teams.

Show the teammate name tag in green while preserving existing concealment for other players. Supply recipient-filtered teammate position snapshots through the existing Exodus map transport and JourneyMap adapter. Reveal position only to the teammate, in the current match dimension while that teammate is physically present. Remove markers on departure, dimension exit, final elimination, match cleanup, and client disconnect. Keep enemy/player radar restrictions intact. Refresh moving markers at a configurable bounded interval, default one second, using loaded player positions without chunk loading. Validate JourneyMap 6.0.6 integration using existing adapter APIs.

## Shared component progress

Both members receive the same component progress mask. Union valid current-match component types in both current inventories, including carried cursor stacks according to existing scanning behavior. Count each of the nine types once, regardless of stack count or duplicate ownership. Losing the last available instance reduces the count. Preserve the existing device-progress behavior: evaluate the pooled inventory mask against each eligible owned device of either member, retaining the best count; do not pool installed parts from separate devices as though one device contained them. Preserve the active teleporter countdown display. Inventory and device ownership are not transferred by party membership.

## Event compatibility

Provide a reusable authoritative same-party query and membership-change notification for opposing-player events. Manhunt may change party freely. If its hunter and target become party members, fail the event immediately with no reward to anyone; invalidate pending reward delivery. Role selection must not pair existing teammates as opponents. The installed 0.6.0 event implementation was found in the exodus-events worktree and must be integrated into main before adding parties. Preserve the existing event lifecycle, sidebar restoration, airdrops, and core uniqueness.

## Implementation boundaries

Keep party state and transition logic in a dedicated module, separate from commands, damage hooks, name-tag rendering, map projection, boss bars, and event policy. Put invitation duration, departure duration, capacity, and marker refresh thresholds in ExodusConfig. Scope all state by match UUID. Extend recipient snapshots without broadcasting private membership or teammate positions to other players. Preserve single-player progress behavior.

## Verification and distribution

Test acceptance validation, capacity, duplicate invitations, expiry, delayed departure, lifecycle cleanup, match isolation, duplicate component masks, pooled device progress, recipient privacy, event failure, and player-attributed damage policy. Compile against Minecraft 1.20.1 and Forge 47.4.10; run the full Gradle test/build gates. Increment mod_version beyond installed 0.6.0 to an unused release version and install the newly versioned JAR with consistent distribution metadata. Never overwrite a released JAR with different contents. Real two-client verification of gun damage, visible tags, JourneyMap movement, departure timing, and shared progress remains user-performed acceptance.

## Execution approval

The user approved continuous implementation and explicitly requested integration of outstanding worktree changes, development directly on main, and commits on main. Preserve this instruction instead of creating another worktree or pausing for artifact review.
