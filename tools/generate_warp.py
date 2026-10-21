#!/usr/bin/env python3
"""Warp assets and original soap crucible recipe; run after primordial."""
import json
from pathlib import Path
from PIL import Image
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft';O=R/'src/main/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
save(A/'models/item/sanity_soap.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/sanity_soap'}})
save(A/'items/sanity_soap.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/sanity_soap'}})
save(D/'recipe/sanity_soap.json',{'type':'thaumcraft:crucible','research':'SANESOAP','catalyst':'thaumcraft:flesh_block','aspects':{'cognitio':75,'alienis':50,'ordo':75,'victus':50},'result':{'id':'thaumcraft:sanity_soap'}})
atlas=Image.open(O/'textures/misc/potions.png')
for n,(x,y) in {'unnatural_hunger':(7,1),'sun_scorned':(6,2),'thaumarhia':(7,2),'death_gaze':(4,2),'blurred_vision':(5,2)}.items():
 p=A/f'textures/mob_effect/{n}.png';p.parent.mkdir(parents=True,exist_ok=True);atlas.crop((x*18,y*18,x*18+18,y*18+18)).save(p)
for n in ['mind_spider','cultist_portal_lesser','eldritch_orb']:save(D/f'loot_table/entities/{n}.json',{'type':'minecraft:entity','pools':[]})
save(D/'loot_table/entities/eldritch_guardian.json',{'type':'minecraft:entity','pools':[]})
for n in ['cultist_knight','cultist_cleric']:
 save(D/f'loot_table/entities/{n}.json',{'type':'minecraft:entity','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'minecraft:gold_nugget','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':0,'max':2}},{'function':'minecraft:enchanted_count_increase','enchantment':'minecraft:looting','count':{'type':'minecraft:uniform','min':0,'max':1}}]}]},{'rolls':1,'conditions':[{'condition':'minecraft:killed_by_player'},{'condition':'minecraft:random_chance_with_enchanted_bonus','enchantment':'minecraft:looting','unenchanted_chance':.0125,'enchanted_chance':{'type':'minecraft:linear','base':.0225,'per_level_above_first':.01}}],'entries':[{'type':'minecraft:item','name':'thaumcraft:curio','functions':[{'function':'minecraft:set_components','components':{'thaumcraft:curio_variant':'rites','minecraft:item_name':{'translate':'item.thaumcraft.curio.rites'}}}]}]}]})
for locale,idx in [('en_us',0),('es_es',1)]:
 p=A/f'lang/{locale}.json';v=json.loads(p.read_text())
 names={'item.thaumcraft.sanity_soap':('Sanity Soap','Jabón de cordura'),'effect.thaumcraft.unnatural_hunger':('Unnatural Hunger','Hambre antinatural'),'effect.thaumcraft.sun_scorned':('Sun Scorned','Rechazo solar'),'effect.thaumcraft.thaumarhia':('Thaumarhia','Taumarrea'),'effect.thaumcraft.death_gaze':('Death Gaze','Mirada mortal'),'effect.thaumcraft.blurred_vision':('Blurred Vision','Visión borrosa'),'entity.thaumcraft.mind_spider':('Mind Spider','Araña mental'),'entity.thaumcraft.eldritch_guardian':('Eldritch Guardian','Guardián sobrenatural'),'entity.thaumcraft.cultist_knight':('Crimson Knight','Caballero carmesí'),'entity.thaumcraft.cultist_cleric':('Crimson Cleric','Clérigo carmesí'),'entity.thaumcraft.cultist_portal_lesser':('Lesser Crimson Portal','Portal carmesí menor'),'entity.thaumcraft.eldritch_orb':('Eldritch Orb','Orbe sobrenatural')}
 v.update({n:label[idx] for n,label in names.items()})
 for line in (O/'lang/en_us.lang').read_text().splitlines():
  if line.startswith('warp.text.') and '=' in line:k,val=line.split('=',1);v.setdefault(k,val)
 save(p,v)
print('Warp: sanity soap, original five effect icons, entity names and hallucination messages')

p=A/'knowledge/recipes.json';v=json.loads(p.read_text());v['thaumcraft:SaneSoap']=['Catalyst: thaumcraft:flesh_block','Cognitio 75, Alienis 50, Ordo 75, Victus 50','Research: SANESOAP'];save(p,v)
p=A/'knowledge/aspects.json';v=json.loads(p.read_text());v['entities'].update({'thaumcraft:mind_spider':{'vitium':5,'ignis':5},'thaumcraft:eldritch_guardian':{'alienis':20,'mortuus':20,'exanimis':20},'thaumcraft:cultist_knight':{'alienis':5,'humanus':15,'aversio':5},'thaumcraft:cultist_cleric':{'alienis':5,'humanus':15,'aversio':5}});v['triggers']['entities']['thaumcraft:eldritch_guardian']=['!EldritchGuardian'];save(p,v)
p=A/'lang/es_es.json';v=json.loads(p.read_text());v.update({'warp.text.'+str(n):text for n,text in {1:'Te sientes extrañamente agotado.',2:'Un hambre repentina y antinatural te consume.',4:'Tu visión se vuelve extraña y siniestra.',5:'La luz se vuelve insoportable y te quema la piel.',6:'Una niebla espesa surge de la nada. Algo se mueve en su interior.',7:'¡Están por todas partes! ¡Corre!',9:'Tus miembros se vuelven pesados.',10:'Tus ojos se adaptan a la oscuridad.',11:'¿Qué ha sido ese ruido?',12:'Algo te observa.',13:'Un pensamiento inquietante cruza tu mente.',14:'Por un instante, tu mente se aclara.',15:'Tu estómago se revuelve de forma alarmante.',16:'La realidad se desgarra cerca de ti.'}.items()});save(p,v)
