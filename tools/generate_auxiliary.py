#!/usr/bin/env python3
"""Original paving stones, ears and configurable redstone relay."""
from loot_schema import for_resource
from pathlib import Path
import json,shutil
R=Path(__file__).resolve().parents[1];root=R/'src/modern/resources';old=R/'src/main/resources/assets/thaumcraft'
def save(p,v):
 p=root/p;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(for_resource(p,v),ensure_ascii=False,indent=2)+'\n')
def texture(n):
 for suffix in ['.png','.png.mcmeta']:
  src=old/f'textures/blocks/{n}{suffix}'
  if src.exists():
   dst=root/f'assets/thaumcraft/textures/block/{n}{suffix}';dst.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(src,dst)
def modern(v):
 if isinstance(v,dict):return {k:modern(x) for k,x in v.items()}
 if isinstance(v,list):return [modern(x) for x in v]
 if isinstance(v,str):return v.replace('thaumcraft:blocks/','thaumcraft:block/').replace('blocks/redstone_torch_off','minecraft:block/redstone_torch_off').replace('blocks/redstone_torch_on','minecraft:block/redstone_torch').replace('blocks/stone_slab_top','minecraft:block/smooth_stone')
 return v
names=['paving_stone_travel','paving_stone_barrier','arcane_ear','arcane_ear_toggle','redstone_relay']
for n in ['paving_stone_travel','paving_stone_barrier','arcane_brick_stone','base_wood','arcane_ear_on','arcane_ear_off','arcane_ear_toggle_on','arcane_ear_toggle_off','redstone_relay_on','redstone_relay_off','levitator_side_on','levitator_side_off']:texture(n)
for n in names:
 if n.startswith('paving'):
  save(Path(f'assets/thaumcraft/models/block/{n}.json'),{'parent':'minecraft:block/farmland','textures':{'dirt':'thaumcraft:block/arcane_brick_stone','top':'thaumcraft:block/'+n,'particle':'thaumcraft:block/'+n}})
  states={'':{'model':'thaumcraft:block/'+n}};item=n
 else:
  for status in ['off','on']:
   source='arcane_ear' if n.startswith('arcane_ear') else n
   v=modern(json.loads((old/f'models/block/{source}_{status}.json').read_text()))
   if 'parent' in v:v['parent']='thaumcraft:block/arcane_ear_on'
   if n=='arcane_ear_toggle':
    v.setdefault('textures',{}).update({'side':f'thaumcraft:block/{n}_{status}','particle':f'thaumcraft:block/{n}_{status}'})
   v['render_type']='minecraft:cutout'
   save(Path(f'assets/thaumcraft/models/block/{n}_{status}.json'),v)
  source=json.loads((old/f'blockstates/{n}.json').read_text())['variants'];states={}
  for k,v in source.items():
   if not k.startswith('enabled='):continue
   model=n+('_on' if k.startswith('enabled=true') else '_off');states[k.replace('enabled=','powered=')]={**{a:b for a,b in v.items() if a in ['x','y']},'model':'thaumcraft:block/'+model}
  item=n+'_off'
 save(Path(f'assets/thaumcraft/blockstates/{n}.json'),{'variants':states})
 save(Path(f'assets/thaumcraft/items/{n}.json'),{'model':{'type':'minecraft:model','model':'thaumcraft:block/'+item}})
 save(Path(f'data/thaumcraft/loot_table/blocks/{n}.json'),{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:'+n}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
save(Path('assets/thaumcraft/models/block/barrier.json'),{'textures':{'particle':'minecraft:block/stone'},'elements':[]})
save(Path('assets/thaumcraft/blockstates/barrier.json'),{'variants':{'':{'model':'thaumcraft:block/barrier'}}})
def arcane(n,research,vis,crystals,pattern,key,count=1):
 save(Path(f'data/thaumcraft/recipe/arcane/{n}.json'),{'type':'thaumcraft:arcane','research':research,'vis':vis,'crystals':crystals,'recipe':{'type':'minecraft:crafting_shaped','pattern':pattern,'key':key,'result':{'id':'thaumcraft:'+n,'count':count}}})
arcane('paving_stone_travel','PAVINGSTONES',50,{'aer':1,'terra':1},['SS','SS'],{'S':'thaumcraft:stone_arcane_brick'},4)
arcane('paving_stone_barrier','PAVINGSTONES',50,{'ignis':1,'ordo':1},['SS','SS'],{'S':'thaumcraft:stone_arcane_brick'},4)
arcane('redstone_relay','REDSTONERELAY',10,{'ordo':1},['TGT','SSS'],{'T':'minecraft:redstone_torch','G':'thaumcraft:mechanism_simple','S':'minecraft:smooth_stone_slab'})
arcane('arcane_ear','ARCANEEAR',15,{'aer':1},['P P',' G ','WRW'],{'P':'thaumcraft:plate_brass','G':'thaumcraft:mechanism_simple','W':'#minecraft:wooden_slabs','R':'minecraft:redstone'})
save(Path('data/thaumcraft/recipe/arcane_ear_toggle.json'),{'type':'minecraft:crafting_shapeless','ingredients':['thaumcraft:arcane_ear','minecraft:lever'],'result':{'id':'thaumcraft:arcane_ear_toggle'}})
p=Path('data/minecraft/tags/block/mineable/pickaxe.json');v=json.loads((root/p).read_text());v['values']=sorted(set(v['values'])|{'thaumcraft:'+n for n in names if not n.startswith('arcane_ear')});save(p,v)
p=Path('data/minecraft/tags/block/mineable/axe.json');v=json.loads((root/p).read_text());v['values']=sorted(set(v['values'])|{'thaumcraft:arcane_ear','thaumcraft:arcane_ear_toggle'});save(p,v)
p=Path('assets/thaumcraft/knowledge/recipes.json');v=json.loads((root/p).read_text())
for alias,n in [('PaveTravel',names[0]),('PaveBarrier',names[1]),('ArcaneEar','arcane_ear'),('RedstoneRelay','redstone_relay')]:
 recipe=json.loads((root/f'data/thaumcraft/recipe/arcane/{n}.json').read_text());craft=recipe['recipe'];v['thaumcraft:'+alias]=[' / '.join(craft['pattern'])]+[k+': '+value for k,value in craft['key'].items()]+['Output: '+str(craft['result'].get('count',1))+' x '+craft['result']['id'],'Vis: '+str(recipe['vis']),'Crystals: '+str(recipe['crystals'])]
v['thaumcraft:ArcaneEarToggle']=['Arcane ear + lever'];save(p,v)
for lang,i in [('en_us',0),('es_es',1)]:
 p=Path('assets/thaumcraft/lang')/(lang+'.json');v=json.loads((root/p).read_text())
 for key,pair in {'block.thaumcraft.paving_stone_travel':('Paving Stone of Travel','Piedra de viaje'),'block.thaumcraft.paving_stone_barrier':('Paving Stone of Warding','Piedra de barrera'),'block.thaumcraft.arcane_ear':('Arcane Ear','Oreja arcana'),'block.thaumcraft.arcane_ear_toggle':('Arcane Ear (Toggle)','Oreja arcana (conmutador)'),'block.thaumcraft.redstone_relay':('Redstone Relay','Relé de redstone'),'auxiliary.thaumcraft.note':('Note: %s · Instrument: %s','Nota: %s · Instrumento: %s'),'auxiliary.thaumcraft.relay':('Input threshold: %s · Output: %s','Umbral de entrada: %s · Salida: %s')}.items():v[key]=pair[i]
 save(p,v)
print('Auxiliary devices: five original recipes, models, animated paving textures and controls')
