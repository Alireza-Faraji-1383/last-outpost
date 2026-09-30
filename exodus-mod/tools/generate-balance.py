"""Regenerate curated balance data from the installed, verified default TacZ pack.

Usage: python tools/generate-balance.py <minecraft-instance>
No third-party mod files are modified.
"""
import json
import re
import sys
import zipfile
from pathlib import Path

INSTANCE = Path(sys.argv[1])
ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/data/exodus'
GUNS = INSTANCE / 'tacz/tacz_default_gun/data/tacz'

def write(relative, value):
    path = ROOT / (relative + '.json')
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, separators=(',', ':')) + '\n', encoding='utf-8')

def item(name, count=None, weight=1, tag=None, durability=None):
    e = dict(type='minecraft:item', name=name, weight=weight)
    f = []
    if count is not None:
        f.append(dict(function='minecraft:set_count', count=count))
    if tag:
        f.append(dict(function='minecraft:set_nbt', tag=tag))
    if durability:
        f.append(dict(function='minecraft:set_damage', damage=dict(min=durability[0], max=durability[1])))
    if f:
        e['functions'] = f
    return e

def ref(name, weight=1):
    return dict(type='minecraft:loot_table', name='exodus:' + name, weight=weight)

def pool(entries, rolls=1, chance=None):
    p = dict(rolls=rolls, entries=entries)
    if chance is not None:
        p['conditions'] = [dict(condition='minecraft:random_chance', chance=chance)]
    return p

def loot(name, pools):
    write('loot_tables/' + name, dict(type='minecraft:chest', pools=pools))

def ammo(ammo_id, count, weight=1):
    assert (GUNS / 'index/ammo' / (ammo_id.split(':')[1] + '.json')).exists(), ammo_id
    return item('tacz:ammo', count, weight, '{AmmoId:"' + ammo_id + '"}')

def gun_data(gun):
    namespace,name=gun.split(':') if ':' in gun else ('tacz',gun)
    base=GUNS if namespace=='tacz' else INSTANCE / 'tacz/daffas/data/daffas_arsenal'
    index = read_pack(base / 'index/guns' / (name + '.json'))
    return index, read_pack(base / 'data/guns' / (index['data'].split(':')[1] + '.json'))

def read_pack(path):
    raw=path.read_text(encoding="utf-8")
    raw=re.sub(r'/\*.*?\*/|//[^\n]*', '', raw, flags=re.S)
    raw=re.sub(r',\s*([}\]])', r'\1', raw)
    return json.loads(raw)

def gun_item(gun, scope=None):
    index, data = gun_data(gun)
    mode = data.get('fire_mode', ['semi'])[0].upper()
    gun_id=gun if ':' in gun else 'tacz:'+gun
    tag = '{GunId:"' + gun_id + '",GunFireMode:"' + mode + '"'
    if scope:
        assert (GUNS / 'index/attachments' / (scope + '.json')).exists()
        tag += ',AttachmentSCOPE:{id:"tacz:attachment",Count:1b,tag:{AttachmentId:"tacz:' + scope + '"}}'
    return item('tacz:modern_kinetic_gun', tag=tag + '}')

COMMON = ['glock_17', 'm1911', 'cz75', 'hk_mp5a5', 'uzi', 'ump45', 'ak47', 'm4a1', 'scar_l', 'm870', 'm1014', 'db_short']
COMMON += ['daffas_arsenal:'+g for g in ['hk45','mp5k','g36c','spasi15']]
VALUABLE = ['scar_h', 'hk416d', 'aug', 'spas_12']
VALUABLE += ['daffas_arsenal:hk417']
SNIPERS = ['ai_awp', 'm107', 'm95', 'mk14']
SNIPERS += ['daffas_arsenal:ssg69']
HEAVY = ['minigun', 'rpg7']
all_guns = COMMON + VALUABLE + SNIPERS + HEAVY
for gun in all_guns:
    index, data = gun_data(gun)
    scope = 'scope_standard_8x' if gun in SNIPERS else None
    amount = 1 if gun == 'rpg7' else 100 if gun == 'minigun' else data['ammo_amount'] * 2
    loot('chests/guns/' + gun.replace(':','/'), [pool([gun_item(gun, scope)]), pool([ammo(data['ammo'], amount)])])

