#!/usr/bin/env python3
"""Native theory-table models, recipes, notes and bilingual interface strings."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/modern/resources';AS=RES/'assets/thaumcraft';DATA=RES/'data/thaumcraft'
def save(path,value):
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n')
for name in ['table_wood','research_table']:
 model=json.loads((ROOT/f'src/main/resources/assets/thaumcraft/models/block/{name}.json').read_text())
 save(AS/f'models/block/{name}.json',model);save(AS/f'blockstates/{name}.json',{'variants':{'':{'model':f'thaumcraft:block/{name}'}}})
 save(AS/f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'thaumcraft:block/{name}'}})
 save(DATA/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'thaumcraft:{name}'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
notes=['sun']+[f'stars_{i}' for i in range(1,5)]+[f'moon_{i}' for i in range(1,9)]
for n in notes:
 save(AS/f'models/item/celestial/{n}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/celestial/sheet','layer1':'thaumcraft:items/celestial/'+n.replace('_','')}})
save(AS/'items/celestial_notes.json',{'model':{'type':'minecraft:select','property':'minecraft:component','component':'thaumcraft:celestial_note','cases':[{'when':i,'model':{'type':'minecraft:model','model':'thaumcraft:item/celestial/'+n}} for i,n in enumerate(notes)],'fallback':{'type':'minecraft:model','model':'thaumcraft:item/celestial/sun'}}})
save(AS/'models/item/scribing_tools.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/scribing_tools'}})
save(AS/'items/scribing_tools.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/scribing_tools'}})
save(DATA/'recipe/table_wood.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['SSS','W W'],'key':{'S':'#minecraft:wooden_slabs','W':'#minecraft:planks'},'result':{'id':'thaumcraft:table_wood'}})
save(DATA/'recipe/scribing_tools.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':['minecraft:glass_bottle','minecraft:feather','minecraft:black_dye'],'result':{'id':'thaumcraft:scribing_tools'}})
save(DATA/'recipe/scribing_refill.json',{'type':'thaumcraft:scribing_refill'})
ui={
 'start':('Start','Empezar'),'draw':('Draw 2','Robar 2'),'bonus':('Draw 3','Robar 3'),'finish':('Finish','Terminar'),'cancel':('Abandon','Abandonar'),'choose':('Choose','Elegir'),
 'status':('Inspiration %s/%s   Bonus draws %s','Inspiración %s/%s   Robos extra %s'),'card_cost':('Inspiration: %s','Inspiración: %s'),
 'supplies':('Insert ink and paper; select optional aids below.','Pon tinta y papel; elige ayudas abajo.'),'locked':('Complete Theorycrafting in the Thaumonomicon.','Completa Teorías en el Thaumonomicon.'),
 'invalid':('Unreadable session preserved. Actions disabled.','Sesión ilegible conservada. Acciones desactivadas.'),'owner':('Another researcher owns this theory.','Esta teoría pertenece a otro investigador.'),
 'consume':('Consumes: %s','Consume: %s'),'keep':('Requires (kept): %s','Requiere (se conserva): %s'),
 'sky_repeat':('Already observed today.','Ya lo has observado hoy.'),'sky_supplies':('You need paper and scribing tools with ink.','Necesitas papel y herramientas con tinta.'),'sky_success':('Recorded: %s','Anotado: %s')}
# Sorted aid IDs match TheoryCards.AID_KEYS.
for i,(en,es) in enumerate([('Beacon','Baliza'),('Enchanting table','Encantamientos'),('End portal','Portal del End'),('Nether portal','Portal del Nether'),('Arcane table','Mesa arcana'),('Ancient glyphs','Glifos antiguos')]):ui['aid.'+str(i)]=(en,es)
cards={
'experimentation':('Experimentation','Experimentación','Random category +15–30%; Basics +1–10%.','Categoría aleatoria +15–30%; Básicos +1–10%.'),
'analyze':('Analyze','Analizar','Spend 16 observations: %s +25–50%; Basics +5%.','Gasta 16 observaciones: %s +25–50%; Básicos +5%.'),
'balance':('Balance','Equilibrar','Equalize categories; Basics +5%; reduce penalties.','Iguala categorías; Básicos +5%; reduce penalización.'),
'ponder':('Ponder','Meditar','Spread +25%; Basics +5%; bonus draw.','Reparte +25%; Básicos +5%; robo extra.'),
'rethink':('Rethink','Reconsiderar','Lose 10%; Basics +1–10%; recover inspiration.','Pierde 10%; Básicos +1–10%; recupera inspiración.'),
'reject':('Reject','Rechazar','Block %s; Basics +5%.','Bloquea %s; Básicos +5%.'),
'inspired':('Inspired','Inspiración','%s +%s%%.','%s +%s%%.'),
'calibrate':('Calibrate','Calibrar','Artifice +15%; bonus draw.','Artificio +15%; robo extra.'),
'focus':('Focus','Concentrarse','Aura +15%; bonus draw.','Aura +15%; robo extra.'),
'measure':('Measure','Medir','Infusion +15%; bonus draw.','Infusión +15%; robo extra.'),
'scripting':('Scripting','Programar','Golems +25%; extra ink and paper.','Gólems +25%; tinta y papel extra.'),
'concentrate':('Concentrate','Concentrar','Alchemy +15%; bonus draw; 33% inspiration.','Alquimia +15%; robo extra; 33% inspiración.'),
'reactions':('Reactions','Reacciones','Alchemy +25%; 33% inspiration.','Alquimia +25%; 33% inspiración.'),
'synthesis':('Synthesis','Síntesis','Alchemy +40%; creates compound crystal.','Alquimia +40%; crea un cristal compuesto.'),
 'tinker':('Tinker','Trastear','Artifice gains progress based on item aspects.','Artificio gana progreso según los aspectos.'),
 'mindmatter':('Mind over matter','Mente y materia','Consumes device for Artifice progress.','Consume un dispositivo para Artificio.'),
 'celestial':('Celestial','Celestial','%s +25–50%; sun/moon/star bonuses and warp.','%s +25–50%; bonos solares/lunares y warp.'),
 'spellbinding':('Spellbinding','Hechizar','Spend up to 5 XP levels: Aura +5% per level.','Gasta hasta 5 niveles: Aura +5% por nivel.'),
 'awareness':('Awareness','Conciencia','Aura +20%; 33% Eldritch and warp.','Aura +20%; 33% Oculto y warp.'),
 'realization':('Realization','Comprensión','Eldritch +15%; two random bonuses; warp.','Oculto +15%; dos bonos aleatorios; warp.'),
 'revelation':('Revelation','Revelación','Eldritch +30%; random bonus; warp.','Oculto +30%; bono aleatorio; warp.'),
 'truth':('Truth','Verdad','Eldritch +10–25%; bonus draw; warp.','Oculto +10–25%; robo extra; warp.'),
 'synergy':('Synergy','Sinergia','Convert 15% Artifice/Alchemy/Infusion to 30% Golems.','Convierte 15% de otras ramas en 30% Gólems.'),
 'enchantment':('Enchantment','Encantamiento','Spend 5 levels: Infusion and Aura +15–20%.','Gasta 5 niveles: Infusión y Aura +15–20%.'),
 'beacon':('Beacon','Baliza','Recover 2 inspiration; bonus draw; reduce penalties.','Recupera 2 inspiración; robo extra; reduce penalización.'),
 'glyphs':('Glyphs','Glifos','Random and Eldritch +10–20%; warp.','Aleatoria y Oculto +10–20%; warp.'),
 'portal':('Portal','Portal','Recover inspiration; three bonuses and warp.','Recupera inspiración; tres bonos y warp.')}
for loc,index in [('en_us',0),('es_es',1)]:
 path=AS/f'lang/{loc}.json';lang=json.loads(path.read_text());lang.update({'theory.thaumcraft.'+k:v[index] for k,v in ui.items()})
 for key,v in cards.items():lang['theory.thaumcraft.card.'+key]=v[index];lang['theory.thaumcraft.effect.'+key]=v[index+2]
 lang.update({'block.thaumcraft.table_wood':['Wood Table','Mesa de madera'][index],'block.thaumcraft.research_table':['Research Table','Mesa de investigación'][index],'item.thaumcraft.scribing_tools':['Scribing Tools','Herramientas de escritura'][index],'item.thaumcraft.celestial_notes':['Celestial Notes','Notas celestes'][index]})
 for i,n in enumerate(notes):lang['item.thaumcraft.celestial_notes.'+n]=(['Sun','Stars: North','Stars: South','Stars: West','Stars: East']+[f'Moon phase {j}' for j in range(1,9)] if index==0 else ['Sol','Estrellas: Norte','Estrellas: Sur','Estrellas: Oeste','Estrellas: Este']+[f'Fase lunar {j}' for j in range(1,9)])[i]
 save(path,lang)
path=AS/'knowledge/recipes.json'
if path.exists():
 pages=json.loads(path.read_text());pages.update({'thaumcraft:tablewood':['SSS / W W','S: wooden slab; W: wooden plank','Use scribing tools on this table to create a research table.'],'thaumcraft:inkwell':['Glass bottle + feather + black dye','Refill a used tool with black dye.']});save(path,pages)
print('2 tables, scribing tools/refill, 13 celestial variants, 27 card descriptions')
