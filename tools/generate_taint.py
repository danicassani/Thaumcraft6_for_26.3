#!/usr/bin/env python3
"""Run after condenser: functional taint and fluids, legacy assets, survival recipes."""
import json,copy
from loot_schema import for_resource
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft';O=R/'src/main/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(for_resource(p,v),ensure_ascii=False,indent=2)+'\n')
def model(n,v):save(A/f'models/block/{n}.json',v)
def state(n,v):save(A/f'blockstates/{n}.json',v)
def cube(n,t,parent='cube_all'):model(n,{'parent':'minecraft:block/'+parent,'textures':t});state(n,{'variants':{'':{'model':'thaumcraft:block/'+n}}})
for n in ['taint_fibre','taint_growth_1','taint_growth_2','taint_growth_3','taint_growth_4','taint_orb_0','taint_orb_1','taint_orb_2','log_taintwood']:
 v=json.loads((O/f'models/block/{n}.json').read_text());v['render_type']='minecraft:cutout'
 if n.startswith('taint_growth_'):v['textures']['particle']=f'thaumcraft:blocks/{n}'
 model(n,v)
v=json.loads((O/'blockstates/taint_fibre.json').read_text())
for part in v['multipart']:
 part['apply']['model']=part['apply']['model'].replace('thaumcraft:','thaumcraft:block/')
 part['when']={('growth' if k.startswith('growth') else k):(k[-1] if k.startswith('growth') else str(val).lower()) for k,val in part['when'].items()}
state('taint_fibre',v)
v=json.loads((O/'blockstates/taint_feature.json').read_text())['variants'];v.pop('inventory',None)
for arr in v.values():
 for entry in arr:entry['model']=entry['model'].replace('thaumcraft:','thaumcraft:block/')
state('taint_feature',{'variants':v})
# Preserve the original eight weighted log face variations and all axis orientations.
logs=[]
for texture in [1,2]:
 for face in ['north','south','east','west']:
  n=f'taint_log_{texture}_{face}';v=json.loads((O/'models/block/log_taintwood.json').read_text());v['textures'][face]=f'thaumcraft:blocks/log_taintwood_{texture}';model(n,v);logs.append('thaumcraft:block/'+n)
state('taint_log',{'variants':{'axis='+axis:[dict(model=n,**rot) for n in logs] for axis,rot in [('y',{}),('x',{'x':90,'y':90}),('z',{'x':90})]}})
for n in ['taint_soil','taint_crust','taint_rock']:
 v=json.loads((O/f'blockstates/{n}.json').read_text());variants=[]
 for i,old in enumerate(v['variants']['normal']):
  name=f'{n}_{i}';model(name,{'parent':'minecraft:block/'+old['model'],'textures':old['textures']});variants.append({'model':'thaumcraft:block/'+name,**{k:v for k,v in old.items() if k in ['x','y','weight']}})
 state(n,{'variants':{'':variants}});model(n,{'parent':variants[0]['model']})
v=json.loads((O/'blockstates/taint_geyser.json').read_text())['defaults'];cube('taint_geyser',v['textures'],v['model'])
cube('stone_porous',{'all':'thaumcraft:blocks/porous_stone'})
items=['taint_soil','taint_crust','taint_rock','taint_geyser','taint_log','taint_fibre','taint_feature','stone_porous']
for n in items:
 save(A/f'items/{n}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:block/'+{'taint_log':'log_taintwood','taint_feature':'taint_orb_0','taint_fibre':'taint_growth_1'}.get(n,n)}})
for n in ['bottle_taint','bath_salts','bucket_death']:
 save(A/f'models/item/{n}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/'+n}});save(A/f'items/{n}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/'+n}})
