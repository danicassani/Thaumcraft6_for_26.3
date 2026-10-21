#!/usr/bin/env python3
"""Resources for the remaining elemental-tool and special armor abilities."""
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]/'src/modern/resources'
def write(path,data):
 p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
write(Path('data/thaumcraft/tags/damage_type/is_magic.json'),{'replace':False,'values':['minecraft:magic','minecraft:indirect_magic','minecraft:wither','minecraft:wither_skull','minecraft:dragon_breath']})
for language,message in [('en_us','Architect plane: %s (sneak-use in air to change)'),('es_es','Plano de construcción: %s (uso agachado en el aire para cambiar)')]:
 path=Path('assets/thaumcraft/lang')/(language+'.json');data=json.loads((root/path).read_text());data['equipment.thaumcraft.shovel_plane']=message;data['equipment.thaumcraft.shovel_controls']='Sneak-use in air: change architect plane' if language=='en_us' else 'Uso agachado en el aire: cambiar plano de construcción';data['equipment.thaumcraft.wind_use']='Hold use: ride the wind' if language=='en_us' else 'Mantén uso: cabalgar el viento';data['equipment.thaumcraft.attract_use']='Hold use: attract dropped items' if language=='en_us' else 'Mantén uso: atraer objetos sueltos';write(path,data)
print('Equipment abilities: extensible magic damage tag and architect controls')
