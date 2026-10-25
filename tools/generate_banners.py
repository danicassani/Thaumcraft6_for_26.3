#!/usr/bin/env python3
"""Restore original TC6 banner IDs, item layers and aspect tint, without modifying vanilla banners."""
import ast,json,re,subprocess,sys
from pathlib import Path
from loot_schema import for_resource
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft';O=R/'src/main/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(for_resource(p,v),ensure_ascii=False,indent=2)+'\n')
colors=next(ast.literal_eval(n.value) for n in ast.parse((R/'tools/generate_luminous_content.py').read_text()).body if isinstance(n,ast.Assign) and any(isinstance(t,ast.Name) and t.id=='colors' for t in n.targets))
aspects={a:int(rgb,16) for a,rgb in re.findall(r'colors.put\("(\w+)", 0x([0-9a-f]+)\)',(R/'src/modern/java/thaumcraft/crystals/CrystalAspects.java').read_text())}
# Original generated item geometry: separate wooden stand, dyed cloth and coloured symbol.
for name,painted,cult in [('banner',False,False),('banner_painted',True,False),('banner_crimson_cult',False,True)]:
 m=json.loads((O/('models/item/banner_crimson_cult.json' if cult else 'models/item/banner.json')).read_text());m['parent']='minecraft:item/generated'
 if not painted:m['textures'].pop('layer2',None)
 m['display']={'thirdperson_righthand':{'rotation':[-90,0,0],'translation':[0,1,-2.5],'scale':[.55]*3},'thirdperson_lefthand':{'rotation':[-90,0,0],'translation':[0,1,-2.5],'scale':[.55]*3},'firstperson_righthand':{'rotation':[0,-135,25],'translation':[0,4,2],'scale':[1.7]*3},'firstperson_lefthand':{'rotation':[0,135,-25],'translation':[0,4,2],'scale':[1.7]*3}}
 save(A/f'models/item/{name}.json',m)
save(A/'models/block/banner_particle.json',{'textures':{'particle':'thaumcraft:models/banner_blank'},'elements':[]})
atlas_path=R/'src/modern/resources/assets/minecraft/atlases/blocks.json'
atlas=json.loads(atlas_path.read_text());source={'type':'minecraft:single','resource':'thaumcraft:models/banner_blank','sprite':'thaumcraft:models/banner_blank'}
if source not in atlas['sources']:atlas['sources'].append(source)
save(atlas_path,atlas)
langs={l:json.loads((A/f'lang/{l}.json').read_text()) for l in ['en_us','es_es']}
for name,rgb,es in colors+[('crimson_cult',0xffffff,'del Culto Carmesí')]:
 id='banner_'+name;cult=name=='crimson_cult'
 def model(painted=False,symbol=0xffffff):return {'type':'minecraft:model','model':'thaumcraft:item/'+('banner_crimson_cult' if cult else 'banner_painted' if painted else 'banner'),'tints':[{'type':'minecraft:constant','value':v-16777216} for v in [0xffffff,rgb]+([symbol] if painted else [])]}
 definition=model() if cult else {'type':'minecraft:select','property':'minecraft:component','component':'thaumcraft:banner_aspect','cases':[{'when':a,'model':model(True,c)} for a,c in aspects.items()],'fallback':model()}
 save(A/f'items/{id}.json',{'model':definition})
 save(A/f'blockstates/{id}.json',{'variants':{'':{'model':'thaumcraft:block/banner_particle'}}})
 save(D/f'loot_table/blocks/{id}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:'+id}]}]})
 langs['en_us']['block.thaumcraft.'+id]='Crimson Cult Banner' if cult else name.replace('lightblue','light blue').replace('silver','light gray').replace('_',' ').title()+' Banner'
 langs['es_es']['block.thaumcraft.'+id]='Estandarte '+es
for l,v in langs.items():save(A/f'lang/{l}.json',v)
# Rebuild book pages after selecting the genuine banner results. This also preserves port.71's guides.
subprocess.run([sys.executable,str(R/'tools/generate_transfusers.py')],check=True)
aspects_path=A/'knowledge/aspects.json'
catalog=json.loads(aspects_path.read_text())
for name,_,_ in colors:catalog.setdefault('complex',{})['thaumcraft:banner_'+name]={'alienis':5}
save(aspects_path,catalog)
print('17 original banner IDs, layered inventory icons, 37 aspect tints and actual book results')
