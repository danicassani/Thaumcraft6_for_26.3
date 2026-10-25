#!/usr/bin/env python3
"""Complete the original book references using the port's actual structures and recipes."""
import json
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft/recipe'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
# Native coloured banners retain vanilla placement, patterns and persistence.
colors=['white','orange','magenta','light_blue','yellow','lime','pink','gray','light_gray','cyan','purple','blue','brown','green','red','black']
for c in colors:save(D/f'arcane/banner_{c}.json',{'type':'thaumcraft:arcane','research':'BASEINFUSION','vis':10,'crystals':{},'recipe':{'type':'minecraft:crafting_shaped','pattern':['WS','WS','WB'],'key':{'W':'minecraft:'+c+'_wool','S':'minecraft:stick','B':'#minecraft:wooden_slabs'},'result':{'id':'minecraft:'+c+'_banner'}}})
# Empty optional tags cannot match: the ores become usable when another datapack/mod supplies them.
for metal in ['tin','silver','lead']:save(D/f'crucible/metal_purification_{metal}.json',{'type':'thaumcraft:crucible','research':'METALPURIFICATION','catalyst':'#c:ores/'+metal,'aspects':{'metallum':5,'ordo':5},'result':{'id':'thaumcraft:cluster_'+metal}})
for metal in ['tin','silver','lead']:save(R/f'src/modern/resources/data/c/tags/item/ores/{metal}.json',{'replace':False,'values':[]})
# Regenerate exact recipe pages before adding the non-crafting diagrams.
import subprocess,sys
subprocess.run([sys.executable,str(R/'tools/generate_recipe_views.py')],check=True)
v=json.loads((A/'knowledge/recipe_views.json').read_text())
v['thaumcraft:Banners']={'choices':['thaumcraft:arcane/banner_'+c for c in colors]}
choices=['thaumcraft:'+name for name in ['amulet_mundane','ring_mundane','girdle_mundane','amulet_fancy','ring_fancy','girdle_fancy']]
assert all(key in v for key in choices)
v['thaumcraft:baubles_stuff']={'choices':choices}
def blueprint(key,result,layers,palette,note):v['thaumcraft:'+key]={'kind':'blueprint','result':{'id':'thaumcraft:'+result},'grid':[],'layers':layers,'palette':{k:{'id':x} for k,x in palette.items()},'note':'research.thaumcraft.diagram.'+note}
blueprint('infernalfurnace','infernal_furnace',[['NON','OOO','NON'],['NON','OLO','NIN'],['NON','O O','NON']],{'N':'minecraft:nether_bricks','O':'minecraft:obsidian','L':'minecraft:lava_bucket','I':'minecraft:iron_bars'},'furnace')
for key,style,stone in [('infusionaltar','arcane','stone_arcane'),('infusionaltarancient','ancient','stone_ancient'),('infusionaltareldritch','eldritch','stone_eldritch_tile')]:
 blueprint(key,'infusion_matrix',[['S S',' P ','S S'],['S S','   ','S S'],['   ',' M ','   ']],{'S':'thaumcraft:'+stone,'P':'thaumcraft:pedestal_'+style,'M':'thaumcraft:infusion_matrix'},'altar')
