# Approved Loot and Supply Balance

## Chest loot

Keep chest quantity and marker IDs. Improve the common chest's useful base pool. Add independent, random resource selections (iron, diamond, gold, copper, redstone, coal, amethyst); higher tiers increase quantities. Obsidian, pearls and blaze rods have an additional rare selection chance of 6/9/16/22 percent for common/standard/valuable/elite.

Armor uses installed LR Armor defense values for rarity and 61–100% remaining durability. Defender (24 combined defense) is rarer than Scout (11). Add curated backpacks/upgrades, construction wand, spyglass, ropes, sack, tactical shield, optics, tactical medicines and food. Do not include administrative tools or technical library items.

Common guns include pistols, SMGs, assault rifles and shotguns from default TacZ and Daffas Arsenal. Precision rifles with installed compatible 8x scopes, minigun and RPG are rarer and restricted to valuable/elite gun pools; weapons chests can reach those pools. Each ordinary gun is paired atomically with two magazines of its actual ammo; minigun gets 100 rounds and RPG gets one rocket. Loose compatible ammo is distributed separately, with rockets and precision ammo rarer.

Emeralds occur only through dedicated pools: common/standard and specialized chest families have 15% chance for 2–6; valuable/elite have 25% chance for 6–15. Remove convertible emerald blocks from old pools. General survival bonuses use category tables, not complete specialized chest tables, to avoid multiplying currency and equipment probabilities. Preserve faction keys, craftable teleporter components and exclusion of the Dimensional Core.

## Simple Enemy Mod

Deployed `sem-common.toml`: detection 80, maximum shooting 70, custom drops enabled, gun drop chance .02, ammo drop chance 1. SEM determines the actual weapon's AmmoId; Exodus's LOWEST-priority adapter normalizes that ammo drop to 16–32 and independently adds a 10% chance of 1–3 emeralds. Recruited, owned PMC units retain their original weapon-return behavior. Ammo/emerald thresholds are configurable in ExodusConfig. No third-party JAR is edited.

## Basic radio packages

| Category | Guaranteed contents | Emerald price |
|---|---|---:|
| Food | 24 bread, 16 steak, 8 golden carrots | 4 |
| Light ammo | Choice: 96 rounds of 9mm or .45 ACP | 5 |
| Combat ammo | Choice: 90 rounds of 5.56/7.62x39/.308 or 32 shotgun shells | 7 |
| Building | 128 cobblestone, 64 planks, 32 glass, 16 ladders, 32 torches | 4 |
| Industrial | 24 iron, 16 copper, 16 redstone, 16 coal, 8 amethyst | 8 |
| Defense | Choice: one Scout armor piece, 80–100% durability | 8 |

Three requests per category, per radio, per match. Basic radio cooldown is 30 seconds. Basic packages remain available on special radios; those radios use 90 seconds for paid new packages.

## Special radio packages

| Category | Guaranteed contents | Emerald price |
|---|---|---:|
| Advanced industry | 48 iron, 24 gold, 12 diamonds, 32 redstone, 16 amethyst, 8 obsidian | 24 |
| Premium rifle | Choice: SCAR-H (80 .308 rounds) or HK416D (120 5.56 rounds), red dot fitted | 30 |
| Sniper | Choice: AWP (15 .338 rounds) or M107 (30 .50 BMG rounds), 8x scope fitted | 36 |
| Heavy armor | Complete Defender set, 80–100% durability | 40 |
| Minigun | Minigun plus 200 .308 rounds | 50 |
| Anti-armor | RPG-7 plus 3 rockets | 45 |

Two requests per category, per radio, per match, and a 90-second radio cooldown. Model variants share their category quota. Catalog previews list actual weapon/ammo amounts and armor set before payment; pagination exposes every entry. Successful purchase closes the snapshot menu so reopening shows updated quotas. Global cooldown is persisted in the existing usage list, resets for a new match and rolls back on failed entity insertion.

The three existing teleporter-material supply definitions and contents remain unchanged. A radio's active shared cooldown also gates them; they retain their own zero additional cooldown.

## Verification and installation

Baseline Gradle tests passed. New acceptance tests were observed failing before schema/policy implementation. Final `gradlew.bat test build` passed: 159 tests, zero failures/errors/skips. All 92 loot tables parsed as JSON and local table references resolved; 24 catalog definitions include selectable variants and the three legacy supplies. Forge/Minecraft compilation passed; installed TacZ NBT accessor and armor resource inspection verified scope keys, durability semantics, magazine sizes and compatible ammo.

Independent review found the old elite emerald-block bypass; it was removed and recursive currency testing now counts both emeralds and emerald blocks. Source lives in the isolated loot-balance worktree; main is neither merged nor committed.

Install with `exodus-mod/tools/install-balanced-runtime.ps1 -MinecraftRoot <instance>`. It backs up old Exodus JARs and SEM config, copies the 0.3.0 build, applies SEM settings and checks SHA-256. Restart Minecraft afterward. Previously opened/generated chest inventories are not rerolled. Test unopened chests or a fresh arena, radio choices/payment/quota/cooldown, and enemy drops. Actual two-client multiplayer acceptance remains manual and unverified.

Installed in the active instance on 2026-09-30. Built and installed JAR SHA-256: FA92EE0330DC09DE52C49F626DDC039F2FEB3F57AB91E6A9789B57246848D1B9. The old Exodus JAR and SEM config were backed up. Runtime reads confirm 80/70, .02 gun chance and guaranteed ammo before normalization.
