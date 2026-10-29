#!/usr/bin/env python3
"""Native spell and equipment assets; original research gates and recipe costs."""
import json,shutil
from loot_schema import for_resource
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft';O=R/'src/main/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(for_resource(p,v),ensure_ascii=False,indent=2)+'\n')
names=['primordial_pearl','primordial_nugget','focus_1','focus_2','focus_3','goggles','traveller_boots','primal_crusher']+[f'{f}_{t}' for f in ['void','elemental'] for t in ['sword','pick','axe','shovel','hoe']]+[f'{f}_{s}' for f in ['cloth','fortress','void','void_robe'] for s in ['helm','chest','legs','boots'] if (f!='cloth' or s!='helm') and (f not in ['fortress','void_robe'] or s!='boots')]
for name in names:
 old='traveller_boots' if name=='traveller_boots' else name
 path=O/f'models/item/{old}.json'
 v=json.loads(path.read_text()) if path.exists() and name!='primordial_pearl' else {'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/'+('primordial_pearl_mote' if old=='primordial_nugget' else old)}}
 save(A/f'models/item/{name}.json',v);save(A/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/'+name}})
for n in ['caster_gauntlet','caster_gauntlet_focus','caster_basic_model','caster_basic_focus_model']:
 save(A/f'models/item/{n}.json',json.loads((O/f'models/item/{n}.json').read_text()))
save(A/'items/caster_basic.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/caster_basic_model'}})
save(A/'models/block/wand_workbench.json',json.loads((O/'models/block/wand_workbench.json').read_text()))
save(A/'blockstates/wand_workbench.json',{'variants':{'':{'model':'thaumcraft:block/wand_workbench'}}})
save(A/'items/wand_workbench.json',{'model':{'type':'minecraft:model','model':'thaumcraft:block/wand_workbench'}})
save(D/'loot_table/blocks/wand_workbench.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:wand_workbench'}]}]})
for name,one,two in [('cloth','robes_1','robes_2'),('goggles','goggles','goggles'),('fortress','fortress_armor','fortress_armor'),('void','void_1','void_2'),('void_robe','void_robe_armor','void_robe_armor'),('traveller','bootstraveler','bootstraveler')]:
 save(A/f'equipment/{name}.json',{'layers':{'humanoid':[{'texture':'thaumcraft:'+name}],'humanoid_leggings':[{'texture':'thaumcraft:'+name}]}})
 for part,old in [('humanoid',one),('humanoid_leggings',two)]:
  p=A/f'textures/entity/equipment/{part}/{name}.png';p.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(O/f'textures/entity/armor/{old}.png',p)
for tag,values in {'repairs_cloth':['thaumcraft:fabric'],'repairs_void':['thaumcraft:ingot_void'],'repairs_brass':['thaumcraft:ingot_brass'],'repairs_leather':['minecraft:leather']}.items():save(D/f'tags/item/{tag}.json',{'replace':False,'values':values})
for t,tag in [('sword','swords'),('pick','pickaxes'),('axe','axes'),('shovel','shovels'),('hoe','hoes')]:
 p=R/f'src/modern/resources/data/minecraft/tags/item/{tag}.json';v=json.loads(p.read_text());v['values']=list(dict.fromkeys(v['values']+[f'thaumcraft:{f}_{t}' for f in ['void','elemental']]+(['thaumcraft:primal_crusher'] if t=='pick' else [])));save(p,v)
