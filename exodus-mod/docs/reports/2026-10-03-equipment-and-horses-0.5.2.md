# Exodus 0.5.2 equipment and saddled horses

Generated from installed TacZ packs and Zero Contact build-72. No third-party files were changed.

## Changes

- Add all indexed TacZ attachments and all Zero Contact item registrations to every Exodus chest family.
- Keep chest/marker quantity, gun bundles, ammunition stacks, existing armor and component rules.
- Spawn 3-5 adult, tamed, saddled horses twice per world day during daytime, with a global match cap of 10.
- Placement uses loaded surface chunks near active players. Unloaded horses still consume the cap.
- Horses are match-bound and removed when the match ends; they do not consume enemy allocations.
- Mod and distribution version: 0.5.2; distribution tools derive output names from gradle.properties.

## Probability

One selection per expanded category. Equipment and attachments share a mutually exclusive pool to fit 27 chest slots; their marginal chances are preserved. Zero Contact has a separate pool.
Item chance = category chance * item weight / total category weight.
Percentages below are loot-table selection probabilities, before chest filling; opening previously generated loot does not reroll it.

| Chest tier | Attachment pool | Zero Contact pool |
|---|---:|---:|
| Common | 22% | 18% |
| Standard / specialized | 30% | 28% |
| Valuable | 40% | 40% |
| Elite | 50% | 55% |

## Scoring

Maximum actual display zoom controls scope weight: <=2x:20, <=4x:12, <=6x:7, <=8x:4, >8x:2.
Non-scope net score starts at 5. Lower recoil/spread and greater positive multipliers raise rarity; ADS/weight penalties lower it. Magazine capacity/level raises rarity. Silence, melee, explosive/incendiary perks and verified ammo-mod functions contribute utility.
Zero armor score: protection class + 0.8*defense + durability/32 + 40*movement modifier + 2*sum(1-hurt multiplier) + 2*(1-durability loss modifier).
Loadout score = container slots/3. Immunity adds 3 per immune effect to armor. Ammo score = penetration class + flesh damage/4 + 4*armor damage - 4*(recoil-1) - 4*(inaccuracy-1) + life/240 - 20*friction - 2*gravity + 0.35*(pellets-1) + 2*explosion radius + 2 for effects.
Non-scope/Zero weights: score <6:20; <10:10; <15:4; >=15:1. These are balance heuristics, not an official item power rating.
Fixed functional items use explicit utility scores; cosmetics use 0. Stats unavailable from their data are not invented.

## Full item tables

### attachments: 115 items; total weight 1240

