#!/usr/bin/env python3
"""Remaining registered TC6 creatures and the two registered, non-climate biomes. Run last."""
from pathlib import Path
import json,shutil,copy
from loot_schema import for_resource
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft';O=R/'src/main/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(for_resource(p,v),ensure_ascii=False,indent=2)+'\n')
names={'brainy_zombie':('Angry Zombie','Zombi furioso'),'giant_brainy_zombie':('Giant Angry Zombie','Zombi furioso gigante'),'inhabited_zombie':('Inhabited Zombie','Zombi habitado'),'eldritch_crab':('Eldritch Crab','Cangrejo sobrenatural'),'firebat':('Firebat','Murciélago de fuego'),'cultist_leader':('Crimson Praetor','Pretor carmesí'),'eldritch_golem':('Eldritch Construct','Constructo sobrenatural'),'eldritch_warden':('Eldritch Warden','Carcelero sobrenatural'),'cultist_portal_greater':('Greater Crimson Portal','Portal carmesí mayor')}
sounds=json.loads((A/'sounds.json').read_text());original=json.loads((O/'sounds.json').read_text())
for n in ['egidle','egattack','egdeath','egscreech','crabtalk','crabclaw','crabdeath','monolith']:
 sounds[n]=dict(original[n]);sounds[n].pop('category',None)
 for entry in sounds[n]['sounds']:
  name=entry if isinstance(entry,str) else entry['name']
  namespace,_,path=name.partition(':')
  if not path: namespace,path='thaumcraft',namespace
  if namespace!='thaumcraft': continue
  source=O/'sounds'/f'{path}.ogg';target=A/'sounds'/f'{path}.ogg'
  target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(source,target)
