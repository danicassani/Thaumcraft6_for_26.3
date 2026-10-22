#!/usr/bin/env python3
"""Native mirror block models and original TC6 infusion/arcane recipes."""
from loot_schema import for_resource
from pathlib import Path
import json,shutil,itertools
R=Path(__file__).resolve().parents[1];root=R/'src/modern/resources';old=R/'src/main/resources/assets/thaumcraft'
def save(p,v):
 p=root/p;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(for_resource(p,v),ensure_ascii=False,indent=2)+'\n')
for texture in ['mirrorframe','mirrorframe2','mirrorpane','mirrorpaneopen','mirrorpanetrans']:
 p=root/f'assets/thaumcraft/textures/block/{texture}.png';p.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(old/f'textures/blocks/{texture}.png',p)
p=root/'assets/thaumcraft/textures/item/hand_mirror.png';p.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(old/'textures/items/hand_mirror.png',p)
faces=['down','up','north','south','west','east']
def point(p,face):
 x,y,z=p
 return {'up':(x,y,z),'down':(x,16-y,16-z),'north':(x,z,16-y),'south':(16-x,z,y),'east':(y,z,16-x),'west':(16-y,z,x)}[face]
def element(lo,hi,face,texture,plane=False):
 corners=[point(p,face) for p in itertools.product(*zip(lo,hi))];mins=[min(p[i] for p in corners) for i in range(3)];maxs=[max(p[i] for p in corners) for i in range(3)]
 return {'from':mins,'to':maxs,'faces':{f:{'texture':texture,'uv':[0,0,16,16]} for f in ([face] if plane else faces)}}
for name,frame in [('mirror','mirrorframe'),('mirror_essentia','mirrorframe2')]:
 states={}
 for face in faces:
  for active in [False,True]:
   model=f'{name}_{face}_{"open" if active else "closed"}';states[f'facing={face},enabled={str(active).lower()}']={'model':'thaumcraft:block/'+model}
   elements=[element([0,0,0],[16,1,16],face,'#frame'),element([1,1.01,1],[15,1.01,15],face,'#pane',True),element([0,1.02,0],[16,1.02,16],face,'#frame',True)]
   save(Path(f'assets/thaumcraft/models/block/{model}.json'),{'parent':'minecraft:block/block','render_type':'minecraft:cutout','textures':{'frame':'thaumcraft:block/'+frame,'pane':'thaumcraft:block/'+('mirrorpaneopen' if active else 'mirrorpane'),'particle':'thaumcraft:block/'+frame},'elements':elements})
 save(Path(f'assets/thaumcraft/blockstates/{name}.json'),{'variants':states})
 save(Path(f'assets/thaumcraft/items/{name}.json'),{'model':{'type':'minecraft:model','model':f'thaumcraft:block/{name}_up_closed'}})
 save(Path(f'data/thaumcraft/loot_table/blocks/{name}.json'),{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:'+name}]}]})
save(Path('assets/thaumcraft/models/item/hand_mirror.json'),{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:item/hand_mirror'}})
save(Path('assets/thaumcraft/items/hand_mirror.json'),{'model':{'type':'minecraft:model','model':'thaumcraft:item/hand_mirror'}})
for name in ['mirror','mirror_essentia']:
 p=Path('data/minecraft/tags/block/mineable/pickaxe.json');v=json.loads((root/p).read_text());v['values']=sorted(set(v['values'])|{'thaumcraft:'+name});save(p,v)
def ing(n):return {'item':n if ':' in n else 'thaumcraft:'+n}
recipes=[('mirror','Mirror','MIRROR','mirrored_glass',['minecraft:gold_ingot']*3+['minecraft:ender_pearl'],{'motus':25,'tenebrae':25,'permutatio':25},1),('mirror_essentia','MirrorEssentia','MIRRORESSENTIA','mirrored_glass',['minecraft:iron_ingot']*3+['minecraft:ender_pearl'],{'motus':25,'aqua':25,'permutatio':25},2),('hand_mirror','MirrorHand','MIRRORHAND','mirror',['minecraft:stick','minecraft:compass','minecraft:map'],{'instrumentum':50,'motus':50},5)]
p=Path('assets/thaumcraft/knowledge/recipes.json');pages=json.loads((root/p).read_text())
for name,alias,research,center,parts,cost,instability in recipes:
 save(Path(f'data/thaumcraft/recipe/infusion/{name}.json'),{'type':'thaumcraft:infusion','research':research,'input':ing(center),'components':list(map(ing,parts)),'aspects':cost,'instability':instability,'result':{'id':'thaumcraft:'+name}});pages['thaumcraft:'+alias]=['Center: thaumcraft:'+center,'Offerings: '+', '.join(parts),'Essentia: '+str(cost),'Instability: '+str(instability)]
save(Path('data/thaumcraft/recipe/arcane/mirrored_glass.json'),{'type':'thaumcraft:arcane','research':'BASEARTIFICE','vis':50,'crystals':{'aqua':1,'ordo':1},'recipe':{'type':'minecraft:crafting_shapeless','ingredients':['thaumcraft:quicksilver','#thaumcraft:glass_panes'],'result':{'id':'thaumcraft:mirrored_glass'}}})
save(Path('data/thaumcraft/tags/item/glass_panes.json'),{'replace':False,'values':['minecraft:glass_pane']+['minecraft:'+color+'_stained_glass_pane' for color in ['white','orange','magenta','light_blue','yellow','lime','pink','gray','light_gray','cyan','purple','blue','brown','green','red','black']]+[{'id':'#c:glass_panes','required':False}]})
pages['thaumcraft:mirrorglass']=['Quicksilver + glass pane','Vis: 50','aqua: 1, ordo: 1'];save(p,pages)
for language,index in [('en_us',0),('es_es',1)]:
 p=Path('assets/thaumcraft/lang')/(language+'.json');v=json.loads((root/p).read_text());labels={'block.thaumcraft.mirror':('Magic Mirror','Espejo mágico'),'block.thaumcraft.mirror_essentia':('Essentia Mirror','Espejo de esencia'),'item.thaumcraft.hand_mirror':('Hand Mirror','Espejo de mano'),'mirror.thaumcraft.deposit':('Unsent items stay here','Lo no enviado queda aquí'),'mirror.thaumcraft.connected':('Mirror linked to a loaded destination','Espejo enlazado con un destino cargado'),'mirror.thaumcraft.waiting':('Mirror unlinked or destination unavailable','Espejo sin enlace o con destino no disponible'),'mirror.thaumcraft.link':('Bound to %s at %s, %s, %s','Vinculado a %s en %s, %s, %s'),'mirror.thaumcraft.controls':('Use on an unpaired mirror of the same type, then place.','Usa sobre un espejo libre del mismo tipo y colócalo después.'),'mirror.thaumcraft.hand_controls':('Use on an item mirror to bind; use in main hand to send.','Usa sobre un espejo de objetos para vincular; usa en la mano principal para enviar.')}
 for key,text in labels.items():v[key]=text[index]
 save(p,v)
print('Mirrors: six orientations, original frames/glass/hand texture and four original recipes')
