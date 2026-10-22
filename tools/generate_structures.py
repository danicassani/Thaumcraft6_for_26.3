#!/usr/bin/env python3
"""Lossless extraction of the 2441 original mound geometry placements; no hand-drawn substitute."""
from pathlib import Path
import re,json,hashlib
R=Path(__file__).resolve().parents[1];D=R/'src/modern/resources/data/thaumcraft'
src=R/'src/main/java/thaumcraft/common/world/objects/WorldGenMound.java';source=src.read_text();body=source.split('float rr')[0]
pattern=r'world.setBlockState\(new BlockPos\(i \+ (\d+), j \+ (\d+), k \+ (\d+)\), Blocks\.([A-Z_]+)\.(getDefaultState\(\)|getStateFromMeta\((\d+)\))\);'
rows=re.findall(pattern,body);assert len(rows)==2441
aliases={'AIR':'air','COBBLESTONE':'cobblestone','DIRT':'dirt','GRASS':'grass_block','MOSSY_COBBLESTONE':'mossy_cobblestone','TALLGRASS':'short_grass','STONE_STAIRS':'cobblestone_stairs','LADDER':'ladder','IRON_BARS':'iron_bars','STONEBRICK':'chiseled_stone_bricks'}
palette=[];cells=[]
for x,y,z,name,_,meta in rows:
 props={};m=int(meta) if meta else None
 if name=='STONE_STAIRS':props={'facing':['east','west','south','north'][m%4] if m is not None else 'north','half':'top' if m is not None and m&4 else 'bottom','shape':'straight','waterlogged':'false'}
 if name=='LADDER':assert m==5;props={'facing':'east','waterlogged':'false'}
 state={'block':'minecraft:'+aliases[name],'properties':props}
 if state not in palette:palette.append(state)
 cells.append([int(x),int(y),int(z),palette.index(state)])
assert len({tuple(c[:3]) for c in cells})==2441
def save(path,v):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(v,ensure_ascii=False,separators=(',',':'))+'\n')
save(D/'blueprints/mound.json',{'source_sha256':hashlib.sha256(src.read_bytes()).hexdigest(),'size':[19,16,19],'palette':palette,'cells':cells})
save(D/'worldgen/structure/mound.json',{'type':'thaumcraft:mound','biomes':'#thaumcraft:has_mound','spawn_overrides':{},'step':'surface_structures','terrain_adaptation':'none'})
save(D/'worldgen/structure_set/mounds.json',{'structures':[{'structure':'thaumcraft:mound','weight':1}],'placement':{'type':'minecraft:random_spread','salt':68413261,'spacing':10,'separation':5}})
save(D/'tags/worldgen/biome/has_mound.json',{'replace':False,'values':['#minecraft:is_forest','#minecraft:is_taiga','#minecraft:is_jungle','#minecraft:is_savanna','#minecraft:is_mountain','#minecraft:is_hill','minecraft:plains','minecraft:sunflower_plains','minecraft:snowy_plains','minecraft:ice_spikes','minecraft:meadow','minecraft:cherry_grove','minecraft:grove','minecraft:swamp','minecraft:stony_shore']})
print('Mound: 2441 original geometry cells, native structure + structure set, stable salt')
