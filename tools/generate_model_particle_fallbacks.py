#!/usr/bin/env python3
"""Run after block generators: keep particles valid when multipart selectors match no geometry."""
from pathlib import Path
import json
A=Path(__file__).resolve().parents[1]/'src/modern/resources/assets/thaumcraft'
def save(p,v):
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,indent=2)+'\n')
for names,model,texture in [(['crystal_'+a for a in ['aer','terra','ignis','aqua','ordo','perditio','vitium']],'crystal_particle','crystal'),(['taint_fibre'],'taint_fibre_particle','taint_fibres')]:
 save(A/f'models/block/{model}.json',{'parent':'minecraft:block/block','textures':{'particle':'thaumcraft:blocks/'+texture},'elements':[]})
 for name in names:
  p=A/f'blockstates/{name}.json';v=json.loads(p.read_text());ref='thaumcraft:block/'+model
  v['multipart']=[part for part in v['multipart'] if part.get('apply',{} )!={'model':ref}]
  v['multipart'].append({'apply':{'model':ref}});save(p,v)
print('Multipart particles: geometry-free fallback for seven crystals and taint fibre')
