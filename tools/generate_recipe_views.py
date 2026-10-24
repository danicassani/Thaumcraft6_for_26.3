#!/usr/bin/env python3
"""Graphical book recipes derived from the exact modern recipe data; legacy names are aliases only."""
import json,re
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft/recipe'
def norm(s):return re.sub('[^a-z0-9]','',s.split(':')[-1].lower())
# The original beginner building recipes are ordinary crafting recipes.
def save_recipe(name,value):
 p=D/(name+'.json');p.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n')
save_recipe('stone_arcane',{'type':'minecraft:crafting_shaped','pattern':['SSS','SCS','SSS'],'key':{'S':['minecraft:stone','minecraft:andesite','minecraft:diorite','minecraft:granite'],'C':'thaumcraft:crystal_essence'},'result':{'id':'thaumcraft:stone_arcane','count':9}})
save_recipe('stone_arcane_brick',{'type':'minecraft:crafting_shaped','pattern':['SS','SS'],'key':{'S':'thaumcraft:stone_arcane'},'result':{'id':'thaumcraft:stone_arcane_brick','count':4}})
views={};byname={}
for p in sorted(D.rglob('*.json')):
 d=json.loads(p.read_text());r=d.get('recipe',d);
 if r['type']=='thaumcraft:jar_void_upgrade':r={'type':'minecraft:crafting_shapeless','ingredients':['thaumcraft:jar_normal'],'result':{'id':'thaumcraft:jar_void'}}
 if r['type']=='thaumcraft:essentia_buffer':r={'type':'minecraft:crafting_shaped','pattern':['PVP','TIT','PRP'],'key':{'P':'thaumcraft:phial','V':'thaumcraft:tube_valve','T':'thaumcraft:tube','I':'thaumcraft:plate_iron','R':'thaumcraft:tube_restrict'},'result':{'id':'thaumcraft:tube_buffer'}}
 result=r.get('result',d.get('result'))
 if not isinstance(result,dict):continue
 v={'kind':d['type'].split(':')[-1],'result':result,'grid':[],'aspects':d.get('aspects',{}),'crystals':d.get('crystals',{}),'vis':d.get('vis',0),'research':d.get('research','')}
 if 'pattern' in r:
  v['grid']=[r['key'].get(row[x],None) if x<len(row) else None for row in (r['pattern']+['']*3)[:3] for x in range(3)]
 elif 'ingredients' in r:v['grid']=(r['ingredients']+[None]*9)[:9]
 elif 'catalyst' in d:v['grid']=[None]*4+[d['catalyst']]+[None]*4
 elif 'components' in d:v['grid']=(d['components']+[None]*9)[:9];v['input']=d.get('input');v['instability']=d.get('instability',0)
 elif 'ingredient' in r:v['grid']=[None]*4+[r['ingredient']]+[None]*4
 else:continue
 if 'infusion_enchantment' in d:
  v['infusion_enchantment']=d['infusion_enchantment']
  if d['infusion_enchantment'] in ['arcing','burrowing','collector','destructive','essence','lamplight','refining','sounding']:v['input']={'item':'minecraft:diamond_sword' if d['infusion_enchantment'] in ['arcing','essence'] else 'minecraft:diamond_pickaxe'}
 key='thaumcraft:'+str(p.relative_to(D).with_suffix(''));views[key]=v;byname.setdefault(norm(p.stem),key)
 for alias in [norm(result['id']),norm(result['id'].split(':')[-1])]:byname.setdefault(alias,key)
