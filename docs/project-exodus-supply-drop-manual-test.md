# Project Exodus Supply Drop Manual Test

The real multiplayer acceptance check requires two clients and is performed by the user.

1. Start a match with two Survival players using `/exodus start`.
2. Give each player a Basic Supply Radio, Special Supply Radio, Drop Beacon, and Linking Tool with `/give <player> exodus:<item>`.
3. Place a Radio and Beacon less than 128 blocks apart and inside the match border.
4. Right-click the Radio and then the Beacon with the Linking Tool. Confirm the item is consumed only after success.
5. Open the owned Radio. Confirm `Basic Food Supplies` shows its price and three-request quota.
6. Put four emeralds in the owner's inventory and request the drop. Confirm payment is removed once and a second request is blocked while the drop is in flight/cooldown.
7. Watch the crate and white parachute descend with brown smoke directly above the Beacon. Put a roof above the Beacon and confirm the next drop lands on the roof.
8. Open the landed 54-slot crate from both clients and confirm its loot is public and does not regenerate after reopening/relogging.
9. Use another Linking Tool on the linked Radio and Beacon from the second player. Confirm ownership transfers and the consumed quota/cooldown remain with the Radio.
10. End and start a new match, reclaim the pair, and confirm its quota is reset to three.
11. Restart the server while a drop is falling. Confirm the recovered IDLE match does not produce loot from that stale entity; landed crates must remain.
12. Run `/reload` after changing a supply JSON and confirm invalid files log an English warning while valid entries appear after reopening the UI.

Record failures with `latest.log`, both player names, Radio/Beacon coordinates, and the relevant supply ID.