| Item ID | Type | Max zoom | Stats (benefits and penalties) | Score | Weight | Common chest | Elite chest |
|---|---|---:|---|---:|---:|---:|---:|
| `daffas_arsenal:knife_skill` | muzzle | 0 | `{"melee":{"distance":2,"range_angle":40,"cooldown":0.5,"damage":3,"knockback":0,"prep":0.1}}` | 6.0 | 10 | 0.1774% | 0.4032% |
| `daffas_arsenal:laser_regular` | laser | 0 | `{"weight":0.133,"ads":{"addend":-0.03},"aim_inaccuracy":{"multiplier":0.88},"sneak_inaccuracy":{"multiplier":0.5}}` | 9.954 | 10 | 0.1774% | 0.4032% |
| `daffas_arsenal:makten_stock` | stock | 0 | `{"ads":{"addend":0.075},"recoil":{"pitch":{"multiplier":0.8},"yaw":{"multiplier":0.8}}}` | 6.45 | 10 | 0.1774% | 0.4032% |
| `daffas_arsenal:mgup1` | muzzle | 0 | `{"inaccuracy":{"multiplier":1.25},"recoil":{"pitch":{"multiplier":1.5},"yaw":{"multiplier":1.5}},"rpm":{"multiplier":1.1}}` | -0.2 | 20 | 0.3548% | 0.8065% |
| `daffas_arsenal:mgup2` | muzzle | 0 | `{"inaccuracy":{"multiplier":0.75},"recoil":{"pitch":{"multiplier":0.75},"yaw":{"multiplier":0.75}},"rpm":{"multiplier":0.75}}` | 7.0 | 10 | 0.1774% | 0.4032% |
| `daffas_arsenal:muzzle_silencer_bandage` | muzzle | 0 | `{"weight":0.35,"ads":{"addend":0.035},"inaccuracy":{"multiplier":1.15},"recoil":{"pitch":{"multiplier":0.75},"yaw":{"multiplier":0.8}},"effective_range":{"multiplier":1.2},"silence":{"distance_addend":-18,"use_silence_sound":true}}` | 10.455 | 4 | 0.0710% | 0.1613% |
| `daffas_arsenal:muzzle_silencer_heknah` | muzzle | 0 | `{"weight":0.4,"ads":{"addend":0.05},"inaccuracy":{"multiplier":1.2},"effective_range":{"multiplier":1.75},"rpm":{"multiplier":0.9},"silence":{"distance_addend":-23,"use_silence_sound":true}}` | 12.217 | 4 | 0.0710% | 0.1613% |
| `daffas_arsenal:reflect_digi_daw` | scope | 1.5 | `{"weight":0.15,"ads_addend":0.0}` | 4.925 | 20 | 0.3548% | 0.8065% |
| `daffas_arsenal:reflect_redot` | scope | 1.5 | `{"weight":0.15,"ads_addend":0.0}` | 4.925 | 20 | 0.3548% | 0.8065% |
| `daffas_arsenal:scope_auxur` | scope | 2.0 | `{"weight":0.4,"ads_addend":0.0}` | 4.8 | 20 | 0.3548% | 0.8065% |
| `daffas_arsenal:scope_g36` | scope | 2.0 | `{"weight":0.4,"ads_addend":0.1}` | 3.8 | 20 | 0.3548% | 0.8065% |
| `daffas_arsenal:scope_g362` | scope | 2 | `{"weight":0.85,"ads":{"addend":0.015},"aim_inaccuracy":{"multiplier":0.85}}` | 5.745 | 20 | 0.3548% | 0.8065% |
| `daffas_arsenal:scope_m2ampon` | scope | 2 | `{"weight":0.22,"ads_addend":0.0}` | 4.89 | 20 | 0.3548% | 0.8065% |
| `daffas_arsenal:scope_m2ampon2` | scope | 2 | `{"weight":0.22,"ads_addend":0.0}` | 4.89 | 20 | 0.3548% | 0.8065% |
| `daffas_arsenal:scope_ssg69` | scope | 4.0 | `{"weight":0.4,"ads_addend":0.1}` | 3.8 | 12 | 0.2129% | 0.4839% |
| `daffas_arsenal:scope_wawan2000` | scope | 8.0 | `{"weight":0.5,"ads_addend":0.15}` | 3.25 | 4 | 0.0710% | 0.1613% |
| `tacz:ammo_mod_fmj` | extended_mag | 0 | `{"weight":0.6,"ads":{"addend":0.02},"pierce":{"function":"if (x > 2) then y = x + 2 else y = x end"},"armor_ignore":{"function":"if (x > 0.5) then y = x*1.5 else y = x*1.75 end"},"damage":{"multiplier":0.9},"ammo_speed":{"multiplier":1.1},"extended_mag_level":3}` | 17.66 | 1 | 0.0177% | 0.0403% |
| `tacz:ammo_mod_he` | extended_mag | 0 | `{"weight":0.6,"explosion":{"explode":true},"armor_ignore":{"multiplier":0.5},"head_shot":{"function":"y = 1"},"pierce":{"function":"y = 1"},"rpm":{"multiplier":0.85},"extended_mag_level":1,"inaccuracy":{"multiplier":1},"aim_inaccuracy":{"multiplier":0.25}}` | 10.5 | 4 | 0.0710% | 0.1613% |
| `tacz:ammo_mod_hp` | extended_mag | 0 | `{"weight":0.6,"ads":{"addend":0.02},"armor_ignore":{"multiplier":0.5},"damage":{"multiplier":1.3},"ammo_speed":{"multiplier":0.9},"pierce":{"multiplier":0},"extended_mag_level":3}` | 3.26 | 20 | 0.3548% | 0.8065% |
| `tacz:ammo_mod_i` | extended_mag | 0 | `{"weight":0.6,"ads":{"addend":0.02},"ignite":{"entity":true},"armor_ignore":{"multiplier":0.8},"damage":{"multiplier":1.1},"ammo_speed":{"multiplier":1.0},"extended_mag_level":3}` | 16.86 | 1 | 0.0177% | 0.0403% |
| `tacz:ammo_mod_slug` | extended_mag | 0 | `{"weight":0.6,"inaccuracy":{"multiplier":1},"aim_inaccuracy":{"multiplier":0.04},"armor_ignore":{"function":"if (x > 0.5) then y = x*1.5 else y = x*1.75 end"},"damage":{"multiplier":0.9},"ammo_speed":{"multiplier":1.1},"extended_mag_level":3}` | 24.38 | 1 | 0.0177% | 0.0403% |
| `tacz:bayonet_6h3` | muzzle | 0 | `{"weight":0.34,"ads":{"addend":0.03},"melee":{"distance":2,"range_angle":45,"damage":5,"knockback":0.4,"prep":0.1}}` | 7.77 | 10 | 0.1774% | 0.4032% |
| `tacz:bayonet_m9` | muzzle | 0 | `{"weight":0.34,"ads":{"addend":0.03},"melee":{"distance":2,"range_angle":45,"cooldown":0,"damage":6,"knockback":0.4,"prep":0.1}}` | 8.27 | 10 | 0.1774% | 0.4032% |
| `tacz:deagle_golden_long_barrel` | muzzle | 0 | `{"weight":0.4,"ads":{"addend":0.04},"inaccuracy":{"multiplier":0.9},"aim_inaccuracy":{"multiplier":0.85},"recoil":{"pitch":{"multiplier":0.8},"yaw":{"multiplier":0.8}},"effective_range":{"addend":5},"silence":{"distance_addend":-12,"use_silence_sound":true}}` | 21.32 | 1 | 0.0177% | 0.0403% |
| `tacz:extended_mag_1` | extended_mag | 0 | `{"weight":0.4,"ads":{"addend":0.01},"extended_mag_level":1}` | 7.78 | 10 | 0.1774% | 0.4032% |
| `tacz:extended_mag_2` | extended_mag | 0 | `{"weight":0.6,"ads":{"addend":0.025},"extended_mag_level":2}` | 10.65 | 4 | 0.0710% | 0.1613% |
| `tacz:extended_mag_3` | extended_mag | 0 | `{"weight":0.8,"ads":{"addend":0.045},"extended_mag_level":3}` | 13.51 | 4 | 0.0710% | 0.1613% |
| `tacz:grip_cobra` | grip | 0 | `{"weight":0.08,"ads":{"multiplier":0.85}}` | 6.16 | 10 | 0.1774% | 0.4032% |
| `tacz:grip_cqr` | grip | 0 | `{"weight":0.167,"ads":{"multiplier":0.85},"recoil":{"pitch":{"multiplier":0.92},"yaw":{"multiplier":0.92}}}` | 6.756 | 10 | 0.1774% | 0.4032% |
| `tacz:grip_magpul_afg_2` | grip | 0 | `{"weight":0.2,"ads":{"multiplier":0.95},"inaccuracy":{"multiplier":1.1},"recoil":{"pitch":{"multiplier":0.85}}}` | 5.1 | 20 | 0.3548% | 0.8065% |
| `tacz:grip_osovets_black` | grip | 0 | `{"weight":0.125,"recoil":{"yaw":{"multiplier":0.75}}}` | 5.938 | 20 | 0.3548% | 0.8065% |
| `tacz:grip_rk0` | grip | 0 | `{"weight":0.138,"recoil":{"pitch":{"multiplier":0.8}}}` | 5.731 | 20 | 0.3548% | 0.8065% |
| `tacz:grip_rk1_b25u` | grip | 0 | `{"weight":0.18,"ads":{"addend":0.18},"recoil":{"yaw":{"multiplier":0.66}},"inaccuracy":{"multiplier":0.48},"aim_inaccuracy":{"multiplier":1.25}}` | 8.07 | 10 | 0.1774% | 0.4032% |
| `tacz:grip_rk6` | grip | 0 | `{"weight":0.1,"ads":{"multiplier":0.85},"inaccuracy":{"multiplier":0.88}}` | 7.11 | 10 | 0.1774% | 0.4032% |
| `tacz:grip_se_5` | grip | 0 | `{"weight":0.09,"ads":{"multiplier":0.85},"recoil":{"pitch":{"multiplier":0.93},"yaw":{"multiplier":0.92}}}` | 6.755 | 10 | 0.1774% | 0.4032% |
| `tacz:grip_td` | grip | 0 | `{"weight":0.133,"aim_inaccuracy":{"multiplier":0.88},"inaccuracy":{"multiplier":0.88}}` | 6.854 | 10 | 0.1774% | 0.4032% |
| `tacz:grip_vertical_military` | grip | 0 | `{"weight":0.25,"ads":{"multiplier":1.02},"aim_inaccuracy":{"multiplier":0.85},"recoil":{"pitch":{"multiplier":0.8},"yaw":{"multiplier":0.8}}}` | 7.515 | 10 | 0.1774% | 0.4032% |
| `tacz:grip_vertical_ranger` | grip | 0 | `{"weight":0.8,"ads":{"multiplier":1.05},"inaccuracy":{"multiplier":0.7},"sneak_inaccuracy":{"multiplier":0.75},"recoil":{"pitch":{"multiplier":0.8},"yaw":{"multiplier":0.7}}}` | 10.6 | 4 | 0.0710% | 0.1613% |
| `tacz:grip_vertical_talon` | grip | 0 | `{"weight":0.2,"ads":{"multiplier":1.01},"aim_inaccuracy":{"multiplier":0.8},"recoil":{"pitch":{"multiplier":0.85}}}` | 7.02 | 10 | 0.1774% | 0.4032% |
| `tacz:laser_compact` | laser | 0 | `{"weight":0.13,"aim_inaccuracy":{"multiplier":0.6},"inaccuracy":{"multiplier":0.7},"sneak_inaccuracy":{"multiplier":0.7}}` | 12.935 | 4 | 0.0710% | 0.1613% |
| `tacz:laser_lopro` | laser | 0 | `{"weight":1,"ads":{"addend":0.12},"aim_inaccuracy":{"multiplier":0.25},"inaccuracy":{"multiplier":0.75},"sneak_inaccuracy":{"multiplier":0.5}}` | 16.26 | 1 | 0.0177% | 0.0403% |
| `tacz:laser_nightstick` | laser | 0 | `{"weight":0.2,"ads":{"addend":0.06},"sneak_inaccuracy":{"multiplier":0.75},"inaccuracy":{"multiplier":0.6}}` | 9.98 | 10 | 0.1774% | 0.4032% |
| `tacz:laser_peq15` | laser | 0 | `{"weight":0.133,"ads":{"addend":-0.03},"aim_inaccuracy":{"multiplier":0.88},"sneak_inaccuracy":{"multiplier":0.5}}` | 9.954 | 10 | 0.1774% | 0.4032% |
| `tacz:laser_peq6` | laser | 0 | `{"weight":0.25,"ads":{"addend":0.07},"head_shot":{"addend":0.25},"sneak_inaccuracy":{"multiplier":0.35},"inaccuracy":{"multiplier":0.5},"lie_inaccuracy":{"multiplier":0.75},"aim_inaccuracy":{"multiplier":0.5}}` | 20.435 | 1 | 0.0177% | 0.0403% |
| `tacz:light_extended_mag_1` | extended_mag | 0 | `{"weight":0.2,"ads":{"addend":0.01},"extended_mag_level":1}` | 7.88 | 10 | 0.1774% | 0.4032% |
| `tacz:light_extended_mag_2` | extended_mag | 0 | `{"weight":0.3,"ads":{"addend":0.02},"extended_mag_level":2}` | 10.81 | 4 | 0.0710% | 0.1613% |
| `tacz:light_extended_mag_3` | extended_mag | 0 | `{"weight":0.4,"ads":{"addend":0.03},"extended_mag_level":3}` | 13.74 | 4 | 0.0710% | 0.1613% |
| `tacz:muzzle_brake_cthulhu` | muzzle | 0 | `{"ads":{"addend":0.02},"inaccuracy":{"multiplier":1.1},"aim_inaccuracy":{"multiplier":0.9},"recoil":{"pitch":{"multiplier":0.85},"yaw":{"multiplier":0.8}}}` | 6.36 | 10 | 0.1774% | 0.4032% |
| `tacz:muzzle_brake_cyclone_d2` | muzzle | 0 | `{"ads":{"addend":0.02},"inaccuracy":{"multiplier":1.1},"recoil":{"pitch":{"multiplier":0.8},"yaw":{"multiplier":0.7}}}` | 6.16 | 10 | 0.1774% | 0.4032% |
| `tacz:muzzle_brake_mastiff_sg` | muzzle | 0 | `{"weight":0.25,"ads":{"addend":0.08},"inaccuracy":{"multiplier":1.25},"aim_inaccuracy":{"multiplier":1.1},"recoil":{"pitch":{"multiplier":0.65},"yaw":{"multiplier":0.7}}}` | 4.515 | 20 | 0.3548% | 0.8065% |
| `tacz:muzzle_brake_pioneer` | muzzle | 0 | `{"ads":{"addend":0.02},"inaccuracy":{"multiplier":1.15},"recoil":{"pitch":{"multiplier":0.33},"yaw":{"multiplier":1.33}}}` | 5.12 | 20 | 0.3548% | 0.8065% |
| `tacz:muzzle_brake_timeless50` | muzzle | 0 | `{"ads":{"addend":0.03},"recoil":{"pitch":{"multiplier":0.35},"yaw":{"multiplier":0.75}},"inaccuracy":{"multiplier":0.6},"sneak_inaccuracy":{"multiplier":0.5}}` | 15.74 | 1 | 0.0177% | 0.0403% |
| `tacz:muzzle_brake_trex` | muzzle | 0 | `{"weight":0.5,"ads":{"addend":0.03},"recoil":{"pitch":{"multiplier":0.66},"yaw":{"multiplier":0.95}}}` | 6.25 | 10 | 0.1774% | 0.4032% |
| `tacz:muzzle_choke_sg` | muzzle | 0 | `{"ads":{"addend":0.05},"inaccuracy":{"multiplier":0.65},"aim_inaccuracy":{"multiplier":0.65},"sneak_inaccuracy":{"multiplier":0.65},"lie_inaccuracy":{"multiplier":0.65},"recoil":{"pitch":{"multiplier":1.45},"yaw":{"multiplier":1.45}}}` | 12.5 | 4 | 0.0710% | 0.1613% |
| `tacz:muzzle_compensator_trident` | muzzle | 0 | `{"ads":{"addend":0.01},"inaccuracy":{"multiplier":0.85},"recoil":{"yaw":{"multiplier":0.6}}}` | 7.78 | 10 | 0.1774% | 0.4032% |
| `tacz:muzzle_silencer_knight_qd` | muzzle | 0 | `{"weight":0.35,"ads":{"addend":0.02},"aim_inaccuracy":{"multiplier":0.75},"recoil":{"pitch":{"multiplier":0.8}},"silence":{"distance_addend":-20,"use_silence_sound":true}}` | 11.252 | 4 | 0.0710% | 0.1613% |
| `tacz:muzzle_silencer_mirage` | muzzle | 0 | `{"weight":0.15,"head_shot":{"multiplier":1.25},"inaccuracy":{"multiplier":1.1},"effective_range":{"multiplier":0.8},"silence":{"distance_addend":-24,"use_silence_sound":true}}` | 8.525 | 10 | 0.1774% | 0.4032% |
| `tacz:muzzle_silencer_phantom_s1` | muzzle | 0 | `{"weight":0.25,"ads":{"addend":0.02},"inaccuracy":{"multiplier":1.1},"effective_range":{"multiplier":1.5},"rpm":{"multiplier":0.95},"silence":{"distance_addend":-20,"use_silence_sound":true}}` | 11.302 | 4 | 0.0710% | 0.1613% |
| `tacz:muzzle_silencer_ptilopsis` | muzzle | 0 | `{"weight":0.4,"ads":{"addend":0.06},"inaccuracy":{"multiplier":0.8},"effective_range":{"multiplier":1.25},"silence":{"distance_addend":-24,"use_silence_sound":true}}` | 12.28 | 4 | 0.0710% | 0.1613% |
| `tacz:muzzle_silencer_sg` | muzzle | 0 | `{"weight":0.4,"ads":{"addend":0.1},"inaccuracy":{"multiplier":0.95},"aim_inaccuracy":{"multiplier":0.9},"effective_range":{"multiplier":1.5},"rpm":{"multiplier":0.95},"recoil":{"pitch":{"multiplier":0.9}},"silence":{"distance_addend":-20,"use_silence_sound":true}}` | 13.467 | 4 | 0.0710% | 0.1613% |
| `tacz:muzzle_silencer_ursus` | muzzle | 0 | `{"weight":0.35,"ads":{"addend":0.035},"inaccuracy":{"multiplier":1.15},"recoil":{"pitch":{"multiplier":0.75},"yaw":{"multiplier":0.8}},"effective_range":{"multiplier":1.2},"silence":{"distance_addend":-18,"use_silence_sound":true}}` | 10.455 | 4 | 0.0710% | 0.1613% |
| `tacz:muzzle_silencer_vulture` | muzzle | 0 | `{"weight":1.55,"ads":{"addend":0.09},"inaccuracy":{"multiplier":1.1},"effective_range":{"multiplier":1.25},"head_shot":{"addend":0.25},"recoil":{"pitch":{"multiplier":0.66},"yaw":{"multiplier":0.75}},"silence":{"distance_addend":-25,"use_silence_sound":true}}` | 12.188 | 4 | 0.0710% | 0.1613% |
| `tacz:muzzle_silencer_wraith` | muzzle | 0 | `{"weight":0.35,"ads":{"addend":0.04},"head_shot":{"addend":0.25},"effective_range":{"multiplier":1.15},"aim_inaccuracy":{"multiplier":0.75},"ammo_speed":{"multiplier":1.2},"silence":{"distance_addend":-20,"use_silence_sound":true}}` | 13.712 | 4 | 0.0710% | 0.1613% |
| `tacz:oem_stock_heavy` | stock | 0 | `{"weight":0.5,"ads":{"addend":0.05},"aim_inaccuracy":{"multiplier":0.9},"recoil":{"pitch":{"multiplier":0.75},"yaw":{"multiplier":0.6}},"melee":{"distance":2,"range_angle":40,"cooldown":0.4,"damage":5,"knockback":0.8,"prep":0.1}}` | 10.25 | 4 | 0.0710% | 0.1613% |
| `tacz:oem_stock_light` | stock | 0 | `{"weight":0.3,"ads":{"addend":-0.02},"aim_inaccuracy":{"multiplier":1.1},"recoil":{"pitch":{"multiplier":0.85},"yaw":{"multiplier":0.8}},"melee":{"distance":2,"range_angle":40,"cooldown":0.1,"damage":3,"knockback":0.4,"prep":0.1}}` | 7.29 | 10 | 0.1774% | 0.4032% |
| `tacz:oem_stock_tactical` | stock | 0 | `{"weight":0.4,"ads":{"addend":0.035},"inaccuracy":{"multiplier":0.9},"recoil":{"pitch":{"multiplier":0.8},"yaw":{"multiplier":0.7}},"melee":{"distance":2,"range_angle":40,"cooldown":0.2,"damage":4,"knockback":0.6,"prep":0.1}}` | 9.63 | 10 | 0.1774% | 0.4032% |
| `tacz:scope_1873_6x` | scope | 6 | `{"weight":1,"ads_addend":0.15,"aim_inaccuracy":{"multiplier":0.66}}` | 5.72 | 7 | 0.1242% | 0.2823% |
| `tacz:scope_98k` | scope | 4.25 | `{"weight":1.3,"ads_addend":0.03}` | 4.05 | 7 | 0.1242% | 0.2823% |
| `tacz:scope_acog_ta31` | scope | 2.5 | `{"weight":1.2,"ads_addend":0.015}` | 4.25 | 12 | 0.2129% | 0.4839% |
| `tacz:scope_aug_default` | scope | 4.25 | `{"weight":0.1,"ads_addend":0.1}` | 3.95 | 7 | 0.1242% | 0.2823% |
| `tacz:scope_contender` | scope | 4.25 | `{"weight":1.6,"ads_addend":0.05,"aim_inaccuracy":{"multiplier":0.85}}` | 4.9 | 7 | 0.1242% | 0.2823% |
| `tacz:scope_elcan_4x` | scope | 4.25 | `{"weight":1.6,"ads_addend":0.05}` | 3.7 | 7 | 0.1242% | 0.2823% |
| `tacz:scope_hamr` | scope | 3.25 | `{"weight":0.85,"ads":{"addend":0.015},"aim_inaccuracy":{"multiplier":0.85}}` | 5.745 | 12 | 0.2129% | 0.4839% |
| `tacz:scope_lpvo_1_6` | scope | 6.25 | `{"weight":1.3,"ads_addend":0.03}` | 4.05 | 4 | 0.0710% | 0.1613% |
| `tacz:scope_mk5hd` | scope | 25 | `{"weight":0.85,"ads":{"addend":0.13},"aim_inaccuracy":{"multiplier":0.66}}` | 7.035 | 2 | 0.0355% | 0.0806% |
| `tacz:scope_qmk152` | scope | 3 | `{"weight":1,"ads_addend":0.02}` | 4.3 | 12 | 0.2129% | 0.4839% |
| `tacz:scope_retro_2x` | scope | 3.25 | `{"weight":1.6,"ads_addend":0.02}` | 4.0 | 12 | 0.2129% | 0.4839% |
| `tacz:scope_standard_8x` | scope | 10 | `{"weight":2.0,"ads_addend":0.09,"aim_inaccuracy":{"multiplier":0.75}}` | 5.1 | 2 | 0.0355% | 0.0806% |
| `tacz:scope_vudu` | scope | 6.5 | `{"weight":0.85,"ads":{"addend":0.015},"aim_inaccuracy":{"multiplier":0.9}}` | 5.345 | 4 | 0.0710% | 0.1613% |
| `tacz:shotgun_extended_mag_1` | extended_mag | 0 | `{"weight":0.4,"ads":{"addend":0.01},"extended_mag_level":1}` | 7.78 | 10 | 0.1774% | 0.4032% |
| `tacz:shotgun_extended_mag_2` | extended_mag | 0 | `{"weight":0.4,"ads":{"addend":0.01},"extended_mag_level":2}` | 10.78 | 4 | 0.0710% | 0.1613% |
| `tacz:shotgun_extended_mag_3` | extended_mag | 0 | `{"weight":0.4,"ads":{"addend":0.01},"extended_mag_level":3}` | 13.78 | 4 | 0.0710% | 0.1613% |
| `tacz:sight_552` | scope | 2 | `{"weight":0.4,"ads":{"addend":-0.015}}` | 4.83 | 20 | 0.3548% | 0.8065% |
| `tacz:sight_acro_pistol` | scope | 2 | `{"weight":0.4,"aim_inaccuracy":{"multiplier":0.95},"ads":{"addend":-0.02}}` | 5.24 | 20 | 0.3548% | 0.8065% |
| `tacz:sight_acro_rifle` | scope | 2 | `{"weight":0.5,"aim_inaccuracy":{"multiplier":0.9},"ads":{"addend":-0.02}}` | 5.59 | 20 | 0.3548% | 0.8065% |
| `tacz:sight_coyote` | scope | 1.5 | `{"weight":0.25,"ads":{"addend":-0.03}}` | 4.935 | 20 | 0.3548% | 0.8065% |
| `tacz:sight_deltapoint_pistol` | scope | 1.5 | `{"weight":0.4,"aim_inaccuracy":{"multiplier":0.95},"ads":{"addend":-0.02}}` | 5.24 | 20 | 0.3548% | 0.8065% |
| `tacz:sight_deltapoint_rifle` | scope | 1.5 | `{"weight":0.5,"aim_inaccuracy":{"multiplier":0.95},"ads":{"addend":-0.02}}` | 5.19 | 20 | 0.3548% | 0.8065% |
| `tacz:sight_exp3` | scope | 2 | `{"weight":0.35,"ads":{"addend":-0.02}}` | 4.865 | 20 | 0.3548% | 0.8065% |
| `tacz:sight_fastfire_pistol` | scope | 1.5 | `{"weight":0.4,"aim_inaccuracy":{"multiplier":0.95},"ads":{"addend":-0.02}}` | 5.24 | 20 | 0.3548% | 0.8065% |
| `tacz:sight_fastfire_rifle` | scope | 1.5 | `{"weight":0.5,"aim_inaccuracy":{"multiplier":0.95},"ads":{"addend":-0.02}}` | 5.19 | 20 | 0.3548% | 0.8065% |
| `tacz:sight_okp7` | scope | 1.5 | `{"weight":0.4,"ads":{"addend":-0.01}}` | 4.82 | 20 | 0.3548% | 0.8065% |
| `tacz:sight_p90` | scope | 1.35 | `{"weight":0.35,"ads":{"addend":-0.02}}` | 4.865 | 20 | 0.3548% | 0.8065% |
| `tacz:sight_pk06_pistol` | scope | 2 | `{"weight":0.4,"aim_inaccuracy":{"multiplier":0.95},"ads":{"addend":-0.02}}` | 5.24 | 20 | 0.3548% | 0.8065% |
| `tacz:sight_pk06_rifle` | scope | 2 | `{"weight":0.5,"aim_inaccuracy":{"multiplier":0.95},"ads":{"addend":-0.02}}` | 5.19 | 20 | 0.3548% | 0.8065% |
| `tacz:sight_rmr_dot` | scope | 1.5 | `{"weight":0.1,"ads":{"addend":-0.05}}` | 5.05 | 20 | 0.3548% | 0.8065% |
| `tacz:sight_sro_dot` | scope | 1.5 | `{"weight":0.1,"ads":{"addend":-0.04}}` | 5.03 | 20 | 0.3548% | 0.8065% |
| `tacz:sight_srs_02` | scope | 1.5 | `{"weight":0.8,"ads":{"addend":-0.025}}` | 4.65 | 20 | 0.3548% | 0.8065% |
| `tacz:sight_t1` | scope | 2.5 | `{"weight":0.2,"ads":{"addend":-0.02}}` | 4.94 | 12 | 0.2129% | 0.4839% |
| `tacz:sight_t2` | scope | 2.5 | `{"weight":0.25,"ads":{"addend":-0.015}}` | 4.905 | 12 | 0.2129% | 0.4839% |
| `tacz:sight_uh1` | scope | 2.5 | `{"weight":0.3,"ads":{"addend":-0.01}}` | 4.87 | 12 | 0.2129% | 0.4839% |
| `tacz:sniper_extended_mag_1` | extended_mag | 0 | `{"weight":0.5,"ads":{"addend":0.03},"extended_mag_level":1}` | 7.69 | 10 | 0.1774% | 0.4032% |
| `tacz:sniper_extended_mag_2` | extended_mag | 0 | `{"weight":0.8,"ads":{"addend":0.05},"extended_mag_level":2}` | 10.5 | 4 | 0.0710% | 0.1613% |
| `tacz:sniper_extended_mag_3` | extended_mag | 0 | `{"weight":1.2,"ads":{"addend":0.08},"extended_mag_level":3}` | 13.24 | 4 | 0.0710% | 0.1613% |
| `tacz:stock_ak12` | stock | 0 | `{"weight":0.148,"ads":{"addend":0.01},"aim_inaccuracy":{"multiplier":0.9},"recoil":{"pitch":{"multiplier":0.8},"yaw":{"multiplier":0.8}}}` | 7.306 | 10 | 0.1774% | 0.4032% |
| `tacz:stock_carbon_bone_c5` | stock | 0 | `{"weight":0.3,"ads":{"addend":-0.02},"aim_inaccuracy":{"multiplier":1.1},"recoil":{"pitch":{"multiplier":0.85},"yaw":{"multiplier":0.8}},"melee":{"distance":2,"range_angle":40,"cooldown":0.1,"damage":3,"knockback":0.4,"prep":0.1}}` | 7.29 | 10 | 0.1774% | 0.4032% |
| `tacz:stock_heavy_spas_12` | stock | 0 | `{"weight":0.5,"ads":{"addend":0.02},"inaccuracy":{"multiplier":0.9},"aim_inaccuracy":{"multiplier":0.7},"recoil":{"pitch":{"multiplier":0.72},"yaw":{"multiplier":0.72}}}` | 10.15 | 4 | 0.0710% | 0.1613% |
| `tacz:stock_hk_slim_line` | stock | 0 | `{"weight":0.695,"ads":{"addend":0.01},"recoil":{"pitch":{"multiplier":0.75},"yaw":{"multiplier":0.72}}}` | 6.753 | 10 | 0.1774% | 0.4032% |
| `tacz:stock_m4ss` | stock | 0 | `{"weight":0.695,"ads":{"addend":0.012},"recoil":{"pitch":{"multiplier":0.85},"yaw":{"multiplier":0.9}}}` | 5.628 | 20 | 0.3548% | 0.8065% |
| `tacz:stock_militech_b5` | stock | 0 | `{"weight":0.5,"ads":{"multiplier":1.1},"aim_inaccuracy":{"multiplier":0.87},"recoil":{"pitch":{"multiplier":0.68},"yaw":{"multiplier":0.6}},"melee":{"distance":2,"range_angle":40,"cooldown":0.4,"damage":5,"knockback":0.8,"prep":0.1}}` | 10.07 | 4 | 0.0710% | 0.1613% |
| `tacz:stock_moe` | stock | 0 | `{"weight":0.15,"ads":{"addend":-0.03},"recoil":{"pitch":{"multiplier":0.85}}}` | 5.585 | 20 | 0.3548% | 0.8065% |
| `tacz:stock_ripstock` | stock | 0 | `{"weight":0.12,"ads":{"multiplier":0.82},"aim_inaccuracy":{"multiplier":1.05}}` | 5.98 | 20 | 0.3548% | 0.8065% |
| `tacz:stock_sba3` | stock | 0 | `{"weight":0.1,"recoil":{"yaw":{"multiplier":0.8}},"inaccuracy":{"multiplier":0.78}}` | 7.51 | 10 | 0.1774% | 0.4032% |
| `tacz:stock_tactical_ar` | stock | 0 | `{"weight":0.4,"recoil":{"pitch":{"multiplier":0.75},"yaw":{"multiplier":0.75}},"melee":{"distance":2,"range_angle":40,"cooldown":0.2,"damage":4,"knockback":0.6,"prep":0.1}}` | 8.9 | 10 | 0.1774% | 0.4032% |
| `tacz:stock_tactical_spas_12` | stock | 0 | `{"weight":0.3,"recoil":{"pitch":{"multiplier":0.8},"yaw":{"multiplier":0.8}},"inaccuracy":{"multiplier":0.7}}` | 8.85 | 10 | 0.1774% | 0.4032% |

