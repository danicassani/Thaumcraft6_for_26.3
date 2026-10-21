#!/usr/bin/env python3
"""Run after taint: wisp discoveries, original sounds and environmental cloud data."""
import json,re,shutil
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft';O=R/'src/main/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
p=A/'sounds.json';v=json.loads(p.read_text());old=json.loads((O/'sounds.json').read_text())
for name in ['wisplive','wispdead','zap']:
 v[name]=old[name];v[name].pop('category',None)
 for sound in v[name]['sounds']:
  n=(sound if isinstance(sound,str) else sound['name']).split(':')[-1]+'.ogg';dest=A/'sounds'/n;dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(O/'sounds'/n,dest)
save(p,v)
save(D/'damage_type/rift_flux.json',{'message_id':'thaumcraft.rift_flux','scaling':'never','exhaustion':0.1})
for tag in ['bypasses_armor','bypasses_shield']:
 p=R/f'src/modern/resources/data/minecraft/tags/damage_type/{tag}.json';v=json.loads(p.read_text());v['values']=list(dict.fromkeys(v['values']+['thaumcraft:rift_flux']));save(p,v)
p=A/'knowledge/aspects.json';v=json.loads(p.read_text());v['entities']['thaumcraft:wisp']={'auram':5,'volatus':5};v['triggers']['entities']['thaumcraft:wisp']=['!Wisp'];save(p,v)
for locale,es in [('en_us',False),('es_es',True)]:
 p=A/f'lang/{locale}.json';v=json.loads(p.read_text());v['entity.thaumcraft.wisp']='Fuego fatuo' if es else 'Wisp';v['entity.thaumcraft.rift_cloud']='Nube de flux de grieta' if es else 'Rift Flux Cloud';v['death.attack.thaumcraft.rift_flux']='%1$s sucumbió a una nube de flux' if es else '%1$s succumbed to a flux cloud';save(p,v)
# Dynamic aspect supplements the original base entity aspects at scan/dissolve time.
print('Rift events: original wisp sounds, discovery, environmental damage and translations')
