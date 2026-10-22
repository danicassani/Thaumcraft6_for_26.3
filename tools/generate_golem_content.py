#!/usr/bin/env python3
"""Functional first golem release: only active parts and six task seals."""
import json,shutil
from loot_schema import for_resource
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(for_resource(p,v),ensure_ascii=False,indent=2)+'\n')
def shaped(n,pat,key,count=1):return {'type':'minecraft:crafting_shaped','pattern':pat,'key':key,'result':{'id':'thaumcraft:'+n,'count':count}}
def arc(n,res,vis,crystals,pat,key,count=1):save(D/f'recipe/arcane/{n}.json',{'type':'thaumcraft:arcane','research':res,'vis':vis,'crystals':crystals,'recipe':shaped(n,pat,key,count)})
def inf(n,res,base,parts,aspects,inst):save(D/f'recipe/infusion/{n}.json',{'type':'thaumcraft:infusion','research':res,'input':{'item':'thaumcraft:'+base},'components':[{'item':p if ':' in p else 'thaumcraft:'+p} for p in parts],'aspects':aspects,'instability':inst,'result':{'id':'thaumcraft:'+n}})
for n,tex in [('golem','golem'),('golem_bell','golem_bell'),('seal_blank','seals/seal_blank')]+[(f'seal_{k}',f'seals/seal_{k}') for k in ['pickup','fill','empty','guard','harvest','breaker']]:
 save(A/f'models/item/{n}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/'+tex}});save(A/f'items/{n}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/'+n}})
for k in ['pickup','fill','empty','guard','harvest','breaker']:
 n='seal_'+k
 for suffix in ['.png','.png.mcmeta']:
  src=R/f'src/main/resources/assets/thaumcraft/textures/items/seals/{n}{suffix}'
  if src.exists():
   dest=A/f'textures/blocks/{n}{suffix}';dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(src,dest)
 save(A/f'models/block/{n}.json',{'textures':{'particle':'thaumcraft:blocks/'+n,'seal':'thaumcraft:blocks/'+n},'elements':[{'from':[3,0,3],'to':[13,1,13],'faces':{face:{'texture':'#seal'} for face in ['up','down','north','south','east','west']}}]})
 save(A/f'blockstates/{n}.json',{'variants':{'facing='+f:{'model':'thaumcraft:block/'+n,**({'x':x} if x else {}),**({'y':y} if y else {})} for f,x,y in [('up',0,0),('down',180,0),('north',90,0),('south',90,180),('west',90,270),('east',90,90)]}})
 save(D/f'loot_table/blocks/{n}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:'+n}]}]})
