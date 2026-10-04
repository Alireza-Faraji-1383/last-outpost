"""Verify release coverage against installed primary pack data and balance monotonicity."""
import importlib.util
import json
import sys
import unittest
import zipfile
from pathlib import Path

INSTANCE=Path(sys.argv.pop(1)).resolve()
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources/data/exodus'
spec=importlib.util.spec_from_file_location('equipment',Path(__file__).with_name('expanded-equipment.py'))
equipment=importlib.util.module_from_spec(spec)
spec.loader.exec_module(equipment)


class EquipmentAudit(unittest.TestCase):
    def setUp(self):
        self.catalog=json.loads((ROOT/'equipment_catalog.json').read_text(encoding='utf-8'))

    def test_every_installed_attachment_index_is_in_loot(self):
        expected={f'{p.parents[2].name}:{p.stem}' for p in (INSTANCE/'tacz').glob('*/data/*/index/attachments/*.json')}
        actual={a['id'] for a in self.catalog['attachments']}
        self.assertEqual(expected,actual)
        entries=json.loads((ROOT/'loot_tables/chests/bonus/attachments.json').read_text())['pools'][0]['entries']
        actual={e['functions'][0]['tag'].split('"')[1] for e in entries}
        self.assertEqual(expected,actual)

    def test_every_zero_data_item_and_ammo_variant_is_in_loot(self):
        expected=set()
        with zipfile.ZipFile(next((INSTANCE/'mods').glob('ZeroContact-*.jar'))) as archive:
            for path in archive.namelist():
                if not path.endswith('.json'):continue
                if path.startswith('data/zerocontact/default_pack/data/zerocontact/items/'):
                    expected.add('zerocontact:'+json.loads(archive.read(path))['id'])
                elif path.startswith('data/zerocontact/default_pack/data/zerocontact/ammoDefinitions/'):
                    expected.add('zerocontact:'+json.loads(archive.read(path))['variant'])
        entries=json.loads((ROOT/'loot_tables/chests/bonus/zero_contact.json').read_text())['pools'][0]['entries']
        actual={e['name'] for e in entries}
        self.assertTrue(expected<=actual,expected-actual)
        fixed={'steel_plate','helmet_altyn_visor','kit_armor','dog_tag','raider_egg','thoughbook'}
        fixed|={'armband_'+c for c in ['black','red','green','blue','white','yellow','flora']}
        fixed|={'uniform_'+c+'_'+s for c in ['british23','g99','spn'] for s in ['top','bottom']}
        self.assertEqual(expected|{'zerocontact:'+s for s in fixed},actual)

    def test_improved_stats_never_increase_weight(self):
        armor=dict(type='armor',protection_class=4,defense=3,default_durability=32,movement_fix=-.02)
        for key,value in [('protection_class',12),('defense',10),('default_durability',128),('movement_fix',.1)]:
            better=armor|{key:value}
            self.assertGreater(equipment.zero_score(better),equipment.zero_score(armor))
            self.assertLessEqual(equipment.tier_weight(equipment.zero_score(better)),equipment.tier_weight(equipment.zero_score(armor)))
        self.assertGreater(equipment.zero_score(dict(type='loadout',container_size=48)),equipment.zero_score(dict(type='loadout',container_size=14)))
        self.assertGreater(equipment.attachment_score({'recoil':{'pitch':{'multiplier':.5}}}),equipment.attachment_score({'recoil':{'pitch':{'multiplier':.9}}}))
        self.assertLess(equipment.attachment_score({'ads_addend':.2}),equipment.attachment_score({'ads_addend':.05}))
        weights=[equipment.scope_weight(z) for z in [1,2,4,6,8,10,16]]
        self.assertEqual(sorted(weights,reverse=True),weights)

    def test_each_zero_ammo_output_is_a_full_native_stack(self):
        rows={a['id']:a for a in self.catalog['zero_contact'] if a['type']=='ammo'}
        entries=json.loads((ROOT/'loot_tables/chests/bonus/zero_contact.json').read_text())['pools'][0]['entries']
        for entry in entries:
            if entry['name'] not in rows:continue
            count=entry['functions'][0]['count']
            self.assertEqual(rows[entry['name']]['stats']['stack_size'],count)


if __name__=='__main__':unittest.main()
