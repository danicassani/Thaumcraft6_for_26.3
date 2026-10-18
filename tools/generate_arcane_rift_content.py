#!/usr/bin/env python3
"""Port original table/charger/siphon meshes and available TC6 arcane recipes."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/modern/resources/assets/thaumcraft'
DATA = ROOT / 'src/modern/resources/data'
OLD = ROOT / 'src/main/resources/assets/thaumcraft'


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + '\n')


for name in ['arcane_workbench', 'void_siphon']:
    model = json.loads((OLD / f'models/block/{name}.json').read_text())
    model.pop('__createdwith', None)
    model['parent'] = 'minecraft:block/block'
    save(ASSETS / f'models/block/{name}.json', model)

vertices, uvs, triangles = [], [], []
for line in (OLD / 'models/block/arcane_workbench_charger.obj').read_text().splitlines():
    values = line.split()
    if not values:
        continue
    if values[0] == 'v':
        vertices.append(list(map(float, values[1:4])))
    elif values[0] == 'vt':
        uvs.append(list(map(float, values[1:3])))
    elif values[0] == 'f':
        face = []
        for index in values[1:]:
            v, t, *_ = map(int, index.split('/'))
            face.append(vertices[v-1] + uvs[t-1])
        for i in range(1, len(face)-1):
            triangles.append([face[0], face[i], face[i+1]])
save(ASSETS / 'models/block/arcane_workbench_charger.json', {
    'parent': 'minecraft:block/block', 'loader': 'thaumcraft:triangles',
    'textures': {'texture': 'thaumcraft:blocks/arcane_workbench_charger', 'particle': 'thaumcraft:blocks/arcane_workbench_charger'},
    'triangles': triangles,
})
for name in ['arcane_workbench', 'arcane_workbench_charger', 'void_siphon']:
    save(ASSETS / f'blockstates/{name}.json', {'variants': {'': {'model': f'thaumcraft:block/{name}'}}})
    save(ASSETS / f'items/{name}.json', {'model': {'type': 'minecraft:model', 'model': f'thaumcraft:block/{name}'}})
    save(DATA / f'thaumcraft/loot_table/blocks/{name}.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': f'thaumcraft:{name}'}]}]})
save(ASSETS / 'models/item/salis_mundus.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'thaumcraft:items/salis_mundus'}})
save(ASSETS / 'items/salis_mundus.json', {'model': {'type': 'minecraft:model', 'model': 'thaumcraft:item/salis_mundus'}})
save(DATA / 'thaumcraft/recipe/salis_mundus.json', {'type': 'thaumcraft:salis_mundus'})


def arcane(name, result, vis, crystals, pattern=None, key=None, ingredients=None):
    recipe = {'type': 'minecraft:crafting_shapeless' if ingredients else 'minecraft:crafting_shaped',
              'result': {'id': 'thaumcraft:' + result}}
    if ingredients:
        recipe['ingredients'] = ingredients
    else:
        recipe.update(pattern=pattern, key=key)
    save(DATA / f'thaumcraft/recipe/arcane/{name}.json', {'type': 'thaumcraft:arcane', 'recipe': recipe, 'vis': vis, 'crystals': crystals})


# Original costs and ingredients; no recipes whose outputs still need a new mechanic.
arcane('mechanism_simple', 'mechanism_simple', 10, {'ignis': 1, 'aqua': 1}, [' B ', 'ISI', ' B '], {'B': 'thaumcraft:plate_brass', 'I': 'thaumcraft:plate_iron', 'S': 'minecraft:stick'})
arcane('mechanism_complex', 'mechanism_complex', 50, {'ignis': 1, 'aqua': 1}, [' M ', 'TQT', ' M '], {'M': 'thaumcraft:mechanism_simple', 'T': 'thaumcraft:plate_thaumium', 'Q': 'minecraft:piston'})
arcane('vis_resonator', 'vis_resonator', 50, {'aer': 1, 'aqua': 1}, ingredients=['thaumcraft:plate_iron', 'minecraft:quartz'])
arcane('workbench_charger', 'arcane_workbench_charger', 200, {'aer': 2, 'ordo': 2}, [' R ', 'W W', 'I I'], {'R': 'thaumcraft:vis_resonator', 'W': 'thaumcraft:plank_greatwood', 'I': 'minecraft:iron_ingot'})
arcane('recharge_pedestal', 'recharge_pedestal', 100, {'aer': 1, 'ordo': 1}, [' R ', 'DID', 'SSS'], {'R': 'thaumcraft:vis_resonator', 'D': 'minecraft:diamond', 'I': 'minecraft:gold_ingot', 'S': '#minecraft:stone_crafting_materials'})
arcane('vis_generator', 'vis_generator', 25, {'ignis': 1, 'ordo': 1}, ['WSW', 'EPE', 'WRW'], {'W': '#minecraft:planks', 'S': 'minecraft:redstone', 'E': 'thaumcraft:nugget_rareearth', 'P': 'minecraft:piston', 'R': 'thaumcraft:vis_resonator'})
arcane('enchanted_fabric', 'fabric', 5, {}, [' S ', 'SCS', ' S '], {'S': 'minecraft:string', 'C': '#minecraft:wool'})

for tool, names in {'axe': ['arcane_workbench', 'arcane_workbench_charger'], 'pickaxe': ['void_siphon']}.items():
    path = DATA / f'minecraft/tags/block/mineable/{tool}.json'
    tag = json.loads(path.read_text()) if path.exists() else {'replace': False, 'values': []}
    tag['values'] = list(dict.fromkeys(tag['values'] + ['thaumcraft:' + n for n in names]))
    save(path, tag)

translations = {
    'en_us': {'block.thaumcraft.arcane_workbench': 'Arcane Workbench', 'block.thaumcraft.arcane_workbench_charger': 'Arcane Workbench Charger', 'block.thaumcraft.void_siphon': 'Void Siphon', 'item.thaumcraft.salis_mundus': 'Salis Mundus', 'entity.thaumcraft.flux_rift': 'Flux Rift', 'menu.thaumcraft.craft': 'Craft', 'menu.thaumcraft.vis': 'Vis: %s / %s', 'menu.thaumcraft.chunks': '%s chunks', 'block.thaumcraft.void_siphon.progress': 'Void seed: %s / %s'},
    'es_es': {'block.thaumcraft.arcane_workbench': 'Mesa de trabajo arcana', 'block.thaumcraft.arcane_workbench_charger': 'Cargador de mesa arcana', 'block.thaumcraft.void_siphon': 'Sifón de vacío', 'item.thaumcraft.salis_mundus': 'Salis Mundus', 'entity.thaumcraft.flux_rift': 'Grieta de flux', 'menu.thaumcraft.craft': 'Crear', 'menu.thaumcraft.vis': 'Vis: %s / %s', 'menu.thaumcraft.chunks': '%s chunks', 'block.thaumcraft.void_siphon.progress': 'Semilla de vacío: %s / %s'},
}
for locale, extra in translations.items():
    path = ASSETS / f'lang/{locale}.json'
    strings = json.loads(path.read_text()); strings.update(extra); save(path, strings)
print(f'Arcane workbench, original charger mesh ({len(triangles)} triangles), void siphon, Salis Mundus and 7 arcane recipes generated.')