# Dynamic and world-interaction recipes have explicit diagrams rather than fabricated recipe IDs.
def process(output,base):return {'kind':'salis_conversion','result':{'id':output},'grid':[None]*4+[base]+[None]*4,'input':'thaumcraft:salis_mundus'}
for out,base in [('thaumcraft:thaumonomicon','minecraft:bookshelf'),('thaumcraft:arcane_workbench','minecraft:crafting_table'),('thaumcraft:crucible','minecraft:cauldron')]:views[out]=process(out,base);byname[norm(out)]=out
salis={'kind':'salis_mundus','result':{'id':'thaumcraft:salis_mundus'},'grid':[{'id':'thaumcraft:crystal_essence','aspect':a} for a in ['aer','ignis','aqua']]+['minecraft:redstone','minecraft:flint','minecraft:bowl',None,None,None]}
views['thaumcraft:salis_mundus']=salis;byname['salismundusfake']='thaumcraft:salis_mundus'
# The book retains old TC6 references; all aliases resolve to current registered recipes.
aliases={'BrickArcane':'stone_arcane_brick','MnemonicMatrix':'brain_box','SealCollect':'seal_pickup','SealCollectAdv':'seal_pickup_advanced','SealStoreAdv':'seal_stock','AdvAlchemyConstruct':'metal_alchemical_advanced','AlchemicalConstruct':'metal_alchemical','AncientPedestal':'pedestal_ancient','EldritchPedestal':'pedestal_eldritch','ArcanePedestal':'pedestal_arcane','ArcaneLamp':'lamp_arcane','ArcaneSpa':'spa','AutomatedCrossbow':'turret_basic','AdvancedCrossbow':'turret_advanced','ArcaneBore':'arcane_bore','BootsTraveller':'traveller_boots','BrassBrace':'baubles','CLOUDRING':'ring_cloud','CuriosityBand':'band_curiosity','EssentiaSmelter':'smelter_basic','EssentiaSmelterThaumium':'smelter_thaumium','EssentiaSmelterVoid':'smelter_void','EssentiaTransportIn':'mirror_essentia','EssentiaTransportOut':'mirror_essentia','HelmGoggles':'fortress_goggles','JarLabel':'label','JarVoid':'jar_void','MaskAngryGhost':'mask_0','MaskGrinningDevil':'mask_1','MaskSippingFiend':'mask_2','MatrixMotion':'matrix_speed','MnemonicMatrix':'thaumatorium_matrix','MirrorHand':'hand_mirror','MorphicResonator':'resonator','PaveBarrier':'paving_stone_barrier','PaveTravel':'paving_stone_travel','RedstoneInlay':'inlay','RobeBoots':'cloth_boots','RobeChest':'cloth_chest','RobeLegs':'cloth_legs','SaneSoap':'sanity_soap','SealBreak':'seal_breaker','SealBreakAdv':'seal_breaker_advanced','SealCollect':'seal_collect','SealCollectAdv':'seal_collect_advanced','SealEmptyAdv':'seal_empty_advanced','SealGuardAdv':'seal_guard_advanced','SealProvide':'seal_provider','SealStore':'seal_stock','SealStoreAdv':'seal_stock_advanced','ThaumiumFortressChest':'fortress_chest','ThaumiumFortressHelm':'fortress_helm','ThaumiumFortressLegs':'fortress_legs','TubeBuffer':'tube_buffer','VerdantHeart':'charm_verdant','VerdantHeartLife':'charm_verdant_life','VerdantHeartSustain':'charm_verdant_sustain','VisAmulet':'amulet_vis','VisBattery':'vis_battery','VoidseerPearl':'charm_voidseer','WardedJar':'jar_normal','inkwell':'scribing_tools','ironplate':'plate_iron','mirrorglass':'mirrored_glass','modaggression':'module_aggression','modvision':'module_vision','arcane_brick':'stone_arcane_brick','arcane_stone':'stone_arcane'}
aliases.update({'MnemonicMatrix':'brain_box','SealCollect':'seal_pickup','SealCollectAdv':'seal_pickup_advanced','SealStoreAdv':'seal_stock'})
for old,target in aliases.items():
 if norm(target) in byname:byname[norm(old)]=byname[norm(target)]
for old,new in [('Air','aer'),('Fire','ignis'),('Water','aqua'),('Earth','terra'),('Order','ordo'),('Entropy','perditio'),('Flux','vitium')]:
 if norm('crystal_'+new) in byname:byname[norm('CrystalCluster'+old)]=byname[norm('crystal_'+new)]
for ench in ['arcing','burrowing','collector','destructive','essence','lamplight','refining','sounding']:
 byname[norm('IE'+ench+'FAKE')]='thaumcraft:infusion/enchantment_'+ench
for rank in range(3):byname[norm('RunicArmorFake'+str(rank))]='thaumcraft:infusion/runic_shield'
# Group references turn into individual pages, not a raw group ID.
for name,predicate in [('nitorgroup',lambda k,v:v['result']['id'].startswith('thaumcraft:nitor_')),('nitorcolor',lambda k,v:v['result']['id'].startswith('thaumcraft:nitor_')),('viscrystalgroup',lambda k,v:'/vis_crystal_' in k),('thaumium_stuff',lambda k,v:'thaumium' in v['result']['id']),('void_stuff',lambda k,v:'void_' in v['result']['id']),('brass_stuff',lambda k,v:'brass' in v['result']['id'])]:
 choices=[k for k,v in views.items() if 'result' in v and predicate(k,v)]
 if choices:views['thaumcraft:'+name]={'choices':choices}
