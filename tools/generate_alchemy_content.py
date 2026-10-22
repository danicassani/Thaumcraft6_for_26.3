#!/usr/bin/env python3
"""Alchemy recipes whose outputs already have native behavior; preserve original costs."""
import json
from loot_schema import for_resource
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];OLD=ROOT/'src/main/resources/assets/thaumcraft';AS=ROOT/'src/modern/resources/assets/thaumcraft';DATA=ROOT/'src/modern/resources/data/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(for_resource(p,v),ensure_ascii=False,indent=2)+'\n')
def model(n,v):save(AS/f'models/block/{n}.json',v)
for name,source in [('crucible','crucible'),('alembic','alembic_normal'),('jar_normal','jar_normal'),('jar_void','jar_void'),('smelter_basic_off','smelter_basic_off'),('smelter_basic_on','smelter_basic_on')]:
 v=json.loads((OLD/f'models/block/{source}.json').read_text());v.pop('__createdwith',None)
 if v.get('parent','').startswith('block/'):v['parent']='minecraft:'+v['parent']
 for k,t in v.get('textures',{}).items():
  if t=='blocks/furnace_top':v['textures'][k]='minecraft:block/furnace_top'
 if name.startswith('jar'):v['render_type']='minecraft:cutout'
 model(name,v)
