#!/usr/bin/env python3
"""TC6 treasure weights, native 26.3 loot and original six container models."""
from pathlib import Path
from fractions import Fraction
from collections import Counter
from math import lcm
import json,re,zipfile,shutil
from loot_schema import for_resource
R=Path(__file__).resolve().parents[1];root=R/'src/modern/resources';A=root/'assets/thaumcraft';D=root/'data/thaumcraft';O=R/'src/main/resources/assets/thaumcraft'
def save(p,v):
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(for_resource(p,v),ensure_ascii=False,indent=2)+'\n')
def item(name,weight=1,count=1,mods=None):
 v={'type':'minecraft:item','name':name,'weight':weight}
 fs=([{'type':'minecraft:set_count','count':count}] if count!=1 else [])+(mods or [])
 if fs:v['modifier']=fs
 return v
def ref(name):return {'type':'minecraft:loot_table','value':'thaumcraft:treasure/'+name}
def table(name,entries,rolls=1):save(D/f'loot_table/treasure/{name}.json',{'type':'minecraft:chest','pools':[{'rolls':rolls,'entries':entries}]})
def uniform(lo,hi):return {'type':'minecraft:uniform','min':lo,'max':hi}
def levels(tier):
 if tier==0:return 5
 values=Counter(int(5+tier*.75*n) for n in range(18))
 return {'type':'minecraft:weighted_list','distribution':[{'data':n,'weight':w} for n,w in sorted(values.items())]}
def enchant(tier):return {'type':'minecraft:enchant_with_levels','levels':levels(tier),'options':'#minecraft:in_enchanting_table'}
# Enumerate current native potions, just as TC6 enumerated its potion registry.
sources=Path('/workspace/.gradle/caches/minecraftforge/forgegradle/mavenizer/caches/forge/net/minecraftforge/forge/26.3-66.0.13/injected-sources.jar')
with zipfile.ZipFile(sources) as z:potions=sorted(set(re.findall(r'register\(\s*"([a-z_]+)"',z.read('net/minecraft/world/item/alchemy/PotionIds.java').decode())))
assert len(potions)>30,len(potions)
# Exact rational distribution of the original independent quality upgrades.
quality={0:Fraction(1,2),1:Fraction(1,2)}
for chance in map(Fraction,['.2','.15','.1','.095','.095']):
 nextq={}
 for q,p in quality.items():
  nextq[q]=nextq.get(q,0)+p*(1-chance);nextq[q+1]=nextq.get(q+1,0)+p*chance
 quality=nextq
