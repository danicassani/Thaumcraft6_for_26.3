#!/usr/bin/env python3
"""Run after worldgen/knowledge generators: natural pechs, original sound and entity data."""
from pathlib import Path
import json,shutil
R=Path(__file__).resolve().parents[1];D=R/'src/modern/resources/data/thaumcraft';A=R/'src/modern/resources/assets/thaumcraft';O=R/'src/main/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
# ConfigEntities.postInitEntitySpawns replaces the biome constructor's 20 / 1..2 entry.
save(D/'forge/biome_modifier/pech.json',{'type':'forge:add_spawns','biomes':'#thaumcraft:pech_habitat','spawners':[{'type':'thaumcraft:pech','weight':10,'count':1}]})
save(D/'tags/worldgen/biome/pech_habitat.json',{'replace':False,'values':['thaumcraft:magical_forest',{'id':'thaumcraft:eerie','required':False}]})
save(D/'loot_table/entities/pech.json',{'type':'minecraft:entity','pools':[
 {'rolls':1,'entries':[{'type':'minecraft:item','name':'minecraft:gold_nugget','modifier':[{'type':'minecraft:set_count','count':{'type':'minecraft:uniform','min':0,'max':2}},{'type':'minecraft:enchanted_count_increase','enchantment':'minecraft:looting','count':{'type':'minecraft:uniform','min':0,'max':1}}]}]},
 {'rolls':1,'condition':{'type':'minecraft:all_of','terms':[{'type':'minecraft:killed_by_player'},{'type':'minecraft:random_chance_with_enchanted_bonus','unenchanted_chance':.0125,'enchanted_chance':{'type':'minecraft:linear','base':.0225,'per_level_above_first':.01},'enchantment':'minecraft:looting'}]},'entries':[{'type':'minecraft:item','name':'thaumcraft:curio','modifier':[{'type':'minecraft:set_components','components':{'thaumcraft:curio_variant':'knowledge','minecraft:item_name':{'translate':'item.thaumcraft.curio.knowledge'}}}]}]}
 ]})
p=A/'sounds.json';v=json.loads(p.read_text());old=json.loads((O/'sounds.json').read_text())
for name in ['pech_idle','pech_hit','pech_death','pech_charge','pech_dice','pech_trade']:
 v[name]=dict(old[name]);v[name].pop('category',None)
 for sound in v[name]['sounds']:
  n=(sound if isinstance(sound,str) else sound['name']).split(':')[-1]+'.ogg';dst=A/'sounds'/n;dst.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(O/'sounds'/n,dst)
save(p,v)
p=A/'knowledge/aspects.json';v=json.loads(p.read_text());v['entities']['thaumcraft:pech']={'humanus':10,'auram':5,'permutatio':10,'desiderium':5};v['triggers']['entities']['thaumcraft:pech']=v['triggers']['entities'].pop('minecraft:pech',['!Pech']);save(p,v)
for locale,es in [('en_us',False),('es_es',True)]:
 p=A/f'lang/{locale}.json';v=json.loads(p.read_text());v.update({'entity.thaumcraft.pech':'Pech','entity.thaumcraft.pech.0':'Pech recolector' if es else 'Pech Forager','entity.thaumcraft.pech.1':'Pech taumaturgo' if es else 'Pech Mage','entity.thaumcraft.pech.2':'Pech acechador' if es else 'Pech Stalker','entity.thaumcraft.pech_bolt':'Hechizo pech' if es else 'Pech Spell','item.thaumcraft.pech_spawn_egg':'Generador de pech' if es else 'Pech Spawn Egg','pech.thaumcraft.roll':'Tratar' if es else 'Trade','pech.thaumcraft.offering':'Ofrenda valiosa → premios' if es else 'Valued offering → rewards','pech.thaumcraft.no_room':'No caben los premios. Conservas tu ofrenda.' if es else 'Rewards do not fit. Your offering is unchanged.'});save(p,v)
# A neutral voxel egg with the original species palette; modern Minecraft removed the generic egg texture.
faces=lambda tint:{d:{'texture':'#egg','tintindex':tint} for d in ['north','south','east','west','up','down']}
elements=[{'from':[8-w/2,y,7],'to':[8+w/2,y+h,9],'faces':faces(0)} for y,w,h in [(1,6,2),(3,10,4),(7,10,3),(10,8,3),(13,4,2)]]
elements += [{'from':[x,y,6.9],'to':[x+2,y+2,7],'faces':faces(1)} for x,y in [(5,4),(9,8),(6,11)]]
save(A/'models/item/pech_spawn_egg.json',{'parent':'minecraft:block/block','textures':{'egg':'minecraft:block/white_concrete','particle':'minecraft:block/white_concrete'},'elements':elements,'gui_light':'front','display':{'gui':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[1,1,1]}}})
save(A/'items/pech_spawn_egg.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/pech_spawn_egg','tints':[{'type':'minecraft:constant','value':-16129},{'type':'minecraft:constant','value':-12582848}]}})
print('Pechs: final original spawn weight, loot, sound, discovery and spawn egg resources')