blueprint('GolemPress','golem_builder',[['CP','AT'],[' I','  ']],{'P':'minecraft:piston','C':'minecraft:cauldron','T':'thaumcraft:table_stone','A':'minecraft:anvil','I':'minecraft:iron_bars'},'press')
blueprint('Thaumatorium','thaumatorium',[['C'],['A'],['A']],{'C':'thaumcraft:crucible','A':'thaumcraft:metal_alchemical'},'thaumatorium')
label=lambda a:{'id':'thaumcraft:label','aspect':a}
v['thaumcraft:JarLabelEssence']={'choices':['thaumcraft:diagram/label_'+a for a in ['aer','ignis','aqua','terra','ordo','perditio']]}
for a in ['aer','ignis','aqua','terra','ordo','perditio']:v['thaumcraft:diagram/label_'+a]={'kind':'crafting_shapeless','result':label(a),'grid':[{'id':'thaumcraft:label'},{'id':'thaumcraft:phial','aspect':a}]+[None]*7,'note':'research.thaumcraft.diagram.label'}
for metal in ['tin','silver','lead']:v['thaumcraft:metal_purification_'+metal]=dict(v['thaumcraft:crucible/metal_purification_'+metal],optional_tag='c:ores/'+metal,note='research.thaumcraft.diagram.optional')
save(A/'knowledge/recipe_views.json',v)
labels={
 'search':('Search research','Buscar investigación'),
 'diagram.layer':('Layer %s (bottom first)','Capa %s (desde abajo)'),
 'diagram.furnace':('Bottom to top; front is down. Keep the top centre open. Apply Salis Mundus to the frame.','De abajo arriba; el frente está abajo. Deja abierto el centro superior. Aplica Salis Mundus al armazón.'),
 'diagram.altar':('Eight stone blocks, central pedestal, matrix two blocks above. Apply Salis Mundus to the matrix.','Ocho bloques de piedra, pedestal central y matriz dos bloques encima. Aplica Salis Mundus a la matriz.'),
 'diagram.press':('Piston faces up. Front is down. Bars above piston. Apply Salis Mundus to piston.','Pistón hacia arriba; frente abajo. Barrotes sobre el pistón. Aplica Salis Mundus al pistón.'),
 'diagram.thaumatorium':('Crucible below two alchemical metal blocks. Apply Salis Mundus to the metal.','Crisol bajo dos bloques de metal alquímico. Aplica Salis Mundus al metal.'),
 'diagram.label':('The phial is returned empty. One encoded label alone erases its aspect.','El vial se devuelve vacío. Una etiqueta codificada sola borra su aspecto.'),
 'diagram.optional':('Requires compatible ores from another mod or datapack.','Requiere minerales de otro mod o paquete de datos.'),
 'diagram.unavailable':('No compatible ore installed','No hay un mineral compatible instalado'),
}
for lang,index in [('en_us',0),('es_es',1)]:
 p=A/f'lang/{lang}.json';l=json.loads(p.read_text());l.update({'research.thaumcraft.'+k:pair[index] for k,pair in labels.items()});l['research.thaumcraft.recipe_kind.blueprint']=['Structure layers','Capas de la estructura'][index];save(p,l)
print('Completed 12 original references, native banners, optional purification and encoded label pages')

p=A/'lang/es_es.json';lang=json.loads(p.read_text());translations={}
for line in (R/'tools/es_research_texts.lang').read_text().splitlines():
 if line and not line.startswith('#'):
  key,value=line.split('=',1);assert key not in translations,key;translations[key]=value
lang.update(translations);save(p,lang)
research=json.loads((A/'knowledge/research.json').read_text());keys={e['name'] for e in research}|{s['text'] for e in research for s in e['stages']+e.get('addenda',[])}
assert len(keys)==433 and not keys-lang.keys()
print('Spanish research translation: 433/433 referenced titles, stages and addenda')

# Keep the English instructions consistent with the actual native adaptations.
p=A/'lang/en_us.json';en=json.loads(p.read_text())
en['research.BASEINFUSION.stage.1']="I am now ready to begin crafting enchanted objects and tools.<LINE>I have drawn up sixteen coloured banners to decorate my laboratory. These use native banner placement and persistence; I can decorate them with patterns at a loom."
en['research.WARDEDJARS.stage.1']="Essentia can be stored almost indefinitely in §lWarded Jars§r. Tubes can fill and empty them, and phials transfer small amounts by hand.<BR>They would also make excellent containers for jam.<PAGE>A §lJar Label§r locks a jar to a single aspect. A filled jar supplies the aspect; an empty jar needs an encoded label.<BR>Craft a blank label with a full phial to encode it. The phial is returned empty. Placing a label on a filled jar uses that jar's aspect.<BR>A single encoded label in a crafting grid becomes blank again. Sneak-use a jar or alembic with an empty hand to remove its label.<PAGE>A §lBrass Lid Brace§r seals a jar against all essentia transfer except through its tube connections. Phials, infusion and remote mirrors cannot access a braced jar.<BR>Picking up the jar returns the brace separately, while the jar retains its contents and label."
save(p,en)

# GUI-only models: the old block models have no item pose, and the matrix atlas
# is a 64px cube unwrap rather than a tiling stone texture. World renderers stay unchanged.
from copy import deepcopy
for name in ['pedestal_arcane','pedestal_ancient','pedestal_eldritch','infusion_matrix']:
 if name=='infusion_matrix':
  model=deepcopy(json.loads((A/'models/block/infusion_matrix.json').read_text()))
  uv={'up':[4,0,8,4],'down':[8,0,12,4],'west':[0,4,4,8],'north':[4,4,8,8],'east':[8,4,12,8],'south':[12,4,16,8]}
  for element in model['elements']:
   for face,value in element['faces'].items():value['uv']=uv[face]
 else:model={'parent':'thaumcraft:block/'+name}
 model['gui_light']='front';model['display']={'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[.9,.9,.9] if name=='infusion_matrix' else [.65,.65,.65]}}
 save(A/f'models/item/{name}.json',model);save(A/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/'+name}})
