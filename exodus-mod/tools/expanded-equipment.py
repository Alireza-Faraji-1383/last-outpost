"""Build equipment loot from installed pack definitions; never modify third-party packs."""
import json
import re
import zipfile
from pathlib import Path


def read(path):
    raw = path.read_text(encoding='utf-8')
    raw = re.sub(r'/\*.*?\*/|//[^\n]*', '', raw, flags=re.S)
    return json.loads(re.sub(r',\s*([}\]])', r'\1', raw))


def tier_weight(score):
    return 20 if score < 6 else 10 if score < 10 else 4 if score < 15 else 1


def scope_weight(zoom):
    return 20 if zoom <= 2 else 12 if zoom <= 4 else 7 if zoom <= 6 else 4 if zoom <= 8 else 2


def attachment_score(data):
    """Net utility: positive effects increase rarity; penalties reduce it."""
    score = 5.0
    for name, value in data.items():
        if isinstance(value, dict):
            if name == 'recoil':
                score += sum((1-v.get('multiplier',1))*4-v.get('addend',0)*2
                             for v in value.values() if isinstance(v,dict))
                continue
            if name == 'silence':
                score += -value.get('distance_addend',0)/12 + (2 if value.get('use_silence_sound') else 0)
                continue
            if name == 'melee':
                score += value.get('damage',0)/2 + value.get('distance',0)/4 - value.get('cooldown',0)*2
                continue
            if name in ('explosion','ignite'):
                score += 4 if any(isinstance(v,bool) and v for v in value.values()) else 0
                continue
            if 'function' in value:
                function=value['function']
                # Verified functions from installed packs. Neutral baselines: pierce=1,
                # headshot=1.5, armor_ignore=.5; no arbitrary code is evaluated.
                if function=='if (x > 0.5) then y = x*1.5 else y = x*1.75 end': score += 3
                elif function=='if (x > 2) then y = x + 2 else y = x end': score += 1
                elif function=='y = 1': score += -2 if name=='head_shot' else 0
                else: raise ValueError('Unreviewed attachment function: '+function)
                continue
            multiplier = value.get('multiplier', 1)
            addend = value.get('addend', 0)
            # Less recoil/spread is better; more damage/velocity is better.
            direction = -1 if any(s in name for s in ('recoil', 'inaccuracy', 'spread', 'ads')) else 1
            score += direction * ((multiplier - 1) * 8 + addend * 2)
        elif isinstance(value, (int, float)):
            if name in ('ads_addend', 'weight'): score -= value * (10 if name == 'ads_addend' else .5)
            elif name in ('extended_mag_ammo_amount', 'extended_mag_level'): score += value * (0.15 if name.endswith('amount') else 3)
    return round(score, 3)


def zero_score(data):
    if data['type'] == 'loadout':
        return round(data.get('container_size', 0) / 3, 3)
    if data['type'] == 'ammo':
        return round(data.get('penetration_class', 0) + data.get('flesh_damage', 0) / 4
                     + data.get('armor_damage', 0) * 4
                     - (data.get('recoil_multiplier', 1) - 1) * 4
                     - (data.get('inaccuracy_multiplier', 1) - 1) * 4
                     + data.get('life', 0) / 240
                     - data.get('friction', 0) * 20 - data.get('gravity', 0) * 2
                     + max(0,data.get('bullet_amount',1)-1)*.35
                     + data.get('explosion',{}).get('radius',0)*2
                     + (2 if data.get('effects') else 0), 3)
    score = data.get('protection_class', 0) + data.get('defense', 0) * .8
    score += data.get('default_durability', data.get('durability', 0)) / 32
    score += data.get('movement_fix', 0) * 40
    for value in data.get('hurt_modifier', {}).values():
        score += (1 - value) * 2
    score += (1 - data.get('durability_loss_modifier', 1)) * 2
    score += len(data.get('immune_effects',[]))*3
    return round(score, 3)


def stats(data):
    return {k: v for k, v in data.items() if k not in
            {'id','variant','type','texture','model','animation','equipment_slot','ammo_id'}}


