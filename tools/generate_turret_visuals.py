#!/usr/bin/env python3
"""Recover original crossbow OBJ groups and model boxes without approximating geometry."""
from pathlib import Path
import json,re,math
from loot_schema import for_resource
R=Path(__file__).resolve().parents[1];O=R/'src/main/resources/assets/thaumcraft';A=R/'src/modern/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(for_resource(p,v),ensure_ascii=False,indent=2)+'\n')
vertices=[];uvs=[];normals=[];groups={};group='default'
for line in (O/'models/obj/crossbow_advanced.obj').read_text().splitlines():
 a=line.split()
 if not a:continue
 if a[0]=='v':vertices.append(list(map(float,a[1:4])))
 elif a[0]=='vt':uvs.append([float(a[1]),1-float(a[2])])
 elif a[0]=='vn':normals.append(list(map(float,a[1:4])))
 elif a[0]=='g':group=a[1]
 elif a[0]=='f':
  face=[]
  for part in a[1:]:
   ids=list(map(int,part.split('/')));v,t=ids[:2];face.append(vertices[v-1]+uvs[t-1]+(normals[ids[2]-1] if len(ids)>2 else [0,0,0]))
  for i in range(1,len(face)-1):
   tri=[v.copy() for v in [face[0],face[i],face[i+1]]];a=[tri[1][j]-tri[0][j] for j in range(3)];b=[tri[2][j]-tri[0][j] for j in range(3)];normal=[a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]];length=math.sqrt(sum(n*n for n in normal))
   for v in tri:
    if v[5:]==[0,0,0]:v[5:]=[n/length for n in normal] if length else [0,1,0]
   groups.setdefault(group,[]).append(tri)
assert set(groups)==set(['legs','mech','box','shield','brain','loader','bow1','bow2']),groups.keys()
save(A/'models/turrets/advanced.json',{'advanced':groups})
s=(R/'src/main/java/thaumcraft/client/renderers/models/entity/ModelCrossbow.java').read_text();parts={}
for name,u,v,box in re.findall(r'\((\w+) = new ModelRenderer\(this, (\d+), (\d+)\)\).addBox\(([^;]+)\);',s):
 def numbers(text):return [float(n.strip().rstrip('f')) for n in text.split(',')]
 pos=numbers(re.search(name+r'\.setRotationPoint\(([^)]+)\)',s)[1]);rot=numbers(re.search(r'setRotation\('+name+r', ([^)]+)\)',s)[1]);parts[name]=dict(uv=[int(u),int(v)],box=numbers(box),pos=pos,rot=rot,parent='crossbow' if 'crossbow.addChild('+name+')' in s else 'root')
assert len(parts)==20,len(parts)
save(A/'models/turrets/basic.json',parts)
for name in ['turret_basic','turret_advanced']:
 save(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'thaumcraft:items/{name}'}})
 save(A/f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'thaumcraft:item/{name}'}})
for locale in ['en_us','es_es']:
 p=A/f'lang/{locale}.json';v=json.loads(p.read_text());es=locale=='es_es'
 for name in ['turret_basic','turret_advanced']:v['entity.thaumcraft.'+name]=v['block.thaumcraft.'+name]
 for key,en,sp in [('animals','Animals','Animales'),('monsters','Monsters','Monstruos'),('players','Players','Jugadores')]:v['turret.thaumcraft.'+key]=sp if es else en
 v['research.BASICTURRET.stage.2']='Coloca la ballesta y úsala para abrir su inventario. Admite flechas normales, con poción y espectrales, conservando sus efectos. Un dispensador orientado hacia arriba debajo de ella la recarga cuando se vacía. Ataca monstruos visibles hasta 24 bloques y consume una flecha por disparo. La redstone la pausa. Usa la mano vacía agachado para recogerla junto con la munición. Dispara mientras su propietario está presente en esta dimensión. Puede viajar en vagoneta.' if es else 'Place the crossbow and use it to open its inventory. Normal, tipped and spectral arrows retain their effects. An upward-facing dispenser beneath it reloads it when empty. It attacks visible monsters within 24 blocks and consumes one arrow per shot. Redstone pauses it. Sneak-use with an empty hand to collect it and its ammunition. It fires while its owner is present in this dimension. It can ride a minecart.'
 v['research.ADVANCEDTURRET.stage.2']='La ballesta avanzada dispara más rápido y con más precisión. Su inventario permite combinar animales, monstruos y jugadores como objetivos. Aliados, mascotas, creativo y jugadores protegidos por las reglas de PVP quedan a salvo. Comparte munición y recarga con la básica. Solo el propietario puede configurar o recoger sus ballestas.' if es else 'The advanced crossbow fires faster and more accurately. Its inventory lets you combine animals, monsters and players as targets. Allies, pets, creative players and players protected by PVP rules remain safe. It shares ammunition and reloading with the basic model. Only the owner can configure or collect a crossbow.'
 save(p,v)
print('Recovered',len(parts),'basic parts and',sum(map(len,groups.values())),'advanced triangles')

for advanced in [False,True]:
 loot=[('mind_biothaumic' if advanced else 'mind_clockwork',.2),('mechanism_simple',.5),('plank_greatwood',.5),('plank_greatwood',.5)]+([('plate_iron',.3),('plate_brass',.4),('plate_brass',.4)] if advanced else [])
 save(R/('src/modern/resources/data/thaumcraft/loot_table/entities/turret_'+('advanced' if advanced else 'basic')+'.json'),{'type':'minecraft:entity','pools':[{'rolls':1,'conditions':[{'condition':'minecraft:random_chance_with_enchanted_bonus','enchantment':'minecraft:looting','unenchanted_chance':chance,'enchanted_chance':{'type':'minecraft:linear','base':chance+.15,'per_level_above_first':.15}}],'entries':[{'type':'minecraft:item','name':'thaumcraft:'+item}]} for item,chance in loot]})
