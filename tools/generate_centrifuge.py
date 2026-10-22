#!/usr/bin/env python3
"""Run after advanced smelters: native separator, original model and arcane recipe."""
import copy,json
from loot_schema import for_resource
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(for_resource(p,v),ensure_ascii=False,indent=2)+'\n')
v=json.loads((R/'src/main/resources/assets/thaumcraft/models/block/centrifuge.json').read_text());v.pop('__createdwith',None);v['textures']['particle']=v['textures']['side']
for phase in [0,1]:
 m=copy.deepcopy(v)
 if phase:
  for e in m['elements'][2:]:e['rotation']={'origin':[8,8,8],'axis':'y','angle':45,'rescale':False}
 save(A/f'models/block/centrifuge{ "_diagonal" if phase else ""}.json',m)
save(A/'blockstates/centrifuge.json',{'variants':{f'phase={i}':{'model':'thaumcraft:block/centrifuge'+('_diagonal' if i%2 else ''),'y':i//2*90} for i in range(8)}})
save(A/'items/centrifuge.json',{'model':{'type':'minecraft:model','model':'thaumcraft:block/centrifuge'}})
save(D/'loot_table/blocks/centrifuge.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:centrifuge'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
r={'type':'thaumcraft:arcane','research':'CENTRIFUGE','vis':100,'crystals':{'ordo':1,'perditio':1},'recipe':{'type':'minecraft:crafting_shaped','pattern':[' T ','RCP',' T '],'key':{'T':'thaumcraft:tube','R':'thaumcraft:morphic_resonator','C':'thaumcraft:metal_alchemical','P':'thaumcraft:mechanism_simple'},'result':{'id':'thaumcraft:centrifuge','count':1}}}
save(D/'recipe/arcane/centrifuge.json',r)
p=A/'knowledge/recipes.json';v=json.loads(p.read_text());v['thaumcraft:Centrifuge']=[' / '.join(r['recipe']['pattern'])]+[k+': '+value for k,value in r['recipe']['key'].items()]+['Vis 100; crystals ordo 1, perditio 1'];save(p,v)
for locale,spanish in [('en_us',False),('es_es',True)]:
 p=A/f'lang/{locale}.json';v=json.loads(p.read_text());v['block.thaumcraft.centrifuge']='Centrifugadora de esencia' if spanish else 'Essentia Centrifuge';v['essentia.thaumcraft.centrifuge.status']='Entrada: %s · Restan %s ticks · %s' if spanish else 'Input: %s · %s ticks remaining · %s'
 guide=(' En este port, aspira por abajo y entrega por arriba. Cada unidad compuesta produce un único componente elegido al azar tras 39 ticks; no duplica la esencia. Rechaza aspectos primarios y conexiones laterales. Redstone pausa la entrada y el proceso, pero permite retirar la salida. Con una salida ocupada espera sin sobrescribirla. El trabajo pagado y su progreso se conservan al guardar; el resonador consulta la salida y la mano vacía muestra también el progreso.' if spanish else ' In this port, input is below and output is above. Each compound unit yields one randomly selected component after 39 ticks; it does not duplicate essentia. Primal aspects and side connections are rejected. Redstone pauses input and processing but allows output extraction. An occupied output is never overwritten. Paid work and progress survive saves; the resonator reads the output and an empty hand also reports progress.')
 key='research.CENTRIFUGE.stage.2'
 if guide not in v.get(key,''):v[key]=v.get(key,'')+guide
 save(p,v)
print('Centrifuge: original model with eight rotor phases, one arcane recipe, bilingual guide.')
