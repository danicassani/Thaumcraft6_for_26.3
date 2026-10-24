#!/usr/bin/env python3
"""Arcane Bore: original infusion, inventory texture, placement icon and journal prose."""
from pathlib import Path
import json
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
save(A/'models/item/arcane_bore.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/turret_bore'}})
save(D/'loot_table/entities/arcane_bore.json',{'type':'minecraft:entity','pools':[]})
save(A/'items/arcane_bore.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/arcane_bore'}})
save(D/'recipe/infusion/arcane_bore.json',{'type':'thaumcraft:infusion','research':'ARCANEBORE','input':{'item':'thaumcraft:turret_basic'},'components':[{'item':s} for s in ['thaumcraft:plank_greatwood','thaumcraft:plank_greatwood','thaumcraft:mechanism_complex','thaumcraft:plate_brass','minecraft:diamond_pickaxe','minecraft:diamond_shovel','thaumcraft:morphic_resonator','thaumcraft:nugget_rareearth']],'aspects':{'potentia':25,'terra':25,'machina':100,'vacuos':25,'motus':25},'instability':4,'result':{'id':'thaumcraft:arcane_bore'}})
for locale,labels in [('en_us',['Arcane Bore','Width: %s · Depth: %s','Vis: %s / 10','Place the bore on a powered support, then fit a pickaxe. It draws vis from the local aura. Strike it to aim; crouch and interact to pack it up. Nearby containers receive the mined materials.']),('es_es',['Perforadora arcana','Anchura: %s · Profundidad: %s','Vis: %s / 10','Coloca la perforadora sobre un soporte alimentado con redstone e introduce un pico. Extrae vis del aura local. Golpéala para orientarla; agáchate e interactúa para recogerla. Los contenedores adyacentes reciben los materiales excavados.'])]:
 p=A/f'lang/{locale}.json';v=json.loads(p.read_text());v.update(dict(zip(['item.thaumcraft.arcane_bore','bore.thaumcraft.size','bore.thaumcraft.vis','bore.thaumcraft.instructions'],labels)));v['entity.thaumcraft.arcane_bore']=labels[0];v['research.ARCANEBORE.stage.2']=labels[3];save(p,v)

p=A/'knowledge/research.json';research=json.loads(p.read_text())
for entry in research:
 if entry['key']=='ARCANEBORE':entry['icons']=['thaumcraft:arcane_bore']
save(p,research)
p=A/'knowledge/aspects.json';v=json.loads(p.read_text());v['entities']['thaumcraft:arcane_bore']={'machina':30,'instrumentum':20,'perditio':20};save(p,v)
