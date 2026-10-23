#!/usr/bin/env python3
"""Native lesser cult portal encounters in new Overworld chunks."""
from pathlib import Path
import json
D=Path(__file__).resolve().parents[1]/'src/modern/resources/data/thaumcraft'
def save(p,v):
 p=D/p;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,indent=2)+'\n')
save(Path('worldgen/feature/cultist_portal.json'),{'type':'thaumcraft:cultist_portal'})
# Original approximately 1/500 chance per eligible chunk after the mound roll.
save(Path('worldgen/placed_feature/cultist_portal.json'),{'feature':'thaumcraft:cultist_portal','placement':[{'type':'minecraft:rarity_filter','chance':500},{'type':'minecraft:in_square'},{'type':'minecraft:heightmap','heightmap':'WORLD_SURFACE_WG'},{'type':'minecraft:biome'}]})
save(Path('forge/biome_modifier/cultist_portal.json'),{'type':'forge:add_features','biomes':'#minecraft:is_overworld','features':'thaumcraft:cultist_portal','step':'surface_structures'})
print('Cult encounters: native feature, 1/500 rarity, Overworld only')
