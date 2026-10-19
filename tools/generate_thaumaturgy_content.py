#!/usr/bin/env python3
"""Native spell and equipment assets; original research gates and recipe costs."""
import json,shutil
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft';O=R/'src/main/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
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
