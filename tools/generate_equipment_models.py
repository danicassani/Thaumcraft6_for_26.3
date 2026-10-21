#!/usr/bin/env python3
"""Preserve original TC6 armor boxes, pivots, mirrors and parent hierarchy."""
import re
from pathlib import Path
R=Path(__file__).resolve().parents[1]
roots={'bipedHead':'head','bipedHeadwear':'hat','bipedBody':'body','bipedRightArm':'right_arm','bipedLeftArm':'left_arm','bipedRightLeg':'right_leg','bipedLeftLeg':'left_leg'}
lines=['package thaumcraft.client;','import net.minecraft.client.model.geom.builders.*;','import net.minecraft.client.model.geom.PartPose;','/** Generated from TC6 armor models by tools/generate_equipment_models.py. */','public final class EquipmentGeometry {']
for kind,file in [('fortress','ModelFortressArmor'),('crimson_plate','ModelKnightArmor'),('crimson_robe','ModelRobe'),('crimson_praetor','ModelLeaderArmor')]:
 source=(R/f'src/main/java/thaumcraft/client/renderers/models/gear/{file}.java').read_text()
 for inner in [False,True]:
  s=source
  # All four constructors use simple f-dependent belt branches with no nested blocks.
  s=re.sub(r'if \(f < 1\.0f\) \{([^{}]*)\}\s*else \{([^{}]*)\}',lambda m:m[1] if inner else m[2],s)
  if kind=='fortress':
   a=s.index('        Mask = new ModelRenderer[3];');b=s.index('        (Goggles =',a)
   s=s[:a]+''.join(f'(Mask{i} = new ModelRenderer(this, {52+i*24}, 2)).addBox(-4.5f, -5.0f, -4.6f, 9, 5, 1);\nMask{i}.setRotationPoint(0,0,0);\n' for i in range(3))+s[b:]
   for i in range(3):s=s.replace(f'Mask[{i}]',f'Mask{i}')
  s=re.sub(r'\((\w+) = new ModelRenderer\(this, (\d+), (\d+)\)\)\.addBox',r'\1 = new ModelRenderer(this, \2, \3);\1.addBox',s)
  assignments={n:(u,v) for n,u,v in re.findall(r'(\w+) = new ModelRenderer\(this, (\d+), (\d+)\)',s)}
  parents={child:parent for parent,child in re.findall(r'(\w+)\.addChild\((\w+)\)',s)}
  parts={}
  for n,box in re.findall(r'\b(\w+)\)?\.addBox\(([^;]+)\);',s):
   if n not in assignments:continue
   vals=box.split(',');assert len(vals)==6,(file,n,box)
   number=lambda x:str(float(x.strip().rstrip('fFdD')))+'F'
   pos=re.search(r'\b'+n+r'\.setRotationPoint\(([^)]+)\)',s);rot=re.search(r'setRotation\('+n+r', ([^)]+)\)',s)
   pose=(pos[1] if pos else '0,0,0')+','+(rot[1] if rot else '0,0,0')
   mirror=bool(re.search(r'\b'+n+r'\.mirror = true;\s*'+n+r'\.addBox',s))
   u,v=assignments[n];parts[n]=f'CubeListBuilder.create().texOffs({u},{v})'+('.mirror()' if mirror else '')+'.addBox('+','.join(map(number,vals))+'),PartPose.offsetAndRotation('+','.join(map(number,pose.split(',')))+')'
  # Discard orphan definitions, exactly as the original render tree does.
  def connected(n,seen=None):
   if n in roots:return True
   seen=set() if seen is None else seen
   if n in seen or n not in parents:return False
   return connected(parents[n],seen|{n})
  parts={n:v for n,v in parts.items() if connected(n)}
  assert len(parts)>20,(kind,len(parts))
  name=kind+('_inner' if inner else '')
  lines+=[' public static LayerDefinition '+name+'(){var mesh=new MeshDefinition();var root=mesh.getRoot();']
  for old,new in roots.items():
   pivot={'head':(0,0,0),'hat':(0,0,0),'body':(0,0,0),'right_arm':(-5,2,0),'left_arm':(5,2,0),'right_leg':(-1.9,12,0),'left_leg':(1.9,12,0)}[new]
   lines.append('var '+old+'='+('bipedHead' if new=='hat' else 'root')+'.addOrReplaceChild("'+new+'",CubeListBuilder.create(),PartPose.offset('+','.join(str(x)+'F' for x in pivot)+'));')
  done=set(roots);pending=dict(parts)
  while pending:
   selected=[n for n in pending if parents[n] in done];assert selected,(kind,pending.keys())
   for n in selected:lines.append('var '+n+'='+parents[n]+'.addOrReplaceChild("'+n+'",'+pending.pop(n)+');');done.add(n)
  lines+=['return LayerDefinition.create(mesh,128,64);}','']
  print(name,len(parts),'original boxes')
lines+=['}'];(R/'src/modern/java/thaumcraft/client/EquipmentGeometry.java').write_text('\n'.join(lines)+'\n')