save(A/'sounds.json',sounds)
for n in names:
 egg=n+'_spawn_egg';save(A/f'models/item/{egg}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'minecraft:item/egg'}});save(A/f'items/{egg}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/'+egg}})
 def item(id,count=None):
  v={'type':'minecraft:item','name':id}
  if count is not None:v['functions']=[{'function':'minecraft:set_count','count':count}]
  return v
 entries=[]
 if n in ['brainy_zombie','giant_brainy_zombie']:
  entries=[{'rolls':1,'entries':[item('thaumcraft:brain')],'conditions':[{'condition':'minecraft:random_chance_with_enchanted_bonus','enchantment':'minecraft:looting','unenchanted_chance':.5,'enchanted_chance':{'type':'minecraft:linear','base':.6,'per_level_above_first':.1}}]}, {'rolls':1,'entries':[item('minecraft:rotten_flesh',{'type':'minecraft:uniform','min':0,'max':24 if n.startswith('giant') else 2})]}]
 if n=='brainy_zombie':entries[1]={'rolls':1,'entries':[{'type':'minecraft:loot_table','value':'minecraft:entities/zombie'}]}
 if n=='giant_brainy_zombie':entries[1]={'rolls':12,'conditions':[{'condition':'minecraft:random_chance','chance':.5}],'entries':[item('minecraft:rotten_flesh',2)]}
 if n=='firebat':entries=[{'rolls':1,'entries':[item('minecraft:gunpowder',{'type':'minecraft:uniform','min':0,'max':2})]}]
 if n=='firebat':entries[0]['entries'][0].setdefault('functions',[]).append({'function':'minecraft:enchanted_count_increase','enchantment':'minecraft:looting','count':{'type':'minecraft:uniform','min':0,'max':1}})
 if n=='eldritch_crab':entries=[{'rolls':1,'entries':[item('minecraft:ender_pearl')],'conditions':[{'condition':'minecraft:random_chance','chance':.2}]}]
 if n in ['cultist_leader','eldritch_golem','eldritch_warden']:entries.append({'rolls':1,'entries':[item('thaumcraft:loot_bag_rare')]})
 if n in ['eldritch_golem','eldritch_warden','cultist_portal_greater']:entries.append({'rolls':1,'entries':[item('thaumcraft:primordial_pearl')]})
 save(D/f'loot_table/entities/{n}.json',{'type':'minecraft:entity','pools':entries})
for locale,i in [('en_us',0),('es_es',1)]:
 p=A/f'lang/{locale}.json';v=json.loads(p.read_text());
 for n,label in names.items():v['entity.thaumcraft.'+n]=label[i];v['item.thaumcraft.'+n+'_spawn_egg']=('Spawn '+label[i] if i==0 else 'Huevo de '+label[i].lower())
 v['entity.thaumcraft.eldritch_warden.shield']=('Eldritch Shield','Escudo sobrenatural')[i];v['biome.thaumcraft.eerie']=('Eerie','Siniestro')[i];v['biome.thaumcraft.eldritch']=('Outer Lands','Tierras exteriores')[i];save(p,v)
save(A/'blockstates/effect_sap.json',{'variants':{'':{'model':'thaumcraft:block/effect_sap'}}})
save(A/'models/block/effect_sap.json',{'textures':{'particle':'minecraft:block/obsidian'},'elements':[]})
save(D/'forge/biome_modifier/halloween_firebats.json',{'type':'thaumcraft:halloween_firebats'})
save(D/'tags/worldgen/biome/is_spooky.json',{'replace':False,'values':['thaumcraft:eerie','thaumcraft:eldritch']})
# The old source registers these biomes, but never adds them to the Overworld climate or a dimension generator.
forest=json.loads((D/'worldgen/biome/magical_forest.json').read_text())
for n in ['eerie','eldritch']:
 b=copy.deepcopy(forest);b['temperature']=.8;b['downfall']=0 if n=='eerie' else .2;b['has_precipitation']=n!='eerie';b['features']=[[] for _ in forest['features']];b['carvers']=[];b['effects']={'water_color':'#2e538f','grass_color':'#404840','foliage_color':'#404840'}
 attrs=b['attributes'];attrs.pop('minecraft:audio/background_music',None)
 spawn={'monster':[{'type':'thaumcraft:inhabited_zombie','weight':1,'count':1},{'type':'thaumcraft:eldritch_guardian','weight':1,'count':1}]} if n=='eldritch' else {'ambient':[{'type':'minecraft:bat','weight':3,'count':1}],'monster':[{'type':'minecraft:witch','weight':8,'count':1},{'type':'minecraft:enderman','weight':4,'count':1},{'type':'thaumcraft:brainy_zombie','weight':32,'count':1},{'type':'thaumcraft:giant_brainy_zombie','weight':8,'count':1},{'type':'thaumcraft:wisp','weight':3,'count':1},{'type':'thaumcraft:eldritch_guardian','weight':1,'count':1}]}
 attrs['minecraft:gameplay/natural_mob_spawns']={'spawn_costs':{},'spawns_by_category':spawn}
 # Registry attribute names match the vanilla 26.3 biome codec; colors are native biome attributes.
 for key,value in [('sky_color','#222299' if n=='eerie' else '#000000'),('fog_color','#202028')]:attrs['minecraft:visual/'+key]=value
 save(D/f'worldgen/biome/{n}.json',b)
for n,biomes,spawner in [('brainy','#minecraft:is_overworld',{'type':'thaumcraft:brainy_zombie','weight':10,'count':1}),('firebat','#minecraft:is_nether',{'type':'thaumcraft:firebat','weight':10,'count':{'type':'minecraft:uniform','min_inclusive':1,'max_inclusive':2}})]:save(D/f'forge/biome_modifier/{n}.json',{'type':'forge:add_spawns','biomes':biomes,'spawners':[spawner]})
p=A/'knowledge/aspects.json';aspects=json.loads(p.read_text());aspects['entities'].update({
 'thaumcraft:firebat':{'bestia':5,'volatus':5,'ignis':10},'thaumcraft:brainy_zombie':{'exanimis':20,'humanus':10,'cognitio':5,'aversio':5},'thaumcraft:giant_brainy_zombie':{'exanimis':25,'humanus':15,'cognitio':5,'aversio':10},'thaumcraft:eldritch_crab':{'alienis':10,'bestia':10,'vinculum':10},'thaumcraft:inhabited_zombie':{'alienis':10,'exanimis':10,'humanus':5},'thaumcraft:eldritch_warden':{'alienis':40,'mortuus':40,'exanimis':40},'thaumcraft:eldritch_golem':{'alienis':40,'potentia':40,'machina':40},'thaumcraft:cultist_leader':{'alienis':40,'aversio':40,'humanus':40}});save(p,aspects)
save(D/'loot_table/entities/taintacle_giant.json',{'type':'minecraft:entity','pools':[]})
print('Eldritch: 9 creatures, 9 eggs, loot, original textures and two non-climate biomes')
