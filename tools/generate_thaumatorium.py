#!/usr/bin/env python3
"""Run last: original two-block Thaumatorium mesh and Mnemonic Matrix recipe."""
import json,copy
from loot_schema import for_resource
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft';O=R/'src/main/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(for_resource(p,v),ensure_ascii=False,indent=2)+'\n')
vertices=[];uv=[];triangles=[]
for line in (O/'models/block/thaumatorium.obj').read_text().splitlines():
 v=line.split()
 if not v:continue
 if v[0]=='v':
  x,y,z=map(float,v[1:4]);vertices.append([x,z,1-y])
 elif v[0]=='vt':uv.append(list(map(float,v[1:3])))
 elif v[0]=='f':
  face=[]
  for index in v[1:]:
   a,b,*_=map(int,index.split('/'));face.append(vertices[a-1]+uv[b-1])
  for i in range(1,len(face)-1):triangles.append([face[0],face[i],face[i+1]])
save(A/'models/block/thaumatorium.json',{'parent':'minecraft:block/block','loader':'thaumcraft:triangles','render_type':'minecraft:cutout','textures':{'texture':'thaumcraft:blocks/thaumatorium','particle':'thaumcraft:blocks/thaumatorium'},'triangles':triangles})
save(A/'models/block/thaumatorium_top.json',{'textures':{'particle':'thaumcraft:blocks/thaumatorium'},'elements':[]})
for n in ['thaumatorium','thaumatorium_top']:save(A/f'blockstates/{n}.json',{'variants':{f'facing={d}':{'model':'thaumcraft:block/'+n,'y':angle} for d,angle in [('west',0),('north',90),('east',180),('south',270)]}})
m=json.loads((O/'models/block/brain_box.json').read_text());m.pop('__createdwith',None);m['render_type']='minecraft:cutout';save(A/'models/block/brain_box.json',m)
save(A/'blockstates/brain_box.json',{'variants':{f'facing={d}':{'model':'thaumcraft:block/brain_box','x':x,'y':y} for d,x,y in [('north',0,0),('south',0,180),('west',0,270),('east',0,90),('up',270,0),('down',90,0)]}})
for n in ['thaumatorium','brain_box']:save(A/f'items/{n}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:block/'+n}})
for n in ['thaumatorium','thaumatorium_top','brain_box']:save(D/f'loot_table/blocks/{n}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:'+('brain_box' if n=='brain_box' else 'metal_alchemical')}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
r={'type':'thaumcraft:arcane','research':'THAUMATORIUM','vis':50,'crystals':{'terra':1,'ordo':1},'recipe':{'type':'minecraft:crafting_shaped','pattern':['IAI','ABA','IAI'],'key':{'I':'thaumcraft:plate_iron','A':'thaumcraft:amber','B':'thaumcraft:brain'},'result':{'id':'thaumcraft:brain_box'}}}
# Original ItemsTC.mind metadata zero is the clockwork mind.
r['recipe']['key']['B']='thaumcraft:mind_clockwork'
save(D/'recipe/arcane/brain_box.json',r)
p=A/'knowledge/recipes.json';v=json.loads(p.read_text());v['thaumcraft:Thaumatorium']=['Salis Mundus: two alchemical constructs above a crucible. Heat below the crucible.'];v['thaumcraft:MnemonicMatrix']=[' / '.join(r['recipe']['pattern'])]+[k+': '+val for k,val in r['recipe']['key'].items()]+['Vis 50; crystals terra 1, ordo 1'];save(p,v)
for loc,es in [('en_us',False),('es_es',True)]:
 p=A/f'lang/{loc}.json';v=json.loads(p.read_text());v['block.thaumcraft.thaumatorium']='Taumatorio' if es else 'Thaumatorium';v['block.thaumcraft.brain_box']='Matriz mnemónica' if es else 'Mnemonic Matrix'
 for k,en,sp in [('catalyst','Input','Insumo'),('heated','Heated','Caliente'),('cold','No heat','Sin calor'),('paused','Paused','Pausado'),('invalid','Invalid data','Datos futuros')]:v['thaumatorium.thaumcraft.'+k]=sp if es else en
 guide=(' En este port: dos estructuras alquímicas encima de un crisol; activa con Salis Mundus. Conserva el crisol y su contenido. Calienta dos bloques bajo el controlador con fuego, lava, magma o nitor. Clic abre el selector de recetas investigadas; + y − configuran hasta una receta, más dos por matriz mnemónica orientada hacia el aparato (máximo siete matrices y quince recetas). Los botones muestran el coste al pasar el cursor. Inserta catalizadores por cualquiera de las dos partes con tolvas. La esencia entra por lados y detrás de ambas partes o por arriba; nunca por delante ni por abajo. Aspira 128 de un aspecto requerido cada vez. Redstone en cualquier parte pausa el trabajo. Reserva un catalizador y conserva costes y resultados pagados al guardar; el cofre frontal lleno retiene productos. Quitar mejoras conserva selecciones pero solo agenda las primeras según la capacidad; no cancela trabajo pagado. No permite borrar la receta activa. Desmontar devuelve los dos metales y el catalizador reservado, y libera como flux la esencia ya gastada.' if es else ' In this port: place two alchemical constructs above a crucible and activate with Salis Mundus. The crucible and its contents remain. Heat two blocks below the controller with fire, lava, magma or nitor. Open the researched recipe selector; + and − configure one recipe plus two per inward-facing Mnemonic Matrix (seven matrices, fifteen recipes maximum). Hover buttons for costs. Hoppers insert catalysts through either part. Essentia enters the sides/rear of both parts and the top, never front/below. Suction 128 requests one required aspect at a time. Redstone at any part pauses work. A reserved catalyst, paid costs and products survive saves; a full front chest holds output. Removing upgrades retains selections but only schedules the first entries allowed by capacity; paid work continues. The active recipe cannot be removed. Dismantling returns the two metals and reserved catalyst, releasing spent essentia as flux.')
 k='research.THAUMATORIUM.stage.2'
 if guide not in v.get(k,''):v[k]=v.get(k,'')+guide
 save(p,v)
p=R/'src/modern/resources/data/minecraft/tags/block/mineable/pickaxe.json';v=json.loads(p.read_text());v['values']=list(dict.fromkeys(v['values']+['thaumcraft:'+n for n in ['thaumatorium','thaumatorium_top','brain_box']]));save(p,v)
print('Thaumatorium:',len(triangles),'original triangles, Mnemonic Matrix recipe and bilingual guide')