save(A/'models/block/golem_builder.json',{'parent':'minecraft:block/cube_all','textures':{'all':'thaumcraft:blocks/golembuilder'}})
save(A/'blockstates/golem_builder.json',{'variants':{'':{'model':'thaumcraft:block/golem_builder'}}})
save(A/'items/golem_builder.json',{'model':{'type':'minecraft:model','model':'thaumcraft:block/golem_builder'}})
save(D/'loot_table/blocks/golem_builder.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:golem_builder'}]}]})
# Compact press substitutes the old placeholder multiblock; retains all five construction ingredients.
arc('golem_builder','MINDCLOCKWORK@2',50,{'terra':1,'ordo':1},['IPC','ATS'],{'I':'minecraft:iron_bars','P':'minecraft:piston','C':'minecraft:cauldron','A':'minecraft:anvil','T':'thaumcraft:table_stone','S':'thaumcraft:salis_mundus'})
save(D/'recipe/golem_bell.json',shaped('golem_bell',[' QQ',' QQ','S  '],{'Q':'minecraft:quartz','S':'minecraft:stick'}))
arc('mind_clockwork','MINDCLOCKWORK@2',25,{'ignis':1,'ordo':1},[' P ','PGP','BCB'],{'P':'minecraft:glass_pane','G':'thaumcraft:mechanism_simple','B':'thaumcraft:plate_brass','C':'minecraft:comparator'})
inf('mind_biothaumic','MINDBIOTHAUMIC','mind_clockwork',['brain','mechanism_complex'],{'cognitio':50,'machina':25},4)
arc('module_aggression','SEALGUARD',50,{'ignis':1},[' R ','RTR','PGP'],{'R':'minecraft:glass_pane','T':'minecraft:blaze_powder','P':'thaumcraft:plate_brass','G':'thaumcraft:mechanism_simple'})
arc('seal_blank','CONTROLSEALS',20,{'aer':1},['CTR',' N '],{'C':'minecraft:clay_ball','T':'thaumcraft:tallow','R':'minecraft:red_dye','N':'thaumcraft:nitor_yellow'},3)
for k,res,aspects in [('pickup','SEALCOLLECT',{'desiderium':10}),('fill','SEALSTORE',{'aversio':10}),('empty','SEALEMPTY',{'vacuos':10}),('guard','SEALGUARD',{'aversio':20,'praemunio':20})]:save(D/f'recipe/crucible/seal_{k}.json',{'type':'thaumcraft:crucible','research':res,'catalyst':'thaumcraft:seal_blank','aspects':aspects,'result':{'id':'thaumcraft:seal_'+k}})
inf('seal_harvest','SEALHARVEST','seal_blank',['minecraft:wheat_seeds','minecraft:pumpkin_seeds','minecraft:melon_seeds','minecraft:beetroot_seeds','minecraft:sugar_cane','minecraft:cactus'],{'herba':10,'sensus':10,'humanus':10},0)
inf('seal_breaker','SEALBREAK','seal_blank',['minecraft:golden_axe','minecraft:golden_pickaxe','minecraft:golden_shovel'],{'instrumentum':10,'perditio':10,'humanus':10},1)
labels={'block.thaumcraft.golem_builder':('Golem Press','Prensa de gólems'),'item.thaumcraft.golem':('Golem','Gólem'),'entity.thaumcraft.golem':('Thaumic Golem','Gólem taumatúrgico'),'item.thaumcraft.golem_bell':('Golemancer’s Bell','Campana de golemante'),'item.thaumcraft.seal_blank':('Blank Control Seal','Sello de control vacío')}
for k,en,es in [('pickup','Collect','Recoger'),('fill','Store','Guardar'),('empty','Empty','Vaciar'),('guard','Guard','Vigilar'),('harvest','Harvest','Cosechar'),('breaker','Break','Romper')]:
 for prefix in ['item','block']:labels[f'{prefix}.thaumcraft.seal_{k}']=(en+' Seal','Sello: '+es)
