#!/usr/bin/env python3
"""Native transport models, original recipes and translated help. Run after alchemy."""
import json,re
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];OLD=ROOT/'src/main/resources/assets/thaumcraft';AS=ROOT/'src/modern/resources/assets/thaumcraft';DATA=ROOT/'src/modern/resources/data/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
def model(n,v):save(AS/f'models/block/{n}.json',v)
for n in ['tube_core','tube_side','tube_side_restrict','tube_filter_core','tube_buffer_core']:
 v=json.loads((OLD/f'models/block/{n}.json').read_text());v.pop('__createdwith',None);model(n,v)
def box(a,b,texture):return {'from':a,'to':b,'faces':{f:{'texture':texture,'uv':[0,0,16,16]} for f in ['up','down','north','south','west','east']}}
# Native shapes for the parts previously drawn by legacy special renderers.
model('tube_valve_open',{'textures':{'metal':'thaumcraft:blocks/base_metal','particle':'thaumcraft:blocks/tube'},'elements':[box([5,5,5],[11,11,11],'#metal'),box([7,7,1],[9,9,5],'#metal'),box([4,4,0],[12,12,2],'#metal')]})
model('tube_valve_closed',{'textures':{'metal':'thaumcraft:blocks/base_metal','particle':'thaumcraft:blocks/tube'},'elements':[box([5,5,5],[11,11,11],'#metal'),box([7,7,1],[9,9,5],'#metal'),box([3,7,0],[13,9,2],'#metal')]})
model('tube_arrow',{'textures':{'metal':'thaumcraft:blocks/base_metal','particle':'thaumcraft:blocks/tube'},'elements':[box([7,7,3],[9,9,9],'#metal'),box([5,7,4],[11,9,6],'#metal'),box([6,7,3],[10,9,4],'#metal')]})
rot={'north':{},'east':{'y':90},'south':{'y':180},'west':{'y':270},'up':{'x':270},'down':{'x':90}}
side_rot={'down':{},'up':{'x':180},'north':{'x':90},'south':{'x':90,'y':180},'west':{'x':90,'y':270},'east':{'x':90,'y':90}}
for kind in ['normal','valve','filter','restrict','oneway','buffer']:
 n='tube' if kind=='normal' else 'tube_'+kind
 core='tube_filter_core' if kind=='filter' else 'tube_buffer_core' if kind=='buffer' else 'tube_core'
 parts=[{'apply':{'model':'thaumcraft:block/'+core}}]
 for side,r in side_rot.items():parts.append({'when':{side:'true'},'apply':{'model':'thaumcraft:block/tube_side_restrict' if kind=='restrict' else 'thaumcraft:block/tube_side',**r}})
 if kind in ['valve','oneway']:
  for side,r in rot.items():
   if kind=='valve':
    for opened in [True,False]:parts.append({'when':{'facing':side,'open':str(opened).lower()},'apply':{'model':'thaumcraft:block/tube_valve_'+('open' if opened else 'closed'),**r}})
   else:parts.append({'when':{'facing':side},'apply':{'model':'thaumcraft:block/tube_arrow',**r}})
 save(AS/f'blockstates/{n}.json',{'multipart':parts})
 save(AS/f'models/item/{n}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/tube_'+kind}})
 save(AS/f'items/{n}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/'+n}})
 save(DATA/f'loot_table/blocks/{n}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:'+n}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
for n in ['label','resonator']:
 save(AS/f'models/item/{n}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/'+n}})
 save(AS/f'items/{n}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/'+n}})
