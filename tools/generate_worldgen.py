#!/usr/bin/env python3
"""26.3 magical forest biome and native placed features; run after alchemy generator."""
from pathlib import Path
import json,zipfile
R=Path(__file__).resolve().parents[1];D=R/'src/modern/resources/data';A=R/'src/modern/resources/assets/thaumcraft'
def save(p,v):
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
vanilla=Path('/workspace/.gradle/caches/minecraftforge/forgegradle/mavenizer/caches/minecraft_tasks/26.3/client.jar')
with zipfile.ZipFile(vanilla) as z:biome=json.loads(z.read('data/minecraft/worldgen/biome/forest.json'))
biome['temperature']=.7;biome['downfall']=.8
biome['effects'].update(water_color='#0077ee',grass_color='#55ff81',foliage_color='#66ffc5')
spawns=biome['attributes']['minecraft:gameplay/natural_mob_spawns']['argument']['spawns_by_category']
spawns['creature']=[s for s in spawns['creature'] if s['type']!='minecraft:wolf']+[{'type':'minecraft:wolf','count':{'type':'minecraft:uniform','min_inclusive':1,'max_inclusive':3},'weight':2},{'type':'minecraft:horse','count':{'type':'minecraft:uniform','min_inclusive':1,'max_inclusive':3},'weight':2}]
for s in spawns['monster']:
 if s['type'] in ['minecraft:witch','minecraft:enderman']:s.update(weight=3,count=1)
spawns['monster'].append({'type':'minecraft:vex','count':1,'weight':1})
# Pech and natural wisp spawning are integrated with the creature delivery, not invented substitutes.
vegetation=biome['features'][9]
vegetation[vegetation.index('minecraft:trees_birch_and_oak_leaf_litter')]='thaumcraft:magical_trees'
vegetation.insert(vegetation.index('thaumcraft:magical_trees')+1,'thaumcraft:magical_ground')
save(D/'thaumcraft/worldgen/biome/magical_forest.json',biome)
for name,ground in [('magical_trees',False),('magical_ground',True)]:
 save(D/f'thaumcraft/worldgen/feature/{name}.json',{'type':'thaumcraft:forest_decoration','ground':ground})
 placement=[{'type':'minecraft:count','count':2},{'type':'minecraft:in_square'},{'type':'minecraft:heightmap','heightmap':'OCEAN_FLOOR_WG'},{'type':'minecraft:biome'}] if not ground else [{'type':'minecraft:heightmap','heightmap':'OCEAN_FLOOR_WG'},{'type':'minecraft:biome'}]
 save(D/f'thaumcraft/worldgen/placed_feature/{name}.json',{'feature':'thaumcraft:'+name,'placement':placement})
for tag in ['is_forest','is_overworld']:
 p=D/f'minecraft/tags/worldgen/biome/{tag}.json';v=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]}
 if 'thaumcraft:magical_forest' not in v['values']:v['values'].append('thaumcraft:magical_forest')
 save(p,v)
for locale,label in [('en_us','Magical Forest'),('es_es','Bosque mágico')]:
 p=A/f'lang/{locale}.json';v=json.loads(p.read_text());v['biome.thaumcraft.magical_forest']=label;save(p,v)
for wood,color in [('greatwood',-12012264),('silverwood',-1)]:
 p=A/f'items/leaves_{wood}.json';v=json.loads(p.read_text());v['model']['tints']=[{'type':'minecraft:constant','value':color}];save(p,v)
print('Magical forest: native biome, two tree attempts/chunk, ground vegetation and biome tags')
