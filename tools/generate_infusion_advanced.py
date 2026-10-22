#!/usr/bin/env python3
"""Functional stabilization, original matrix upgrades and eight active tool infusions."""
import json
from loot_schema import for_resource
from pathlib import Path
R=Path(__file__).resolve().parents[1]
AS=R/'src/modern/resources/assets/thaumcraft'
DATA=R/'src/modern/resources/data/thaumcraft'
OLD=R/'src/main/resources/assets/thaumcraft'
def save(path,value):
 path.parent.mkdir(parents=True,exist_ok=True)
 path.write_text(json.dumps(for_resource(path,value),ensure_ascii=False,indent=2)+'\n')
for n in ['stabilizer','inlay_dot','inlay_dot_0','inlay_side','inlay_side0']:
 v=json.loads((OLD/f'models/block/{n}.json').read_text());v.pop('__createdwith',None);v['render_type']='minecraft:cutout';save(AS/f'models/block/{n}.json',v)
save(AS/'blockstates/stabilizer.json',{'variants':{'':{'model':'thaumcraft:block/stabilizer'}}})
save(AS/'blockstates/inlay.json',{'multipart':[{'apply':{'model':'thaumcraft:block/inlay_dot_0'}}]+[{'when':{d:'true'},'apply':{'model':'thaumcraft:block/inlay_side0','y':angle}} for d,angle in [('north',0),('east',90),('south',180),('west',270)]]})
save(AS/'models/block/effect_glimmer.json',{'textures':{'particle':'minecraft:block/glass'},'elements':[]})
save(AS/'blockstates/effect_glimmer.json',{'variants':{'':{'model':'thaumcraft:block/effect_glimmer'}}})
save(DATA/'loot_table/blocks/effect_glimmer.json',{'type':'minecraft:block','pools':[]})
for name in ['stabilizer','inlay']:
 save(AS/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:block/'+('inlay_dot_0' if name=='inlay' else name),'tints':[{'type':'minecraft:constant','value':-1}]}})
 save(DATA/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:'+name}]}]})
save(DATA/'tags/item/infusion_tools.json',{'replace':False,'values':['#minecraft:axes','#minecraft:pickaxes','#minecraft:shovels','#minecraft:swords']})
def arcane(n,alias,research,vis,crystals,pattern,key,count=1):
 r={'type':'thaumcraft:arcane','research':research,'vis':vis,'crystals':crystals,'recipe':{'type':'minecraft:crafting_shaped','pattern':pattern,'key':key,'result':{'id':'thaumcraft:'+n,'count':count}}};save(DATA/f'recipe/arcane/{n}.json',r);return ('thaumcraft:'+alias,[' / '.join(pattern)]+[k+': '+str(v) for k,v in key.items()]+['Vis '+str(vis)+'; '+str(crystals)])
nitor=['thaumcraft:nitor_'+v for v in ['white','orange','magenta','lightblue','yellow','lime','pink','gray','silver','cyan','purple','blue','brown','green','red','black']]
pages=dict([
 arcane('inlay','RedstoneInlay','INFUSIONSTABLE',25,{'aqua':1},['RG'],{'R':'minecraft:redstone','G':'minecraft:gold_ingot'},2),
 arcane('stabilizer','Stabilizer','INFUSIONSTABLE',250,{'terra':1,'aqua':1,'perditio':1},['SRS','BVB','IMI'],{'S':'thaumcraft:slab_arcane_stone','R':'minecraft:redstone_block','B':'thaumcraft:stone_arcane','V':'thaumcraft:vis_resonator','I':'thaumcraft:inlay','M':'thaumcraft:mechanism_complex'}),
 arcane('matrix_speed','MatrixMotion','INFUSIONBOOST',500,{'aer':1,'ordo':1},['SNS','NGN','SNS'],{'S':'thaumcraft:stone_arcane','N':nitor,'G':'minecraft:diamond_block'}),
 arcane('matrix_cost','MatrixCost','INFUSIONBOOST',500,{'aer':1,'aqua':1,'perditio':1},['SAS','AGA','SAS'],{'S':'thaumcraft:stone_arcane','A':'thaumcraft:alumentum','G':'minecraft:diamond_block'})])
# Inlay is originally shapeless.
p=DATA/'recipe/arcane/inlay.json';r=json.loads(p.read_text());r['recipe']={'type':'minecraft:crafting_shapeless','ingredients':['minecraft:redstone','minecraft:gold_ingot'],'result':{'id':'thaumcraft:inlay','count':2}};save(p,r)
for n,part,aspects in [('burrowing','minecraft:rabbit_foot',{'sensus':80,'terra':150}),('collector','minecraft:lead',{'desiderium':80,'aqua':100}),('destructive','minecraft:tnt',{'aversio':200,'perditio':250}),('refining','thaumcraft:salis_mundus',{'ordo':80,'permutatio':60}),('sounding','minecraft:map',{'sensus':40,'ignis':60}),('arcing','minecraft:redstone_block',{'potentia':40,'aer':60}),('essence','thaumcraft:crystal_essence',{'bestia':40,'vitium':60}),('lamplight',nitor,{'lux':80,'aer':20})]:
 r={'type':'thaumcraft:infusion','research':'INFUSIONENCHANTMENT','input':{'item':'#thaumcraft:infusion_tools'},'components':[{'item':'minecraft:enchanted_book'},{'item':part}],'aspects':aspects,'instability':4,'result':{'id':'minecraft:book'},'infusion_enchantment':n};save(DATA/f'recipe/infusion/enchantment_{n}.json',r)
 pages['thaumcraft:IE'+n.upper()+'FAKE']=['Infusion enchantment: '+n,'Center: compatible tool / weapon; raises one rank, preserving all components.','Offerings: enchanted book + '+str(part),'Essentia: '+str(aspects),'Cost multiplier: next rank + 0.33 per other infusion enchantment.']
p=AS/'knowledge/recipes.json';v=json.loads(p.read_text());v.update(pages);save(p,v)
names={'infusion.thaumcraft.sharpness_requirement':('Item enchanted with Sharpness I or higher','Objeto con Filo I o superior'),'block.thaumcraft.stabilizer':('Stabilizer','Estabilizador'),'block.thaumcraft.inlay':('Redstone Inlay','Incrustación de redstone'),'block.thaumcraft.effect_glimmer':('Glimmer','Resplandor'),'infusion.thaumcraft.charge':('Charge: %s / %s','Carga: %s / %s'),'infusion.thaumcraft.state.mitigated':('Instability mitigated','Inestabilidad mitigada')}
for n,en,es in [('COLLECTOR','Collector','Recolector'),('DESTRUCTIVE','Destructive','Destructivo'),('BURROWING','Burrowing','Excavación'),('SOUNDING','Sounding','Sondeo'),('REFINING','Refining','Refinado'),('ARCING','Arcing','Arco eléctrico'),('ESSENCE','Essence Harvester','Cosechador de esencia'),('LAMPLIGHT','Lamplight','Luz de lámpara')]:names['enchantment.infusion.'+n]=(en,es)
for i,loc in enumerate(['en_us','es_es']):
 p=AS/f'lang/{loc}.json';v=json.loads(p.read_text());v.update({k:val[i] for k,val in names.items()});save(p,v)
# Pickaxes must be able to harvest the functional devices.
p=R/'src/modern/resources/data/minecraft/tags/block/mineable/pickaxe.json';v=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]};v['values']=list(dict.fromkeys(v['values']+['thaumcraft:stabilizer','thaumcraft:inlay']));save(p,v)
for kind,typ in [('pick','pickaxes'),('axe','axes'),('sword','swords'),('shovel','shovels'),('hoe','hoes')]:
 p=R/f'src/modern/resources/data/minecraft/tags/item/{typ}.json';v=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]};v['values']=list(dict.fromkeys(v['values']+['thaumcraft:thaumium_'+kind]));save(p,v)
print('Advanced infusion: 2 devices, 4 arcane recipes, 8 tool enchantments')