colors={a:int(h,16) for a,h in re.findall(r'colors.put\("([^"]+)", 0x([0-9a-f]+)\)',(ROOT/'src/modern/java/thaumcraft/crystals/CrystalAspects.java').read_text())}
filled={'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/label','layer1':'thaumcraft:items/label_overlay'}};save(AS/'models/item/label_filled.json',filled)
save(AS/'items/label.json',{'model':{'type':'minecraft:select','property':'minecraft:component','component':'thaumcraft:essentia_aspect','fallback':{'type':'minecraft:model','model':'thaumcraft:item/label'},'cases':[{'when':a,'model':{'type':'minecraft:model','model':'thaumcraft:item/label_filled','tints':[{'type':'minecraft:constant','value':-1},{'type':'minecraft:constant','value':(c|0xff000000)-(1<<32)}]}} for a,c in colors.items()]}})
def arcane(n,vis,crystals,recipe):save(DATA/f'recipe/arcane/{n}.json',{'type':'thaumcraft:arcane','research':'TUBES','vis':vis,'crystals':crystals,'recipe':recipe})
def shaped(n,vis,crystals,pattern,key,count=1):arcane(n,vis,crystals,{'type':'minecraft:crafting_shaped','pattern':pattern,'key':key,'result':{'id':'thaumcraft:'+n,'count':count}})
def shapeless(n,crystals,ingredients):arcane(n,10,crystals,{'type':'minecraft:crafting_shapeless','ingredients':ingredients,'result':{'id':'thaumcraft:'+n}})
shaped('tube',10,{},[' Q ','IGI',' B '],{'I':'thaumcraft:plate_iron','G':'minecraft:glass','B':'thaumcraft:nugget_brass','Q':'thaumcraft:quicksilver'},8)
shaped('resonator',50,{},['I I','INI',' S '],{'I':'thaumcraft:plate_iron','N':'minecraft:quartz','S':'minecraft:stick'})
shapeless('tube_valve',{},['thaumcraft:tube','minecraft:lever']);shapeless('tube_filter',{},['thaumcraft:tube','thaumcraft:filter']);shapeless('tube_restrict',{'terra':1},['thaumcraft:tube']);shapeless('tube_oneway',{'aqua':1},['thaumcraft:tube'])
arcane('tube_buffer',25,{}, {'type':'thaumcraft:essentia_buffer'})
save(DATA/'recipe/label.json',{'type':'minecraft:crafting_shapeless','ingredients':['minecraft:black_dye','minecraft:slime_ball']+['minecraft:paper']*4,'result':{'id':'thaumcraft:label','count':4}})
save(DATA/'recipe/label_aspect.json',{'type':'thaumcraft:essentia_label'})
ui={
 'block.thaumcraft.tube':('Essentia Tube','Tubo de esencia'),'block.thaumcraft.tube_valve':('Essentia Valve','Válvula de esencia'),'block.thaumcraft.tube_filter':('Filtered Essentia Tube','Tubo filtrador de esencia'),'block.thaumcraft.tube_restrict':('Restricted Essentia Tube','Tubo restrictor de esencia'),'block.thaumcraft.tube_oneway':('Directional Essentia Tube','Tubo direccional de esencia'),'block.thaumcraft.tube_buffer':('Essentia Buffer','Depósito de esencia'),
 'item.thaumcraft.resonator':('Essentia Resonator','Resonador de esencia'),'item.thaumcraft.label':('Jar Label','Etiqueta para tarros'),
 'essentia.thaumcraft.label':('Label: %s','Etiqueta: %s'),'essentia.thaumcraft.reading':('Essentia: %s | Suction: %s (%s)','Esencia: %s | Succión: %s (%s)'),
 'essentia.thaumcraft.any':('any aspect','cualquier aspecto'),'essentia.thaumcraft.conflict':('Conflicting suction: transport is venting.','Succiones incompatibles: el transporte está venteando.'),'essentia.thaumcraft.filter':('Filter: %s','Filtro: %s'),'essentia.thaumcraft.choke':('Face choke: %s (0 normal, 1 limited, 2 no suction)','Restricción de cara: %s (0 normal, 1 limitada, 2 sin succión)')}
for i,loc in enumerate(['en_us','es_es']):
 p=AS/f'lang/{loc}.json';v=json.loads(p.read_text());v.update({k:vs[i] for k,vs in ui.items()});save(p,v)
p=AS/'knowledge/recipes.json';pages=json.loads(p.read_text())
for n,alias in [('tube','Tube'),('tube_valve','TubeValve'),('tube_filter','TubeFilter'),('tube_restrict','TubeRestrict'),('tube_oneway','TubeOneway'),('tube_buffer','TubeBuffer'),('resonator','Resonator')]:
 r=json.loads((DATA/f'recipe/arcane/{n}.json').read_text());inner=r['recipe'];lines=['Vis '+str(r['vis'])+'; '+str(r['crystals'])]
 if 'pattern' in inner:lines+=[' / '.join(inner['pattern'])]+[k+': '+v for k,v in inner['key'].items()]
 elif 'ingredients' in inner:lines+=inner['ingredients']
 else:lines+=['PVP / TWT / PRP','P: empty phial; V: valve; T: tube; W: iron plate; R: restricted tube']
 pages['thaumcraft:'+alias]=lines
pages['thaumcraft:JarLabel']=['4 paper + black dye + slime -> 4 labels.','Blank label + filled phial -> aspect label + empty phial.','One aspect label alone -> blank label.']
save(p,pages)
print('Essentia transport: 6 blocks, 2 items and 9 recipes generated; cards unchanged')