research=json.loads((A/'knowledge/research.json').read_text());missing=set()
for entry in research:
 for stage in entry['stages']:
  for ref in stage.get('recipes',[]):
   key=byname.get(norm(ref));
   if key and key in views:views.setdefault(ref,views[key])
   elif ref not in views:missing.add(ref)
# Exact old aliases which differ beyond case/underscores.
for alias,target in {'thaumcraft:workbenchcharger':'thaumcraft:arcane/workbench_charger','thaumcraft:rechargepedestal':'thaumcraft:arcane/recharge_pedestal'}.items():
 if target in views:views[alias]=views[target];missing.discard(alias)
p=A/'knowledge/recipe_views.json';p.write_text(json.dumps(views,ensure_ascii=False,indent=2)+'\n')
labels={
 'menu.thaumcraft.vis_available':('Vis: %s','Vis: %s'),'menu.thaumcraft.vis_cost':('Cost: %s','Coste: %s'),
 'menu.thaumcraft.reason.0':('Take the result to craft','Recoge el resultado para fabricar'),'menu.thaumcraft.reason.1':('No matching recipe','No coincide con una receta'),'menu.thaumcraft.reason.2':('Complete the required research','Completa la investigación necesaria'),'menu.thaumcraft.reason.3':('Missing aspect crystals','Faltan cristales de aspecto'),'menu.thaumcraft.reason.4':('Not enough local vis','No hay suficiente vis cercana'),'menu.thaumcraft.reason.5':('Clear the stored output','Retira el resultado almacenado'),
 'research.thaumcraft.need_crucible_research':('Complete First Steps, then reach stage 2 of Basic Alchemy in the Thaumonomicon before making a crucible.','Completa Primeros pasos y alcanza la etapa 2 de Alquimia básica en el Thaumonomicon para crear el crisol.'),
 'research.thaumcraft.recipe_kind.arcane':('Arcane workbench','Mesa de trabajo arcana'),'research.thaumcraft.recipe_kind.crafting_shaped':('Crafting table','Mesa de trabajo'),'research.thaumcraft.recipe_kind.crafting_shapeless':('Shapeless crafting','Fabricación sin forma'),'research.thaumcraft.recipe_kind.crucible':('Crucible','Crisol'),'research.thaumcraft.recipe_kind.infusion':('Infusion altar','Altar de infusión'),'research.thaumcraft.recipe_kind.salis_mundus':('Any three different aspects','Tres aspectos distintos, a elección'),'research.thaumcraft.recipe_kind.salis_conversion':('Use Salis Mundus on the block','Usa Salis Mundus sobre el bloque'),'research.thaumcraft.recipe_kind.smelting':('Furnace','Horno'),
 'research.thaumcraft.recipe_returned':('Bowl and flint are returned','Se devuelven el cuenco y el pedernal'),'research.thaumcraft.recipe_cost':('Vis: %s','Vis: %s'),'research.thaumcraft.process_page':('Follow the process described on the preceding page.','Sigue el proceso descrito en la página anterior.'),'research.thaumcraft.recipe_title':('Recipe','Receta'),
 'research.thaumcraft.points_short':('Obs: %s  Theo: %s','Obs.: %s  Teo.: %s'),'research.thaumcraft.points_progress':('Next observation: %s%%; next theory: %s%%','Próxima observación: %s%%; próxima teoría: %s%%'),
 'research.thaumcraft.points.observation':('Observation','Observación'),'research.thaumcraft.points.theory':('Theory','Teoría'),
 'research.thaumcraft.done':('Done','Hecho'),'research.thaumcraft.goal.gotthaumonomicon':('Open the Thaumonomicon','Abre el Thaumonomicon')}
for lang,i in [('en_us',0),('es_es',1)]:
 p=A/f'lang/{lang}.json';v=json.loads(p.read_text());v.update({k:label[i] for k,label in labels.items()});p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
print(len(views),'graphical recipe aliases;',len(missing),'process references without a modern static recipe')