def shaped(n,pattern,key):return {'type':'minecraft:crafting_shaped','pattern':pattern,'key':key,'result':{'id':'thaumcraft:'+n}}
def arc(n,research,vis,crystals,pattern,key):save(D/f'recipe/arcane/{n}.json',{'type':'thaumcraft:arcane','research':research,'vis':vis,'crystals':crystals,'recipe':shaped(n,pattern,key)})
arc('wand_workbench','BASEAUROMANCY@2',100,{'terra':1,'aqua':1},['ISI','BRB','GTG'],{'I':'thaumcraft:plate_iron','S':'thaumcraft:slab_arcane_stone','B':'thaumcraft:stone_arcane','R':'thaumcraft:vis_resonator','G':'minecraft:gold_ingot','T':'thaumcraft:table_stone'})
arc('caster_basic','UNLOCKAUROMANCY@2',100,dict.fromkeys(['aer','ignis','aqua','terra','ordo','perditio'],1),['III','LRL','LTL'],{'I':'minecraft:iron_ingot','L':'minecraft:leather','R':'thaumcraft:vis_resonator','T':'thaumcraft:thaumometer'})
arc('goggles','UNLOCKARTIFICE',50,{},['LGL','L L','TGT'],{'L':'minecraft:leather','G':'thaumcraft:ingot_brass','T':'thaumcraft:thaumometer'})
for s,pat in [('chest',['I I','III','III']),('legs',['III','I I','I I']),('boots',['I I','I I'])]:arc('cloth_'+s,'UNLOCKINFUSION',100,{},pat,{'I':'thaumcraft:fabric'})
for s,pat in [('helm',['III','I I']),('chest',['I I','III','III']),('legs',['III','I I','I I']),('boots',['I I','I I'])]:save(D/f'recipe/void_{s}.json',shaped('void_'+s,pat,{'I':'thaumcraft:ingot_void'}))
for t,pat in [('sword',['I','I','S']),('pick',['III',' S ',' S ']),('axe',['II','SI','S ']),('shovel',['I','S','S']),('hoe',['II','S ','S '])]:save(D/f'recipe/void_{t}.json',shaped('void_'+t,pat,{'I':'thaumcraft:ingot_void','S':'minecraft:stick'}))
save(D/'recipe/crucible/focus_1.json',{'type':'thaumcraft:crucible','research':'UNLOCKAUROMANCY','catalyst':'thaumcraft:crystal_essence','crystal_aspect':'ordo','aspects':{'vitreus':20,'praecantatio':10,'auram':5},'result':{'id':'thaumcraft:focus_1'}})
def ing(n):return n if isinstance(n,dict) else {'item':n if ':' in n else 'thaumcraft:'+n}
def inf(n,research,base,parts,aspects,inst=1):save(D/f'recipe/infusion/{n}.json',{'type':'thaumcraft:infusion','research':research,'input':ing(base),'components':list(map(ing,parts)),'aspects':aspects,'instability':inst,'result':{'id':'thaumcraft:'+n}})
def crystal(a):return {'item':'thaumcraft:crystal_essence','crystal_aspect':a}
inf('focus_2','FOCUSADVANCED@1','focus_1',['quicksilver','minecraft:diamond','quicksilver','minecraft:ender_pearl'],{'praecantatio':25,'ordo':50},3)
inf('focus_3','FOCUSGREATER@1','focus_2',['quicksilver','primordial_pearl','quicksilver','minecraft:nether_star'],{'praecantatio':25,'ordo':50,'vacuos':100},5)
for t,a,cost in [('axe','aqua',{'aqua':60,'herba':30}),('pick','ignis',{'ignis':30,'metallum':30,'sensus':30}),('sword','aer',{'aer':30,'motus':30,'aversio':30}),('shovel','terra',{'terra':60,'fabrico':30}),('hoe','ordo',{'ordo':30,'herba':30,'perditio':30})]:inf('elemental_'+t,'ELEMENTALTOOLS','thaumium_'+t,[crystal(a),crystal('perditio' if t=='hoe' else a),'primordial_nugget','plank_greatwood'],cost)
inf('traveller_boots','BOOTSTRAVELLER','minecraft:leather_boots',[crystal('aer'),crystal('aer'),'fabric','fabric','minecraft:feather','minecraft:cod'],{'volatus':100,'motus':100})
for s,count,protect in [('helm',2,20),('chest',4,30),('legs',3,25)]:inf('fortress_'+s,'ARMORFORTRESS','thaumium_'+s,['plate_thaumium']*count+(['minecraft:gold_ingot','minecraft:gold_ingot','minecraft:emerald'] if s=='helm' else ['minecraft:gold_ingot','minecraft:leather']),{'metallum':50,'praemunio':protect,'potentia':25},3)
for s,m in [('helm',25),('chest',35),('legs',30)]:inf('void_robe_'+s,'VOIDROBEARMOR','void_'+s,(['goggles','fabric','fabric','salis_mundus','fabric','fabric'] if s=='helm' else ['cloth_'+s,'plate_void','plate_void','salis_mundus','fabric','minecraft:leather']),{'metallum':m,'praemunio':m,'potentia':25,'alienis':25,'vacuos':m,**({'sensus':25} if s=='helm' else {})},6)
inf('primal_crusher','PRIMALCRUSHER','primordial_pearl',['void_pick','void_shovel','elemental_pick','elemental_shovel'],{'terra':75,'instrumentum':75,'perditio':50,'vacuos':50,'aversio':50,'alienis':50,'desiderium':50},6)
v={'type':'thaumcraft:infusion','research':'RUNICSHIELDING','input':{'item':'minecraft:leather_chestplate'},'components':[ing('salis_mundus'),ing('amber')],'aspects':{'praemunio':40,'vitreus':20,'potentia':20},'instability':5,'result':{'id':'minecraft:book'},'infusion_enchantment':'runic'};save(D/'recipe/infusion/runic_shield.json',v)
labels={'caster.thaumcraft.no_focus':('Equip a programmed focus: sneak use with a focus in the off hand.','Equipa un foco programado: usa agachado con el foco en la mano secundaria.'),'caster.thaumcraft.no_vis':('Not enough aura vis.','No hay suficiente vis en el aura.'),'equipment.thaumcraft.runic':('Runic shielding +%s','Escudo rúnico +%s'),'equipment.thaumcraft.discount':('Vis discount: %s%%','Descuento de vis: %s%%'),'equipment.thaumcraft.repair':('Repairs itself over time','Se repara con el tiempo'),'equipment.thaumcraft.shield':('Runic shield: %s / %s','Escudo rúnico: %s / %s'),'block.thaumcraft.wand_workbench':('Focal Manipulator','Manipulador de focos'),'focal.thaumcraft.complexity':('Complexity %s / %s • %s effects','Complejidad %s / %s • %s efectos'),'focal.thaumcraft.progress':('Vis: %s / %s','Vis: %s / %s')}
for n,en,es in [('craft','Craft','Crear'),('medium','Medium','Medio'),('effect','Effect','Efecto'),('power','Power','Potencia'),('duration','Duration','Duración'),('add','Add effect','Añadir efecto'),('remove','Remove last','Quitar último')]:labels['focal.thaumcraft.'+n]=(en,es)
for n,en,es in [('touch','Touch','Toque'),('bolt','Bolt','Rayo'),('projectile','Projectile','Proyectil'),('fire','Fire','Fuego'),('air','Air','Aire'),('earth','Earth','Tierra'),('frost','Frost','Hielo'),('flux','Flux','Polución'),('heal','Heal','Curación'),('break','Break','Rotura'),('exchange','Exchange','Intercambio'),('curse','Curse','Maldición')]:labels['focus.thaumcraft.'+n]=(en,es)
for name in names+['caster_basic']:labels['item.thaumcraft.'+name]=(name.replace('_',' ').title(),name.replace('_',' ').title())
for i,loc in enumerate(['en_us','es_es']):
 p=A/f'lang/{loc}.json';v=json.loads(p.read_text());v.update({k:values[i] for k,values in labels.items()});save(p,v)
