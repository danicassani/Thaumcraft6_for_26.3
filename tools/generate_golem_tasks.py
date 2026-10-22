#!/usr/bin/env python3
"""Use/Lumber/Butcher content; run after generate_golem_logistics.py."""
import json,shutil
from loot_schema import for_resource
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft';O=R/'src/main/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(for_resource(p,v),ensure_ascii=False,indent=2)+'\n')
pages=json.loads((A/'knowledge/recipes.json').read_text())
for k,alias in [('use','SealUse'),('lumber','SealLumber'),('butcher','SealButcher')]:
 n='seal_'+k
 for suffix in ['.png','.png.mcmeta']:
  src=O/f'textures/items/seals/{n}{suffix}'
  if src.exists():
   dst=A/f'textures/blocks/{n}{suffix}';dst.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(src,dst)
 for folder in ['blockstates','models/block']:save(A/f'{folder}/{n}.json',json.loads((A/f'{folder}/seal_pickup.json').read_text().replace('seal_pickup',n)))
 save(A/f'models/item/{n}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/seals/'+n}});save(A/f'items/{n}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/'+n}})
 save(D/f'loot_table/blocks/{n}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:'+n}]}]})
 if k!='butcher':
  recipe={'type':'thaumcraft:crucible','research':'SEAL'+k.upper(),'catalyst':'thaumcraft:seal_'+('blank' if k=='use' else 'breaker'),'aspects':{'fabrico':20,'sensus':10,'cognitio':20} if k=='use' else {'herba':40,'sensus':20},'result':{'id':'thaumcraft:'+n}}
  save(D/f'recipe/crucible/{n}.json',recipe)
  desc=['Research: '+recipe['research'],'Catalyst: '+recipe['catalyst'],'Aspects: '+str(recipe['aspects']),'Result: thaumcraft:'+n]
 else:
  ingredients=['minecraft:leather','#minecraft:wool','minecraft:rabbit_hide','minecraft:porkchop','minecraft:mutton','minecraft:beef'];aspects={'bestia':10,'sensus':10,'humanus':10}
  save(D/f'recipe/infusion/{n}.json',{'type':'thaumcraft:infusion','research':'SEALBUTCHER','input':{'item':'thaumcraft:seal_guard'},'components':[{'item':x} for x in ingredients],'aspects':aspects,'instability':0,'result':{'id':'thaumcraft:'+n}})
  desc=['Research: SEALBUTCHER','Input: thaumcraft:seal_guard','Components: '+', '.join(ingredients),'Aspects: '+str(aspects),'Instability: 0','Result: thaumcraft:'+n]
 pages['thaumcraft:'+alias]=desc;pages['thaumcraft:'+n]=desc
save(A/'knowledge/recipes.json',pages)
labels={}
for k,en,es in [('use','Use Seal','Sello: Usar'),('lumber','Lumber Seal','Sello: Talar'),('butcher','Butcher Seal','Sello: Carnicero')]:
 for prefix in ['item','block']:labels[f'{prefix}.thaumcraft.seal_{k}']=(en,es)
for k,en,es in [('use_left','Left click','Clic izquierdo'),('use_air','Air target','Objetivo aire'),('use_empty','Empty hand','Mano vacía'),('use_sneak','Sneak','Agacharse'),('use_provision','Request item','Pedir objeto')]:labels['golem.thaumcraft.'+k]=(en,es)
guides={
 'SEALUSE':('Requires Smart and Deft. Uses the marked support face with a real survival hand; cargo samples are never payment. Third page: left/right click, target air in front of the seal, empty hand, sneak and request supplies. Air target right-clicks the support below that air cell. Request needs one whitelist sample and a matching same-owner/channel Provide seal; a worker fetches one physical item, then returns to use it. Empty/Vacate seals can also supply cargo. Exact components can be disabled on page two. Tools lose durability, items are consumed normally, and containers/remainders return to cargo or drop if full. Left click performs one native block break per action; destroying the support also detaches and drops the seal. Place it on a side face to till dirt, which requires air above. Forge interaction/break/place events and owner permissions apply. Menus are not operated automatically. No crafting requests, entity clicks, tag/mod matching or separate damage matching yet.',
 'Requiere Inteligente y Hábil. Usa la cara marcada del soporte con una mano real de supervivencia; las muestras no pagan acciones. Tercera página: clic izquierdo/derecho, objetivo aire delante del sello, mano vacía, agacharse y pedir suministros. En aire, el clic derecho se dirige al soporte bajo esa celda. Pedir necesita una muestra de lista blanca y Proporcionar del mismo propietario/canal: el gólem busca un objeto físico y vuelve a usarlo. Vaciar también puede abastecer la carga. La segunda página permite desactivar datos exactos. Las herramientas se desgastan y los objetos se consumen; recipientes y sobrantes vuelven a la carga o caen si no caben. Clic izquierdo rompe un bloque por acción nativa; si elimina el soporte, el sello se desprende y cae. Para labrar tierra coloca el sello en un lateral: la azada necesita aire encima. Respeta eventos Forge y permisos del dueño. No opera menús automáticamente. Pendientes peticiones de fabricación, clics en entidades, coincidencia por etiquetas/mod y daño separado.'),
 'SEALLUMBER':('Requires Smart and Breaker. Finds a lower log in the configured radius and cuts connected logs of the same block type, furthest first, one per second. Horizontal radius 1–8; vertical bounds −1 to twice that radius above the seal, at most 256 connected logs per search. Only loaded chunks, legal breakable logs and owner/Forge permissions. Drops use an iron axe; cargo overflow drops physically. Leaves decay normally. No replanting; player-placed logs in the working area are also eligible.',
 'Requiere Inteligente y Rompedor. Busca un tronco inferior dentro del radio y corta los troncos conectados del mismo bloque, empezando por el más lejano, uno por segundo. Radio horizontal 1–8; altura desde un bloque bajo el sello hasta el doble del radio por encima, máximo 256 troncos por búsqueda. Solo chunks cargados, troncos rompibles y permisos del dueño/eventos Forge. Botín con hacha de hierro; el exceso cae físicamente y las hojas decaen normalmente. No replanta; también corta troncos colocados por jugadores dentro del área.'),
 'SEALBUTCHER':('Requires Smart and Fighter. Attacks adult surplus animals only while more than two eligible adults of the same entity type remain inside the radius. Recounts before every hit, including with multiple workers. Babies, tamed/leashed animals, allies and owner pets are excluded from targets and from the breeding pair count. Drops remain physical for Collect seals. Does not breed or feed animals.',
 'Requiere Inteligente y Luchador. Ataca adultos sobrantes solo mientras haya más de dos adultos válidos del mismo tipo de entidad dentro del radio. Recuenta antes de cada golpe, incluso con varios trabajadores. Excluye crías, animales domesticados/con correa, aliados y mascotas del dueño, también del recuento de la pareja. El botín queda físico para Recoger. No cría ni alimenta animales.')}
for i,locale in enumerate(['en_us','es_es']):
 p=A/f'lang/{locale}.json';v=json.loads(p.read_text());v.update({k:pair[i] for k,pair in labels.items()})
 for key,pair in guides.items():v['research.'+key+'.stage.1']=pair[i]
 v['research.SEALPROVIDE.stage.2']=v['research.SEALPROVIDE.stage.2'].replace('until a Stock seal requests them','until a Stock or Use seal requests them').replace('hasta que Abastecer los pide','hasta que Abastecer o Usar los pide')
 save(p,v)
print('3 task seals, original crucible/infusion recipes and bilingual guides generated.')