for tier, names in [('common', COMMON), ('valuable', COMMON + VALUABLE + SNIPERS + HEAVY), ('elite', COMMON + VALUABLE + SNIPERS + HEAVY)]:
    loot('chests/bonus/weapons_' + tier, [pool([ref('chests/guns/' + g.replace(':','/'), 12 if g in COMMON else 8 if g in VALUABLE else 2 if g in SNIPERS else 1) for g in names])])

ammo_ids = sorted({gun_data(g)[1]['ammo'] for g in all_guns})
loot('chests/bonus/ammunition', [pool([ammo(a, dict(min=1, max=2) if 'rocket' in a else dict(min=8, max=20) if a in {'tacz:338','tacz:50bmg'} else dict(min=24, max=64), 1 if 'rocket' in a else 4 if a in {'tacz:338','tacz:50bmg'} else 14) for a in ammo_ids], dict(min=2, max=3))])

armor_zip = zipfile.ZipFile(next((INSTANCE / 'mods').glob('lrarmor-*.jar')))
armor_entries = []
for name in armor_zip.namelist():
    if not name.startswith('data/lrarmor/armor_data/') or not name.endswith('.json'):
        continue
    kind = Path(name).stem
    parts = json.loads(armor_zip.read(name))
    defense = sum(v.get('defense', 0) for v in parts.values() if isinstance(v, dict))
    weight = 1 if defense >= 20 else 4 if defense >= 13 else 10
    for slot in ['helmet', 'chestplate', 'leggings', 'boots']:
        if f'assets/lrarmor/models/item/{kind}_{slot}.json' in armor_zip.namelist():
            armor_entries.append(item(f'lrarmor:{kind}_{slot}', weight=weight, durability=(.61, 1.0)))
loot('chests/bonus/armor', [pool(armor_entries)])
loot('chests/bonus/equipment', [pool([
    item('sophisticatedbackpacks:backpack', weight=16), item('sophisticatedbackpacks:iron_backpack', weight=6),
    item('sophisticatedbackpacks:gold_backpack', weight=3), item('sophisticatedbackpacks:diamond_backpack', weight=1),
    item('sophisticatedbackpacks:stack_upgrade_tier_1', weight=2), item('sophisticatedbackpacks:pickup_upgrade', weight=4),
    item('constructionwand:iron_wand', weight=6), item('minecraft:spyglass', weight=10),
    item('supplementaries:rope', dict(min=8,max=16), weight=8), item('supplementaries:sack', weight=5),
    item('lrtactical:flash_shield', weight=2),
    item('tacz:attachment', weight=10, tag='{AttachmentId:"tacz:sight_t1"}'),
    item('tacz:attachment', weight=4, tag='{AttachmentId:"tacz:scope_standard_8x"}')])])

resources = [('iron_ingot', 20, 8, 16), ('diamond', 6, 1, 3), ('gold_ingot', 10, 4, 10), ('copper_ingot', 16, 8, 20), ('redstone', 14, 8, 20), ('coal', 16, 8, 20), ('amethyst_shard', 8, 4, 12)]
for tier, scale, rare in [('common',1,.06), ('standard',1.5,.09), ('valuable',2,.16), ('elite',3,.22)]:
    loot('chests/bonus/resources_' + tier, [pool([item('minecraft:' + n, dict(min=max(1,int(lo*scale)), max=int(hi*scale)), w) for n,w,lo,hi in resources], dict(min=2,max=4)), pool([item('minecraft:obsidian',dict(min=2,max=6),6),item('minecraft:ender_pearl',dict(min=1,max=3),3),item('minecraft:blaze_rod',dict(min=1,max=2),2)], chance=rare)])
    emerald_chance, emerald_count = (.15, dict(min=2,max=6)) if tier in ['common','standard'] else (.25,dict(min=6,max=15))
    loot('chests/bonus/emeralds_' + tier, [pool([item('minecraft:emerald',emerald_count)],chance=emerald_chance)])