# Resolve the original recipe page names used by research stages.
p=A/'knowledge/recipes.json';pages=json.loads(p.read_text());aliases={'caster_basic':'CasterBasic','wand_workbench':'FocalManipulator','focus_1':'Focus1','focus_2':'Focus2','focus_3':'Focus3','goggles':'Goggles','traveller_boots':'BootsTraveller','primal_crusher':'PrimalCrusher'}
for recipe in list((D/'recipe/arcane').glob('*.json'))+list((D/'recipe/infusion').glob('*.json')):
 if recipe.stem not in names+['caster_basic','wand_workbench','runic_shield']:continue
 r=json.loads(recipe.read_text());lines=['Research: '+r['research']]
 if 'recipe' in r:lines += ['['+line+']' for line in r['recipe']['pattern']]+[k+' = '+str(v) for k,v in r['recipe']['key'].items()]+['Vis: '+str(r['vis']),'Crystals: '+str(r['crystals'])]
 else:lines += ['Center: '+str(r['input']),'Offerings: '+str(r['components']),'Essentia: '+str(r['aspects'])]
 pages['thaumcraft:'+aliases.get(recipe.stem,''.join(s.title() for s in recipe.stem.split('_')))]=lines
pages['thaumcraft:RunicShielding']=['Center: any equippable armor.','Offerings: Salis Mundus + amber × next shield rank.','Essentia: praemunio = 20 + 20 × 2^current rank; vitreus and potentia = half.','Ranks 1–9; preserves the actual armor stack and its components.']
save(p,pages)
for recipe in list((D/'recipe/arcane').glob('*.json'))+list((D/'recipe/infusion').glob('*.json')):
 if recipe.stem in names+['caster_basic','wand_workbench']:pages['thaumcraft:'+recipe.stem]=pages.get('thaumcraft:'+aliases.get(recipe.stem,''.join(s.title() for s in recipe.stem.split('_'))),[])
save(p,pages)
print('Thaumaturgy and equipment: generated assets and recipes')

# Stone table is an ordinary crafted block, required by the original focal manipulator recipe.
save(A/'models/block/table_stone.json',json.loads((O/'models/block/table_stone.json').read_text()))
save(A/'blockstates/table_stone.json',{'variants':{'':{'model':'thaumcraft:block/table_stone'}}})
save(A/'items/table_stone.json',{'model':{'type':'minecraft:model','model':'thaumcraft:block/table_stone'}})
save(D/'loot_table/blocks/table_stone.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:table_stone'}]}]})
save(D/'recipe/table_stone.json',shaped('table_stone',['SSS',' P ',' P '],{'S':'thaumcraft:slab_arcane_stone','P':'thaumcraft:stone_arcane'}))
save(D/'recipe/primordial_pearl.json',shaped('primordial_pearl',['NNN','NNN','NNN'],{'N':'thaumcraft:primordial_nugget'}))
save(D/'recipe/primordial_nugget.json',{'type':'minecraft:crafting_shapeless','ingredients':['thaumcraft:primordial_pearl'],'result':{'id':'thaumcraft:primordial_nugget','count':9}})

for n,texture in [('amulet_vis','amulet_vis'),('charm_verdant','verdant_charm'),('charm_verdant_life','verdant_charm'),('charm_verdant_sustain','verdant_charm'),('charm_voidseer','voidseer_charm'),('band_curiosity','curiosity_band'),('ring_cloud','cloud_ring'),('charm_undying','charm_undying'),('sanity_checker','sanity_checker')]:
 old=O/f'models/item/{n}.json'
 v=json.loads(old.read_text()) if old.exists() else {'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/'+texture}}
 if 'overrides' in v:v={'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/'+texture}}
 save(A/f'models/item/{n}.json',v);save(A/f'items/{n}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/'+n}})
inf('ring_cloud','CLOUDRING','minecraft:gold_ingot',[crystal('aer'),'minecraft:feather'],{'aer':50})
inf('charm_undying','CHARMUNDYING','minecraft:totem_of_undying',['plate_brass'],{'victus':25},2)
inf('charm_verdant','VERDANTCHARMS','minecraft:gold_ingot',['primordial_nugget',crystal('victus'),'minecraft:milk_bucket',crystal('herba')],{'victus':60,'ordo':30,'herba':60},5)
inf('charm_verdant_life','VERDANTCHARMS','charm_verdant',['minecraft:golden_apple',crystal('victus'),crystal('humanus')],{'victus':80,'humanus':80},5)
inf('charm_verdant_sustain','VERDANTCHARMS','charm_verdant',['triple_meat_treat',crystal('desiderium'),crystal('aer')],{'desiderium':80,'aer':80},5)
inf('amulet_vis','VISAMULET','minecraft:gold_ingot',['vis_resonator']+[crystal(a) for a in ['aer','ignis','aqua','terra','ordo']],{'auram':50,'potentia':100,'vacuos':50},6)
inf('charm_voidseer','VOIDSEERPEARL','minecraft:gold_ingot',['brain','void_seed','brain','primordial_pearl'],{'cognitio':150,'vacuos':150,'praecantatio':100},8)
inf('band_curiosity','CURIOSITYBAND','minecraft:gold_ingot',['minecraft:emerald','minecraft:writable_book']*4,{'cognitio':150,'vacuos':50,'vinculum':100},5)
arc('sanity_checker','SANITYCHECKER',5,{},[' M ','BPB',' B '],{'B':'thaumcraft:ingot_brass','P':'thaumcraft:brain','M':'thaumcraft:mirrored_glass'})

