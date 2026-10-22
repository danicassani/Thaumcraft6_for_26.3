#!/usr/bin/env python3
"""Six utility devices: original materials, arcane/alchemy/infusion recipes and models."""
from pathlib import Path
import json,shutil,itertools
R=Path(__file__).resolve().parents[1];root=R/'src/modern/resources';old=R/'src/main/resources/assets/thaumcraft'
def save(p,v):
 p=root/p;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
def texture(n):
 for suffix in ['.png','.png.mcmeta']:
  a=old/f'textures/blocks/{n}{suffix}'
  if a.exists():
   p=root/f'assets/thaumcraft/textures/block/{n}{suffix}';p.parent.mkdir(parents=True,exist_ok=True)
   if suffix.endswith('mcmeta'):p.write_text(json.dumps(json.loads(a.read_text()),indent=2)+'\n')
   else:shutil.copyfile(a,p)
for n in ['lamp_on','lamp_off','lamp_grow_on','lamp_grow_off','lamp_fertility_on','lamp_fertility_off','base_metal','everfull_urn','spa_side','spa_top','potion_sprayer_bottom','potion_sprayer_top','potion_sprayer_side']:texture(n)
body=json.loads((old/'models/block/lamp.json').read_text())['elements'];stand=json.loads((old/'models/block/lamp_stand.json').read_text())['elements']
faces=['down','up','north','south','east','west']
def pt(p,f):
 x,y,z=p
 return {'down':(x,y,z),'up':(x,16-y,16-z),'north':(x,16-z,y),'south':(x,z,16-y),'east':(16-y,z,x),'west':(y,z,16-x)}[f]
for name,tex in [('lamp_arcane','lamp'),('lamp_growth','lamp_grow'),('lamp_fertility','lamp_fertility')]:
 variants={}
 for face in faces:
  for on in [False,True]:
   v=json.loads(json.dumps(stand));
   for e in v:
    corners=[pt(p,face) for p in itertools.product(*zip(e['from'],e['to']))];e['from']=[min(p[i] for p in corners) for i in range(3)];e['to']=[max(p[i] for p in corners) for i in range(3)]
    vectors={'down':(0,-1,0),'up':(0,1,0),'north':(0,0,-1),'south':(0,0,1),'east':(1,0,0),'west':(-1,0,0)};reverse={v:k for k,v in vectors.items()};center=pt((8,8,8),face);transformed={}
    for direction,oldface in e.get('faces',{}).items():
     directionpoint=pt(tuple(8+v for v in vectors[direction]),face);normal=tuple(directionpoint[i]-center[i] for i in range(3));oldface['texture']='#stand';oldface.pop('cullface',None);transformed[reverse[normal]]=oldface
    e['faces']=transformed
   model=f'{name}_{face}_{"on" if on else "off"}';save(Path(f'assets/thaumcraft/models/block/{model}.json'),{'render_type':'minecraft:cutout','ambientocclusion':False,'textures':{'side':'thaumcraft:block/'+tex+('_on' if on else '_off'),'stand':'thaumcraft:block/base_metal','particle':'thaumcraft:block/'+tex+'_off'},'elements':body+v});variants[f'facing={face},enabled={str(on).lower()}']={'model':'thaumcraft:block/'+model}
 save(Path(f'assets/thaumcraft/blockstates/{name}.json'),{'variants':variants})
 save(Path(f'assets/thaumcraft/items/{name}.json'),{'model':{'type':'minecraft:model','model':f'thaumcraft:block/{name}_down_off'}})
urn=json.loads((old/'models/block/everfull_urn.json').read_text());urn=json.loads(json.dumps(urn).replace('thaumcraft:blocks/','thaumcraft:block/'));save(Path('assets/thaumcraft/models/block/everfull_urn.json'),urn)
save(Path('assets/thaumcraft/models/block/spa.json'),{'parent':'minecraft:block/cube','textures':{'north':'thaumcraft:block/spa_side','south':'thaumcraft:block/spa_side','east':'thaumcraft:block/spa_side','west':'thaumcraft:block/spa_side','up':'thaumcraft:block/spa_top','down':'minecraft:block/furnace_top','particle':'thaumcraft:block/spa_side'}})
save(Path('assets/thaumcraft/models/block/potion_sprayer.json'),{'parent':'minecraft:block/cube_bottom_top','textures':{k:'thaumcraft:block/potion_sprayer_'+k for k in ['bottom','top','side']}})
for n in ['everfull_urn','spa','potion_sprayer']:
 variants={}
 rot={'up':{},'down':{'x':180},'north':{'x':90},'south':{'x':90,'y':180},'east':{'x':90,'y':90},'west':{'x':90,'y':270}}
 for face in faces:
  for on in [False,True]:variants[f'facing={face},enabled={str(on).lower()}']={'model':'thaumcraft:block/'+n,**(rot[face] if n=='potion_sprayer' else {})}
 save(Path(f'assets/thaumcraft/blockstates/{n}.json'),{'variants':variants});save(Path(f'assets/thaumcraft/items/{n}.json'),{'model':{'type':'minecraft:model','model':'thaumcraft:block/'+n}})
