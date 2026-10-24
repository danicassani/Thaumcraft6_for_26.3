#!/usr/bin/env python3
"""Translate all original static golem/crab ModelRenderer boxes, preserving rest poses and mirrors."""
import re
from pathlib import Path
R=Path(__file__).resolve().parents[1]
lines=['package thaumcraft.client;','import net.minecraft.client.model.geom.builders.*;','import net.minecraft.client.model.geom.PartPose;','public final class EldritchGeometry {']
num=lambda s:float(s.strip().rstrip('fFdD'))
f=lambda v:str(float(v))+'F'
for kind in ['Crab','Golem']:
 s=(R/f'src/main/java/thaumcraft/client/renderers/models/entity/ModelEldritch{kind}.java').read_text();s=s[:s.index('    private void setRotation') if '    private void setRotation' in s else s.index('    public void render')]
 defs=re.findall(r'(\w+) = new ModelRenderer\(this, (\d+), (\d+)\)',s)
 lines.append(' public static LayerDefinition '+kind.lower()+'(){var mesh=new MeshDefinition();var root=mesh.getRoot();')
 for n,u,v in defs:
  m=re.search(r'\('+n+r' = new ModelRenderer\(this, \d+, \d+\)\)\.addBox\(([^;]+)\);',s) or re.search(r'\b'+n+r'\.addBox\(([^;]+)\);',s)
  assert m,n
  box=list(map(num,m[1].split(',')));pos=re.search(r'\b'+n+r'\.setRotationPoint\(([^)]+)\)',s);rot=re.search(r'setRotation\('+n+r', ([^)]+)\)',s)
  pos=list(map(num,pos[1].split(','))) if pos else [0]*3;rot=list(map(num,rot[1].split(','))) if rot else [0]*3
  mirror='.mirror()' if re.search(n+r'\.mirror = true',s) else ''
  lines.append(' root.addOrReplaceChild("'+n+'",CubeListBuilder.create().texOffs('+u+','+v+')'+mirror+'.addBox('+','.join(map(f,box))+'),PartPose.offsetAndRotation('+','.join(map(f,pos+rot))+'));')
 assert len(defs)==len(set(n for n,_,_ in defs))
 lines.append(' return LayerDefinition.create(mesh,128,64);}')
 print(kind,len(defs),'original boxes')
lines.append('}');(R/'src/modern/java/thaumcraft/client/EldritchGeometry.java').write_text('\n'.join(lines)+'\n')
