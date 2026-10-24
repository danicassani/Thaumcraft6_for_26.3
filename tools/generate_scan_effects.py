#!/usr/bin/env python3
"""Extract the sixteen original TC6 scan glyphs for the native particle atlas."""
from pathlib import Path
import json, shutil
from PIL import Image
root=Path(__file__).resolve().parents[1]
old=root/'src/main/resources/assets/thaumcraft'
assets=root/'src/modern/resources/assets/thaumcraft'
sheet=Image.open(old/'textures/misc/particles.png')
unit=sheet.width//64
out=assets/'textures/particle';out.mkdir(parents=True,exist_ok=True)
for i in range(16):
    sheet.crop((i*unit,6*unit,(i+1)*unit,7*unit)).save(out/f'scan_rune_{i}.png')
p=assets/'particles/scan_rune.json';p.parent.mkdir(parents=True,exist_ok=True)
p.write_text(json.dumps({'textures':[f'thaumcraft:scan_rune_{i}' for i in range(16)]},indent=2)+'\n')
sounds=json.loads((assets/'sounds.json').read_text())
sounds['scan']={'sounds':['thaumcraft:scan']}
(assets/'sounds.json').write_text(json.dumps(sounds,ensure_ascii=False,indent=2)+'\n')
shutil.copyfile(old/'sounds/scan.ogg',assets/'sounds/scan.ogg')
