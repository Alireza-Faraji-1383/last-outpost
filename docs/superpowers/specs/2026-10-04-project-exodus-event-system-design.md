# Project Exodus Event System

# Scheduling update
The approved October 5 [daily events design](2026-10-05-project-exodus-daily-events-design.md) supersedes this document's elapsed-day random scheduling, cooperative Zombie Hunt, and initial ordinary airdrop catalog. Existing unique-Core, radio isolation, rewards, UI, and lifecycle requirements continue to apply.

## Intent and approved scope
Build a match-owned extensible event foundation and three initial event families: Zombie Hunt, Manhunt, and supply airdrops. Reuse the existing Forge match lifecycle, map integration, supply entity/crate, and teleporter component authority. Preserve current local changes and radio behavior. No third-party mod changes.

## Responsibilities
Event definitions describe ID, scope, eligible match days, weight, one-time flag, cooldown, duration, participant selection, objective, reward, and presentation. Scheduler, active registry, objective tracking, participant selection, presentation, and rewards have separate responsibilities. World-drop delivery stays in the supply system; EventManager requests it. Definitions are data-driven, while objective handlers implement Minecraft interactions.

## Scheduling
Use match elapsed ticks, not mutable world daylight, for event days (24,000 ticks per day; day 1 starts at match commit). Configurable daily random probability, weighted eligible pool, cooldown days, and fixed-day milestones. Proposed initial default: 60 percent daily random roll; missed boundaries are reconciled once without repeating completed rolls. Persist scheduling history and terminal outcome before issuing a reward. The day-5 core milestone retries safe placement when temporarily unavailable and records success only after successful delivery scheduling. It must never duplicate the unique core.

Global and targeted events may coexist. At most one Manhunt per player. At most one active Zombie Hunt; personal UI takes priority while a global event remains active underneath. No spectator objectives or rewards. Events do not continue across server restart because matches already recover to IDLE.

## Initial events
### Zombie Hunt
GLOBAL cooperative objective: active participants kill 50 minecraft:zombie entities within 90 seconds. Only eligible player kills in the match dimension count; unrelated deaths do not. Show shared kills and remaining time on participant sidebars. Proposed reward rule: each participant who contributed at least one qualifying kill and remains eligible receives 10 emeralds on completion. Timeout gives no reward. This contributor rule avoids awarding spectators or idle noncontributors.

### Manhunt
TARGETED: select two distinct active living players as hunter and prey, with 300 seconds duration. Hunter sees live distance to prey in blocks and remaining time; prey sees survival countdown without hunter identity or position. Any prey death during the event awards the hunter 16 emeralds regardless of damage source. Living prey at timeout receives 8 emeralds. Offline/out-of-dimension time alone is not a death: objective pauses neither deadline nor match grace; loss of eligibility cancels without reward, except an actual prey death is evaluated before elimination cancellation. Hunter death cancels. A death observed at the deadline is resolved before survival payout. Rewards are issued once; completion notification goes to the recipient in English after delivery. Full inventory overflow is dropped at the recipient's position rather than silently lost.

### Airdrops
Reuse SupplyDropEntity and SupplyCrateBlockEntity. Radio drops retain their current appearance and quota/payment behavior. Event drops have blue falling outline and blue smoke; landed crates do not glow. Provide configurable high-value medical, combat, and equipment drop variants with explicit existing loot tables/item IDs, reviewed against installed content. Location events use public map markers plus a brief English start announcement, not an objective sidebar. Airdrops land inside the safe match border on suitable surface; bounded incremental placement avoids forcing large synchronous world scans. Configurable lifetime and cleanup remove event-owned resources and markers on expiry and match cleanup. Do not delete unrelated blocks or items.

### Guaranteed core drop
On day 5 deliver exactly one special event airdrop containing Dimensional Core, the noncraftable center teleporter component. One successful execution per match; claim/binding must use the existing component lifecycle and uniqueness authority, never bypass it via a generic loot roll. If the core already exists for the match, do not create another. Persist crate ownership and milestone state; the payload is not duplicated by reloading a chunk or reopening its container.

## Integration and extensibility
Supply event creation does not require a physical radio, consume a radio payment, or alter radio cooldowns. Public airdrop markers extend the current map projection. Scoreboards are per-player and must be cleared/restored on event completion, logout, dimension departure, stop, and recovery. Rejoining participants receive the current view. Administrative event start/status/stop commands require permission level 2 and support testing without advancing days. Configuration holds thresholds and reward amounts. Unsupported future TEAM/ZONE scopes and objective types are explicit extension points, not fake implemented functionality.

## Validation and release
Pure Java tests cover scheduler eligibility, milestones, cooldowns, one-time behavior, concurrency, objective attribution, death/timeout precedence, and reward idempotency. Integration must compile against Minecraft 1.20.1 and Forge 47.4.10. Run gradlew.bat clean test build. Increase mod_version from the current unreleased workspace value 0.5.3 to a fresh available version before delivery; update distribution contracts consistently. Only claim automated verification. Manual two-client acceptance covers blue glow, landing, map cleanup, recipient-specific sidebars, distances, rewards/chat, and core uniqueness.

## Review decisions
Proposed defaults requiring review: 60 percent daily random probability; contributor-only 10-emerald Zombie Hunt payout; 24,000 elapsed match ticks per event day. Actual airdrop item counts and weights will be recorded in the implementation plan before coding.
