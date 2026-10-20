#!/usr/bin/env python3
"""Original smelter tiers/upgrades, costs and native research requirements. Run after earlier generators."""
import json,copy
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft';O=R/'src/main/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
for name in ['smelter_thaumium','smelter_void','smelter_aux','smelter_vent','bellows']:
 bases=[name+'_off',name+'_on'] if name.startswith(('smelter_thaumium','smelter_void')) else [name]
 for base in bases:
  v=json.loads((O/f'models/block/{base}.json').read_text());v.pop('__createdwith',None)
  if v.get('parent','').startswith('block/'):v['parent']='minecraft:'+v['parent']
  save(A/f'models/block/{base}.json',v)
 variants={}
 for facing,rotation in [('north',{}),('east',{'y':90}),('south',{'y':180}),('west',{'y':270})]+([('up',{'x':90}),('down',{'x':270})] if name in ['bellows','smelter_aux','smelter_vent'] else []):
  if name.startswith(('smelter_thaumium','smelter_void')):
   for lit in [False,True]:variants[f'facing={facing},lit={str(lit).lower()}']={'model':'thaumcraft:block/'+name+('_on' if lit else '_off'),**rotation}
  elif name=='bellows':
   for compressed in [False,True]:variants[f'facing={facing},compressed={str(compressed).lower()}']={'model':'thaumcraft:block/bellows'+('_compressed' if compressed else ''),**rotation}
  else:variants[f'facing={facing}']={'model':'thaumcraft:block/'+name,**rotation}
 save(A/f'blockstates/{name}.json',{'variants':variants})
 save(A/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:block/'+bases[0]}})
 save(D/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
v=json.loads((A/'models/block/bellows.json').read_text());v=copy.deepcopy(v)
for i in [0,1,3]:
 for bound in ['from','to']:v['elements'][i][bound][1]=8+(v['elements'][i][bound][1]-8)*.4
save(A/'models/block/bellows_compressed.json',v)
recipes={}
def arcane(n,research,vis,crystals,pattern,key,count=1,result=None,alias=None):
 v={'type':'thaumcraft:arcane','research':research,'vis':vis,'crystals':crystals,'recipe':{'type':'minecraft:crafting_shaped','pattern':pattern,'key':key,'result':{'id':'thaumcraft:'+(result or n),'count':count}}};save(D/f'recipe/arcane/{n}.json',v);recipes[alias or n]=v
arcane('smelter_thaumium','ESSENTIASMELTERTHAUMIUM',250,{'ignis':2},['BFB','IGI','III'],{'B':'thaumcraft:plate_brass','F':'thaumcraft:smelter_basic','I':'thaumcraft:plate_thaumium','G':'thaumcraft:metal_alchemical'},alias='EssentiaSmelterThaumium')
arcane('smelter_void','ESSENTIASMELTERVOID',750,{'ignis':3},['BFB','IGI','III'],{'B':'thaumcraft:plate_brass','F':'thaumcraft:smelter_basic','I':'thaumcraft:plate_void','G':'thaumcraft:metal_alchemical_advanced'},alias='EssentiaSmelterVoid')
arcane('smelter_aux','IMPROVEDSMELTING',100,{'aer':1,'terra':1},['WTW','RGR','IBI'],{'W':'thaumcraft:plank_greatwood','T':'thaumcraft:tube_filter','R':'thaumcraft:plate_brass','G':'thaumcraft:metal_alchemical','I':'thaumcraft:plate_iron','B':'thaumcraft:bellows'},alias='SmelterAux')
arcane('smelter_vent','IMPROVEDSMELTING2',150,{'aer':1},['IBI','MGF','IBI'],{'I':'thaumcraft:plate_iron','B':'thaumcraft:plate_brass','M':'thaumcraft:filter','F':'thaumcraft:filter','G':'thaumcraft:metal_alchemical'},alias='SmelterVent')
arcane('bellows','BELLOWS',25,{'aer':1},['WW ','LLI','WW '],{'W':'#minecraft:planks','L':'minecraft:leather','I':'minecraft:iron_ingot'},alias='Bellows')
arcane('metal_alchemical','TUBES',75,{'aqua':1,'ordo':1,'perditio':1},['IVI','TWT','IVI'],{'I':'thaumcraft:plate_iron','V':'thaumcraft:tube_valve','T':'thaumcraft:tube','W':'thaumcraft:plank_greatwood'},2,alias='AlchemicalConstruct')
arcane('metal_alchemical_advanced','ESSENTIASMELTERVOID@1',200,{'terra':1,'ignis':1},[' A ','VPV',' A '],{'A':'thaumcraft:metal_alchemical','V':'thaumcraft:plate_void','P':'thaumcraft:primordial_pearl'},alias='AdvAlchemyConstruct')
p=A/'knowledge/recipes.json';v=json.loads(p.read_text())
for alias,r in recipes.items():v['thaumcraft:'+alias]=[' / '.join(r['recipe']['pattern'])]+[k+': '+value for k,value in r['recipe']['key'].items()]+['Vis '+str(r['vis'])+'; crystals '+str(r['crystals'])]
save(p,v)
p=A/'knowledge/research.json';v=json.loads(p.read_text())
for entry in v:
 if entry['key'] in ['ESSENTIASMELTERTHAUMIUM','ESSENTIASMELTERVOID']:
  suffix='thaumium' if entry['key'].endswith('THAUMIUM') else 'void'
  for stage in entry['stages']:
   for item in stage.get('required_item',[]):
    if 'unavailable:thaumcraft:plate'==item['id']:item['id']='thaumcraft:plate_'+suffix
   for item in stage.get('required_craft',[]):
    if 'unavailable:thaumcraft:metal'==item['id']:item['id']='thaumcraft:metal_alchemical'+('_advanced' if suffix=='void' else '')
save(p,v)
for loc,spanish in [('en_us',False),('es_es',True)]:
 p=A/f'lang/{loc}.json';v=json.loads(p.read_text())
 for n,en,es in [('smelter_thaumium','Thaumium Essentia Smelter','Horno de esencia de taumio'),('smelter_void','Void Essentia Smelter','Horno de esencia del vacío'),('smelter_aux','Auxiliary Venting Port','Puerto auxiliar de destilación'),('smelter_vent','Essentia Smelter Vent','Respiradero del horno de esencia'),('bellows','Arcane Bellows','Fuelle arcano')]:v['block.thaumcraft.'+n]=es if spanish else en
 if not spanish:
  v['research.ESSENTIASMELTERTHAUMIUM.stage.2']=v.get('research.ESSENTIASMELTERTHAUMIUM.stage.2','').replace('Its main benefit is that it breaks down essentia slurry at double the normal speed.','Its distillation interval is 10 ticks, compared with 15 for the basic smelter.')
  v['research.ESSENTIASMELTERVOID.stage.2']=v.get('research.ESSENTIASMELTERVOID.stage.2','').replace('It should break down essentia slurry slightly quicker than the basic smeltery, but not nearly as fast as the thaumium smeltery.','Its distillation interval is 15 ticks, compared with 10 for the thaumium smelter.')
 if not spanish:v['research.BELLOWS.stage.2']=v.get('research.BELLOWS.stage.2','').replace('It is also possible to attach them to crucibles to reduce the time it takes for their water to boil.<BR>','')
 v['alchemy.thaumcraft.upgrades']='Retención %s%% · Fuelles %s · Puertos %s · Resp. %s' if spanish else 'Retention %s%% · Bellows %s · Ports %s · Vents %s'
 guide=(' Los hornos retienen 80/90/95%% de cada aspecto (vitium usa el 66%% de esa probabilidad). Fuelles orientados al horno, salvo su frente, reducen el tiempo un 12,5%% cada uno; redstone los detiene. Cada puerto auxiliar orientado al horno añade una columna de hasta cinco alambiques. Cada respiradero tiene una probabilidad de un tercio de eliminar cada unidad de contaminación restante. Alumentum acelera la destilación un 20%% y conserva su efecto al guardar. Fuelles también aceleran hornos vanilla y elevan la succión de depósitos de tubos a 32 por fuelle; el estrangulamiento conserva sus prioridades.' if spanish else ' Smelters retain 80/90/95%% of each aspect (vitium uses 66%% of that chance). Bellows facing the smelter, excluding its front, reduce cooking time by 12.5%% each; redstone stops them. Each attached auxiliary port adds a column of up to five alembics. Each vent has a one-third chance to remove each remaining pollution unit. Alumentum speeds distillation by 20%% and preserves its effect across saves. Bellows also accelerate vanilla furnaces and raise tube-buffer suction to 32 per bellows; choke settings retain their priorities.').replace('%%','%')
 for key in ['research.ESSENTIASMELTERTHAUMIUM.stage.2','research.ESSENTIASMELTERVOID.stage.2','research.IMPROVEDSMELTING.stage.2','research.IMPROVEDSMELTING2.stage.2','research.BELLOWS.stage.2']:
  if guide not in v.get(key,''):v[key]=v.get(key,'')+guide
 save(p,v)
print('Advanced smelters: 5 functional blocks, 7 arcane recipes, original models, research and bilingual guide.')