families = ['general/common','general/standard','general/valuable','general/elite','food','medical','weapons','utility','tech']
for family in families:
    path = ROOT / ('loot_tables/chests/' + family + '.json')
    t = json.loads(path.read_text(encoding="utf-8"))
    # Generator is idempotent: remove only its owned bonus references.
    t['pools'] = [p for p in t['pools'] if not any(e.get('name','').startswith(('exodus:chests/bonus/resources_', 'exodus:chests/bonus/emeralds_', 'exodus:chests/bonus/armor','exodus:chests/bonus/equipment')) for e in p['entries'])]
    tier = family.split('/')[-1] if family.startswith('general/') else 'standard'
    # Improve the low tier's base pool rather than retaining junk-heavy selection.
    if family == 'general/common':
        t['pools'][0] = pool([item('minecraft:bread',dict(min=3,max=8),10),item('minecraft:cooked_beef',dict(min=2,max=6),8),item('minecraft:torch',dict(min=8,max=20),8),item('minecraft:iron_ingot',dict(min=8,max=16),12),item('minecraft:coal',dict(min=8,max=16),10),item('minecraft:oak_planks',dict(min=16,max=32),8),item('minecraft:gold_ingot',dict(min=3,max=8),6)],dict(min=4,max=7))
    for p in t['pools']:
        p['entries']=[e for e in p['entries'] if e.get('name') not in {'minecraft:emerald','minecraft:emerald_block','minecraft:ender_pearl','minecraft:blaze_rod','minecraft:obsidian'}]
    if not family.startswith('general/') and family!='weapons':
        t['pools']=[p for p in t['pools'] if not any(e.get('name') in {'exodus:chests/bonus/ammunition','exodus:chests/bonus/weapons_common'} for e in p['entries'])]
        t['pools'] += [pool([ref('chests/bonus/ammunition')],chance=.7),pool([ref('chests/bonus/weapons_common')],chance=.25)]
    if family=='weapons':
        # One paired weapon selection, rather than up to eight multi-item bundles,
        # leaves enough chest slots to retain every selected gun's matching ammo.
        t['pools'][0]['entries']=[e for e in t['pools'][0]['entries'] if e.get('type')!='minecraft:loot_table']
        t['pools']=[p for p in t['pools'] if not any(e.get('name')=='exodus:chests/bonus/weapons_elite' for e in p['entries'])]
        t['pools'].append(pool([ref('chests/bonus/weapons_elite')],chance=.85))
    for p in t['pools']:
        for e in p['entries']:
            if family.startswith('general/') and e.get('name','').startswith('exodus:chests/bonus/weapons_') and 'conditions' in p:
                p['conditions'] = [dict(condition='minecraft:random_chance',chance={'common':.35,'standard':.50,'valuable':.65,'elite':.75}[tier])]
            if family.startswith('general/') and e.get('name') == 'exodus:chests/bonus/ammunition':
                p['conditions'] = [dict(condition='minecraft:random_chance',chance=.95)]
    t['pools'] += [pool([ref('chests/bonus/resources_'+tier)]),pool([ref('chests/bonus/emeralds_'+tier)]),pool([ref('chests/bonus/armor')],chance={'common':.18,'standard':.28,'valuable':.40,'elite':.55}[tier]),pool([ref('chests/bonus/equipment')],chance=.22 if tier in ['common','standard'] else .4)]
    write('loot_tables/chests/'+family,t)

# Medical items are a single registered item carrying the verified ConsumableId.
for family, consumables in [('medical',['ai2','carfak','blood_pack','ibuprofen','cms']),('food',['condensed_milk'])]:
    path=ROOT / ('loot_tables/chests/'+family+'.json')
    t=json.loads(path.read_text(encoding="utf-8"))
    t['pools'][0]['entries']=[e for e in t['pools'][0]['entries'] if e.get('name')!='lrtactical:consumable']
    t['pools'][0]['entries'] += [item('lrtactical:consumable',weight=3,tag='{ConsumableId:"lrtactical:'+c+'"}') for c in consumables]
    write('loot_tables/chests/'+family,t)

# General survival bonuses use only category contents, so exact emerald/armor
# probabilities cannot be multiplied through nested specialized chest tables.
for category in ['food','medical','utility','tech']:
    t=json.loads((ROOT / ('loot_tables/chests/'+category+'.json')).read_text(encoding='utf-8'))
    t['pools'][0]['rolls']=dict(min=1,max=2)
    loot('chests/category/'+category,[t['pools'][0]])
loot('chests/bonus/survival',[pool([ref('chests/category/food',5),ref('chests/category/medical',4),ref('chests/category/utility',3),ref('chests/category/tech',2)])])

