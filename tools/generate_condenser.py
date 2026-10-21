#!/usr/bin/env python3
"""Run last: original flux condenser, connected lattices, recipes and guide."""
import json,copy
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft';O=R/'src/main/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
for n in ['condenser','condenser_lattice_core','condenser_lattice_core_dirty','condenser_lattice_side']:
 m=json.loads((O/f'models/block/{n}.json').read_text());m.pop('__createdwith',None);m['render_type']='minecraft:cutout';save(A/f'models/block/{n}.json',m)
m=json.loads((A/'models/block/condenser.json').read_text());m['textures']['side']='thaumcraft:blocks/condenser_off';save(A/'models/block/condenser_off.json',m)
save(A/'blockstates/condenser.json',{'variants':{'enabled=true':{'model':'thaumcraft:block/condenser'},'enabled=false':{'model':'thaumcraft:block/condenser_off'}}})
for n in ['condenser_lattice','condenser_lattice_dirty']:
 m=json.loads((O/f'blockstates/{n}.json').read_text());
 for part in m['multipart']:part['apply']['model']=part['apply']['model'].replace('thaumcraft:','thaumcraft:block/')
 m['multipart']=[{**v,'when':{k:str(val).lower() for k,val in v['when'].items()}} if 'when' in v else v for v in m['multipart']];save(A/f'blockstates/{n}.json',m)
for n in ['condenser','condenser_lattice','condenser_lattice_dirty']:
 if n=='condenser':model='thaumcraft:block/condenser'
 else:
  model='thaumcraft:block/'+n+'_item';save(A/f'models/block/{n}_item.json',{'parent':'thaumcraft:block/condenser_lattice_core'+('_dirty' if n.endswith('dirty') else '')})
 save(A/f'items/{n}.json',{'model':{'type':'minecraft:model','model':model}})
 save(D/f'loot_table/blocks/{n}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:'+n}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
recipes={
'condenser':{'type':'thaumcraft:arcane','research':'FLUXCLEANUP','vis':500,'crystals':{'aer':5,'aqua':5,'perditio':5},'recipe':{'type':'minecraft:crafting_shaped','pattern':['BCB','WMW','BTB'],'key':{'T':'thaumcraft:tube','C':'thaumcraft:morphic_resonator','W':'#minecraft:planks','M':'thaumcraft:mechanism_complex','B':'thaumcraft:plate_brass'},'result':{'id':'thaumcraft:condenser'}}},
'condenser_lattice':{'type':'thaumcraft:arcane','research':'FLUXCLEANUP','vis':100,'crystals':{'terra':3,'aer':3},'recipe':{'type':'minecraft:crafting_shaped','pattern':['QTQ','QFQ','QTQ'],'key':{'T':'thaumcraft:plate_thaumium','F':'thaumcraft:filter','Q':'minecraft:quartz'},'result':{'id':'thaumcraft:condenser_lattice'}}}}
for n,r in recipes.items():save(D/f'recipe/arcane/{n}.json',r)
p=A/'knowledge/recipes.json';v=json.loads(p.read_text())
for n,key in [('condenser','Condenser'),('condenser_lattice','CondenserLattice')]:
 r=recipes[n];v['thaumcraft:'+key]=[' / '.join(r['recipe']['pattern'])]+[k+': '+val for k,val in r['recipe']['key'].items()]+['Vis '+str(r['vis'])+'; crystals '+', '.join(k+' '+str(n) for k,n in r['crystals'].items())]
save(p,v)
for loc,es in [('en_us',False),('es_es',True)]:
 p=A/f'lang/{loc}.json';v=json.loads(p.read_text())
 for n,en,sp in [('condenser','Flux Condenser','Condensador de flux'),('condenser_lattice','Condenser Lattice','Celosía de condensación'),('condenser_lattice_dirty','Clogged Condenser Lattice','Celosía de condensación atascada')]:v['block.thaumcraft.'+n]=sp if es else en
 v['condenser.thaumcraft.status']=('Combustible %s/100 · Vitium %s/100 · Coste %s · Ciclo %s/%s ticks · Celosías %s · %s' if es else 'Fuel %s/100 · Vitium %s/100 · Cost %s · Cycle %s/%s ticks · Lattices %s · %s')
 for n,en,sp in [('ready','Ready','Listo'),('paused','Redstone paused','Pausa redstone'),('clogged','Missing or clogged lattice','Sin celosía eficaz o atascada'),('unloaded','Waiting for loaded network','Esperando a la red cargada'),('shared','Lattice shared by condensers','Celosías compartidas entre condensadores')]:v['condenser.thaumcraft.'+n]=sp if es else en
 guide=(' En este port: celosía encima del condensador y ramificaciones por encima de la base, dentro de distancia al cuadrado 74. Se conserva el cálculo original ordenado: penalización de conexiones, ramas atascadas, eficiencia máxima 40, intervalo máximo(5, redondear(600 − 15 × eficiencia)) y coste entero(4 + raíz del número de celosías). No compartas celosías entre bases. Aspira cualquier aspecto salvo Vitium por los lados, succión 128, hasta 100 unidades. Cada ciclo paga el coste de esencia y retira exactamente una unidad de flux del aura; produce un Vitium, hasta 100. Extrae por debajo hacia tubos o tarros. Redstone pausa entrada y avance, pero permite retirar salida. Una salida llena o menos de un flux en el aura conserva combustible y progreso. Cada conversión tiene probabilidad 1/50 de atascar una celosía; ofrecer Vitium por un lateral atasca sin consumir ese Vitium. Clic con un filtro limpia y puede devolver un cristal de Vitium (50 %). Mano vacía consulta costes, ciclo y depósitos. No carga chunks: si la red tiene bordes desconocidos espera. Actualiza el cálculo cada segundo y lo revalida antes del pago. Guardado versionado de depósitos, ciclos y total; datos futuros quedan preservados e inactivos. Romper la base libera el contenido de ambos depósitos como flux y las celosías sin soporte se desprenden.' if es else ' In this port: place a lattice above the condenser, branching above the base within squared distance 74. Original ordered calculation: connection penalties, clogged branches, efficiency capped at 40, interval max(5, round(600 − 15 × efficiency)), integer cost(4 + square root of lattice count). Do not share lattices between bases. Sides pull any aspect except Vitium at suction 128 into a 100-unit fuel buffer. Each cycle pays its fuel cost and removes exactly one ambient flux, producing one Vitium in a 100-unit buffer. Extract below into tubes or jars. Redstone pauses input and progress while allowing output. Full output or less than one ambient flux preserves fuel and progress. Each conversion has a 1/50 clog chance; presenting Vitium at a side clogs without consuming it. Use a filter to clean, with a 50% Vitium crystal chance. An empty hand reports cost, cycle and buffers. Never loads chunks: unknown network edges wait. Recalculates each second and revalidates before payment. Versioned buffers, progress and conversion total; future data is preserved inactive. Breaking the base releases both buffers as flux and unsupported lattices drop.')
 k='research.FLUXCLEANUP.stage.2'
 if guide not in v.get(k,''):v[k]=v.get(k,'')+guide
 save(p,v)
p=R/'src/modern/resources/data/minecraft/tags/block/mineable/pickaxe.json';v=json.loads(p.read_text());v['values']=list(dict.fromkeys(v['values']+['thaumcraft:'+n for n in ['condenser','condenser_lattice','condenser_lattice_dirty']]));save(p,v)
print('Condenser: original connected models, two original arcane recipes and bilingual guide')
