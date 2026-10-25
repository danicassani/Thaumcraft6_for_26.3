#!/usr/bin/env python3
"""Restore the original loaded/unloaded grapple models through a dedicated native component condition."""
import json,shutil
from pathlib import Path
R=Path(__file__).resolve().parents[1];O=R/'src/main/resources/assets/thaumcraft';A=R/'src/modern/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
for n in ['grapple_gun_1','grapple_gun_2']:
 v=json.loads((O/f'models/item/{n}.json').read_text());v.pop('__createdwith',None);save(A/f'models/item/{n}.json',v)
save(A/'items/grapple_gun.json',{'model':{'type':'minecraft:condition','property':'minecraft:has_component','component':'thaumcraft:grapple_link','on_true':{'type':'minecraft:model','model':'thaumcraft:item/grapple_gun_2'},'on_false':{'type':'minecraft:model','model':'thaumcraft:item/grapple_gun_1'}}})
# Retain the public base model ID for resource packs and old references.
save(A/'models/item/grapple_gun.json',{'parent':'thaumcraft:item/grapple_gun_1'})

sounds=json.loads((A/'sounds.json').read_text());sound=json.loads((O/'sounds.json').read_text())['ice'];sound.pop('category',None);sounds['ice']=sound;save(A/'sounds.json',sounds)
for name in ['ice1','ice2','ice3']:shutil.copyfile(O/f'sounds/{name}.ogg',A/f'sounds/{name}.ogg')