# Names and recipe pages for the native off-hand adaptation.
for loc in ['en_us','es_es']:
 p=A/f'lang/{loc}.json';v=json.loads(p.read_text());v['equipment.thaumcraft.warp']='Warp: %s permanent / %s normal / %s temporary' if loc=='en_us' else 'Distorsión: %s permanente / %s normal / %s temporal'
 v['block.thaumcraft.table_stone']='Stone Table' if loc=='en_us' else 'Mesa de piedra'
 for n in ['amulet_vis','charm_verdant','charm_verdant_life','charm_verdant_sustain','charm_voidseer','band_curiosity','ring_cloud','charm_undying','sanity_checker']:v['item.thaumcraft.'+n]=n.replace('_',' ').title()
 save(p,v)
p=A/'knowledge/recipes.json';v=json.loads(p.read_text())
for recipe in list((D/'recipe/infusion').glob('*.json'))+list((D/'recipe/arcane').glob('*.json')):
 r=json.loads(recipe.read_text());key='thaumcraft:'+recipe.stem
 if recipe.stem in ['amulet_vis','charm_verdant','charm_verdant_life','charm_verdant_sustain','charm_voidseer','band_curiosity','ring_cloud','charm_undying','sanity_checker']:v[key]=['Research: '+r['research']]+(['Center: '+str(r['input']),'Offerings: '+str(r['components']),'Essentia: '+str(r['aspects'])] if 'components' in r else ['Vis: '+str(r['vis']),'Ingredients: '+str(r['recipe']['key'])])+['Accessories are equipped in the off hand. Right click to equip.']
save(p,v)

for i,loc in enumerate(['en_us','es_es']):
 p=A/f'lang/{loc}.json';v=json.loads(p.read_text());v.update({k:text[i] for k,text in {'focus.thaumcraft.details':('%s • Complexity: %s • Vis: %s','%s • Complejidad: %s • Vis: %s'),'focus.thaumcraft.materials':('XP levels: %s • Crystals: %s','Niveles de experiencia: %s • Cristales: %s'),'equipment.thaumcraft.accessory':('Right click to equip in the off hand. Only equipped accessories are active.','Clic derecho para equipar en la mano secundaria. Solo funciona equipado.'),'equipment.thaumcraft.charge':('Vis charge: %s / %s','Carga de vis: %s / %s')}.items()});save(p,v)

for name in ['caster_basic','caster_advanced']:
 target=A/f'textures/items/{name}_skin.png';target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(O/f'textures/entity/armor/{name}.png',target)
for model in ['caster_gauntlet','caster_gauntlet_focus','caster_basic_model','caster_basic_focus_model']:
 p=A/f'models/item/{model}.json';v=json.loads(p.read_text());v.pop('__createdwith',None);textures=v.setdefault('textures',{});textures['skin']='thaumcraft:items/'+('caster_advanced' if model.startswith('caster_gauntlet') else 'caster_basic')+'_skin';textures['particle']='#skin';save(p,v)

save(D/'tags/block/mineable/primal_crusher.json',{'replace':False,'values':['#minecraft:mineable/pickaxe','#minecraft:mineable/shovel']})

for i,loc in enumerate(['en_us','es_es']):
 p=A/f'lang/{loc}.json';v=json.loads(p.read_text());v.update({k:t[i] for k,t in {'equipment.thaumcraft.protection_requirement':('Item enchanted with Protection I or higher','Objeto con Protección I o superior'),'equipment.thaumcraft.silk_requirement':('Item enchanted with Silk Touch','Objeto con Toque de seda'),'equipment.thaumcraft.fortune_requirement':('Item enchanted with Fortune I or higher','Objeto con Fortuna I o superior')}.items()});save(p,v)

p=A/'knowledge/recipes.json';v=json.loads(p.read_text())
for old,new in {'ThaumiumFortressHelm':'FortressHelm','ThaumiumFortressChest':'FortressChest','ThaumiumFortressLegs':'FortressLegs','CHARMUNDYING':'charm_undying','CLOUDRING':'ring_cloud','CuriosityBand':'band_curiosity','VerdantHeart':'charm_verdant','VerdantHeartLife':'charm_verdant_life','VerdantHeartSustain':'charm_verdant_sustain','VisAmulet':'amulet_vis','VoidseerPearl':'charm_voidseer','RobeChest':'ClothChest','RobeLegs':'ClothLegs','RobeBoots':'ClothBoots'}.items():v['thaumcraft:'+old]=v['thaumcraft:'+new]
for n in range(3):v['thaumcraft:RunicArmorFake'+str(n)]=v['thaumcraft:RunicShielding']
v['thaumcraft:tablestone']=['[SSS]','[ P ]','[ P ]','S = Arcane Stone Slab','P = Arcane Stone']
for recipe in (D/'recipe').glob('void_*.json'):
 r=json.loads(recipe.read_text())
 if 'pattern' not in r:continue
 v['thaumcraft:'+recipe.stem.replace('_','')]=['['+row+']' for row in r['pattern']]+[key+' = '+value for key,value in r['key'].items()]
save(p,v)

