#!/usr/bin/env python3
"""Original wisp spawn weights, using the 26.3 Forge environment-attribute adapter."""
from pathlib import Path
import json
D=Path(__file__).resolve().parents[1]/'src/modern/resources/data/thaumcraft/forge/biome_modifier'
D.mkdir(parents=True,exist_ok=True)
for name,biomes,weight,count in [('forest','thaumcraft:magical_forest',20,{'type':'minecraft:uniform','min_inclusive':1,'max_inclusive':2}),('nether','#minecraft:is_nether',5,1)]:
 (D/f'wisp_{name}.json').write_text(json.dumps({'type':'forge:add_spawns','biomes':biomes,'spawners':[{'type':'thaumcraft:wisp','weight':weight,'count':count}]},indent=2)+'\n')
print('Wisp native spawns: magical forest weight 20 packs 1..2; Nether weight 5 single')