scale=lcm(*(p.denominator for p in quality.values()));weights={q:int(p*scale) for q,p in quality.items()}
assert sum(weights.values())*5<2**31
for tier,label in enumerate(['common','uncommon','rare']):
 pool=[item('minecraft:gold_nugget',[2500,2250,2000][tier],tier+1),item('thaumcraft:salis_mundus',3*(tier+1))]
 for name in ['chorus_fruit','compass','cookie']:pool.append(item('minecraft:'+name,5))
 pearls=[[(7,1)],[(7,3),(6,1)],[(5,9),(3,3),(0,1)]][tier]
 for wear,w in pearls:pool.append(item('thaumcraft:primordial_pearl',w,mods=[{'type':'minecraft:set_components','components':{'minecraft:custom_model_data':{'floats':[wear]}}}]))
 if tier==2:pool.append(item('minecraft:nether_star'))
 for name,w in [('diamond',10 if tier==0 else 50),('emerald',15 if tier==0 else 75),('gold_ingot',100),('ender_pearl',100),('experience_bottle',[5,10,20][tier]),('enchanted_golden_apple',tier+1),('golden_apple',3*(tier+1))]:pool.append(item('minecraft:'+name,w))
 jewels=[['amulet_mundane','ring_mundane','girdle_mundane'],['amulet_fancy','ring_fancy','girdle_fancy'],['ring_apprentice']][tier]
 for n in jewels:pool.append(item('thaumcraft:'+n,10 if tier==0 else 5))
 if tier>0:pool.append(item('thaumcraft:amulet_vis',6))
 pool.append(item('minecraft:book',10,mods=[enchant(tier)]))
 for potion in potions:
  for kind in ['potion','splash_potion']+(['lingering_potion'] if tier>0 else []):
   pool.append(item('minecraft:'+kind,2,mods=[{'type':'minecraft:set_potion','id':'minecraft:'+potion}]))
 table('base_'+label,pool)
 reward=[ref('base_'+label)]
 if tier>0:
  gear=[]
  for q,w in sorted(weights.items()):
   material=['leather','golden','chainmail','iron','thaumium','diamond','void'][q]
   armor=[('thaumcraft:'+material+'_'+part if q in [4,6] else 'minecraft:'+material+'_'+vanilla) for part,vanilla in [('boots','boots'),('legs','leggings'),('chest','chestplate'),('helm','helmet')]]
   weapon=['minecraft:iron_axe','minecraft:iron_sword','minecraft:golden_axe','minecraft:golden_sword','thaumcraft:thaumium_sword','minecraft:diamond_sword','thaumcraft:void_sword'][q]
   for name in [weapon]+armor:
    ench=enchant(tier);ench['condition']={'type':'minecraft:random_chance','chance':tier/4}
    gear.append(item(name,w,mods=[{'type':'minecraft:set_damage','damage':uniform(5/6,1)},ench]))
  table('gear_'+label,gear)
  entry=ref('gear_'+label);entry['condition']={'type':'minecraft:random_chance','chance':.025*tier}
  reward=[{'type':'minecraft:alternatives','children':[entry,ref('base_'+label)]}]
 table(label,reward)
 table('bag_'+label,[ref(label)],uniform(8,12))
 table('cache_'+label,[ref(label)],uniform(tier+1,tier+3))
 for kind in ['crate','urn']:
  name='loot_'+kind+'_'+label
  model=json.loads((O/f'models/block/{name}.json').read_text());save(A/f'models/block/{name}.json',model)
  save(A/f'blockstates/{name}.json',{'variants':{'':{'model':'thaumcraft:block/'+name}}})
  save(A/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:block/'+name}})
  intact=item('thaumcraft:'+name);intact['condition']='minecraft:tool/can_silk_touch'
  save(D/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'condition':{'type':'minecraft:survives_explosion'},'entries':[{'type':'minecraft:alternatives','children':[intact,ref('cache_'+label)]}]}]})
  key='block.thaumcraft.'+name
  for locale,word,adjectives in [('en_us','Treasure '+('Crate' if kind=='crate' else 'Urn'),['Common','Uncommon','Rare']),('es_es','Caja de tesoro' if kind=='crate' else 'Urna de tesoro',['común','poco común','rara'])]:
   p=A/f'lang/{locale}.json';v=json.loads(p.read_text());v[key]=(adjectives[tier]+' '+word if locale=='en_us' else word+' '+adjectives[tier]);save(p,v)
 name='loot_bag_'+label
 save(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/'+name}})
 save(A/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/'+name}})
 for locale,word,adjectives in [('en_us','Treasure Bag',['Common','Uncommon','Rare']),('es_es','Bolsa de tesoro',['común','poco común','rara'])]:
  p=A/f'lang/{locale}.json';v=json.loads(p.read_text());v['item.thaumcraft.'+name]=(adjectives[tier]+' '+word if locale=='en_us' else word+' '+adjectives[tier]);save(p,v)
# Original research/aspect values; treasure has no crafting recipe in TC6.
p=A/'knowledge/aspects.json';v=json.loads(p.read_text())
for tier,label in enumerate(['common','uncommon','rare']):
 for kind,extra in [('bag',{}),('crate',{'herba':2}),('urn',{'terra':2})]:v['items']['thaumcraft:loot_'+kind+'_'+label]={'desiderium':10*(tier+1),**extra}
save(p,v)
p=A/'sounds.json';v=json.loads(p.read_text())
for name in ['coins','urnbreak']:
 v[name]={'sounds':['thaumcraft:'+name]};dest=A/f'sounds/{name}.ogg';dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(O/f'sounds/{name}.ogg',dest)
save(p,v)
print('Treasure: 3 bags, 6 containers, 9 item definitions; native potions:',len(potions),'gear weight sum:',sum(weights.values())*5)

for kind,tool in [('crate','axe'),('urn','pickaxe')]:
 p=root/f'data/minecraft/tags/block/mineable/{tool}.json';v=json.loads(p.read_text())
 v['values']=list(dict.fromkeys(v['values']+['thaumcraft:loot_'+kind+'_'+label for label in ['common','uncommon','rare']]))
 save(p,v)