# Advanced focus editor and persistent delivery media.
labels={
 'focus.thaumcraft.cloud':('Cloud','Nube'),'focus.thaumcraft.mine':('Mine','Mina'),
 'focus.thaumcraft.none':('No modifier','Sin modificador'),'focus.thaumcraft.scatter':('Scatter','Dispersión'),
 'focus.thaumcraft.split_target':('Split target','Dividir impacto'),'focus.thaumcraft.split_trajectory':('Split trajectory','Dividir trayectoria'),
 'focal.thaumcraft.page':('Setup','Más'),'focal.thaumcraft.modifier':('Modifier','Modificador'),
 'focal.thaumcraft.branch':('Path','Rama'),'focal.thaumcraft.path':('Editing path %s','Editando rama %s'),
 'focal.thaumcraft.forks':('Forks','Rayos'),'focal.thaumcraft.cone':('Cone','Cono'),'focal.thaumcraft.radius':('Radius','Radio'),
 'focal.thaumcraft.seconds':('Seconds','Segundos'),'focal.thaumcraft.target':('Target','Objetivo'),
 'focal.thaumcraft.friend':('Ally','Aliado'),'focal.thaumcraft.enemy':('Foe','Hostil'),
 'focal.thaumcraft.field_settings':('Radius %s • Duration %s seconds','Radio %s • Duración %s segundos'),
 'focal.thaumcraft.inventory_crystals':('Crystals are taken from the table, then your inventory.','Se usan los cristales de la mesa y después los del inventario.')
}
for i,loc in enumerate(['en_us','es_es']):
 p=A/f'lang/{loc}.json';v=json.loads(p.read_text());v.update({k:text[i] for k,text in labels.items()});save(p,v)

# Plan medium (port.16): persistent per-focus dimensions.
for locale,values in {
'en_us':{'focus.thaumcraft.plan':'Plan','focal.thaumcraft.plan_x':'X / Height','focal.thaumcraft.plan_y':'Y','focal.thaumcraft.plan_z':'Z / Width','focal.thaumcraft.plan_mode':'Plan mode','focal.thaumcraft.plan_full':'Full','focal.thaumcraft.plan_surface':'Surface','focal.thaumcraft.plan_settings':'Plan: X %s / Y %s / Z %s • %s','research.FOCUSPLAN.stage.2':'Plan selects blocks up to 16 blocks away. On the third Setup page of the Focal Manipulator, set X/Y/Z radii from 0 to 3 and Full or Surface mode before crafting. Full extends into the hit face. Surface follows matching, exposed (non-opaque neighbor), connected blocks on that face; X and Z control its two dimensions and Y is unused. The held gauntlet previews the geometric selection; server permissions and hardness still decide each effect. Plan supports Split Target, but does not combine with other media or Scatter in this port.'},
'es_es':{'focus.thaumcraft.plan':'Plan','focal.thaumcraft.plan_x':'X / Alto','focal.thaumcraft.plan_y':'Y','focal.thaumcraft.plan_z':'Z / Ancho','focal.thaumcraft.plan_mode':'Modo de plan','focal.thaumcraft.plan_full':'Volumen','focal.thaumcraft.plan_surface':'Superficie','focal.thaumcraft.plan_settings':'Plan: X %s / Y %s / Z %s • %s','research.FOCUSPLAN.stage.2':'Plan selecciona bloques hasta 16 bloques. En la tercera página Más del manipulador, configura radios X/Y/Z de 0 a 3 y el modo Volumen o Superficie antes de crear el foco. Volumen se extiende hacia dentro de la cara apuntada. Superficie sigue bloques contiguos, expuestos junto a vecinos no opacos y de estado idéntico en ese plano; X y Z controlan sus dos dimensiones y Y no se utiliza. El guantelete muestra la selección geométrica; el servidor aplica permisos y dureza en cada efecto. Plan permite Split Target; en este port no se combina con otros medios ni con Scatter.'}
}.items():
    path=A/'lang'/f'{locale}.json'
    lang=json.loads(path.read_text());lang.update(values);path.write_text(json.dumps(lang,ensure_ascii=False,indent=2)+'\n')

# Spellbat and Rift (port.17). The temporary block has no item or recipe.
save(A/'blockstates/hole.json',{'variants':{'':{'model':'minecraft:block/air'}}})
save(D/'tags/block/rift_immune.json',{'replace':False,'values':['minecraft:bedrock','minecraft:barrier','minecraft:end_portal_frame','minecraft:reinforced_deepslate','thaumcraft:hole']})
labels={
'focus.thaumcraft.spellbat':('Spellbat','Murciélago de hechizo'),
'focus.thaumcraft.rift':('Rift','Agujero temporal'),
'focal.thaumcraft.depth':('Depth','Profundidad'),
'entity.thaumcraft.focus_spellbat':('Spellbat','Murciélago de hechizo'),
'block.thaumcraft.hole':('Temporary rift','Agujero temporal'),
'research.FOCUSSPELLBAT.stage.2':('Unlock through Mine and scans of a bat and a blaze (native substitute for the unported firebat). Spellbats seek visible targets within 12 blocks. Configure friend/enemy on the second Setup page. Each bat applies effects at 0.33 power every two seconds, consumes one health per strike and has at most five strikes and 30 seconds of world time. Friendly healing bats seek injured allies. No effects while the owner is absent. This port uses the native bat model with magic particles.','Se desbloquea tras Mine y escanear un murciélago y un blaze (sustituto nativo del murciélago de fuego pendiente). Los murciélagos buscan objetivos visibles hasta 12 bloques. Configura amigos/enemigos en la segunda página Más. Aplican efectos con potencia 0,33 cada dos segundos, gastan una vida por uso y tienen como máximo cinco usos y 30 segundos de tiempo del mundo. Los curativos amigos buscan aliados heridos. No actúan sin su propietario. Este port utiliza el modelo nativo de murciélago con partículas mágicas.'),
'research.FOCUSRIFT.stage.2':('Rift temporarily opens a 3 by 3 tunnel. The power button selects depth 8/16/24/32 and duration selects 2–10 seconds. Blocks restore their exact state when the deadline passes, including after save/restart or chunk reload. Only full opaque solid blocks without inventories, fluids, falling behavior or pistons are eligible; protected or immune central blocks stop the tunnel. No drops. Maximum 288 journaled blocks per owner and 1024 per dimension.','Rift abre temporalmente un túnel de 3 por 3. El botón de potencia selecciona profundidad 8/16/24/32 y la duración permite 2–10 segundos. Los bloques recuperan su estado exacto al vencer el plazo, incluso tras guardar/reiniciar o recargar chunks. Solo admite bloques sólidos opacos completos sin inventarios, fluidos, caída ni pistones; un bloque central protegido o inmune detiene el túnel. No genera drops. Máximo 288 bloques registrados por propietario y 1024 por dimensión.')
}
for i,loc in enumerate(['en_us','es_es']):
 p=A/f'lang/{loc}.json';v=json.loads(p.read_text());v.update({k:text[i] for k,text in labels.items()});save(p,v)

