#!/usr/bin/env python3
"""Advanced seals and physical provision routes; run after generate_golem_content.py."""
import json,shutil
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft';O=R/'src/main/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
recipes=[('pickup_advanced','pickup','SEALCOLLECT&&MINDBIOTHAUMIC',{'sensus':10,'cognitio':10},'SealCollectAdv'),('fill_advanced','fill','SEALSTORE&&MINDBIOTHAUMIC',{'sensus':10,'cognitio':10},'SealStoreAdv'),('empty_advanced','empty','SEALEMPTY&&MINDBIOTHAUMIC',{'sensus':10,'cognitio':10},'SealEmptyAdv'),('guard_advanced','guard','SEALGUARD&&MINDBIOTHAUMIC',{'sensus':20,'cognitio':20},'SealGuardAdv'),('breaker_advanced','breaker','SEALBREAK&&MINDBIOTHAUMIC',{'sensus':10,'cognitio':10,'instrumentum':20},'SealBreakAdv'),('provider','empty_advanced','SEALPROVIDE',{'permutatio':10,'desiderium':10},'SealProvide'),('stock','fill','SEALSTOCK',{'cognitio':10,'desiderium':10},'SealStock')]
pages=json.loads((A/'knowledge/recipes.json').read_text())
for k,base,research,aspects,alias in recipes:
 n='seal_'+k
 for suffix in ['.png','.png.mcmeta']:
  src=O/f'textures/items/seals/{n}{suffix}'
  if src.exists():
   dst=A/f'textures/blocks/{n}{suffix}';dst.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(src,dst)
 for folder in ['blockstates','models/block']:
  template=json.loads((A/f'{folder}/seal_pickup.json').read_text().replace('seal_pickup',n));save(A/f'{folder}/{n}.json',template)
 save(A/f'models/item/{n}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/seals/'+n}});save(A/f'items/{n}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/'+n}})
 save(D/f'loot_table/blocks/{n}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:'+n}]}]})
 save(D/f'recipe/crucible/{n}.json',{'type':'thaumcraft:crucible','research':research,'catalyst':'thaumcraft:seal_'+base,'aspects':aspects,'result':{'id':'thaumcraft:'+n}})
 pages['thaumcraft:'+alias]=['Research: '+research,'Catalyst: thaumcraft:seal_'+base,'Aspects: '+str(aspects),'Result: thaumcraft:'+n];pages['thaumcraft:'+n]=pages['thaumcraft:'+alias]
save(A/'knowledge/recipes.json',pages)
p=A/'knowledge/research.json';research=json.loads(p.read_text())
for e in research:
 for k,base,key,aspects,alias in recipes[:5]:
  if e['key']==key.split('&&')[0]:
   stage=e['stages'][-1];stage['recipes']=list(dict.fromkeys(stage.get('recipes',[])+['thaumcraft:'+alias]))
save(p,research)
labels={}
for k,en,es in [('pickup_advanced','Advanced Collect Seal','Sello avanzado: Recoger'),('fill_advanced','Advanced Store Seal','Sello avanzado: Guardar'),('empty_advanced','Advanced Empty Seal','Sello avanzado: Vaciar'),('guard_advanced','Advanced Guard Seal','Sello avanzado: Vigilar'),('breaker_advanced','Advanced Break Seal','Sello avanzado: Romper'),('provider','Provide Seal','Sello: Proporcionar'),('stock','Stock Seal','Sello: Abastecer')]:
 for pre in ['item','block']:labels[f'{pre}.thaumcraft.seal_{k}']=(en,es)
for k,en,es in [('filter_index','Filter %s/%s','Filtro %s/%s'),('exact','Exact data','Datos exactos'),('more','Amount +1','Cantidad +1'),('less','Amount −1','Cantidad −1'),('guard_targets','Targets','Objetivos'),('guard_0','Monsters','Monstruos'),('guard_1','Animals','Animales'),('guard_2','Players','Jugadores'),('silk','Silk Touch','Toque de seda'),('filter_selected','Filter %s/%s • %s','Filtro %s/%s • %s')]:labels['golem.thaumcraft.'+k]=(en,es)
guide_en=' Advanced seals require Smart (and Fighter/Breaker where applicable). Click a ghost slot to select one of nine samples; Set filter copies your held item, or the offhand item when holding a bell. Samples are not consumed. The second page controls exact components, exclusion, guard targets and Silk Touch. Empty exclusions match nothing. Guard can explicitly target animals or players, protecting allies/pets/creative and respecting PVP. Break remains a single support block, with optional Silk Touch.'
guide_es=' Los sellos avanzados requieren Inteligente (y Luchador/Rompedor cuando corresponda). Pulsa un hueco fantasma para elegir una de nueve muestras; Fijar filtro copia el objeto en mano o en la secundaria si sostienes la campana. No consume muestras. La segunda página controla datos exactos, exclusión, objetivos de guardia y Seda. Excluir sin muestras no acepta nada. Vigilar puede dirigirse expresamente a animales o jugadores, protegiendo aliados/mascotas/creativo y respetando PVP. Romper sigue trabajando un solo bloque soporte, con Seda opcional.'
for i,loc in enumerate(['en_us','es_es']):
 p=A/f'lang/{loc}.json';v=json.loads(p.read_text());v.update({k:t[i] for k,t in labels.items()});v['research.CONTROLSEALS.stage.1']=v['research.CONTROLSEALS.stage.1'].split(' Advanced seals require Smart' if i==0 else ' Los sellos avanzados requieren Inteligente')[0]+(guide_en if i==0 else guide_es)
 v['research.SEALPROVIDE.stage.2']=('Provide leaves items in its inventory until a Stock seal requests them. Same owner/channel, loaded chunks and at most 32 blocks from the worker home. Providers and Stock reject Clumsy, but do not require Smart. All extraction respects the marked Forge inventory face.' if i==0 else 'Proporcionar deja los objetos en su inventario hasta que Abastecer los pide. Deben compartir propietario/canal y estar cargados, a 32 bloques del hogar del gólem. Ambos rechazan Torpe, pero no exigen Inteligente. La extracción respeta la cara Forge marcada.')
 v['research.SEALSTOCK.stage.2']=('Configure up to nine samples and exact target amounts. Set filter initializes the amount from the held stack; the second page adjusts it by one, 1–4096. Matching duplicate samples use the largest goal. Only the deficit that fits is requested. A worker walks to a provider and back with physical cargo. Delivery recounts inventory to avoid overstock; surplus stays in cargo. Loaded powered destinations pause a pending delivery; unloaded destinations wait. Removed or incompatible destinations release the route and keep the cargo for another valid destination or manual recovery. Routes with cargo persist across restart; empty reservations are rebuilt.' if i==0 else 'Configura hasta nueve muestras y cantidades objetivo. Fijar filtro toma la cantidad de la pila en mano; la segunda página ajusta de uno en uno, 1–4096. Muestras equivalentes usan el objetivo mayor. Solo se pide el déficit que cabe. El gólem camina al proveedor y vuelve con carga física. Recuenta al entregar para no sobreabastecer; el sobrante permanece en su carga. El destino cargado con redstone pausa el viaje y el descargado espera. Un destino retirado o incompatible libera la ruta conservando los objetos para otro destino válido o recuperación manual. La carga y ruta sobreviven al reinicio; las reservas vacías se reconstruyen.')
 save(p,v)
print('7 advanced/logistics seals, original recipe costs and bilingual guides generated.')
