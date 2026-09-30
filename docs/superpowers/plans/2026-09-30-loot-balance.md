# Loot and Supply Balance Implementation Plan

> Execute inline using executing-plans; approval was given in chat. Use an isolated worktree; do not merge or commit main.

**Goal:** Generous varied chest loot, exact SEM drops, selectable basic/special supply bundles.

**Architecture:** Preserve marker IDs, faction keys and teleporter-material supplies. Compose vanilla nested loot tables for atomic gun/ammo bundles. Add optional supply quota groups and previews, persistent radio-wide cooldowns, and paginated selection. Normalize the weapon-specific ammo supplied by SEM after its drop handler; no mandatory third-party compile dependency.

- [x] Add balance acceptance tests; run and observe failures.
- [x] Generate curated loot from verified installed gun data and armor resources. Keep rare guns rare, add >60% durability, emerald probabilities, all compatible ammo, backpacks and utility.
- [x] Add selectable supply variants with shared category quotas, explicit contents, and global radio cooldowns. Preserve request ownership, validation, payment rollback and match reset.
- [x] Set SEM detection 80/shooting 70 and gun chance .02; normalize ammo drops to 16–32 and add .10 chance of 1–3 emeralds with configurable Exodus thresholds.
- [x] Run full Gradle test/build, inspect JSON references and installed-item compatibility, review final diff, install versioned JAR/config with backups.

**Acceptance:** Six basic categories (3/category/match, 30-second radio cooldown), six special categories (2/category/match, 90-second radio cooldown), choice before payment, coherent armor set, old material supplies unchanged. Real two-client gameplay is manual.

**Review Focus:** Nested gun tables must output both weapon and compatible ammo; selectable variants cannot multiply quotas; failed spawn cannot consume payment/quota/cooldown; persisted cooldowns reset on a new match; all choices are reachable after pagination; SEM handler cannot duplicate default ammo.