names=['lamp_arcane','lamp_growth','lamp_fertility','everfull_urn','spa','potion_sprayer']
for n in names:save(Path(f'data/thaumcraft/loot_table/blocks/{n}.json'),{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:'+n}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
save(Path('assets/thaumcraft/models/block/effect_glimmer.json'),{'textures':{'particle':'minecraft:block/glass'},'elements':[]});save(Path('assets/thaumcraft/blockstates/effect_glimmer.json'),{'variants':{'':{'model':'thaumcraft:block/effect_glimmer'}}})
for n,research,vis,crystals,pattern,key in [('lamp_arcane','ARCANELAMP',50,{'aer':1,'ignis':1},[' I ','IAI',' I '],{'I':'thaumcraft:plate_iron','A':'thaumcraft:amber_block'}),('spa','ARCANESPA',50,{'aqua':1},['QIQ','SJS','SPS'],{'Q':'minecraft:quartz_block','I':'minecraft:iron_bars','S':'thaumcraft:stone_arcane','J':'thaumcraft:jar_normal','P':'thaumcraft:mechanism_simple'}),('potion_sprayer','POTIONSPRAYER',75,{'aqua':1,'ignis':1},['BDB','IAI','ICI'],{'B':'thaumcraft:plate_brass','D':'minecraft:dispenser','I':'thaumcraft:plate_iron','A':'minecraft:brewing_stand','C':'thaumcraft:metal_alchemical'})]:save(Path(f'data/thaumcraft/recipe/arcane/{n}.json'),{'type':'thaumcraft:arcane','research':research,'vis':vis,'crystals':crystals,'recipe':{'type':'minecraft:crafting_shaped','pattern':pattern,'key':key,'result':{'id':'thaumcraft:'+n}}})
save(Path('data/thaumcraft/recipe/alchemy/everfull_urn.json'),{'type':'thaumcraft:crucible','research':'EVERFULLURN','catalyst':'minecraft:flower_pot','aspects':{'aqua':30,'fabrico':10,'terra':10},'result':{'id':'thaumcraft:everfull_urn'}})
def item(n,aspect=''):
 d={'item':n}
 if aspect:d['crystal_aspect']=aspect
 return d
for n,research,aspects,parts in [('lamp_growth','LAMPGROWTH',{'herba':20,'lux':15,'victus':15,'instrumentum':15},[item('minecraft:gold_ingot'),item('minecraft:bone_meal'),item('thaumcraft:crystal_essence','terra')]*2),('lamp_fertility','LAMPFERTILITY',{'bestia':20,'lux':15,'victus':15,'desiderium':15},[item('minecraft:gold_ingot'),item('minecraft:wheat'),item('thaumcraft:crystal_essence','ignis'),item('minecraft:gold_ingot'),item('minecraft:carrot'),item('thaumcraft:crystal_essence','ignis')])]:save(Path(f'data/thaumcraft/recipe/infusion/{n}.json'),{'type':'thaumcraft:infusion','research':research,'input':item('thaumcraft:lamp_arcane'),'components':parts,'aspects':aspects,'instability':4,'result':{'id':'thaumcraft:'+n}})
p=Path('data/minecraft/tags/block/mineable/pickaxe.json');v=json.loads((root/p).read_text());v['values']=sorted(set(v['values'])|{'thaumcraft:'+n for n in names});save(p,v)
p=Path('assets/thaumcraft/knowledge/recipes.json');v=json.loads((root/p).read_text())
for alias,n,folder in [('ArcaneLamp','lamp_arcane','arcane'),('ArcaneSpa','spa','arcane'),('PotionSprayer','potion_sprayer','arcane'),('EverfullUrn','everfull_urn','alchemy'),('LampGrowth','lamp_growth','infusion'),('LampFertility','lamp_fertility','infusion')]:
 r=json.loads((root/f'data/thaumcraft/recipe/{folder}/{n}.json').read_text());lines=[]
 if folder=='arcane':lines=[' / '.join(r['recipe']['pattern'])]+[k+': '+val for k,val in r['recipe']['key'].items()]+['Vis: '+str(r['vis']),'Crystals: '+str(r['crystals'])]
 elif folder=='alchemy':lines=['Catalyst: '+r['catalyst'],'Essentia: '+str(r['aspects'])]
 else:lines=['Center: thaumcraft:lamp_arcane','Offerings: '+str(r['components']),'Essentia: '+str(r['aspects']),'Instability: 4']
 v['thaumcraft:'+alias]=lines
save(p,v)
for lang,i in [('en_us',0),('es_es',1)]:
 p=Path('assets/thaumcraft/lang')/(lang+'.json');v=json.loads((root/p).read_text())
 labels={'lamp_arcane':('Arcane Lamp','Lámpara arcana'),'lamp_growth':('Lamp of Growth','Lámpara de crecimiento'),'lamp_fertility':('Lamp of Fertility','Lámpara de fertilidad'),'everfull_urn':('Everfull Urn','Urna inagotable'),'spa':('Arcane Spa','Spa arcano'),'potion_sprayer':('Potion Sprayer','Pulverizador de pociones')}
 for n,pair in labels.items():v['block.thaumcraft.'+n]=pair[i]
 for n,pair in {'water':('Water: %s mB','Agua: %s mB'),'charges':('Charges: %s','Cargas: %s'),'mode':('Mode','Modo'),'mixing':('%s mB · mixing salts','%s mB · mezcla de sales'),'pouring':('%s mB · pouring fluid','%s mB · vertido de fluido'),'sprayer':('%s/8 · essentia %s/%s','%s/8 · esencia %s/%s')}.items():v['utility.thaumcraft.'+n]=pair[i]
 save(p,v)
print('Utility devices: six recipes, original models/textures and native menus')
