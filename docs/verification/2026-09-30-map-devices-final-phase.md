# Map, devices and final phase verification â€” 2026-09-30

Implementation is delivered for client acceptance in the managed worktree on `codex/exodus-match-map`. The user subsequently authorized committing and integrating this implementation into `main`. Only the built private Exodus JAR was replaced in the live instance.

## Implemented behavior

- Recipient-filtered JourneyMap overlays: red zone/exterior, verified Lost Cities center, consumed-arena faction/camp coordinates, private original bases/owned devices, persistent proximity discoveries, public activated destination and current-match dropped rare components. Stable base marker IDs are opaque. No carrier/player position is included in the Exodus projection.
- API 2.0.0 overlays target minimap/fullscreen. Exodus removes only its own overlays. Disconnect forgets pending render state; epoch/revision checks reject stale packets.
- JourneyMap 6.0.6 server player/name radar is denied for ordinary players and operators. Dimension override enablement and unrelated map preferences remain intact; original radar options restore on cleanup.
- Match boss bars start at RUNNING. Successful commit sets dayTime to zero without resetting gameTime. Personal nine-type progress combines inventory with one best owned device; final phase uses the public captured countdown/coordinates.
- Public devices allow uninterrupted 20-second claims within five blocks, multiple ownership, owner warnings/transfers and persistent offline notices. Original devices and the active destination cannot transfer. Ownership does not restrict eligible incomplete-device inventory access or decide winners.
- Owned-device spawn selection, per-death occupied-device fallback within ten blocks, permanent original fallback on ownership loss/removal, reserved two-block headroom, disabled bed/anchor changes, ten-second incoming/outgoing player damage protection, and current Overworld spawn cleanup.
- First activation disables playable respawn globally. Final deaths permanently eliminate; reconnect retains spectator state. A delayed pre-final death-screen respawn after activation also eliminates. A sole survivor still needs the teleporter; all roster deaths end without winners. Leaving remains distinct from death.

## Automated evidence

| Boundary | Observed result |
| --- | --- |
| Full Java 17 `test build` | 183 tests, zero failures/errors/skips; BUILD SUCCESSFUL in `.final-test-build.log` |
| Actual Forge dedicated server + installed JourneyMap 6.0.6 | `.final-packet-smoke.log` contains `[Exodus Test] lifecycle assertions passed` and BUILD SUCCESSFUL |
| Actual S2C packet capture/codec | Undiscovered camp coordinates and another player's private base label excluded; grace-expired spectator receives public-only projection |
| JourneyMap permissions | Real permission construction for op and ordinary recipient denies normal/name/expanded player radar; all captured option values restore exactly |
| Gameplay harness | Claim transfer and protected-base rejection; contested fallback preserves selection; pending death target changes on transfer; actual vanilla recreation resolves water obstruction above the device |
| Combat/final phase | Environmental incoming protection; direct, arrow and player-attributed explosion outgoing protection; expiry; actual activation/locking; delayed respawn denial; actual lethal damage; spectator recreation; sole survivor remains RUNNING; all-dead cleanup/world spawn |
| Regression proof | Intentional old alive-at-death gate fails `Lethal death must permanently eliminate` in `.red-lethal.log`; pending-transfer bug fails in `.red-transfer-respawn.log`; corrected lifecycle passes |
| Persistence/policies | Session/opaque markers/pending respawns/ownership/discovery/elimination round trips; offline notices/returns; clock/discovery/claim/progress/final-state/session-epoch/codec bounds |
| Independent review | Prior findings fixed; final review found no remaining concrete bugs |
| Product JAR inspection | Four map icons and network classes present; no JourneyMap classes/API copy or GameTest classes/templates included |

The dedicated harness uses synthetic in-memory connections to actual ServerPlayer objects. It runs real Forge events, actual vanilla player recreation, actual packet encoding and JourneyMap permission code; it does not render a client or establish real multiplayer sockets. Session time is advanced deterministically for claim/protection boundaries.

JourneyMap's own GameTestServer login path treats that server as non-dedicated and attempts to load Minecraft client classes. The integration smoke therefore runs on an actual DedicatedServer, without changing JourneyMap. The isolated test runtime omits TaCZ/armor mods, so existing gun/armor loot references emit missing-item warnings; those mods and the user's worlds were not changed.

## Reproduce

Use Java 17 and PowerShell from `exodus-mod`:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17'
$jm='C:\Users\Alireza\AppData\Roaming\PrismLauncher\instances\1.20.1(1)\minecraft\mods\journeymap-forge-1.20.1-6.0.6.jar'
.\gradlew.bat test build "-PjourneyMapJar=$jm" --console=plain
.\gradlew.bat runServer "-PjourneyMapJar=$jm" -PjourneyMapRuntime --console=plain
```

`journeyMapRuntime` enables the installed local JAR only in the isolated development runtime, remaps its mixin references, attaches the test-only source set and starts/stops the dedicated lifecycle harness automatically. Success requires the explicit lifecycle assertion message, not Gradle exit code alone. Never run this harness against a user save. Ordinary product builds exclude all GameTest content.

## Runtime delivery

- Installed: live instance `mods/exodus-0.3.0.jar`.
- SHA-256: `0137A98C1DE9B055B9FC976D49E269D07DEE3ABA6D44CE8CCCFB8550442E2EEC`.
- Previous JAR backed up outside mods at `backups/exodus-20260930-before-map/exodus-0.3.0.jar`.
- Installed/built hashes matched. Every other mod's name, size and modification timestamp was unchanged. JourneyMap remains the pre-existing 6.0.6 JAR.
- Fresh production client loading of this replacement has not been observed.

## Pending real client and two-client acceptance

Both clients and the server need the same new Exodus JAR and JourneyMap. Restart Minecraft before testing.

1. Prepare/start a fresh arena; check one correct city, faction markers, day one/time reset, immediate personal boss bar, red border/exterior and terrain exploration. Check minimap/fullscreen icons and absence of in-world Exodus beacons.
2. Separate clients and discover a camp independently. Confirm undiscovered camps and all other player radar, including an op, stay hidden. Spectator movement must not discover new locations.
3. Open a public device: claim/steal for twenty uninterrupted seconds, observe owner chats, move away/cancel, test multiple ownership and protected original rejection. Verify inventory access without ownership and both buttons.
4. Select a public device; die with/without another active player nearby. Check original fallback only for that death, spawn above device/headroom, lost-owner fallback, disabled beds/anchors, and ten seconds of symmetric damage protection.
5. Drop/pick up Alpha/Beta/Core: map marker and existing glow must follow the item entity and disappear on pickup/unload. Check personal distinct progress across inventory and one best device.
6. Activate: public star/countdown/coordinates, no further playable respawn, death/reconnect as spectator, retained old private discoveries, and sole survivor still needing teleport. Repeat with all participants killed to verify no-winner ending.
7. Stop/end and restart recovery: overlays/bars/claims clear, border restores, respawn returns to current Overworld spawn; verify offline owner notice/return delivery and unrelated JourneyMap settings/waypoints remain intact.

These client visuals, real network/reconnect interactions, full arena generation correspondence and end-to-end restart/offline flows remain pending until observed or reported by the user. City generation is unchanged; the marker uses verified Lost Cities center metadata when no fixed-city manifest exists.
