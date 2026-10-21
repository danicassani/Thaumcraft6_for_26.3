#!/usr/bin/env python3
from pathlib import Path
import json,shutil
R=Path(__file__).resolve().parents[1];root=R/'src/modern/resources'
def save(p,v):
 p=root/p;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
p=root/'assets/thaumcraft/textures/entity/chest/hungry.png';p.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(R/'src/main/resources/assets/thaumcraft/textures/models/chesthungry.png',p)
save(Path('assets/minecraft/atlases/chest.json'),{'sources':[{'type':'minecraft:single','resource':'thaumcraft:entity/chest/hungry','sprite':'thaumcraft:entity/chest/hungry'}]})
save(Path('assets/thaumcraft/blockstates/hungry_chest.json'),{'variants':{'':{'model':'thaumcraft:block/hungry_chest_particle'}}})
save(Path('assets/thaumcraft/models/block/hungry_chest_particle.json'),{'textures':{'particle':'thaumcraft:entity/chest/hungry'}})
def cube(lo,hi,u,v):
 w,h,d=[hi[i]-lo[i] for i in range(3)];uv={'west':[u,v+d,u+d,v+d+h],'north':[u+d,v+d,u+d+w,v+d+h],'east':[u+d+w,v+d,u+d+w+d,v+d+h],'south':[u+d+w+d,v+d,u+2*d+2*w,v+d+h],'up':[u+d,v,u+d+w,v+d],'down':[u+d+w,v,u+d+2*w,v+d]}
 return {'from':lo,'to':hi,'faces':{f:{'texture':'#chest','uv':[n/4 for n in coords]} for f,coords in uv.items()}}
save(Path('assets/thaumcraft/models/item/hungry_chest.json'),{'parent':'minecraft:block/block','textures':{'chest':'thaumcraft:entity/chest/hungry','particle':'thaumcraft:entity/chest/hungry'},'elements':[cube([1,0,1],[15,10,15],0,19),cube([1,9,1],[15,14,15],0,0),cube([7,7,0],[9,11,1],0,0)]})
save(Path('assets/thaumcraft/items/hungry_chest.json'),{'model':{'type':'minecraft:model','model':'thaumcraft:item/hungry_chest'}})
save(Path('data/thaumcraft/loot_table/blocks/hungry_chest.json'),{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:hungry_chest'}]}]})
save(Path('data/thaumcraft/recipe/arcane/hungry_chest.json'),{'type':'thaumcraft:arcane','research':'HUNGRYCHEST','vis':15,'crystals':{'terra':1,'aqua':1},'recipe':{'type':'minecraft:crafting_shaped','pattern':['WTW','W W','WWW'],'key':{'W':'thaumcraft:plank_greatwood','T':'#minecraft:wooden_trapdoors'},'result':{'id':'thaumcraft:hungry_chest'}}})
p=Path('data/minecraft/tags/block/mineable/axe.json');data=json.loads((root/p).read_text());data['values']=sorted(set(data['values'])|{'thaumcraft:hungry_chest'});save(p,data)
p=Path('assets/thaumcraft/knowledge/recipes.json');data=json.loads((root/p).read_text());data['thaumcraft:HungryChest']=['[WTW]','[W W]','[WWW]','W = thaumcraft:plank_greatwood','T = #minecraft:wooden_trapdoors','Vis: 15','terra: 1, aqua: 1'];save(p,data)
for locale,label in [('en_us','Hungry Chest'),('es_es','Cofre hambriento')]:
 p=Path('assets/thaumcraft/lang')/(locale+'.json');data=json.loads((root/p).read_text());data['block.thaumcraft.hungry_chest']=label;save(p,data)
print('Hungry chest: original texture, 27-slot native chest, original arcane recipe and guide')

p=Path('assets/thaumcraft/knowledge/research.json');data=json.loads((root/p).read_text())
for entry in data:
 if entry.get('key')=='HUNGRYCHEST':
  for stage in entry.get('stages',[]):
   for item in stage.get('required_item',[]):
    if item.get('legacy','').split(';')[0]=='oredict:chest':item['id']='minecraft:chest'
save(p,data)