for k,en,es in [('part_0','Material','Material'),('part_1','Head','Cabeza'),('part_2','Arms','Brazos'),('part_3','Addon','Mejora'),('build','Build','Construir'),('wood','Greatwood','Gran madera'),('iron','Iron','Hierro'),('clay','Clay','Arcilla'),('brass','Brass','Latón'),('thaumium','Thaumium','Taumio'),('void','Void','Vacío'),('basic','Basic','Básica'),('smart','Smart','Inteligente'),('smart_armored','Smart armored','Inteligente blindada'),('fine','Fine','Precisos'),('claws','Claws','Garras'),('breakers','Breakers','Rompedores'),('none','None','Ninguna'),('armored','Armored','Blindado'),('fighter','Fighter','Luchador'),('hauler','Hauler','Porteador'),('channel','Channel %s','Canal %s'),('priority','Priority','Prioridad'),('radius','Radius','Radio'),('redstone','Redstone','Redstone'),('set_filter','Set filter','Fijar filtro'),('clear_filter','Clear filter','Quitar filtro'),('blacklist','Exclude','Excluir'),('filter_hint','Filter: held item','Filtro: objeto en mano'),('home','Home set for %s golems','Hogar fijado: %s gólems'),('mode_0','Work','Trabajar'),('mode_1','Follow','Seguir'),('mode_2','Stay','Esperar')]:labels['golem.thaumcraft.'+k]=(en,es)
labels['golem.thaumcraft.missing_research']=('Research for one or more selected parts is incomplete.','Falta completar la investigación de alguna pieza seleccionada.')
labels['golem.thaumcraft.missing_components']=('Missing components. Hover Build to see the full list.','Faltan materiales. Pasa el cursor sobre Construir para ver la lista.')
labels['golem.thaumcraft.smart_pending']=('Advanced seals for Smart are not available yet.','Los sellos avanzados para Inteligente aún no están disponibles.')
for k,en,es in [('light','Light','Ligero'),('heavy','Heavy','Pesado'),('fragile','Fragile','Frágil'),('fireproof','Fireproof','Incombustible'),('blastproof','Blast resistant','Resistente a explosiones'),('repair','Self-repair','Autorreparación'),('deft','Deft','Destreza'),('clumsy','Clumsy','Torpe'),('brutal','Brutal','Brutal'),('breaker','Breaker','Rompedor')]:labels['golem.thaumcraft.'+k]=(en,es)
# Dedicated channel label for UI, where the integer is added by the screen.
labels['golem.thaumcraft.channel_button']=('Channel','Canal')
guides={
'research.MINDCLOCKWORK.stage.3':('Build the compact press using the recipe. Choose material, head, arms and addon, then Build. All listed components are taken from your inventory atomically. Feed Machina essentia from the sides or bottom; the paid job survives restarts. Take the golem and use it on a free block. Bell on a golem cycles Work/Follow/Stay; sneak bell packs it without restoring health. Bell on the ground assigns nearby golems a home.','Fabrica la prensa compacta con la receta. Elige material, cabeza, brazos y mejora; Construir consume todos los componentes de tu inventario. Alimenta esencia Machina por los lados o debajo; el trabajo pagado sobrevive al reinicio. Retira el gólem y úsalo sobre un bloque libre. La campana sobre él alterna Trabajar/Seguir/Esperar; agachado lo recoge sin curarlo. La campana sobre el suelo fija el hogar de los gólems cercanos.'),
'research.CONTROLSEALS.stage.1':('Seals attach as visible plates in the adjacent free cell. Sneak-use a bell on a seal to configure owner-only channel 0–15, priority, radius, redstone pause and an exact held-item filter. Clear the filter for any item. Bell use in the air changes its channel; bell on a seal copies it. Golems work only for their owner and channel within 32 blocks of home, while their owner is online in that dimension.','Los sellos se colocan como placas visibles en la celda libre junto al soporte. Usa la campana agachado sobre el sello para configurar canal 0–15, prioridad, radio, pausa por redstone y filtro exacto del objeto en mano. Quita el filtro para admitir cualquiera. La campana al aire cambia su canal; sobre un sello lo copia. Los gólems trabajan solo para su propietario y canal a 32 bloques del hogar mientras su dueño está conectado en esa dimensión.'),
'research.SEALCOLLECT.stage.1':('Collect seals pick up physical dropped items. Store seals insert cargo into the inventory behind their support face. A worker carries one stack, or two with Hauler. It walks to each task and keeps cargo when destinations fill or chunks unload. Use different channels to separate routes.','Recoger toma objetos físicos del suelo. Guardar inserta la carga en el inventario del soporte por la cara del sello. El gólem lleva una pila o dos con Porteador. Camina hasta cada tarea y conserva la carga si se llena el destino o se descarga un chunk. Separa rutas con canales.'),
'research.SEALEMPTY.stage.2':('Empty extracts through the marked face into the golem, then Store delivers. Filters compare item components too. Priorities choose tasks; inventory insertion and extraction respect Forge sided capabilities.','Vaciar extrae por la cara marcada hacia el gólem; Guardar entrega después. Los filtros comparan también los componentes. Las prioridades eligen tareas y el transporte respeta las capacidades Forge de cada cara.'),
'research.SEALGUARD.stage.2':('A Fighter golem attacks visible hostile monsters in the seal radius. It protects its owner, allies and pets. Equip the Fighter addon or Claws arms.','Un gólem Luchador ataca monstruos hostiles visibles dentro del radio del sello. Protege a su dueño, aliados y mascotas. Usa la mejora Luchador o brazos de Garras.'),
'research.SEALHARVEST.stage.2':('Fine arms grant Deft. Harvest handles mature native crops, reserves one seed from the actual drops and replants; drops enter cargo and overflow falls physically. Store seals deliver the harvest.','Los brazos Precisos conceden Destreza. Cosechar trabaja cultivos nativos maduros, reserva una semilla del botín real y replanta. Guarda la cosecha en la carga y deja el excedente en el suelo. Guardar entrega la cosecha.'),
'research.SEALBREAK.stage.2':('Breaker arms are required. The seal breaks its support block with iron-pickaxe loot, subject to player permissions and Forge cancellation. It refuses block entities, unbreakable blocks and hardness above 5. It is a single-block task, not a quarry.','Requiere brazos Rompedores. Rompe el bloque soporte con botín de pico de hierro, sujeto a permisos del jugador y cancelación Forge. Rechaza entidades de bloque, bloques irrompibles y dureza mayor que 5. Trabaja un solo bloque; no es una cantera.')}
for i,locale in enumerate(['en_us','es_es']):
 p=A/f'lang/{locale}.json';v=json.loads(p.read_text());v.update({k:a[i] for k,a in labels.items()});v.update({k:a[i] for k,a in guides.items()});save(p,v)
