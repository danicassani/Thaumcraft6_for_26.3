#!/usr/bin/env python3
"""Final layer after rift_events: charged pearl assets and lossless legacy fragment exchange."""
import json,shutil
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft';O=R/'src/main/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
for i in range(3):save(A/f'models/item/primordial_pearl_{i}.json',json.loads((O/f'models/item/primordial_pearl_{i}.json').read_text()))
def model(i):return {'type':'minecraft:model','model':f'thaumcraft:item/primordial_pearl_{i}'}
save(A/'items/primordial_pearl.json',{'model':{'type':'minecraft:range_dispatch','property':'minecraft:custom_model_data','fallback':model(0),'entries':[{'threshold':3,'model':model(1)},{'threshold':6,'model':model(2)}]}})
# Compatibility is one-way: permitting charged pearls to split into fragments would
# multiply intact item aspect values when each fragment became a separate mote.
save(D/'recipe/primordial_pearl.json',{'type':'minecraft:crafting_shapeless','ingredients':['thaumcraft:primordial_nugget'],'result':{'id':'thaumcraft:primordial_pearl','components':{'minecraft:custom_model_data':{'floats':[7.0]}}}})
(D/'recipe/primordial_nugget.json').unlink(missing_ok=True)
for p in (D/'recipe/infusion').glob('*.json'):
 v=json.loads(p.read_text());changed=False
 for c in v.get('components',[]):
  ingredient=c.get('item')
  if ingredient=='thaumcraft:primordial_nugget' or isinstance(ingredient,list) and 'thaumcraft:primordial_nugget' in ingredient:
   c['item']=['thaumcraft:primordial_nugget','thaumcraft:primordial_pearl'];changed=True
 if changed:save(p,v)
for locale,names in [('en_us',['Primordial Pearl','Primordial Nodule','Primordial Mote']),('es_es',['Perla primordial','Nódulo primordial','Mota primordial'])]:
 p=A/f'lang/{locale}.json';v=json.loads(p.read_text());v.update({'item.thaumcraft.primordial_pearl.'+k:n for k,n in zip(['pearl','nodule','mote'],names)});save(p,v)
print('Primordial: original three models, eight uses and one-way legacy fragment migration')

p=A/'sounds.json';v=json.loads(p.read_text());v['evilportal']=json.loads((O/'sounds.json').read_text())['evilportal'];v['evilportal'].pop('category',None);save(p,v)
for sound in v['evilportal']['sounds']:
 name=(sound if isinstance(sound,str) else sound['name']).split(':')[-1]+'.ogg';dest=A/'sounds'/name;dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(O/'sounds'/name,dest)