# Runtime focus options and rebindable caster controls (port.18).
focus_options={
 'focal.thaumcraft.speed':('Speed','Velocidad'),
 'focal.thaumcraft.option':('Projectile option','Opción de proyectil'),
 'focal.thaumcraft.fortune':('Fortune','Fortuna'),
 'focal.thaumcraft.silk':('Silk touch','Toque de seda'),
 'focal.thaumcraft.silk_yes':('Silk: yes','Seda: sí'),
 'focal.thaumcraft.silk_no':('Silk: no','Seda: no'),
 'focal.thaumcraft.projectile_0':('Normal','Normal'),
 'focal.thaumcraft.projectile_1':('Bouncy','Rebote'),
 'focal.thaumcraft.projectile_2':('Seek enemies','Busca enemigos'),
 'focal.thaumcraft.projectile_3':('Seek allies','Busca aliados'),
 'key.thaumcraft.plan_resize':('Resize Plan / Ctrl: change axis','Redimensionar Plan / Ctrl: cambiar eje'),
 'key.category.thaumcraft.caster':('Thaumcraft caster','Guantelete de Thaumcraft'),
 'caster.thaumcraft.plan_size':('Plan: X %s / Y %s / Z %s • %s','Plan: X %s / Y %s / Z %s • %s'),
 'caster.thaumcraft.axis_0':('All axes','Todos los ejes'),
 'caster.thaumcraft.axis_1':('X axis','Eje X'),
 'caster.thaumcraft.axis_2':('Z axis','Eje Z'),
 'caster.thaumcraft.axis_3':('Y axis','Eje Y'),
 'research.FOCUSPROJECTILE.stage.2':('Projectile carries the complete focus effects. On the fourth Setup page choose speed 1–5 (one third to five thirds blocks per tick), normal, bouncy, seek enemies or seek allies. Special options require completed Projectile research. Bouncy shots reflect from solid faces with damping. Seeking checks sight, teams, pets and PVP; healing remains safe for allies. Projectiles expire after at most one minute; older unchanged shots keep their original speed and lifetime.','Proyectil transporta los efectos completos del foco. En la cuarta página Más elige velocidad 1–5 (de un tercio a cinco tercios de bloque por tick), normal, rebote, búsqueda de enemigos o aliados. Las opciones especiales requieren completar Proyectil. El rebote pierde velocidad. La búsqueda comprueba visibilidad, equipos, mascotas y PVP. Duran como máximo un minuto; los focos antiguos conservan su velocidad y duración originales.'),
 'research.FOCUSBREAK.stage.2':('Break gradually cracks and mines blocks. Harder blocks take longer; higher power speeds up the work. Each completed block also needs local aura: 0.25 vis, plus 0.25 for Silk Touch and 0.1 per Fortune level. Plan staggers its targets. Fortune 0–IV and Silk Touch are on the fourth Setup page and increase complexity. Native loot, experience and Forge protections apply; Silk takes priority over Fortune. Work stops if the block changes or its aura runs out. Containers can be mined: chests spill their contents and shulker boxes retain them in the dropped item. Unrecognized data remains protected.','Rotura agrieta y mina los bloques poco a poco. Los bloques duros tardan más; aumentar la potencia acelera el trabajo. Cada bloque terminado necesita además aura local: 0,25 vis, más 0,25 por Toque de seda y 0,1 por nivel de Fortuna. Plan escalona sus objetivos. Fortuna 0–IV y Seda se eligen en la cuarta página Más y aumentan la complejidad. Se respetan el botín, la experiencia y las protecciones de Forge; Seda tiene prioridad sobre Fortuna. El trabajo se cancela si cambia el bloque o se agota su aura. Puede romper contenedores: los cofres sueltan su contenido y las shulkers lo conservan dentro del objeto. Los datos desconocidos quedan protegidos.'),
 'caster.thaumcraft.exchange_selected':('Sample: %s','Muestra: %s'),
 'research.FOCUSEXCHANGE.stage.2':('Sneak-use the gauntlet on a block to select its sample. Exchange takes matching blocks from your inventory, returns the removed block loot, and pays local aura per replacement: 0.25 vis, plus 0.25 for Silk Touch and 0.1 per Fortune level. The sample icon appears beneath the aura display. Without a saved sample, blocks in the off hand still work. Fortune 0–IV and Silk Touch are on the fourth Setup page. Protected or unsupported replacements leave the original intact and consume nothing. Containers preserve their loot and the consumed item data. A sample stores only the block type, never its contents. Multi-position structures and unrecognized data remain excluded.','Agáchate y usa el guantelete sobre un bloque para elegirlo como muestra. Intercambio toma bloques iguales del inventario, recupera el botín del bloque retirado y paga aura local por sustitución: 0,25 vis, más 0,25 por Seda y 0,1 por nivel de Fortuna. La muestra aparece bajo el indicador de aura. Sin una muestra guardada se siguen usando los bloques de la mano secundaria. Fortuna 0–IV y Seda se eligen en la cuarta página Más. Si falta soporte o una protección deniega el cambio, el bloque original queda intacto y no se consume nada. Los contenedores conservan el botín y los datos del objeto consumido. Una muestra guarda solo el tipo, nunca su contenido. Siguen excluidas las estructuras de varias posiciones y los datos desconocidos.')}