p=A/'knowledge/recipes.json';v=json.loads(p.read_text());aliases={'golem_builder':'GolemPress','golem_bell':'GolemBell','mind_clockwork':'MindClockwork','mind_biothaumic':'MindBiothaumic','module_aggression':'modaggression','seal_blank':'SealBlank','seal_pickup':'SealCollect','seal_fill':'SealStore','seal_empty':'SealEmpty','seal_guard':'SealGuard','seal_harvest':'SealHarvest','seal_breaker':'SealBreak'}
for n,alias in aliases.items():
 path=next(p for p in (D/'recipe').rglob(n+'.json'));recipe=json.loads(path.read_text());lines=['Research: '+recipe.get('research',''), 'Result: '+n]
 if 'recipe' in recipe:
  r=recipe['recipe'];lines+=['Vis: '+str(recipe['vis']), 'Crystals: '+str(recipe['crystals']), 'Pattern: '+' / '.join(r['pattern'])]+[f'{k}: {value}' for k,value in r['key'].items()]
 elif 'pattern' in recipe:lines+=['Pattern: '+' / '.join(recipe['pattern'])]+[f'{k}: {value}' for k,value in recipe['key'].items()]
 elif 'catalyst' in recipe:lines+=['Catalyst: '+recipe['catalyst'],'Aspects: '+str(recipe['aspects'])]
 else:lines+=['Center: '+recipe['input']['item'],'Offerings: '+', '.join(p['item'] for p in recipe['components']),'Aspects: '+str(recipe['aspects'])]
 v['thaumcraft:'+alias]=lines;v['thaumcraft:'+n]=lines
save(p,v)
print('Golem press, 6 seals, active parts, recipes and bilingual guides generated.')
# The original ore-dictionary scans must have concrete modern providers.
p=A/'knowledge/aspects.json';v=json.loads(p.read_text());triggers=v['triggers']['items']
for mat,ids in {'IRON':['minecraft:iron_ingot','minecraft:iron_block','minecraft:iron_ore','minecraft:deepslate_iron_ore','thaumcraft:plate_iron'],'BRASS':['thaumcraft:ingot_brass','thaumcraft:metal_brass','thaumcraft:plate_brass'],'THAUMIUM':['thaumcraft:ingot_thaumium','thaumcraft:metal_thaumium','thaumcraft:plate_thaumium'],'VOID':['thaumcraft:ingot_void','thaumcraft:metal_void','thaumcraft:plate_void']}.items():
 for id in ids:triggers[id]=list(dict.fromkeys(triggers.get(id,[])+['f_MAT'+mat]))
save(p,v)