# The original pure bucket texture was generated at runtime; use the native water bucket with its fluid tint.
save(A/'models/item/bucket_pure.json',{'parent':'minecraft:item/generated','textures':{'layer0':'minecraft:item/water_bucket'}})
save(A/'items/bucket_pure.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/bucket_pure','tints':[{'type':'minecraft:constant','value':0xFFCCAA}]}})
for n in ['flux_goo','liquid_death','purifying_fluid']:state(n,{'variants':{'':{'model':'thaumcraft:block/'+n}}});model(n,{'textures':{'particle':'thaumcraft:blocks/flux_goo' if n=='flux_goo' else 'thaumcraft:blocks/animatedglow' if n=='liquid_death' else 'minecraft:block/water_still'}})
for n in ['taint','dissolve']:
 save(D/f'damage_type/{n}.json',{'message_id':'thaumcraft.'+n,'scaling':'never','exhaustion':0.1})
recipes={
 'liquid_death':{'catalyst':'minecraft:bucket','research':'LIQUIDDEATH','aspects':{'mortuus':100,'alkimia':20,'perditio':50},'result':{'id':'thaumcraft:bucket_death'}},
 'bath_salts':{'catalyst':'thaumcraft:salis_mundus','research':'BATHSALTS','aspects':{'cognitio':40,'aer':40,'ordo':40,'victus':40},'result':{'id':'thaumcraft:bath_salts'}},
 'bottle_taint':{'catalyst':'thaumcraft:phial','phial_aspect':'vitium','research':'BOTTLETAINT','aspects':{'vitium':30,'aqua':30},'result':{'id':'thaumcraft:bottle_taint'}}}
for n,r in recipes.items():save(D/f'recipe/{n}.json',{'type':'thaumcraft:crucible',**r})
# Original tainted blocks generally cannot be harvested intact without silk touch.
def entry(n,weight=1,functions=None):return dict(type='minecraft:item',name=n,weight=weight,**({'functions':functions} if functions else {}))
silk={'condition':'minecraft:match_tool','predicate':{'predicates':{'minecraft:enchantments':[{'enchantments':'minecraft:silk_touch','levels':{'min':1}}]}}}
crystal=[{'function':'minecraft:set_components','components':{'thaumcraft:crystal_aspect':'vitium','minecraft:item_name':{'translate':'item.thaumcraft.crystal_essence.vitium'}}}]
for n in items:
 drops=[entry('thaumcraft:'+n)] if n=='taint_log' else []
 if n in ['taint_soil','taint_crust','taint_rock']:drops=[{'type':'minecraft:alternatives','children':[dict(entry('thaumcraft:'+n),conditions=[silk]),dict(entry('thaumcraft:crystal_essence',functions=crystal),conditions=[{'condition':'minecraft:random_chance','chance':1/15}])]}]
 if n=='stone_porous':
  # Weighted TC6 common pool; optional mod metals are deliberately omitted when absent.
  pool=[entry('thaumcraft:amber',20),entry('thaumcraft:cluster_iron',20),entry('thaumcraft:cluster_gold',10),entry('thaumcraft:cluster_cinnabar',10),entry('minecraft:diamond',2),entry('minecraft:emerald',4),entry('minecraft:redstone',8),entry('minecraft:prismarine_crystals',3),entry('minecraft:prismarine_shard',3),entry('minecraft:clay_ball',30),entry('minecraft:quartz',15)]
  for aspect in [k for k,v in json.loads((A/'knowledge/aspects.json').read_text())['components'].items() if v]:pool.append(entry('thaumcraft:crystal_essence',1,[{'function':'minecraft:set_components','components':{'thaumcraft:crystal_aspect':aspect,'minecraft:item_name':{'translate':'item.thaumcraft.crystal_essence.'+aspect}}}]))
  # Explicit table with a shared weight so one roll yields exactly one item.
  save(D/'loot_table/blocks/porous_bonus.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':pool}]})
  drops=[{'type':'minecraft:alternatives','children':[dict(entry('thaumcraft:stone_porous'),conditions=[silk]),{'type':'minecraft:loot_table','value':'thaumcraft:blocks/porous_bonus','conditions':[{'condition':'minecraft:table_bonus','enchantment':'minecraft:fortune','chances':[1/15,2/15,3/15,4/15]}]},entry('minecraft:gravel')]}]
 save(D/f'loot_table/blocks/{n}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':drops,'conditions':[{'condition':'minecraft:survives_explosion'}]}] if drops else []})
