#!/usr/bin/env python3
"""Original infusion costs and viable outputs; native models, loot and 32 active theory cards."""
import json
from loot_schema import for_resource
from pathlib import Path
R=Path(__file__).resolve().parents[1];AS=R/'src/modern/resources/assets/thaumcraft';DATA=R/'src/modern/resources/data/thaumcraft';OLD=R/'src/main/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(for_resource(p,v),ensure_ascii=False,indent=2)+'\n')
def box(a,b,texture):return {'from':a,'to':b,'faces':{f:{'texture':texture} for f in ['up','down','north','south','west','east']}}
for name,old in [('pedestal_arcane','pedestal_normal'),('pedestal_ancient','pedestal_ancient'),('pedestal_eldritch','pedestal_eldritch'),('jar_brain','jar_brain_item')]:
 v=json.loads((OLD/f'models/block/{old}.json').read_text());v.pop('__createdwith',None)
 if name=='jar_brain':v['render_type']='minecraft:cutout'
 save(AS/f'models/block/{name}.json',v)
# Eight stone segments correspond to the original rotating matrix cubes.
elements=[]
for x in [3,9]:
 for y in [3,9]:
  for z in [3,9]:elements.append(box([x,y,z],[x+4,y+4,z+4],'#stone'))
save(AS/'models/block/infusion_matrix.json',{'textures':{'stone':'thaumcraft:blocks/infuser_normal','particle':'thaumcraft:blocks/infuser_normal'},'elements':elements})
for name in ['pedestal_arcane','pedestal_ancient','pedestal_eldritch','infusion_matrix','jar_brain']:
 save(AS/f'blockstates/{name}.json',{'variants':{'':{'model':'thaumcraft:block/'+name}}})
 save(AS/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:block/'+name}})
 save(DATA/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:'+name}]}]})
for name in ['brain']:
 save(AS/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/'+name}});save(AS/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/'+name}})
variants=['arcane','preserved','ancient','eldritch','knowledge','twisted','rites']
for v in variants:save(AS/f'models/item/curio_{v}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/'+('crimson_rites' if v=='rites' else 'curio_'+v)}})
save(AS/'items/curio.json',{'model':{'type':'minecraft:select','property':'minecraft:component','component':'thaumcraft:curio_variant','fallback':{'type':'minecraft:model','model':'thaumcraft:item/curio_arcane'},'cases':[{'when':v,'model':{'type':'minecraft:model','model':'thaumcraft:item/curio_'+v}} for v in variants]}})
# Original preserved curiosities replace an amber drop in 6.6% of non-Silk-Touch breaks.
p=DATA/'loot_table/blocks/ore_amber.json';loot=json.loads(p.read_text());children=loot['pools'][0]['entries'][0]['children'];children[:]=[c for c in children if c.get('name')!='thaumcraft:curio'];children.insert(1,{'type':'minecraft:item','name':'thaumcraft:curio','condition':{'type':'minecraft:random_chance','chance':.066},'modifier':[{'type':'minecraft:set_components','components':{'thaumcraft:curio_variant':'preserved','minecraft:item_name':{'translate':'item.thaumcraft.curio.preserved'}}}]});save(p,loot)
def arcane(n,research,vis,crystals,pattern,key):save(DATA/f'recipe/arcane/{n}.json',{'type':'thaumcraft:arcane','research':research,'vis':vis,'crystals':crystals,'recipe':{'type':'minecraft:crafting_shaped','pattern':pattern,'key':key,'result':{'id':'thaumcraft:'+n}}})
arcane('infusion_matrix','INFUSION@2',150,dict.fromkeys(['aer','ignis','aqua','terra','ordo','perditio'],1),['S S',' N ','S S'],{'S':'thaumcraft:stone_arcane_brick','N':['thaumcraft:nitor_'+v for v in ['white','orange','magenta','lightblue','yellow','lime','pink','gray','silver','cyan','purple','blue','brown','green','red','black']]})
for style,vis,key,slab,stone in [('arcane',10,'INFUSION','slab_arcane_stone','stone_arcane'),('ancient',150,'INFUSIONANCIENT','slab_ancient','stone_ancient'),('eldritch',150,'INFUSIONELDRITCH','slab_eldritch','stone_eldritch_tile')]:arcane('pedestal_'+style,key,vis,{},['SSS',' B ','SSS'],{'S':'thaumcraft:'+slab,'B':'thaumcraft:'+stone})
def item(s,aspect=None):return {'item':s,**({'crystal_aspect':aspect} if aspect else {})}
def infusion(n,research,primary,parts,cost,instability,result):save(DATA/f'recipe/infusion/{n}.json',{'type':'thaumcraft:infusion','research':research,'input':primary,'components':parts,'aspects':cost,'instability':instability,'result':{'id':'thaumcraft:'+result}})
for a in ['aer','ignis','aqua','terra','ordo','perditio','vitium']:infusion('crystal_'+a,'CRYSTALFARMER',item('thaumcraft:crystal_essence',a),[item('minecraft:wheat_seeds'),item('thaumcraft:salis_mundus')],{a:10,'vitreus':10,'vinculum':5},4 if a=='vitium' else 0,'crystal_'+a)
infusion('jar_brain','JARBRAIN',{**item('thaumcraft:jar_normal'),'empty_essentia':True},[item('thaumcraft:brain'),item('minecraft:spider_eye'),item('minecraft:water_bucket'),item('minecraft:spider_eye')],{'cognitio':25,'sensus':25,'exanimis':25},4,'jar_brain')
infusion('void_siphon','VOIDSIPHON',item('thaumcraft:metal_void'),[item('thaumcraft:stone_arcane'),item('thaumcraft:stone_arcane'),item('thaumcraft:mechanism_complex'),item('thaumcraft:plate_brass'),item('thaumcraft:plate_brass'),item('minecraft:nether_star')],{'alienis':50,'perditio':50,'vacuos':100,'fabrico':50},7,'void_siphon')
# Map original aliases into the knowledge book alongside the actual native recipe data.
p=AS/'knowledge/recipes.json';pages=json.loads(p.read_text())
for n,alias in [('infusion_matrix','InfusionMatrix'),('pedestal_arcane','ArcanePedestal'),('pedestal_ancient','AncientPedestal'),('pedestal_eldritch','EldritchPedestal')]:
 r=json.loads((DATA/f'recipe/arcane/{n}.json').read_text());pages['thaumcraft:'+alias]=[' / '.join(r['recipe']['pattern'])]+[k+': '+str(v) for k,v in r['recipe']['key'].items()]+['Vis '+str(r['vis'])+'; '+str(r['crystals'])]
