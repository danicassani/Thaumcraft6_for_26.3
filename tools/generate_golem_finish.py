#!/usr/bin/env python3
"""Final golem resources; run after golem_matching."""
import json,shutil,math
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft';old=R/'src/main/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
meshes={}
for p in sorted((old/'models/obj').glob('golem_*.obj'))+[old/'models/block/golembuilder.obj']:
 vertices=[];uvs=[];normals=[];groups={};group='default'
 for line in p.read_text().splitlines():
  a=line.split()
  if not a:continue
  if a[0]=='v':vertices.append(list(map(float,a[1:4])))
  elif a[0]=='vt':uvs.append([float(a[1]),float(a[2]) if p.stem=='golembuilder' else 1-float(a[2])])
  elif a[0]=='vn':normals.append(list(map(float,a[1:4])))
  elif a[0]=='g':group=a[1]
  elif a[0]=='f':
   face=[]
   for part in a[1:]:
    indices=part.split('/');v=int(indices[0]);t=int(indices[1]);n=int(indices[2]) if len(indices)>2 else 0;face.append(vertices[v-1]+uvs[t-1]+(normals[n-1] if n else [0,0,0]))
   for i in range(1,len(face)-1):
    tri=[v.copy() for v in [face[0],face[i],face[i+1]]];a=[tri[1][j]-tri[0][j] for j in range(3)];b=[tri[2][j]-tri[0][j] for j in range(3)];normal=[a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]];length=math.sqrt(sum(n*n for n in normal))
    for v in tri:
     if v[5:]==[0,0,0]:v[5:]=[n/length for n in normal] if length else [0,1,0]
    groups.setdefault(group,[]).append(tri)
 assert groups and all(math.isfinite(n) for triangles in groups.values() for triangle in triangles for v in triangle for n in v)
 meshes[p.stem]=groups
save(A/'models/golems/parts.json',meshes)
textures=list((old/'textures/blocks').glob('*pattern*'))
tex='thaumcraft:blocks/'+(textures[0].stem if textures else 'golembuilder')
pattern_model=json.loads((old/'models/block/pattern_crafter.json').read_text())
pattern_model['textures']={k:('minecraft:block/'+v[7:] if v.startswith('blocks/') else v) for k,v in pattern_model['textures'].items()}
save(A/'models/block/pattern_crafter.json',pattern_model)
save(A/'models/block/golem_press_part.json',{'textures':{'particle':'minecraft:block/iron_block'},'elements':[]})
save(A/'blockstates/golem_press_part.json',{'variants':{'':{'model':'thaumcraft:block/golem_press_part'}}})
save(D/'loot_table/blocks/golem_press_part.json',{'type':'minecraft:block','pools':[]})
save(D/'loot_table/blocks/golem_builder.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:alternatives','children':[{'type':'minecraft:item','name':'minecraft:piston','conditions':[{'condition':'minecraft:block_state_property','block':'thaumcraft:golem_builder','properties':{'assembled':'true'}}]},{'type':'minecraft:item','name':'thaumcraft:golem_builder'}]}]}]})
save(A/'blockstates/pattern_crafter.json',{'variants':{'':{'model':'thaumcraft:block/pattern_crafter'}}})
save(A/'items/pattern_crafter.json',{'model':{'type':'minecraft:model','model':'thaumcraft:block/pattern_crafter'}})
save(D/'loot_table/blocks/pattern_crafter.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:pattern_crafter'}]}]})
save(D/'recipe/arcane/pattern_crafter.json',{'type':'thaumcraft:arcane','research':'ARCANEPATTERNCRAFTER','vis':50,'crystals':{'terra':1,'aqua':1,'ordo':1},'recipe':{'type':'minecraft:crafting_shaped','pattern':['VH ','GCG',' W '],'key':{'V':'thaumcraft:vis_resonator','H':'minecraft:hopper','G':'thaumcraft:mechanism_simple','C':'minecraft:crafting_table','W':'thaumcraft:plank_greatwood'},'result':{'id':'thaumcraft:pattern_crafter'}}})
labels={'leave_one':['Keep one','Dejar uno'],'single':['One at a time','De uno en uno'],'cycle':['Cycle filters','Ciclar filtros'],'existing_only':['Existing only','Solo existentes'],'replant':['Replant','Replantar'],'harvest_provision':['Supply seeds','Pedir semillas'],'pattern':['Pattern %s / 10','Patrón %s / 10'],'target_monsters':['Monsters','Monstruos'],'target_animals':['Animals','Animales'],'target_players':['Players','Jugadores'],'shared':['Shared','Compartido'],'private':['Private','Privado']}
for i,locale in enumerate(['en_us','es_es']):
 p=A/f'lang/{locale}.json';v=json.loads(p.read_text())
 for key,values in labels.items():v['golem.thaumcraft.'+key]=values[i]
 v['block.thaumcraft.pattern_crafter']=['Arcane Pattern Crafter','Fabricador de patrones arcano'][i]
 for key,paragraphs in {
  'research.MINDCLOCKWORK.stage.3':[
   ' The original press can also be assembled: put iron bars over an upward piston, a cauldron at one side, a stone table at the perpendicular side and an anvil at the remaining corner of the 2 by 2 square. Use salis on the piston. All four rotations work. Breaking a part restores the remaining construction; incomplete paid jobs never give a free golem. Existing compact presses remain supported. Original models now show all selected parts, both hauler loads, channel flags and work animations. Fighters defend their owner, while Stay revokes defense.',
   ' También puede montarse la prensa original: barras sobre un pistón hacia arriba, caldero a un lado, mesa de piedra en el lado perpendicular y yunque en la esquina restante del cuadrado 2 por 2. Usa salis sobre el pistón. Funcionan las cuatro rotaciones. Romper una pieza restaura el resto del montaje; los trabajos incompletos pagados no dan gólems gratis. Las prensas compactas existentes siguen funcionando. Los modelos originales muestran las piezas elegidas, ambas cargas del porteador, banderas de canal y animaciones de trabajo. Los luchadores defienden al dueño y Esperar revoca su defensa.'
  ],
  'research.CONTROLSEALS.stage.1':[
   ' The final options page adds Keep one, One at a time, Cycle filters, Existing only, Replant and Supply seeds where applicable. Advanced Store quotas are per sample and keep surplus cargo physical. Guard target categories can be combined. Shared explicitly permits other owners on the same channel; only the seal owner may configure it. Old seals remain private. Seed provisioning consumes real provider items and replant queues survive saving. Tall crop bases are retained. Breaker works progressively with native diamond-tool loot, including hard blocks and containers; Silk takes longer. No search loads chunks.',
   ' La última página añade Dejar uno, De uno en uno, Ciclar filtros, Solo existentes, Replantar y Pedir semillas donde corresponda. Guardar avanzado aplica cuotas por muestra y conserva el excedente físico. Las categorías de Vigilar se pueden combinar. Compartido permite explícitamente gólems de otros dueños del mismo canal; solo el dueño configura el sello. Los sellos anteriores siguen privados. Pedir semillas consume objetos reales del proveedor y las colas de replantado se guardan. Se conservan las bases de plantas altas. Romper trabaja progresivamente con botín nativo de herramienta de diamante, incluidos bloques duros y contenedores; Seda tarda más. Las búsquedas no cargan chunks.'
  ]
 }.items():
  text=paragraphs[i];v[key]=v.get(key,'').split(text)[0]+text
 p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
print('Original golem meshes',len(meshes),'triangles',sum(len(t) for m in meshes.values() for t in m.values()))