def supply(id, name, price, contents, pools, special=False, group=None, icon='minecraft:emerald', order=50):
    write('exodus_supply_drops/'+id,dict(display_name=name,icon=icon,loot_table='exodus:supply_drops/'+id,radio_types=['special'] if special else ['basic','special'],max_requests=2 if special else 3,cooldown_seconds=90 if special else 30,cost=dict(item='minecraft:emerald',count=price),drop=dict(smoke_color='#8B5CF6' if special else '#C47A35'),enabled=True,sort_order=order,quota_group='exodus:'+(group or id),contents=contents))
    loot('supply_drops/'+id,pools)

def fixed(items):
    return [pool([item('minecraft:'+n,c)]) for n,c in items]

supply('basic_food','Food Supplies',4,'24 Bread, 16 Steak, 8 Golden Carrots',fixed([('bread',24),('cooked_beef',16),('golden_carrot',8)]),icon='minecraft:bread',order=10)
for a in ['9mm','45acp']:
    supply('basic_light_'+a,'Light Ammo: '+a,5,'96 rounds of '+a,[pool([ammo('tacz:'+a,96)])],group='basic_light',order=11)
for a in ['556x45','762x39','308','12g']:
    amount=32 if a=='12g' else 90
    supply('basic_combat_'+a,'Combat Ammo: '+a,7,str(amount)+' rounds of '+a,[pool([ammo('tacz:'+a,amount)])],group='basic_combat',order=12)
supply('basic_building','Building Materials',4,'128 Cobblestone, 64 Planks, 32 Glass, 16 Ladders, 32 Torches',fixed([('cobblestone',128),('oak_planks',64),('glass',32),('ladder',16),('torch',32)]),order=13)
supply('basic_industrial','Industrial Resources',8,'24 Iron, 16 Copper, 16 Redstone, 16 Coal, 8 Amethyst',fixed([('iron_ingot',24),('copper_ingot',16),('redstone',16),('coal',16),('amethyst_shard',8)]),order=14)
for slot in ['helmet','chestplate','leggings','boots']:
    supply('basic_armor_'+slot,'Scout '+slot.title(),8,'One Scout '+slot+' (80-100% durability)',[pool([item('lrarmor:scout_'+slot,durability=(.8,1.0))])],group='basic_armor',icon='lrarmor:scout_'+slot,order=15)
supply('special_industrial','Advanced Industry',24,'48 Iron, 24 Gold, 12 Diamonds, 32 Redstone, 16 Amethyst, 8 Obsidian',fixed([('iron_ingot',48),('gold_ingot',24),('diamond',12),('redstone',32),('amethyst_shard',16),('obsidian',8)]),special=True,order=50)
for gun in ['scar_h','hk416d']+['ai_awp','m107']+HEAVY:
    group='special_sniper' if gun in SNIPERS else 'special_minigun' if gun=='minigun' else 'special_rpg' if gun=='rpg7' else 'special_rifle'
    price={'special_sniper':36,'special_minigun':50,'special_rpg':45,'special_rifle':30}[group]
    index,data=gun_data(gun)
    amount=200 if gun=='minigun' else 3 if gun=='rpg7' else data['ammo_amount']*(3 if gun in SNIPERS else 4)
    scope='scope_standard_8x' if gun in SNIPERS else 'sight_t1' if gun not in HEAVY else None
    name=gun.replace('_',' ').upper()
    preview=f'{name}, {amount} rounds of {data["ammo"].split(":")[1]}' + (', 8x Scope' if gun in SNIPERS else ', Red Dot' if scope else '')
    supply(group+'_'+gun, name+' Package',price,preview,[pool([gun_item(gun,scope)]),pool([ammo(data['ammo'],amount)])],special=True,group=group,order=51 if group=='special_rifle' else 52 if group=='special_sniper' else 54)
supply('special_armor','Defender Armor Set',40,'Full Defender set: helmet, chestplate, leggings, boots (80-100% durability)',[pool([item('lrarmor:defender_'+slot,durability=(.8,1.0))]) for slot in ['helmet','chestplate','leggings','boots']],special=True,order=53,icon='lrarmor:defender_chestplate')
print('Generated balance tables and selectable supply definitions.')
