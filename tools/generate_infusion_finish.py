#!/usr/bin/env python3
"""Final infusion resources; run after the other mechanics generators."""
from pathlib import Path
import json,shutil
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';O=R/'src/main/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
p=A/'sounds.json';v=json.loads(p.read_text());old=json.loads((O/'sounds.json').read_text())
for name,source in [('infusion_start','craftstart'),('infusion_fail','craftfail'),('infusion_complete','wand')]:
 v[name]=dict(old[source]);v[name].pop('category',None)
 for sound in v[name]['sounds']:
  n=(sound if isinstance(sound,str) else sound['name']).split(':')[-1]+'.ogg';dst=A/'sounds'/n;dst.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(O/'sounds'/n,dst)
save(p,v)
for locale,es in [('en_us',False),('es_es',True)]:
 p=A/f'lang/{locale}.json';v=json.loads(p.read_text());v['infusion.thaumcraft.warping']='Distorsión del objeto: %s' if es else 'Item warping: %s';v['infusion.thaumcraft.state.invalid_remainder']='Resto de fabricación incompatible' if es else 'Incompatible crafting remainder';save(p,v)
print('Complete infusion: original ritual sounds and item warping tooltip')