for i,loc in enumerate(['en_us','es_es']):
 p=A/f'lang/{loc}.json';v=json.loads(p.read_text());v.update({k:t[i] for k,t in focus_options.items()})
 v['research.FOCUSPLAN.stage.2'] += (' G resizes radii 0–3 on the held caster; Ctrl+G cycles all/X/Z/Y axes. Rebind this control in Options. The server synchronizes the dimensions used by both preview and execution.' if i==0 else ' G cambia los radios de 0 a 3 en el guantelete; Ctrl+G alterna todos/X/Z/Y. Puedes cambiar la tecla en Opciones. El servidor sincroniza las dimensiones usadas por la vista previa y el hechizo.')
 save(p,v)

# Deep graphs and thaumaturgy devices (port.19).
device_names=['focus_pouch','pech_wand','causality_collapser','grapple_gun','grapple_gun_tip','grapple_gun_spool']
for name in device_names:
 old=O/f'models/item/{name}.json';v=json.loads(old.read_text()) if old.exists() else {'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/'+name}}
 v.pop('overrides',None)
 # Old grapple models use missing 1.12 loaders; retain their original item texture.
 if name.startswith('grapple_gun'):v={'parent':'minecraft:item/handheld','textures':{'layer0':'thaumcraft:items/'+name}}
 save(A/f'models/item/{name}.json',v);save(A/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/'+name}})
for name in ['turret_basic','turret_advanced']:
 def element(fr,to,texture):return {'from':fr,'to':to,'faces':{face:{'texture':'#'+texture} for face in ['north','south','east','west','up','down']}}
 save(A/f'models/block/{name}.json',{'textures':{'wood':'minecraft:block/dark_oak_planks','metal':'minecraft:block/iron_block','particle':'minecraft:block/dark_oak_planks'},'elements':[element([3,0,3],[13,3,13],'wood'),element([6,3,6],[10,11,10],'wood'),element([4,10,3],[12,14,13],'metal' if name.endswith('advanced') else 'wood'),element([0,11,4],[16,13,6],'wood'),element([7,11,0],[9,13,16],'metal')]})
 save(A/f'blockstates/{name}.json',{'variants':{'':{'model':'thaumcraft:block/'+name}}})
 save(A/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:block/'+name}})
 save(D/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:'+name}]}]})
arc('focus_pouch','FOCUSPOUCH',25,{},['LGL','LBL','LLL'],{'L':'minecraft:leather','G':'minecraft:gold_ingot','B':'minecraft:string'})
arc('grapple_gun_tip','GRAPPLEGUN',25,{'terra':1},['BRB','RHR','BRB'],{'B':'thaumcraft:plate_brass','R':'thaumcraft:nugget_rareearth','H':'minecraft:tripwire_hook'})
arc('grapple_gun_spool','GRAPPLEGUN',25,{'aqua':1},['SHS','SGS','SSS'],{'S':'minecraft:string','H':'minecraft:tripwire_hook','G':'thaumcraft:mechanism_simple'})
arc('grapple_gun','GRAPPLEGUN',75,{'aer':1,'ignis':1},['  S','TII',' BW'],{'S':'thaumcraft:grapple_gun_spool','T':'thaumcraft:grapple_gun_tip','I':'thaumcraft:plate_iron','B':'thaumcraft:plate_brass','W':'thaumcraft:plank_greatwood'})
arc('turret_basic','BASICTURRET',100,{'aer':1},['BGI','WMW','S S'],{'B':'minecraft:bow','G':'thaumcraft:mechanism_simple','I':'thaumcraft:plate_iron','W':'thaumcraft:plank_greatwood','M':'thaumcraft:mind_clockwork','S':'minecraft:stick'})
arc('turret_advanced','ADVANCEDTURRET',150,{'aer':2},['PMP','PTP'],{'P':'thaumcraft:plate_iron','M':'thaumcraft:mind_biothaumic','T':'thaumcraft:turret_basic'})
inf('causality_collapser','RIFTCLOSER','minecraft:tnt',['morphic_resonator','minecraft:redstone_block','alumentum','nitor_yellow','vis_resonator','minecraft:redstone_block','alumentum','nitor_yellow'],{'alienis':50,'vitium':50},8)
extra={
 'focal.thaumcraft.graph_node':('Node %s','Nodo %s'),'focal.thaumcraft.graph_enter':('Enter next','Entrar siguiente'),'focal.thaumcraft.graph_add':('Add next','Añadir siguiente'),'focal.thaumcraft.graph_back':('Parent','Anterior'),'focal.thaumcraft.graph_remove':('Remove next','Quitar siguiente'),'focal.thaumcraft.graph_path':('Level %s / Path %s','Nivel %s / Rama %s'),
 'device.thaumcraft.pech_studied':('Pech wand studied. Curse research revealed.','Vara pech estudiada. Investigación de Maldición descubierta.'),
 'device.thaumcraft.turret_status':('Arrows: %s / 64 • %s','Flechas: %s / 64 • %s'),
 'device.thaumcraft.targets_0':('Monsters','Monstruos'),'device.thaumcraft.targets_1':('Other creatures','Otras criaturas'),'device.thaumcraft.targets_2':('Players','Jugadores')}
for n,en,es in [('focus_pouch','Focus Pouch','Bolsa de focos'),('pech_wand','Pech Wand','Vara pech'),('causality_collapser','Causality Collapser','Colapsador causal'),('grapple_gun','Grappling Gun','Lanzador de gancho'),('grapple_gun_tip','Grapple Tip','Punta de gancho'),('grapple_gun_spool','Grapple Spool','Bobina de gancho'),('turret_basic','Automated Crossbow','Ballesta automática'),('turret_advanced','Advanced Crossbow','Ballesta automática avanzada')]:extra[('block.' if n.startswith('turret_') else 'item.')+'thaumcraft.'+n]=(en,es)
for i,loc in enumerate(['en_us','es_es']):
 p=A/f'lang/{loc}.json';v=json.loads(p.read_text());v.update({k:t[i] for k,t in extra.items()});save(p,v)
p=A/'knowledge/recipes.json';v=json.loads(p.read_text());aliases={'focus_pouch':'FocusPouch','grapple_gun':'GrappleGun','grapple_gun_tip':'GrappleGunTip','grapple_gun_spool':'GrappleGunSpool','turret_basic':'AutomatedCrossbow','turret_advanced':'AdvancedCrossbow','causality_collapser':'CausalityCollapser'}
for n,alias in aliases.items():
 path=D/f'recipe/arcane/{n}.json';path=path if path.exists() else D/f'recipe/infusion/{n}.json';r=json.loads(path.read_text());v['thaumcraft:'+alias]=['Research: '+r['research']]+(['Vis: '+str(r['vis']),'Ingredients: '+str(r['recipe']['key'])] if 'recipe' in r else ['Center: '+str(r['input']),'Offerings: '+str(r['components']),'Essentia: '+str(r['aspects'])]);v['thaumcraft:'+n]=v['thaumcraft:'+alias]
save(p,v)

guides={
 'research.FOCUSPOUCH.stage.2':('Open the pouch in your main hand to store 18 physical foci. With the pouch in the off hand and caster in the main hand, sneak-use the caster to cycle them. Installed foci return to the same pouch slot; names and spell graphs are preserved.','Abre la bolsa en la mano principal para guardar 18 focos físicos. Con la bolsa en la mano secundaria y el guantelete en la principal, usa el guantelete agachado para alternarlos. El foco instalado vuelve al hueco de la bolsa; conserva nombre y grafo.'),
 'research.GRAPPLEGUN.stage.2':('Charge the gun on a charging pedestal or with a Vis Amulet (capacity 100). A shot consumes one charge and attaches to a solid block within 32 blocks. The tether pulls you toward its anchor. Sneak or use again to release. Changing the support or an obstructed tether also releases it.','Carga el lanzador en un pedestal de recarga o con un amuleto de vis (capacidad 100). Cada disparo consume una carga y se fija a un bloque sólido hasta 32 bloques. El cable te atrae al anclaje. Agáchate o usa otra vez para soltarte. Cambiar el soporte u obstruir el cable también lo suelta.'),
 'research.BASICTURRET.stage.2':('Place your automated crossbow and right click it with normal arrows to load up to 64. It attacks visible monsters up to 24 blocks away, consuming one arrow per shot. Redstone pauses it. Sneak-use an empty hand to retrieve ammunition. Only its owner can configure it; it fires while the owner is present in this dimension.','Coloca tu ballesta automática y usa flechas normales sobre ella para cargar hasta 64. Ataca monstruos visibles hasta 24 bloques y consume una flecha por disparo. La redstone la pausa. Usa la mano vacía agachado para recuperar munición. Solo el propietario puede configurarla; dispara mientras está presente en esta dimensión.'),
 'research.ADVANCEDTURRET.stage.2':('The advanced crossbow fires faster and more accurately. Use a stick to cycle monsters, other non-allied creatures or players. Allies, pets and PVP-disabled targets are protected. It uses the same arrows, owner and redstone rules as the basic model.','La ballesta avanzada dispara más rápido y con más precisión. Usa un palo para alternar monstruos, otras criaturas no aliadas o jugadores. Protege aliados, mascotas y objetivos con PVP desactivado. Comparte munición, propietario y control por redstone con el modelo básico.'),
 'research.RIFTCLOSER.stage.2':('Throw a Causality Collapser toward a flux rift. It explodes on impact and starts the collapse of rifts within three blocks. Collapse persists and produces the rift normal loot. Stay clear of the blast.','Lanza el colapsador causal hacia una grieta de flux. Explota al impactar e inicia el colapso de las grietas a tres bloques. El colapso persiste y produce el botín normal de la grieta. Aléjate de la explosión.')}
for i,loc in enumerate(['en_us','es_es']):
 p=A/f'lang/{loc}.json';v=json.loads(p.read_text());v.update({k:t[i] for k,t in guides.items()})
 v['research.BASEAUROMANCY.stage.3'] = (' The fifth Setup page adds, enters or removes the continuation of the selected path, or returns to its parent. Each level can have its own modifier and two paths; at most four levels, eight total effects and 64 deliveries. Cost, crystals and research include the entire graph. A continuation starts at the preceding impact.' if i==0 else ' La quinta página Más añade, abre o elimina la continuación de la rama seleccionada, o vuelve al nivel anterior. Cada nivel admite su modificador y dos ramas; máximo cuatro niveles, ocho efectos totales y 64 ejecuciones. Coste, cristales e investigación incluyen todo el grafo. Cada continuación parte del impacto anterior.')
 save(p,v)

# Restore the rare-earth prerequisite and its original additional mining drop.
p=A/'knowledge/research.json';v=json.loads(p.read_text())
for entry in v:
 if entry['key']=='GRAPPLEGUN':
  for stage in entry['stages']:
   for requirement in stage.get('required_item',[]):
    if requirement.get('legacy')=='thaumcraft:nugget;1;10':requirement['id']='thaumcraft:nugget_rareearth'
save(p,v)
save(R/'src/modern/resources/data/forge/loot_modifiers/global_loot_modifiers.json',{'replace':False,'entries':['thaumcraft:rare_earth']})
save(D/'loot_modifiers/rare_earth.json',{'type':'thaumcraft:rare_earth','conditions':[]})