for name in ['crucible','alembic','jar_normal','jar_void','smelter_basic']:
 variants={}
 if name=='smelter_basic':
  for facing,y in [('north',0),('east',90),('south',180),('west',270)]:
   for lit in [False,True]:variants[f'facing={facing},lit={str(lit).lower()}']={'model':'thaumcraft:block/smelter_basic_'+('on' if lit else 'off'),'y':y}
 else:variants={'':{'model':'thaumcraft:block/'+name}}
 save(AS/f'blockstates/{name}.json',{'variants':variants})
 base='smelter_basic_off' if name=='smelter_basic' else name
 save(AS/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:block/'+base}})
 save(DATA/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
for name,texture in [('phial','phial'),('alumentum','alumentum')]:
 if name=='phial':texture='phial'
 save(AS/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/'+texture}})
 save(AS/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/'+name}})
aspects=json.loads((AS/'knowledge/aspects.json').read_text())
# Colors are extracted from the original aspect definitions, same order as the component registry.
import re
text=(ROOT/'src/modern/java/thaumcraft/crystals/CrystalAspects.java').read_text();colors={a:int(h,16) for a,h in re.findall(r'colors.put\("([^"]+)", 0x([0-9a-f]+)\)',text)}
for kind in ['phial','jar_normal','jar_void']:
 cases=[]
 for a,c in colors.items():
  name=f'{kind}_{a}'
  if kind=='phial':v={'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/phial','layer1':'thaumcraft:items/phial_overlay'}}
  else:
   v=json.loads((AS/f'models/block/{kind}.json').read_text());v['elements']+=json.loads((OLD/f'models/block/{kind}_100.json').read_text())['elements']
  save(AS/f'models/item/{name}.json',v)
  cases.append({'when':a,'model':{'type':'minecraft:model','model':'thaumcraft:item/'+name,'tints':[{'type':'minecraft:constant','value':-1},{'type':'minecraft:constant','value':(c|0xff000000)-(1<<32)}]}})
 fallback={'type':'minecraft:model','model':'thaumcraft:item/phial' if kind=='phial' else 'thaumcraft:block/'+kind}
 save(AS/f'items/{kind}.json',{'model':{'type':'minecraft:select','property':'minecraft:component','component':'thaumcraft:essentia_aspect','cases':cases,'fallback':fallback}})

def alchemy(name,research,result,catalyst,cost,count=1,components=None):
 r={'id':result,'count':count}
 if components:r['components']=components
 save(DATA/f'recipe/crucible/{name}.json',{'type':'thaumcraft:crucible','research':research,'catalyst':catalyst,'aspects':cost,'result':r})
for a in colors:alchemy('vis_crystal_'+a,'BASEALCHEMY','thaumcraft:crystal_essence','thaumcraft:nugget_quartz',{a:2},components={'thaumcraft:crystal_aspect':a,'minecraft:item_name':{'translate':'item.thaumcraft.crystal_essence.'+a}})
alchemy('nitor','UNLOCKALCHEMY@3','thaumcraft:nitor_yellow','minecraft:glowstone_dust',{'potentia':10,'ignis':10,'lux':10})
alchemy('alumentum','ALUMENTUM','thaumcraft:alumentum',['minecraft:coal','minecraft:charcoal'],{'potentia':10,'ignis':10,'perditio':5})
alchemy('brassingot','METALLURGY@1','thaumcraft:ingot_brass','minecraft:iron_ingot',{'instrumentum':5})
alchemy('thaumiumingot','METALLURGY@2','thaumcraft:ingot_thaumium','minecraft:iron_ingot',{'praecantatio':5,'terra':5})
alchemy('voidingot','BASEELDRITCH','thaumcraft:ingot_void','thaumcraft:void_seed',{'metallum':10,'vitium':5})
alchemy('hedge_tallow','HEDGEALCHEMY@1','thaumcraft:tallow','minecraft:rotten_flesh',{'ignis':1})
alchemy('hedge_leather','HEDGEALCHEMY@1','minecraft:leather','minecraft:rotten_flesh',{'aer':3,'bestia':3})
for name,item in [('gunpowder','gunpowder'),('slime','slime_ball'),('dye','ink_sac'),('glowstone','glowstone_dust')]:
 cost=aspects['items']['minecraft:'+item];alchemy('hedge_'+name,'HEDGEALCHEMY@2','minecraft:'+item,'minecraft:'+item,cost,2)
for name,result,catalyst in [('clay','clay_ball','dirt'),('string','string','wheat'),('web','cobweb','string')]:
 r=aspects['items'].get('minecraft:'+result);c=aspects['items'].get('minecraft:'+catalyst,{})
 if r:
  cost={a:v-c.get(a,0) for a,v in r.items() if v>c.get(a,0)}
  if cost:alchemy('hedge_'+name,'HEDGEALCHEMY@3','minecraft:'+result,'minecraft:'+catalyst,cost)
alchemy('hedge_lava','HEDGEALCHEMY@3','minecraft:lava_bucket','minecraft:bucket',{'ignis':15,'terra':5})
for name,ore,index in [('iron','minecraft:iron_ore',0),('gold','minecraft:gold_ore',1),('cinnabar','thaumcraft:ore_cinnabar',6)]:alchemy('metal_purification_'+name,'METALPURIFICATION','thaumcraft:cluster_'+name,ore,{'metallum':5,'ordo':5})

def arcane(name,research,result,vis,crystals,pattern,key,count=1):save(DATA/f'recipe/arcane/{name}.json',{'type':'thaumcraft:arcane','research':research,'vis':vis,'crystals':crystals,'recipe':{'type':'minecraft:crafting_shaped','pattern':pattern,'key':key,'result':{'id':'thaumcraft:'+result,'count':count}}})
arcane('filter','BASEALCHEMY','filter',15,{'aqua':1},['GWG'],{'G':'minecraft:gold_ingot','W':'thaumcraft:plank_silverwood'},2)
arcane('alembic','ESSENTIASMELTER','alembic',50,{'aqua':1},['WFW','SBS','WFW'],{'W':'thaumcraft:plank_greatwood','F':'thaumcraft:filter','S':'thaumcraft:plate_brass','B':'minecraft:bucket'})
arcane('smelter_basic','ESSENTIASMELTER@2','smelter_basic',50,{'ignis':1},['BCB','SFS','SSS'],{'B':'thaumcraft:plate_brass','C':'thaumcraft:crucible','S':'#minecraft:stone_crafting_materials','F':'minecraft:furnace'})
arcane('jar_normal','WARDEDJARS','jar_normal',5,{},['GWG','G G','GGG'],{'G':'minecraft:glass_pane','W':'#minecraft:wooden_slabs'})
save(DATA/'recipe/arcane/jar_void.json',{'type':'thaumcraft:arcane','research':'WARDEDJARS','vis':50,'crystals':{'perditio':1},'recipe':{'type':'thaumcraft:jar_void_upgrade'}})
save(DATA/'recipe/phial.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':[' C ','G G',' G '],'key':{'C':'minecraft:clay_ball','G':'minecraft:glass'},'result':{'id':'thaumcraft:phial','count':8}})
save(DATA/'recipe/scribing_tools_phial.json',{'type':'thaumcraft:phial_scribing'})
ui={
 'alchemy.thaumcraft.stored':('Essentia: %s / %s','Esencia: %s / %s'),
 'alchemy.thaumcraft.phial':('Phial: %s (%s)','Frasco: %s (%s)'), 'alchemy.thaumcraft.jar':('Jar: %s (%s)','Tarro: %s (%s)'),
 'alchemy.thaumcraft.invalid':('Unsupported save preserved; machine disabled.','Guardado incompatible conservado; máquina desactivada.'),
 'alchemy.thaumcraft.water':('Water: %s mB   Heat: %s / 200','Agua: %s mB   Calor: %s / 200'),
 'alchemy.thaumcraft.burning':('Fuel: %s ticks   Progress: %s / %s','Combustible: %s ticks   Progreso: %s / %s')}
for i,loc in enumerate(['en_us','es_es']):
 p=AS/f'lang/{loc}.json';v=json.loads(p.read_text());v.update({k:a[i] for k,a in ui.items()})
 for n,en,es in [('crucible','Crucible','Crisol'),('smelter_basic','Essentia Smelter','Horno de esencia'),('alembic','Arcane Alembic','Alambique arcano'),('jar_normal','Warded Jar','Tarro protegido'),('jar_void','Void Jar','Tarro del vacío')]:v['block.thaumcraft.'+n]=[en,es][i]
 v.update({'item.thaumcraft.phial':['Empty Phial','Frasco vacío'][i],'item.thaumcraft.alumentum':'Alumentum','entity.thaumcraft.alumentum':'Alumentum'});save(p,v)
p=AS/'knowledge/recipes.json';pages=json.loads(p.read_text())
for path in (DATA/'recipe/crucible').glob('*.json'):
 r=json.loads(path.read_text());pages['thaumcraft:'+path.stem]=['Crucible: '+r['result']['id']+' x'+str(r['result'].get('count',1)),'Catalyst: '+str(r['catalyst']),'Aspects: '+', '.join(a+' '+str(n) for a,n in r['aspects'].items()),'Requires hot water; consumes 50 mB per craft.']
for n,alias in [('filter','Filter'),('alembic','Alembic'),('smelter_basic','EssentiaSmelter'),('jar_normal','WardedJar')]:
 r=json.loads((DATA/f'recipe/arcane/{n}.json').read_text());pages['thaumcraft:'+alias]=[' / '.join(r['recipe']['pattern'])]+[k+': '+v for k,v in r['recipe']['key'].items()]+['Vis '+str(r['vis'])+'; '+str(r['crystals'])]
pages['thaumcraft:phial']=[' C / G G / G','C: clay ball; G: glass','Produces 8 empty phials.']
pages['thaumcraft:viscrystalgroup']=['2 units of any aspect + quartz sliver -> one vis crystal','37 aspect recipes; 50 mB water per craft.']
line='Cauldron + Salis Mundus -> crucible (UNLOCKALCHEMY@2).'
if line not in pages['thaumcraft:salismundusfake']:pages['thaumcraft:salismundusfake'].append(line)
pages['thaumcraft:JarVoid']=['Upgrade one warded jar; all essence is preserved.','50 vis; 1 perditio crystal.']
save(p,pages)
print('Alchemy models and recipes generated; theory cards unchanged')
# Native sapling/leaves cycle supplies the original special woods used by distillation recipes.
import zipfile
client=next(Path('/workspace/.gradle/caches/minecraftforge/forgegradle/mavenizer/caches/minecraft_tasks/26.3').glob('client.jar'))
with zipfile.ZipFile(client) as z:leaf_loot=json.loads(z.read('data/minecraft/loot_table/blocks/oak_leaves.json'))
for wood,chance in [('greatwood',8),('silverwood',64)]:
 for kind in ['sapling','leaves']:
  n=kind+'_'+wood
  save(AS/f'models/block/{n}.json',{'parent':'minecraft:block/cross' if kind=='sapling' else 'minecraft:block/leaves','render_type':'minecraft:cutout','textures':{'cross' if kind=='sapling' else 'all':'thaumcraft:blocks/'+n}})
  save(AS/f'blockstates/{n}.json',{'variants':{'':{'model':'thaumcraft:block/'+n}}})
  save(AS/f'items/{n}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:block/'+n}})
  if kind=='sapling':loot={'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:'+n}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]}
  else:
   loot=json.loads(json.dumps(leaf_loot).replace('minecraft:oak_leaves','thaumcraft:'+n).replace('minecraft:oak_sapling','thaumcraft:sapling_'+wood))
   if wood=='greatwood':loot['pools']=[pool for pool in loot['pools'] if 'minecraft:apple' not in json.dumps(pool)]
   else:loot=json.loads(json.dumps(loot).replace('minecraft:apple','thaumcraft:quicksilver'))
  save(DATA/f'loot_table/blocks/{n}.json',loot)
 save(DATA/f'worldgen/feature/{wood}.json',{'type':'thaumcraft:special_tree','silverwood':wood=='silverwood'})
 save(DATA/f'worldgen/placed_feature/{wood}.json',{'feature':'thaumcraft:'+wood,'placement':[{'type':'minecraft:rarity_filter','chance':chance},{'type':'minecraft:in_square'},{'type':'minecraft:heightmap','heightmap':'WORLD_SURFACE_WG'},{'type':'minecraft:biome'}]})
 save(DATA/f'forge/biome_modifier/{wood}.json',{'type':'forge:add_features','biomes':'#minecraft:is_forest','features':['thaumcraft:'+wood],'step':'vegetal_decoration'})
for tag,names in [('saplings',['sapling_greatwood','sapling_silverwood']),('leaves',['leaves_greatwood','leaves_silverwood'])]:
 for registry in ['block','item']:
  p=DATA.parent/f'minecraft/tags/{registry}/{tag}.json';v=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]};v['values']=list(dict.fromkeys(v['values']+['thaumcraft:'+n for n in names]));save(p,v)
for i,loc in enumerate(['en_us','es_es']):
 p=AS/f'lang/{loc}.json';v=json.loads(p.read_text())
 for wood in ['greatwood','silverwood']:
  for kind,en,es in [('sapling','Sapling','Brote'),('leaves','Leaves','Hojas')]:v['block.thaumcraft.'+kind+'_'+wood]=wood.title()+' '+en if i==0 else es+' de '+wood
 save(p,v)