for n,alias in [('jar_brain','JarBrain'),('void_siphon','VoidSiphon')]+[('crystal_'+a,'CrystalCluster'+b) for a,b in zip(['aer','ignis','aqua','terra','ordo','perditio','vitium'],['Air','Fire','Water','Earth','Order','Entropy','Flux'])]:
 r=json.loads((DATA/f'recipe/infusion/{n}.json').read_text());pages['thaumcraft:'+alias]=['Infusion: '+r['result']['id'],'Center: '+str(r['input']),'Offerings: '+str(r['components']),'Essentia: '+str(r['aspects'])+'; instability '+str(r['instability'])]
for name,style in [('infusionaltar','arcane'),('infusionaltarancient','ancient'),('infusionaltareldritch','eldritch')]:pages['thaumcraft:'+name]=['Matrix at y+2 above central '+style+' pedestal.','Four two-block columns of matching stone at the diagonal corners.','Use Salis Mundus on the matrix to assemble pillars, then empty hand to start.','Use the resonator to inspect stability and missing costs.']
save(p,pages)
names={
 'block.thaumcraft.infusion_matrix':('Runic Matrix','Matriz rúnica'),'block.thaumcraft.pedestal_arcane':('Arcane Pedestal','Pedestal arcano'),'block.thaumcraft.pedestal_ancient':('Ancient Pedestal','Pedestal antiguo'),'block.thaumcraft.pedestal_eldritch':('Eldritch Pedestal','Pedestal oculto'),'block.thaumcraft.jar_brain':('Brain in a Jar','Cerebro en frasco'),'item.thaumcraft.brain':('Zombie Brain','Cerebro de zombi'),
 'infusion.thaumcraft.status':('%s | Stability %s (%s/cycle) | Essentia %s | Items %s','%s | Estabilidad %s (%s/ciclo) | Esencia %s | Objetos %s'),'infusion.thaumcraft.brain_xp':('Stored XP: %s / %s','XP guardada: %s / %s'),'infusion.thaumcraft.knowledge':('You have gained knowledge.','Has adquirido conocimiento.')}
for state,en,es in [('idle','Ready / use Salis Mundus to assemble','Preparada / usa Salis Mundus para montar'),('essence','Drawing essentia','Absorbiendo esencia'),('ingredients','Consuming offerings','Consumiendo ingredientes'),('missing_essence','Waiting for essentia','Falta esencia'),('missing_item','Waiting for offering','Falta un ingrediente'),('complete','Infusion complete','Infusión terminada'),('cancelled','Infusion interrupted','Infusión interrumpida'),('unloaded','Waiting for loaded chunks','Esperando a los chunks'),('mishap','Instability event','Suceso de inestabilidad')]:names['infusion.thaumcraft.state.'+state]=(en,es)
curionames=[('Arcane Curiosity','Curiosidad arcana'),('Preserved Curiosity','Curiosidad preservada'),('Ancient Curiosity','Curiosidad antigua'),('Eldritch Curiosity','Curiosidad oculta'),('Illuminating Curiosity','Curiosidad esclarecedora'),('Twisted Curiosity','Curiosidad retorcida'),('Crimson Rites','Ritos carmesíes')]
for v,name in zip(variants,curionames):names['item.thaumcraft.curio.'+v]=name
for i,loc in enumerate(['en_us','es_es']):
 p=AS/f'lang/{loc}.json';v=json.loads(p.read_text());v.update({k:val[i] for k,val in names.items()});save(p,v)
print('Infusion: 5 blocks, brain/7 curiosity variants, 9 infusion recipes and 4 arcane recipes')
