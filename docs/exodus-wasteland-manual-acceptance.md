# Exodus Wasteland manual acceptance

Automated build and unit checks do not replace this real-client test.

1. Install the built private Exodus JAR and public Lost Cities 1.20-7.5.5 file.
2. Start a fresh server, provide every required NBT template, and run `/exodus arena prepare`.
3. Confirm progress appears only at 10-percent boundaries and preparation reaches `READY`.
4. Restart once during pregeneration and confirm status resumes without duplicated structures, entities, or loot chests.
5. Join with two real clients in Survival at the Overworld lobby and run `/exodus start`.
6. Confirm both clients enter unique underground bases at depth -6, both four-part faction bases join correctly, camps and copied enemies appear once, and marker chests draw shared loot.
7. Confirm a non-member is bounced from the wasteland while `/exodus dimension enter|leave` preserves an operator's game mode.
8. Run `/exodus stop`; confirm both clients return to the Overworld spawn and the consumed arena cannot start another match.

Record screenshots and server logs for failures. Real two-client acceptance remains pending until a human reports this checklist complete.