def generate(instance, root, item, pool, loot, write):
    attachments = []
    for meta in sorted((instance / 'tacz').glob('*/gunpack.meta.json')):
        pack = meta.parent
        for path in sorted(pack.glob('data/*/index/attachments/*.json')):
            namespace = path.parents[2].name
            index = read(path)
            data_namespace, data_name = index['data'].split(':')
            data = read(pack / f'data/{data_namespace}/data/attachments/{data_name}.json')
            zoom = 0
            if index['type'] == 'scope':
                display_namespace, display_name = index['display'].split(':')
                display = read(pack / f'assets/{display_namespace}/display/attachments/{display_name}.json')
                zoom = max(display.get('zoom', [1]))
            score = attachment_score(data)
            weight = scope_weight(zoom) if zoom else tier_weight(score)
            attachments.append(dict(id=f'{namespace}:{path.stem}', type=index['type'],
                                    zoom=zoom, score=score, weight=weight, stats=data))
    assert len({a['id'] for a in attachments}) == len(attachments)
    loot('chests/bonus/attachments', [pool([
        item('tacz:attachment', weight=a['weight'], tag='{AttachmentId:"'+a['id']+'"}')
        for a in attachments])])

    zero = []
    jar = next((instance / 'mods').glob('ZeroContact-*.jar'))
    with zipfile.ZipFile(jar) as archive:
        for path in sorted(archive.namelist()):
            if not path.endswith('.json'): continue
            if path.startswith('data/zerocontact/default_pack/data/zerocontact/items/'):
                data = json.loads(archive.read(path))
            elif path.startswith('data/zerocontact/default_pack/data/zerocontact/ammoDefinitions/'):
                data = json.loads(archive.read(path))
                data.update(id=data['variant'], type='ammo')
            else: continue
            score = zero_score(data)
            zero.append(dict(id='zerocontact:'+data['id'], type=data['type'], score=score,
                             weight=tier_weight(score), count=data.get('stack_size', 1), stats=stats(data)))
    # Fixed registrations verified against build-72 bytecode (ItemsReg, ItemsRegForge,
    # Helmets, Armbands, Uniforms, BlocksRegForge). Cosmetic pieces have no combat stats.
    fixed = [
        ('steel_plate', 'plate', 13.4, dict(protection_class=7, defense=10, movement_fix=-.04)),
        ('helmet_altyn_visor', 'helmet', 15, dict(protection_class=10,defense=4,default_durability=72,blunt_multiplier=.21,penetrate_multiplier=1.25,visor=True)),
        ('kit_armor', 'repair', 10, dict(repair_kit_durability=256)),
        ('dog_tag', 'utility', 0, {}),
        ('raider_egg', 'spawn_egg', 15, dict(spawns_raider=True)),
        ('thoughbook', 'workbench', 10, dict(workbench=True)),
    ]
    fixed += [('armband_'+c, 'cosmetic', 0, {}) for c in ['black','red','green','blue','white','yellow','flora']]
    fixed += [('uniform_'+c+'_'+s, 'cosmetic', 0, {}) for c in ['british23','g99','spn'] for s in ['top','bottom']]
    for name, kind, score, properties in fixed:
        zero.append(dict(id='zerocontact:'+name, type=kind, score=score,
                         weight=tier_weight(score), count=1, stats=properties))
    zero.sort(key=lambda a: a['id'])
    assert len({a['id'] for a in zero}) == len(zero)
    loot('chests/bonus/zero_contact', [pool([
        item(a['id'], count=a['count'] if a['type']=='ammo' else None, weight=a['weight'])
        for a in zero])])
    catalog = dict(attachments=attachments, zero_contact=zero,
                   chest_chances=dict(attachments=dict(common=.22, standard=.30, valuable=.40, elite=.50),
                                      zero_contact=dict(common=.18, standard=.28, valuable=.40, elite=.55)))
    write('equipment_catalog', catalog)
    version = next(line.split('=',1)[1] for line in (root.parents[4]/'gradle.properties').read_text().splitlines() if line.startswith('mod_version='))
    report = root.parents[4] / f'docs/reports/2026-10-03-equipment-and-horses-{version}.md'
    report.parent.mkdir(parents=True, exist_ok=True)
    lines = [f'# Exodus {version} equipment and saddled horses', '',
             'Generated from installed TacZ packs and Zero Contact build-72. No third-party files were changed.', '',
             '## Changes', '',
             '- Add all indexed TacZ attachments and all Zero Contact item registrations to every Exodus chest family.',
             '- Keep chest/marker quantity, gun bundles, ammunition stacks, existing armor and component rules.',
             '- Spawn 3-5 adult, tamed, saddled horses twice per world day during daytime, with a global match cap of 10.',
             '- Placement uses loaded surface chunks near active players. Unloaded horses still consume the cap.',
             '- Horses are match-bound and removed when the match ends; they do not consume enemy allocations.',
             f'- Mod and distribution version: {version}; distribution tools derive output names from gradle.properties.', '',
             '## Probability', '',
             'One selection per expanded category. Equipment and attachments share a mutually exclusive pool to fit 27 chest slots; their marginal chances are preserved. Zero Contact has a separate pool.',
             'Item chance = category chance * item weight / total category weight.',
             'Percentages below are loot-table selection probabilities, before chest filling; opening previously generated loot does not reroll it.', '',
             '| Chest tier | Attachment pool | Zero Contact pool |', '|---|---:|---:|',
             '| Common | 22% | 18% |', '| Standard / specialized | 30% | 28% |',
             '| Valuable | 40% | 40% |', '| Elite | 50% | 55% |', '',
             '## Scoring', '',
             'Maximum actual display zoom controls scope weight: <=2x:20, <=4x:12, <=6x:7, <=8x:4, >8x:2.',
             'Non-scope net score starts at 5. Lower recoil/spread and greater positive multipliers raise rarity; ADS/weight penalties lower it. Magazine capacity/level raises rarity. Silence, melee, explosive/incendiary perks and verified ammo-mod functions contribute utility.',
             'Zero armor score: protection class + 0.8*defense + durability/32 + 40*movement modifier + 2*sum(1-hurt multiplier) + 2*(1-durability loss modifier).',
             'Loadout score = container slots/3. Immunity adds 3 per immune effect to armor. Ammo score = penetration class + flesh damage/4 + 4*armor damage - 4*(recoil-1) - 4*(inaccuracy-1) + life/240 - 20*friction - 2*gravity + 0.35*(pellets-1) + 2*explosion radius + 2 for effects.',
             'Non-scope/Zero weights: score <6:20; <10:10; <15:4; >=15:1. These are balance heuristics, not an official item power rating.',
             'Fixed functional items use explicit utility scores; cosmetics use 0. Stats unavailable from their data are not invented.', '',
             '## Full item tables', '']
    for group in ['attachments', 'zero_contact']:
        rows = catalog[group]
        total = sum(a['weight'] for a in rows)
        lines += [f'### {group}: {len(rows)} items; total weight {total}', '',
                  '| Item ID | Type | Max zoom | Stats (benefits and penalties) | Score | Weight | Common chest | Elite chest |',
                  '|---|---|---:|---|---:|---:|---:|---:|']
        for a in rows:
            detail = json.dumps(a['stats'], separators=(',', ':')).replace('|','/')
            chance = catalog['chest_chances'][group]
            lines.append(f"| `{a['id']}` | {a['type']} | {a.get('zoom', '-')} | `{detail}` | {a['score']} | {a['weight']} | {chance['common']*a['weight']/total*100:.4f}% | {chance['elite']*a['weight']/total*100:.4f}% |")
        lines.append('')
    lines += ['## Acceptance', '',
              'Full Gradle test/build and installed-JAR hash must be verified after generation.',
              'Real two-client play, horse riding/spawn cadence, and fresh-chest sampling remain manual acceptance.', '']
    report.write_text('\n'.join(lines), encoding='utf-8')
    print(f'Expanded loot: {len(attachments)} attachments, {len(zero)} Zero Contact items. Report: {report}')