for n in ['flux_goo','liquid_death','purifying_fluid']:save(D/f'loot_table/blocks/{n}.json',{'type':'minecraft:block','pools':[]})
for n,count in [('taint_seed',1),('taint_seed_prime',3),('taint_crawler',1),('taint_swarm',1),('taintacle',2),('taintacle_small',1),('taintacle_giant',5),('thaumic_slime',1)]:
 save(D/f'loot_table/entities/{n}.json',{'type':'minecraft:entity','pools':[{'rolls':1,'entries':[entry('thaumcraft:crystal_essence',functions=crystal+[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':count}}])]}]})
for tag in ['bypasses_armor','bypasses_shield']:save(R/f'src/modern/resources/data/minecraft/tags/damage_type/{tag}.json',{'replace':False,'values':['thaumcraft:taint','thaumcraft:dissolve']})
p=A/'knowledge/recipes.json';v=json.loads(p.read_text())
for n,r in recipes.items():v['thaumcraft:'+{'liquid_death':'LiquidDeath','bath_salts':'BathSalts','bottle_taint':'BottleTaint'}[n]]=['Catalyst '+r['catalyst']+(' (Vitium 10)' if n=='bottle_taint' else ''),', '.join(k+' '+str(v) for k,v in r['aspects'].items())]
save(p,v)
names={'taint_fibre':('Taint Fibre','Fibra corrupta'),'taint_crust':('Taint Crust','Costra corrupta'),'taint_soil':('Tainted Soil','Tierra corrupta'),'taint_rock':('Tainted Rock','Roca corrupta'),'taint_geyser':('Taint Geyser','Géiser corrupto'),'taint_log':('Tainted Log','Tronco corrupto'),'taint_feature':('Taint Growth','Excrecencia corrupta'),'stone_porous':('Porous Stone','Piedra porosa'),'flux_goo':('Flux Goo','Masa de flux'),'liquid_death':('Liquid Death','Muerte líquida'),'purifying_fluid':('Purifying Fluid','Fluido purificador'),'bottle_taint':('Bottle of Taint','Botella de corrupción'),'bath_salts':('Purifying Bath Salts','Sales de baño purificadoras'),'bucket_death':('Liquid Death Bucket','Cubo de muerte líquida'),'bucket_pure':('Purifying Fluid Bucket','Cubo de fluido purificador')}
for loc,idx in [('en_us',0),('es_es',1)]:
 p=A/f'lang/{loc}.json';v=json.loads(p.read_text())
 for n,label in names.items():v[('block' if n in items or n in ['flux_goo','liquid_death','purifying_fluid'] else 'item')+'.thaumcraft.'+n]=label[idx]
 for n,label in {'flux_taint':('Flux Taint','Corrupción de flux'),'vis_exhaust':('Vis Exhaustion','Agotamiento de vis'),'infectious_vis_exhaust':('Infectious Vis Exhaustion','Agotamiento de vis infeccioso'),'warp_ward':('Warp Ward','Protección contra la distorsión')}.items():v['effect.thaumcraft.'+n]=label[idx]
 for n,label in {'taint_seed':('Taint Seed','Semilla de corrupción'),'taint_seed_prime':('Giant Taint Seed','Gran semilla de corrupción'),'taint_crawler':('Taint Crawler','Reptador corrupto'),'taint_swarm':('Taint Swarm','Enjambre corrupto'),'taintacle':('Taint Tendril','Tentáculo corrupto'),'taintacle_small':('Lesser Taint Tendril','Tentáculo corrupto menor'),'taintacle_giant':('Giant Taint Tendril','Tentáculo corrupto gigante'),'thaumic_slime':('Thaumic Slime','Slime taumatúrgico'),'bottle_taint':names['bottle_taint']}.items():v['entity.thaumcraft.'+n]=label[idx]
 v['death.attack.thaumcraft.taint']='%1$s sucumbió a la corrupción' if idx else '%1$s succumbed to taint';v['death.attack.thaumcraft.dissolve']='%1$s se disolvió' if idx else '%1$s dissolved'
 guides={'BOTTLETAINT.stage.1':(' Throw to spread flux goo and disease. Corruption grows only near live seeds; eliminate seeds to stop it. Goo has finite volume, decays into flux and can spawn thaumic slimes.',' Lánzala para esparcir masa de flux y enfermedad. La corrupción crece cerca de semillas vivas: elimínalas para frenarla. La masa tiene volumen finito, se degrada en flux y puede generar slimes taumatúrgicos.'),'BATHSALTS.stage.2':(' Drop one salt in source water and wait 10 seconds to create a purifying bath. Its source is consumed on granting Warp Ward, and can be transported in a bucket. Ward duration depends on permanent warp; the bath does not remove permanent warp.',' Suelta una sal en agua fuente y espera 10 segundos para crear un baño purificador. Su fuente se consume al conceder protección contra la distorsión y se puede transportar en un cubo. La duración depende de la distorsión permanente; el baño no la elimina.'),'LIQUIDDEATH.stage.2':(' Native flowing liquid, source damage 5, no infinite sources. Dissolved creatures yield crystals of their aspects.',' Fluido con corrientes nativas, daño de fuente 5 y sin fuentes infinitas. Las criaturas disueltas dejan cristales de sus aspectos.')}
 for key,text in guides.items():
  k='research.'+key
  if text[idx] not in v.get(k,''):v[k]=v.get(k,'')+text[idx]
 save(p,v)
p=R/'src/modern/resources/data/minecraft/tags/block/mineable/pickaxe.json';v=json.loads(p.read_text());v['values']=list(dict.fromkeys(v['values']+['thaumcraft:taint_rock','thaumcraft:stone_porous']));save(p,v)
print('Taint: 8 harvestable blocks, 3 finite/native fluids, 3 alchemy recipes, loot and bilingual guide')

# Split the original potion atlas into the modern standalone sprites.
from PIL import Image
atlas=Image.open(O/'textures/misc/potions.png')
for n,(x,y) in {'flux_taint':(3,1),'vis_exhaust':(5,1),'infectious_vis_exhaust':(6,1),'warp_ward':(3,2)}.items():
 p=A/f'textures/mob_effect/{n}.png';p.parent.mkdir(parents=True,exist_ok=True);atlas.crop((x*18,y*18,x*18+18,y*18+18)).save(p)
# Restore original entity aspects and inherited scan discoveries for the new IDs.
import re
p=A/'knowledge/aspects.json';v=json.loads(p.read_text());source=(R/'src/main/java/thaumcraft/common/config/ConfigAspects.java').read_text();aspect_names=dict(re.findall(r'public static Aspect (\w+) = new Aspect\("([^"]+)"',(R/'src/main/java/thaumcraft/api/aspects/Aspect.java').read_text()))
entity_ids={'ThaumSlime':'thaumic_slime','TaintCrawler':'taint_crawler','TaintSeed':'taint_seed','TaintSeedPrime':'taint_seed_prime','TaintSwarm':'taint_swarm','Taintacle':'taintacle','TaintacleTiny':'taintacle_small','TaintacleGiant':'taintacle_giant'}
for name,formula in re.findall(r'registerEntityTag\("Thaumcraft\.([^"]+)", (new AspectList\(\)(?:\.add\(Aspect\.\w+, \d+\))+)[)]',source):
 if name in entity_ids:v['entities']['thaumcraft:'+entity_ids[name]]={aspect_names[a]:int(n) for a,n in re.findall(r'\.add\(Aspect\.(\w+), (\d+)\)',formula)}
for n,flag in {'taint_seed':'!TaintSeed','taint_seed_prime':'!TaintSeed','taint_crawler':'!TaintCrawler','taintacle':'!Taintacle','taintacle_small':'!Taintacle','taintacle_giant':'!Taintacle','thaumic_slime':'!ThaumSlime'}.items():v['triggers']['entities']['thaumcraft:'+n]=[flag]
save(p,v)
# The original lesser tendril expires without loot.
save(D/'loot_table/entities/taintacle_small.json',{'type':'minecraft:entity','pools':[]})