### zero_contact: 119 items; total weight 1398

| Item ID | Type | Max zoom | Stats (benefits and penalties) | Score | Weight | Common chest | Elite chest |
|---|---|---:|---|---:|---:|---:|---:|
| `zerocontact:12g_ap` | ammo | - | `{"life":42,"friction":0.035,"gravity":0.11,"recoil_multiplier":1.12,"inaccuracy_multiplier":1.1,"bullet_amount":1,"penetration_class":7,"armor_damage":0.13,"flesh_damage":16,"stack_size":20}` | 9.895 | 10 | 0.1288% | 0.3934% |
| `zerocontact:12g_db` | ammo | - | `{"life":24,"friction":0.1,"gravity":0.21,"recoil_multiplier":0.96,"inaccuracy_multiplier":0.95,"bullet_amount":8,"penetration_class":2,"armor_damage":0.13,"flesh_damage":0.42,"stack_size":20,"effects":[{"trigger":"HIT_BLOCK","actions":[{"target":"NEARBY","effect":"zerocontact:ignition","duration":5,"amplifier":0,"chance":0.34,"radius":1.0}]},{"trigger":"HIT_ENTITY","actions":[],"scripts":[{"script":"zerocontact:50cal_api_t_logic","function":"on_hit_ignite"}]},{"trigger":"BULLET_TICKING","actions":[],"scripts":[{"script":"zerocontact:50cal_api_t_logic","function":"on_fly_tracer"}]}]}` | 5.115 | 20 | 0.2575% | 0.7868% |
| `zerocontact:12g_flechette` | ammo | - | `{"life":30,"friction":0.065,"gravity":0.16,"recoil_multiplier":1.05,"inaccuracy_multiplier":1.04,"bullet_amount":8,"penetration_class":5,"armor_damage":0.03,"flesh_damage":0.42,"stack_size":20}` | 5.82 | 20 | 0.2575% | 0.7868% |
| `zerocontact:308_m62` | ammo | - | `{"life":76,"friction":0.014,"gravity":0.062,"recoil_multiplier":1,"inaccuracy_multiplier":1.01,"penetration_class":5,"armor_damage":0.1,"flesh_damage":8,"stack_size":60,"tracer_color":[0,200,0]}` | 7.273 | 10 | 0.1288% | 0.3934% |
| `zerocontact:308_m80` | ammo | - | `{"life":78,"friction":0.013,"gravity":0.058,"recoil_multiplier":1.02,"inaccuracy_multiplier":1.02,"penetration_class":6,"armor_damage":0.12,"flesh_damage":7,"stack_size":60}` | 8.019 | 10 | 0.1288% | 0.3934% |
| `zerocontact:308_m80a1` | ammo | - | `{"life":80,"friction":0.012,"gravity":0.055,"recoil_multiplier":1.06,"inaccuracy_multiplier":1.04,"penetration_class":8,"armor_damage":0.15,"flesh_damage":7,"stack_size":60}` | 9.933 | 10 | 0.1288% | 0.3934% |
| `zerocontact:308_m993` | ammo | - | `{"life":82,"friction":0.011,"gravity":0.052,"recoil_multiplier":1.12,"inaccuracy_multiplier":1.09,"penetration_class":12,"armor_damage":0.3,"flesh_damage":9,"stack_size":60}` | 14.628 | 4 | 0.0515% | 0.1574% |
| `zerocontact:308_sp` | ammo | - | `{"life":76,"friction":0.0135,"gravity":0.06,"recoil_multiplier":1,"inaccuracy_multiplier":1,"penetration_class":5,"armor_damage":0.06,"flesh_damage":8,"stack_size":60}` | 7.167 | 10 | 0.1288% | 0.3934% |
| `zerocontact:308_un` | ammo | - | `{"life":74,"friction":0.0145,"gravity":0.065,"recoil_multiplier":0.96,"inaccuracy_multiplier":0.96,"penetration_class":2,"armor_damage":0.08,"flesh_damage":13,"stack_size":60}` | 5.778 | 20 | 0.2575% | 0.7868% |
| `zerocontact:338_ap` | ammo | - | `{"life":108,"friction":0.0085,"gravity":0.038,"recoil_multiplier":1.16,"inaccuracy_multiplier":1.1,"penetration_class":15,"armor_damage":0.6,"flesh_damage":18,"stack_size":8}` | 21.064 | 1 | 0.0129% | 0.0393% |
| `zerocontact:338_fmj` | ammo | - | `{"life":102,"friction":0.0095,"gravity":0.043,"recoil_multiplier":1,"inaccuracy_multiplier":1,"penetration_class":6,"armor_damage":0.4,"flesh_damage":14,"stack_size":8}` | 11.249 | 4 | 0.0515% | 0.1574% |
| `zerocontact:338_tacx` | ammo | - | `{"life":98,"friction":0.0105,"gravity":0.05,"recoil_multiplier":0.96,"inaccuracy_multiplier":0.96,"penetration_class":3,"armor_damage":0.15,"flesh_damage":15,"stack_size":15}` | 7.768 | 10 | 0.1288% | 0.3934% |
| `zerocontact:338_ucw` | ammo | - | `{"life":98,"friction":0.011,"gravity":0.052,"recoil_multiplier":1,"inaccuracy_multiplier":1,"penetration_class":6,"armor_damage":0.2,"flesh_damage":12,"stack_size":15}` | 9.884 | 10 | 0.1288% | 0.3934% |
| `zerocontact:40mm_incendiary` | ammo | - | `{"life":100,"friction":0.05,"gravity":0.15,"recoil_multiplier":1,"inaccuracy_multiplier":1.02,"penetration_class":0,"armor_damage":0,"flesh_damage":2,"stack_size":2,"knockback":0,"explosion":{"radius":2,"delay_count":30},"effects":[{"trigger":"HIT_BLOCK","actions":[{"target":"NEARBY","effect":"zerocontact:ignition","duration":20,"amplifier":0,"chance":1.0,"radius":4.0}],"scripts":[{"script":"zerocontact:40mm_incendiary_logic","function":"on_hit_glowing","arguments":{"duration":50,"amplifier":0,"radius":4.0}}]}]}` | 5.537 | 20 | 0.2575% | 0.7868% |
| `zerocontact:40mm_smoke` | ammo | - | `{"life":100,"friction":0.05,"gravity":0.15,"recoil_multiplier":1,"inaccuracy_multiplier":0.98,"penetration_class":0,"armor_damage":0,"flesh_damage":2,"stack_size":2,"knockback":0,"explosion":{"radius":2,"delay_count":30},"effects":[{"trigger":"HIT_BLOCK_TICKING","actions":[{"target":"NEARBY","effect":"zerocontact:smoke","duration":400,"amplifier":0,"chance":1.0,"radius":10.0}]}]}` | 5.697 | 20 | 0.2575% | 0.7868% |
| `zerocontact:45acp_ap` | ammo | - | `{"life":40,"friction":0.034,"gravity":0.17,"recoil_multiplier":1.12,"inaccuracy_multiplier":1.12,"penetration_class":8,"armor_damage":0.1,"flesh_damage":6,"stack_size":60}` | 8.087 | 10 | 0.1288% | 0.3934% |
| `zerocontact:45acp_fmj` | ammo | - | `{"life":38,"friction":0.038,"gravity":0.185,"recoil_multiplier":1,"inaccuracy_multiplier":1,"penetration_class":4,"armor_damage":0.06,"flesh_damage":7,"stack_size":60,"tracer_color":[200,0,0]}` | 5.018 | 20 | 0.2575% | 0.7868% |
| `zerocontact:45acp_hs` | ammo | - | `{"life":36,"friction":0.041,"gravity":0.195,"recoil_multiplier":0.97,"inaccuracy_multiplier":0.97,"penetration_class":3,"armor_damage":0.04,"flesh_damage":9,"stack_size":60}` | 4.59 | 20 | 0.2575% | 0.7868% |
| `zerocontact:45acp_rip` | ammo | - | `{"life":34,"friction":0.045,"gravity":0.21,"recoil_multiplier":0.92,"inaccuracy_multiplier":0.94,"penetration_class":1,"armor_damage":0.06,"flesh_damage":12,"stack_size":60}` | 3.622 | 20 | 0.2575% | 0.7868% |
| `zerocontact:50bmg_api_t` | ammo | - | `{"life":110,"friction":0.009,"gravity":0.035,"recoil_multiplier":1.12,"inaccuracy_multiplier":1.08,"penetration_class":10,"armor_damage":0.44,"flesh_damage":32,"stack_size":8,"effects":[{"trigger":"HIT_ENTITY","actions":[],"scripts":[{"script":"zerocontact:50cal_api_t_logic","function":"on_hit_ignite"}]},{"trigger":"BULLET_TICKING","actions":[],"scripts":[{"script":"zerocontact:50cal_api_t_logic","function":"on_fly_tracer"}]}]}` | 21.168 | 1 | 0.0129% | 0.0393% |
| `zerocontact:545x39bp` | ammo | - | `{"life":66,"friction":0.0135,"gravity":0.058,"recoil_multiplier":1.08,"inaccuracy_multiplier":1.07,"penetration_class":9,"armor_damage":0.12,"flesh_damage":5,"stack_size":60}` | 10.019 | 4 | 0.0515% | 0.1574% |
| `zerocontact:545x39bs` | ammo | - | `{"life":68,"friction":0.0125,"gravity":0.055,"recoil_multiplier":1.1,"inaccuracy_multiplier":1.09,"penetration_class":10,"armor_damage":0.15,"flesh_damage":4,"stack_size":60}` | 10.763 | 4 | 0.0515% | 0.1574% |
| `zerocontact:545x39bt` | ammo | - | `{"life":64,"friction":0.015,"gravity":0.064,"recoil_multiplier":1.04,"inaccuracy_multiplier":1.04,"penetration_class":7,"armor_damage":0.12,"flesh_damage":4,"stack_size":60,"tracer_color":[255,0,0]}` | 7.999 | 10 | 0.1288% | 0.3934% |
| `zerocontact:545x39pp` | ammo | - | `{"life":64,"friction":0.0145,"gravity":0.063,"recoil_multiplier":1.02,"inaccuracy_multiplier":1.02,"penetration_class":6,"armor_damage":0.06,"flesh_damage":5,"stack_size":60}` | 7.181 | 10 | 0.1288% | 0.3934% |
| `zerocontact:545x39ps` | ammo | - | `{"life":62,"friction":0.0155,"gravity":0.068,"recoil_multiplier":0.98,"inaccuracy_multiplier":0.98,"penetration_class":4,"armor_damage":0.05,"flesh_damage":4,"stack_size":60}` | 5.172 | 20 | 0.2575% | 0.7868% |
| `zerocontact:545x39t` | ammo | - | `{"life":60,"friction":0.017,"gravity":0.075,"recoil_multiplier":0.94,"inaccuracy_multiplier":0.96,"penetration_class":2,"armor_damage":0.03,"flesh_damage":4,"stack_size":60,"tracer_color":[0,200,0]}` | 3.28 | 20 | 0.2575% | 0.7868% |
| `zerocontact:556x45ap` | ammo | - | `{"life":72,"friction":0.0105,"gravity":0.05,"recoil_multiplier":1.14,"inaccuracy_multiplier":1.1,"penetration_class":12,"armor_damage":0.1,"flesh_damage":4,"stack_size":60}` | 12.43 | 4 | 0.0515% | 0.1574% |
| `zerocontact:556x45hp` | ammo | - | `{"life":62,"friction":0.0165,"gravity":0.072,"recoil_multiplier":0.94,"inaccuracy_multiplier":0.96,"penetration_class":2,"armor_damage":0.05,"flesh_damage":9,"stack_size":60}` | 4.634 | 20 | 0.2575% | 0.7868% |
| `zerocontact:556x45m855` | ammo | - | `{"life":64,"friction":0.015,"gravity":0.065,"recoil_multiplier":0.98,"inaccuracy_multiplier":1,"penetration_class":4,"armor_damage":0.02,"flesh_damage":4,"stack_size":60}` | 4.997 | 20 | 0.2575% | 0.7868% |
| `zerocontact:556x45m855a1` | ammo | - | `{"life":68,"friction":0.0125,"gravity":0.055,"recoil_multiplier":1.04,"inaccuracy_multiplier":1.03,"penetration_class":7,"armor_damage":0.07,"flesh_damage":6,"stack_size":60}` | 8.423 | 10 | 0.1288% | 0.3934% |
| `zerocontact:556x45m856` | ammo | - | `{"life":60,"friction":0.0175,"gravity":0.075,"recoil_multiplier":0.94,"inaccuracy_multiplier":0.97,"penetration_class":2,"armor_damage":0.03,"flesh_damage":4,"stack_size":60}` | 3.23 | 20 | 0.2575% | 0.7868% |
| `zerocontact:556x45m856a1` | ammo | - | `{"life":65,"friction":0.0155,"gravity":0.065,"recoil_multiplier":1,"inaccuracy_multiplier":1.02,"penetration_class":5,"armor_damage":0.08,"flesh_damage":3,"stack_size":60,"tracer_color":[200,0,0]}` | 5.821 | 20 | 0.2575% | 0.7868% |
| `zerocontact:556x45m995` | ammo | - | `{"life":70,"friction":0.0115,"gravity":0.052,"recoil_multiplier":1.1,"inaccuracy_multiplier":1.08,"penetration_class":10,"armor_damage":0.25,"flesh_damage":4,"stack_size":60}` | 11.238 | 4 | 0.0515% | 0.1574% |
| `zerocontact:57x28l191` | ammo | - | `{"life":50,"friction":0.022,"gravity":0.12,"recoil_multiplier":0.98,"inaccuracy_multiplier":0.98,"penetration_class":4,"armor_damage":0.023,"flesh_damage":3,"stack_size":60,"tracer_color":[255,255,255]}` | 4.53 | 20 | 0.2575% | 0.7868% |
| `zerocontact:57x28sb193` | ammo | - | `{"life":42,"friction":0.035,"gravity":0.18,"recoil_multiplier":1.02,"inaccuracy_multiplier":1.02,"penetration_class":6,"armor_damage":0.026,"flesh_damage":4,"stack_size":60,"tracer_color":[255,255,255]}` | 6.059 | 10 | 0.1288% | 0.3934% |
| `zerocontact:57x28ss190` | ammo | - | `{"life":54,"friction":0.018,"gravity":0.1,"recoil_multiplier":1.07,"inaccuracy_multiplier":1.06,"penetration_class":8,"armor_damage":0.15,"flesh_damage":5,"stack_size":60,"tracer_color":[255,255,255]}` | 8.995 | 10 | 0.1288% | 0.3934% |
| `zerocontact:58x42dbp191` | ammo | - | `{"life":68,"friction":0.012,"gravity":0.055,"recoil_multiplier":1.04,"inaccuracy_multiplier":1.03,"penetration_class":7,"armor_damage":0.22,"flesh_damage":6,"stack_size":60,"tracer_color":[255,255,255]}` | 9.033 | 10 | 0.1288% | 0.3934% |
| `zerocontact:58x42dbx95` | ammo | - | `{"life":64,"friction":0.015,"gravity":0.065,"recoil_multiplier":0.98,"inaccuracy_multiplier":0.98,"penetration_class":4,"armor_damage":0.15,"flesh_damage":5,"stack_size":60,"tracer_color":[255,255,255]}` | 5.847 | 20 | 0.2575% | 0.7868% |
| `zerocontact:58x42dvc12` | ammo | - | `{"life":70,"friction":0.011,"gravity":0.052,"recoil_multiplier":1.1,"inaccuracy_multiplier":1.08,"penetration_class":10,"armor_damage":0.38,"flesh_damage":5,"stack_size":60,"tracer_color":[255,255,255]}` | 12.018 | 4 | 0.0515% | 0.1574% |
| `zerocontact:762x39ap` | ammo | - | `{"life":64,"friction":0.0175,"gravity":0.074,"recoil_multiplier":1.13,"inaccuracy_multiplier":1.1,"penetration_class":12,"armor_damage":0.3,"flesh_damage":7,"stack_size":60}` | 13.799 | 4 | 0.0515% | 0.1574% |
| `zerocontact:762x39bp` | ammo | - | `{"life":62,"friction":0.0185,"gravity":0.078,"recoil_multiplier":1.08,"inaccuracy_multiplier":1.07,"penetration_class":10,"armor_damage":0.2,"flesh_damage":8,"stack_size":60}` | 11.932 | 4 | 0.0515% | 0.1574% |
| `zerocontact:762x39hp` | ammo | - | `{"life":54,"friction":0.026,"gravity":0.105,"recoil_multiplier":0.94,"inaccuracy_multiplier":0.95,"penetration_class":2,"armor_damage":0.01,"flesh_damage":10,"stack_size":60}` | 4.475 | 20 | 0.2575% | 0.7868% |
| `zerocontact:762x39pp` | ammo | - | `{"life":60,"friction":0.02,"gravity":0.084,"recoil_multiplier":1.02,"inaccuracy_multiplier":1.02,"penetration_class":6,"armor_damage":0.1,"flesh_damage":5,"stack_size":60}` | 7.172 | 10 | 0.1288% | 0.3934% |
| `zerocontact:762x39ps` | ammo | - | `{"life":58,"friction":0.021,"gravity":0.088,"recoil_multiplier":1,"inaccuracy_multiplier":1,"penetration_class":5,"armor_damage":0.05,"flesh_damage":6,"stack_size":60}` | 6.346 | 10 | 0.1288% | 0.3934% |
| `zerocontact:762x39sp` | ammo | - | `{"life":56,"friction":0.024,"gravity":0.098,"recoil_multiplier":0.97,"inaccuracy_multiplier":0.97,"penetration_class":3,"armor_damage":0.03,"flesh_damage":6,"stack_size":60}` | 4.417 | 20 | 0.2575% | 0.7868% |
| `zerocontact:762x54bt` | ammo | - | `{"life":78,"friction":0.0135,"gravity":0.06,"recoil_multiplier":1.05,"inaccuracy_multiplier":1.05,"penetration_class":8,"armor_damage":0.15,"flesh_damage":6,"stack_size":60,"tracer_color":[0,255,0]}` | 9.635 | 10 | 0.1288% | 0.3934% |
| `zerocontact:762x54fmj` | ammo | - | `{"life":76,"friction":0.014,"gravity":0.062,"recoil_multiplier":0.99,"inaccuracy_multiplier":0.99,"penetration_class":5,"armor_damage":0.1,"flesh_damage":7,"stack_size":60}` | 7.143 | 10 | 0.1288% | 0.3934% |
| `zerocontact:762x54hp` | ammo | - | `{"life":72,"friction":0.016,"gravity":0.07,"recoil_multiplier":0.95,"inaccuracy_multiplier":0.96,"penetration_class":3,"armor_damage":0.08,"flesh_damage":8,"stack_size":60}` | 5.52 | 20 | 0.2575% | 0.7868% |
| `zerocontact:762x54lps` | ammo | - | `{"life":78,"friction":0.013,"gravity":0.058,"recoil_multiplier":1.03,"inaccuracy_multiplier":1.03,"penetration_class":7,"armor_damage":0.12,"flesh_damage":6,"stack_size":60}` | 8.689 | 10 | 0.1288% | 0.3934% |
| `zerocontact:762x54snb` | ammo | - | `{"life":82,"friction":0.011,"gravity":0.052,"recoil_multiplier":1.12,"inaccuracy_multiplier":1.1,"penetration_class":12,"armor_damage":0.33,"flesh_damage":5,"stack_size":60}` | 13.708 | 4 | 0.0515% | 0.1574% |
| `zerocontact:9x19ap` | ammo | - | `{"life":42,"friction":0.025,"gravity":0.135,"recoil_multiplier":1.08,"inaccuracy_multiplier":1.08,"penetration_class":8,"armor_damage":0.03,"flesh_damage":4,"stack_size":60}` | 7.885 | 10 | 0.1288% | 0.3934% |
| `zerocontact:9x19luger_cci` | ammo | - | `{"life":38,"friction":0.034,"gravity":0.17,"recoil_multiplier":0.94,"inaccuracy_multiplier":0.96,"penetration_class":2,"armor_damage":0.1,"flesh_damage":12,"stack_size":60}` | 4.938 | 20 | 0.2575% | 0.7868% |
| `zerocontact:9x19pbp` | ammo | - | `{"life":44,"friction":0.022,"gravity":0.12,"recoil_multiplier":1.15,"inaccuracy_multiplier":1.12,"penetration_class":10,"armor_damage":0.15,"flesh_damage":6,"stack_size":60}` | 10.523 | 4 | 0.0515% | 0.1574% |
| `zerocontact:9x19pst` | ammo | - | `{"life":38,"friction":0.032,"gravity":0.165,"recoil_multiplier":0.97,"inaccuracy_multiplier":0.98,"penetration_class":3,"armor_damage":0.08,"flesh_damage":7,"stack_size":60}` | 4.458 | 20 | 0.2575% | 0.7868% |
| `zerocontact:9x19rip` | ammo | - | `{"life":36,"friction":0.042,"gravity":0.185,"recoil_multiplier":0.93,"inaccuracy_multiplier":0.95,"penetration_class":2,"armor_damage":0.05,"flesh_damage":10,"stack_size":60}` | 4.12 | 20 | 0.2575% | 0.7868% |
| `zerocontact:armband_black` | cosmetic | - | `{}` | 0 | 20 | 0.2575% | 0.7868% |
| `zerocontact:armband_blue` | cosmetic | - | `{}` | 0 | 20 | 0.2575% | 0.7868% |
| `zerocontact:armband_flora` | cosmetic | - | `{}` | 0 | 20 | 0.2575% | 0.7868% |
| `zerocontact:armband_green` | cosmetic | - | `{}` | 0 | 20 | 0.2575% | 0.7868% |
| `zerocontact:armband_red` | cosmetic | - | `{}` | 0 | 20 | 0.2575% | 0.7868% |
| `zerocontact:armband_white` | cosmetic | - | `{}` | 0 | 20 | 0.2575% | 0.7868% |
| `zerocontact:armband_yellow` | cosmetic | - | `{}` | 0 | 20 | 0.2575% | 0.7868% |
| `zerocontact:armor_6b2` | armor | - | `{"defense":3,"protection_class":5,"movement_fix":-0.025,"default_durability":12,"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":1.3,"blunt_multiplier":0.4},"durability_loss_modifier":1}` | 8.675 | 10 | 0.1288% | 0.3934% |
| `zerocontact:armor_6b23_1` | armor | - | `{"defense":4,"protection_class":7,"movement_fix":-0.025,"default_durability":48,"hurt_modifier":{"ricochet_multiplier":0.15,"penetrate_multiplier":0.8,"blunt_multiplier":0.08},"durability_loss_modifier":1}` | 14.64 | 4 | 0.0515% | 0.1574% |
| `zerocontact:armor_6b23_2` | armor | - | `{"defense":4,"protection_class":7,"movement_fix":-0.025,"default_durability":48,"hurt_modifier":{"ricochet_multiplier":0.15,"penetrate_multiplier":0.8,"blunt_multiplier":0.08},"durability_loss_modifier":1}` | 14.64 | 4 | 0.0515% | 0.1574% |
| `zerocontact:armor_6b43` | armor | - | `{"defense":7,"protection_class":12,"movement_fix":-0.08,"default_durability":72,"hurt_modifier":{"ricochet_multiplier":0.05,"penetrate_multiplier":0.7,"blunt_multiplier":0.05},"durability_loss_modifier":1}` | 21.05 | 1 | 0.0129% | 0.0393% |
| `zerocontact:armor_avs` | armor | - | `{"defense":2,"protection_class":3,"movement_fix":-0.03,"default_durability":48,"hurt_modifier":{"ricochet_multiplier":0.2,"penetrate_multiplier":0.87,"blunt_multiplier":0.2},"durability_loss_modifier":1}` | 8.36 | 10 | 0.1288% | 0.3934% |
| `zerocontact:armor_defender_2` | armor | - | `{"defense":2,"protection_class":4,"movement_fix":-0.013,"default_durability":48,"hurt_modifier":{"ricochet_multiplier":0.15,"penetrate_multiplier":0.85,"blunt_multiplier":0.33},"durability_loss_modifier":1}` | 9.92 | 10 | 0.1288% | 0.3934% |
| `zerocontact:armor_hexgrid_black` | armor | - | `{"defense":0,"protection_class":2,"movement_fix":0.01,"default_durability":31,"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":1.2,"blunt_multiplier":0.5},"durability_loss_modifier":1}` | 5.269 | 20 | 0.2575% | 0.7868% |
| `zerocontact:armor_jpc_v1` | armor | - | `{"defense":0,"protection_class":0,"movement_fix":0.05,"default_durability":32,"hurt_modifier":{"ricochet_multiplier":0.15,"penetrate_multiplier":0.95,"blunt_multiplier":0.25},"durability_loss_modifier":1}` | 6.3 | 10 | 0.1288% | 0.3934% |
| `zerocontact:armor_jpc_v2` | armor | - | `{"defense":3,"protection_class":2,"movement_fix":0.05,"default_durability":32,"hurt_modifier":{"ricochet_multiplier":0.15,"penetrate_multiplier":0.95,"blunt_multiplier":0.25},"durability_loss_modifier":1}` | 10.7 | 4 | 0.0515% | 0.1574% |
| `zerocontact:armor_jpc_v2_swimmer_cut` | armor | - | `{"defense":3,"protection_class":2,"movement_fix":0.2,"default_durability":20,"hurt_modifier":{"ricochet_multiplier":0.15,"penetrate_multiplier":0.95,"blunt_multiplier":0.35},"durability_loss_modifier":1}` | 16.125 | 1 | 0.0129% | 0.0393% |
| `zerocontact:armor_thor_black` | armor | - | `{"defense":0,"protection_class":2,"movement_fix":-0.01,"default_durability":35,"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":0.95,"blunt_multiplier":0.35},"durability_loss_modifier":1}` | 5.394 | 20 | 0.2575% | 0.7868% |
| `zerocontact:armor_untar_blue` | armor | - | `{"defense":4,"protection_class":6,"movement_fix":-0.1,"default_durability":24,"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":0.98,"blunt_multiplier":0.2},"durability_loss_modifier":1}` | 8.89 | 10 | 0.1288% | 0.3934% |
| `zerocontact:backpack_6sh118` | loadout | - | `{"container_size":48}` | 16.0 | 1 | 0.0129% | 0.0393% |
| `zerocontact:backpack_british23_red` | loadout | - | `{"container_size":18}` | 6.0 | 10 | 0.1288% | 0.3934% |
| `zerocontact:backpack_t20_multicam` | loadout | - | `{"container_size":25}` | 8.333 | 10 | 0.1288% | 0.3934% |
| `zerocontact:backpack_t20_umbra` | loadout | - | `{"container_size":25}` | 8.333 | 10 | 0.1288% | 0.3934% |
| `zerocontact:backpack_vkbo_olive` | loadout | - | `{"container_size":8}` | 2.667 | 20 | 0.2575% | 0.7868% |
| `zerocontact:cap_boss` | armor | - | `{"defense":0,"protection_class":0,"default_durability":24,"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":0.78,"blunt_multiplier":1},"durability_loss_modifier":1}` | 2.49 | 20 | 0.2575% | 0.7868% |
| `zerocontact:cap_cyan` | armor | - | `{"defense":0,"protection_class":0,"default_durability":24,"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":1,"blunt_multiplier":1},"durability_loss_modifier":1}` | 2.05 | 20 | 0.2575% | 0.7868% |
| `zerocontact:dog_tag` | utility | - | `{}` | 0 | 20 | 0.2575% | 0.7868% |
| `zerocontact:helmet_6b47_ratnik_arc` | armor | - | `{"defense":0,"protection_class":4,"default_durability":32,"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":0.95,"blunt_multiplier":0.55},"durability_loss_modifier":1}` | 7.3 | 10 | 0.1288% | 0.3934% |
| `zerocontact:helmet_6b47_ratnik_emr` | armor | - | `{"defense":0,"protection_class":4,"default_durability":32,"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":0.95,"blunt_multiplier":0.55},"durability_loss_modifier":1}` | 7.3 | 10 | 0.1288% | 0.3934% |
| `zerocontact:helmet_airframe` | armor | - | `{"defense":0,"protection_class":8,"default_durability":42,"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":0.78,"blunt_multiplier":0.38},"durability_loss_modifier":1}` | 12.293 | 4 | 0.0515% | 0.1574% |
| `zerocontact:helmet_altyn_visor` | helmet | - | `{"protection_class":10,"defense":4,"default_durability":72,"blunt_multiplier":0.21,"penetrate_multiplier":1.25,"visor":true}` | 15 | 1 | 0.0129% | 0.0393% |
| `zerocontact:helmet_bastion_black` | armor | - | `{"defense":0,"protection_class":9,"default_durability":48,"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":0.84,"blunt_multiplier":0.33},"durability_loss_modifier":1}` | 13.46 | 4 | 0.0515% | 0.1574% |
| `zerocontact:helmet_bastion_green` | armor | - | `{"defense":0,"protection_class":9,"default_durability":48,"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":0.84,"blunt_multiplier":0.33},"durability_loss_modifier":1}` | 13.46 | 4 | 0.0515% | 0.1574% |
| `zerocontact:helmet_bastion_multicam` | armor | - | `{"defense":0,"protection_class":9,"default_durability":48,"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":0.84,"blunt_multiplier":0.33},"durability_loss_modifier":1}` | 13.46 | 4 | 0.0515% | 0.1574% |
| `zerocontact:helmet_british23` | armor | - | `{"defense":0,"protection_class":9,"default_durability":128,"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":1,"blunt_multiplier":1},"durability_loss_modifier":1}` | 14.3 | 4 | 0.0515% | 0.1574% |
| `zerocontact:helmet_phonetalker_iiia` | armor | - | `{"defense":0,"protection_class":6,"default_durability":32,"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":0.7,"blunt_multiplier":0.32},"durability_loss_modifier":1}` | 10.26 | 4 | 0.0515% | 0.1574% |
| `zerocontact:helmet_tbh_iiia` | armor | - | `{"defense":0,"protection_class":7,"default_durability":36,"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":0.8,"blunt_multiplier":0.4},"durability_loss_modifier":1}` | 11.025 | 4 | 0.0515% | 0.1574% |
| `zerocontact:helmet_untar_blue` | armor | - | `{"defense":0,"protection_class":6,"default_durability":24,"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":1,"blunt_multiplier":0.67},"durability_loss_modifier":1}` | 8.71 | 10 | 0.1288% | 0.3934% |
| `zerocontact:kit_armor` | repair | - | `{"repair_kit_durability":256}` | 10 | 4 | 0.0515% | 0.1574% |
| `zerocontact:mask_cold_fear` | armor | - | `{"defense":0,"protection_class":8,"default_durability":24,"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":1,"blunt_multiplier":0.79},"durability_loss_modifier":1}` | 10.47 | 4 | 0.0515% | 0.1574% |
| `zerocontact:mask_m50` | armor | - | `{"defense":0,"protection_class":2,"default_durability":12,"immune_effects":["minecraft:blindness"],"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":1,"blunt_multiplier":1},"durability_loss_modifier":1}` | 6.675 | 10 | 0.1288% | 0.3934% |
| `zerocontact:mask_mp5` | armor | - | `{"defense":0,"protection_class":2,"default_durability":12,"immune_effects":["minecraft:blindness"],"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":1,"blunt_multiplier":1},"durability_loss_modifier":1}` | 6.675 | 10 | 0.1288% | 0.3934% |
| `zerocontact:mask_pmk2` | armor | - | `{"defense":0,"protection_class":2,"default_durability":12,"immune_effects":["minecraft:blindness"],"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":1,"blunt_multiplier":1},"durability_loss_modifier":1}` | 6.675 | 10 | 0.1288% | 0.3934% |
| `zerocontact:mask_tagilla_manhunt` | armor | - | `{"defense":0,"protection_class":12,"default_durability":64,"hurt_modifier":{"ricochet_multiplier":0.78,"penetrate_multiplier":1,"blunt_multiplier":0.25},"durability_loss_modifier":1}` | 15.94 | 1 | 0.0129% | 0.0393% |
| `zerocontact:mask_tagilla_ybey` | armor | - | `{"defense":0,"protection_class":12,"default_durability":64,"hurt_modifier":{"ricochet_multiplier":0.78,"penetrate_multiplier":1,"blunt_multiplier":0.25},"durability_loss_modifier":1}` | 15.94 | 1 | 0.0129% | 0.0393% |
| `zerocontact:mask_zk` | armor | - | `{"defense":0,"protection_class":2,"default_durability":12,"immune_effects":["minecraft:blindness"],"hurt_modifier":{"ricochet_multiplier":0.35,"penetrate_multiplier":1,"blunt_multiplier":1},"durability_loss_modifier":1}` | 6.675 | 10 | 0.1288% | 0.3934% |
| `zerocontact:plate_ballistic_convoy` | plate | - | `{"durability":128,"defense":0,"protection_class":11,"movement_fix":-0.1,"durability_loss_modifier":1,"hurt_modifier":{"blunt_modifier":0.05,"penetrate_modifier":0.87,"ricochet_modifier":0.02}}` | 15.12 | 1 | 0.0129% | 0.0393% |
| `zerocontact:plate_cult_locust` | plate | - | `{"durability":72,"defense":0,"protection_class":8,"movement_fix":-0.025,"durability_loss_modifier":1,"hurt_modifier":{"blunt_modifier":0.1,"penetrate_modifier":0.75,"ricochet_modifier":0.13}}` | 13.29 | 4 | 0.0515% | 0.1574% |
| `zerocontact:plate_slime` | plate | - | `{"durability":24,"defense":0,"protection_class":4,"movement_fix":0.03,"durability_loss_modifier":1,"hurt_modifier":{"blunt_modifier":0.2,"penetrate_modifier":1,"ricochet_modifier":0.2}}` | 9.15 | 10 | 0.1288% | 0.3934% |
| `zerocontact:plate_steel` | plate | - | `{"durability":48,"defense":0,"protection_class":6,"movement_fix":-0.015,"durability_loss_modifier":1,"hurt_modifier":{"blunt_modifier":0.25,"penetrate_modifier":0.92,"ricochet_modifier":0.21}}` | 10.14 | 4 | 0.0515% | 0.1574% |
| `zerocontact:raider_egg` | spawn_egg | - | `{"spawns_raider":true}` | 15 | 1 | 0.0129% | 0.0393% |
| `zerocontact:rigs_alice_olive` | loadout | - | `{"container_size":6}` | 2.0 | 20 | 0.2575% | 0.7868% |
| `zerocontact:rigs_cr498_desert` | loadout | - | `{"container_size":6}` | 2.0 | 20 | 0.2575% | 0.7868% |
| `zerocontact:rigs_fast_tac` | loadout | - | `{"container_size":14}` | 4.667 | 20 | 0.2575% | 0.7868% |
| `zerocontact:rigs_sop_mr_desert` | loadout | - | `{"container_size":8}` | 2.667 | 20 | 0.2575% | 0.7868% |
| `zerocontact:rigs_thunderbolt_gray` | loadout | - | `{"container_size":16}` | 5.333 | 20 | 0.2575% | 0.7868% |
| `zerocontact:steel_plate` | plate | - | `{"protection_class":7,"defense":10,"movement_fix":-0.04}` | 13.4 | 4 | 0.0515% | 0.1574% |
| `zerocontact:thoughbook` | workbench | - | `{"workbench":true}` | 10 | 4 | 0.0515% | 0.1574% |
| `zerocontact:uniform_british23_bottom` | cosmetic | - | `{}` | 0 | 20 | 0.2575% | 0.7868% |
| `zerocontact:uniform_british23_top` | cosmetic | - | `{}` | 0 | 20 | 0.2575% | 0.7868% |
| `zerocontact:uniform_g99_bottom` | cosmetic | - | `{}` | 0 | 20 | 0.2575% | 0.7868% |
| `zerocontact:uniform_g99_top` | cosmetic | - | `{}` | 0 | 20 | 0.2575% | 0.7868% |
| `zerocontact:uniform_spn_bottom` | cosmetic | - | `{}` | 0 | 20 | 0.2575% | 0.7868% |
| `zerocontact:uniform_spn_top` | cosmetic | - | `{}` | 0 | 20 | 0.2575% | 0.7868% |

## Acceptance

Full Gradle test/build and installed-JAR hash must be verified after generation.
Real two-client play, horse riding/spawn cadence, and fresh-chest sampling remain manual acceptance.
