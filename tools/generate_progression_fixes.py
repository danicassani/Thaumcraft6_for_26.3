#!/usr/bin/env python3
"""Apply progression corrections after the historical content generators."""
import json
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft/recipe'
def read(p):return json.loads(p.read_text())
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
p=A/'knowledge/aspects.json';v=read(p);triggers=v['triggers']['entities']
mapping={'minecraft:wisp':['thaumcraft:wisp'],'minecraft:thaumic_slime':['thaumcraft:thaumic_slime'],'minecraft:fire_bat':['thaumcraft:firebat'],'minecraft:brainy_zombie':['thaumcraft:brainy_zombie','thaumcraft:giant_brainy_zombie'],'minecraft:eldritch_crab':['thaumcraft:eldritch_crab'],'minecraft:inhabited_zombie':['thaumcraft:inhabited_zombie'],'minecraft:cultist':['thaumcraft:cultist_knight','thaumcraft:cultist_cleric','thaumcraft:cultist_leader'],'minecraft:eldritch_guardian':['thaumcraft:eldritch_guardian'],'minecraft:golem':['minecraft:iron_golem','minecraft:snow_golem'],'minecraft:owned_construct':['thaumcraft:golem','thaumcraft:arcane_bore','thaumcraft:turret_basic','thaumcraft:turret_advanced'],'minecraft:taint_crawler':['thaumcraft:taint_crawler'],'minecraft:taintacle':['thaumcraft:taintacle'],'minecraft:taint_seed':['thaumcraft:taint_seed'],'minecraft:taint_swarm':['thaumcraft:taint_swarm'],'minecraft:thaumcraft_boss':['thaumcraft:eldritch_golem','thaumcraft:eldritch_warden']}
for old,ids in mapping.items():
 flags=triggers.pop(old,[])
 for id in ids:triggers[id]=sorted(set(triggers.get(id,[])+flags))
# Preserve class-based discoveries for aliases whose historical trigger was already removed.
for id,flags in {'thaumcraft:firebat':['!Firebat','f_BAT','f_FLY'],'thaumcraft:brainy_zombie':['!BrainyZombie','f_BRAIN'],'thaumcraft:giant_brainy_zombie':['!BrainyZombie','f_BRAIN'],'thaumcraft:inhabited_zombie':['!EldritchCrab'],'thaumcraft:eldritch_crab':['!EldritchCrab'],'thaumcraft:cultist_knight':['!CrimsonCultist'],'thaumcraft:cultist_cleric':['!CrimsonCultist'],'thaumcraft:cultist_leader':['!CrimsonCultist'],'minecraft:iron_golem':['f_golem'],'minecraft:snow_golem':['f_golem'],'thaumcraft:wisp':['!Wisp','f_FLY']}.items():triggers[id]=sorted(set(triggers.get(id,[])+flags))
items=v['triggers']['items'];items.pop('minecraft:nether_portal',None);items.pop('minecraft:end_portal',None);items.pop('minecraft:stained_hardened_clay',None)
for key in items:items[key]=sorted(set(items[key]))
save(p,v)
p=A/'knowledge/research.json';es=read(p)
for e in es:
 if e['key']=='FOCUSSPELLBAT':e['parents']=['!Firebat' if q=='!minecraft:blaze' else q for q in e['parents']]
