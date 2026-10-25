#!/usr/bin/env python3
"""Recover the original research atlas positions, frames and icon assets."""
import json,re,zipfile
from pathlib import Path
from PIL import Image
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';O=R/'src/main/resources/assets/thaumcraft'
entries=json.loads((A/'knowledge/research.json').read_text());views=json.loads((A/'knowledge/recipe_views.json').read_text());items={p.stem for p in (A/'items').glob('*.json')};legacy={r['legacy']:r for e in entries for s in e['stages'] for f in ['required_item','required_craft'] for r in s.get(f,[])}
manual={'minecraft:hardened_clay':'minecraft:terracotta','thaumcraft:plate':'thaumcraft:plate_brass','thaumcraft:turret;1;0':'thaumcraft:turret_basic','thaumcraft:turret;1;1':'thaumcraft:turret_advanced','thaumcraft:plate;1;1':'thaumcraft:plate_iron','thaumcraft:plate;1;2':'thaumcraft:plate_thaumium','thaumcraft:plate;1;3':'thaumcraft:plate_void','thaumcraft:mind;1;1':'thaumcraft:mind_biothaumic','thaumcraft:amulet_vis;1;1':'thaumcraft:amulet_vis'}
byname={re.sub('[^a-z0-9]','',k.split(':')[-1].lower()):v for k,v in views.items() if 'result' in v};unknown=[];topics={}
for e in entries:
 icons=[]
 for raw in e.get('icons',[]):
  if raw.startswith('focus:'):
   name=raw.split('.')[-1].lower();name={'splittarget':'split_target','splittrajectory':'split_trajectory'}.get(name,name);raw='thaumcraft:textures/foci/'+name+'.png'
  if 'textures/' in raw:
   path=O/raw.split(':',1)[-1];assert path.exists(),raw
   w,h=Image.open(path).size;icons.append({'texture':raw,'width':w,'height':h});continue
  if raw in manual:icon={'id':manual[raw]}
  elif raw in legacy and not legacy[raw]['id'].startswith('unavailable:'):icon={k:v for k,v in legacy[raw].items() if k in ['id','aspect']}
  elif raw.split(';')[0].startswith('minecraft:') or raw.split(':')[-1] in items:icon={'id':raw.split(';')[0]}
  else:
   v=byname.get(re.sub('[^a-z0-9]','',raw.split(':')[-1].lower()))
   if v:icon=v['result']
   else:unknown.append((e['key'],raw));icon={'id':'minecraft:book'}
  icons.append(icon)
 topics[e['key']]={'x':e['location'][0]*24,'y':e['location'][1]*24,'meta':e.get('meta',[]),'icons':icons}
# Remaining old item icons fall back to a valid illustration from that research's first recipe.
for key,raw in unknown:
 e=next(e for e in entries if e['key']==key);choices=[views.get(ref) for s in e['stages'] for ref in s.get('recipes',[])];v=next((v for v in choices if v and 'result' in v),None)
 if v:topics[key]['icons']=[v['result']]
 else:raise RuntimeError('Unresolved original research icon '+key+' '+raw)
client=next(Path('/workspace/.gradle/caches/minecraftforge/forgegradle/mavenizer/caches/minecraft_tasks/26.3').glob('client.jar'))
with zipfile.ZipFile(client) as jar:known={'minecraft:'+Path(n).stem for n in jar.namelist() if n.startswith('assets/minecraft/items/') and n.endswith('.json')}
known|={'thaumcraft:'+name for name in items}
for key,topic in topics.items():
 for icon in topic['icons']:
  if 'id' in icon:assert icon['id'] in known,(key,icon)
p=A/'knowledge/research_visuals.json';p.write_text(json.dumps(topics,ensure_ascii=False,indent=2)+'\n');print(len(topics),'original research positions;',len(unknown),'legacy icons resolved through their exact recipes')

for locale,i in [('en_us',0),('es_es',1)]:
 p=A/f'lang/{locale}.json';v=json.loads(p.read_text());v.update({k:pair[i] for k,pair in {'research.thaumcraft.atlas_center':('Center map','Centrar mapa'),'research.thaumcraft.atlas_back':('Back to the research map','Volver al mapa de investigación'),'research.thaumcraft.previous':('Previous pages','Páginas anteriores'),'research.thaumcraft.next':('Next pages','Páginas siguientes')}.items()});p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')

# The modern texture decoder accepts PNG, not the original JPG backgrounds. Preserve decoded pixels.
for n in range(1,8):
 original=O/f'textures/gui/gui_research_back_{n}.jpg';dest=A/f'textures/gui/gui_research_back_{n}.png';dest.parent.mkdir(parents=True,exist_ok=True)
 with Image.open(original) as im:im.convert('RGB').save(dest)