save(p,es)
save(D/'arcane/vis_battery.json',{'type':'thaumcraft:arcane','research':'VISBATTERY','vis':50,'crystals':dict.fromkeys(['aer','ignis','aqua','terra','ordo','perditio'],2),'recipe':{'type':'minecraft:crafting_shaped','pattern':['SSS','SRS','SSS'],'key':{'S':'thaumcraft:slab_arcane_stone','R':'thaumcraft:vis_resonator'},'result':{'id':'thaumcraft:vis_battery'}}})
save(D/'arcane/activator_rail.json',{'type':'thaumcraft:arcane','research':'FIRSTSTEPS','vis':10,'crystals':{},'recipe':{'type':'minecraft:crafting_shapeless','ingredients':['minecraft:activator_rail'],'result':{'id':'thaumcraft:activator_rail'}}})
save(D/'jar_brace.json',{'type':'minecraft:crafting_shaped','pattern':['NSN','S S','NSN'],'key':{'N':'thaumcraft:nugget_brass','S':'#minecraft:wooden_rods'},'result':{'id':'thaumcraft:jar_brace','count':2}})
# Modern vanilla has sticks; use the existing shared stick tag rather than a fictitious vanilla tag.
r=read(D/'jar_brace.json');r['key']['S']='minecraft:stick';save(D/'jar_brace.json',r)
# Retain original labels as localization aliases; don't show 1.12 entity keys in the topic list.
entity_names={'BrainyZombie':'brainy_zombie','EldritchCrab':'eldritch_crab','EldritchGuardian':'eldritch_guardian','Firebat':'firebat','TaintCrawler':'taint_crawler','TaintSeed':'taint_seed','TaintSwarm':'taint_swarm','Taintacle':'taintacle','ThaumSlime':'thaumic_slime','Wisp':'wisp'}
goals={
'f_golem':('an iron or snow golem','un gólem de hierro o de nieve'),'f_BAT':('a bat','un murciélago'),'f_FLY':('a flying creature','una criatura voladora'),'f_BRAIN':('a zombie brain','un cerebro de zombi'),'f_DISPENSER':('a dispenser','un dispensador'),'f_SPIDER':('a spider','una araña'),'f_TELEPORT':('an ender pearl or an enderman','una perla de ender o un enderman'),'f_VOIDSEED':('a void seed','una semilla del vacío'),'f_arrow':('an arrow','una flecha'),'f_fireball':('a ghast fireball','una bola de fuego de ghast'),'f_spit':('llama spit','un escupitajo de llama'),'f_onfire':('the sensation of being on fire','la sensación de estar en llamas'),'f_toomuchflux':('a flux rift','una grieta de flux'),
'm_deepdown':('the depths below Y=10','las profundidades por debajo de Y=10'),'m_uphigh':('a high place','un lugar elevado'),'m_hellandback':('the Nether','el Nether'),'m_walker':('walking more than 1,600 blocks','caminar más de 1600 bloques'),'m_runner':('sprinting more than 800 blocks','esprintar más de 800 bloques'),'m_swimmer':('swimming more than 80 blocks','nadar más de 80 bloques'),'m_jumper':('jumping more than 500 times','saltar más de 500 veces'),
'!BATHSALTS':('the effects of lasting warp','los efectos de la distorsión duradera'),'!DRAGONBREATH':("dragon's breath",'aliento de dragón'),'!INSTABILITY':('an unstable infusion','una infusión inestable'),'!ORBLOCK1':('ancient stone','piedra ancestral'),'!ORBLOCK2':('eldritch stone tiles','baldosas de piedra arcana antigua'),'!ORECRYSTAL':('a natural vis crystal','un cristal de vis natural'),'!Pechwand':('a pech wand','una vara pech'),'!TOTEMUNDYING':('a totem of undying','un tótem de inmortalidad'),'!FluxRift':('a flux rift','una grieta de flux')}
for mat,en,esname in [('IRON','iron','hierro'),('CLAY','clay','arcilla'),('BRASS','brass','latón'),('THAUMIUM','thaumium','taumio'),('VOID','void metal','metal del vacío')]:goals['f_MAT'+mat]=(en,esname)
for locale,index in [('en_us',0),('es_es',1)]:
 p=A/f'lang/{locale}.json';lang=read(p)
 for old,modern in entity_names.items():lang['entity.'+old+'.name']=lang.get('entity.thaumcraft.'+modern,modern.replace('_',' '))
 for key,pair in goals.items():lang['research.thaumcraft.goal.'+key.lstrip('!')]=pair[index]
 lang['research.thaumcraft.goal_stage']=('%s (stage %s)','%s (etapa %s)')[index]
 lang['research.thaumcraft.addendum']=('Additional findings','Hallazgos adicionales')[index]
 save(p,lang)

for ore in ['copper_ore','deepslate_copper_ore']:
 save(D/('crucible/metal_purification_'+ore+'.json'),{'type':'thaumcraft:crucible','research':'METALPURIFICATION','catalyst':'minecraft:'+ore,'aspects':{'metallum':5,'ordo':5},'result':{'id':'thaumcraft:cluster_copper'}})

p=A/'knowledge/aspects.json';v=read(p);v['triggers']['entities'].pop('thaumcraft:fire_bat',None)
for id in ['minecraft:small_fireball','minecraft:dragon_fireball']:v['triggers']['entities'][id]=['f_fireball']
save(p,v)
p=D/'arcane/sanity_checker.json';v=read(p);v['research']='WARP';save(p,v)
p=A/'knowledge/aspects.json';v=read(p)
v.setdefault('complex',{}).update({'minecraft:torch':{'lux':5},'minecraft:cauldron':{'alkimia':15},'minecraft:fermented_spider_eye':{'alkimia':5},'minecraft:blaze_powder':{'alkimia':5},'minecraft:glistering_melon_slice':{'alkimia':5},'minecraft:magma_cream':{'alkimia':5},'minecraft:golden_carrot':{'sensus':10,'alkimia':5},'minecraft:paper':{'cognitio':2},'minecraft:bookshelf':{'cognitio':8},'minecraft:chest':{'vacuos':15}})
save(p,v)
# Earlier golem ports appended advanced recipes to the base page as a temporary substitute for addenda.
p=A/'knowledge/research.json';entries=read(p)
for entry in entries:
 gated={ref for addendum in entry.get('addenda',[]) if addendum.get('required_research') for ref in addendum.get('recipes',[])}
 for stage in entry['stages']:
  if 'recipes' in stage:stage['recipes']=[ref for ref in stage['recipes'] if ref not in gated]
save(p,entries)
