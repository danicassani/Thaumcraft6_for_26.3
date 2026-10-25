#!/usr/bin/env python3
"""Structural stage reachability, craft gates, discovery producers and book reference audit.
Assumes obtainable resources/knowledge and encounters; not a simulated survival playthrough.
"""
import json,re
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft/recipe'
def audit():
 entries=json.loads((A/'knowledge/research.json').read_text());by={e['key']:e for e in entries};aspects=json.loads((A/'knowledge/aspects.json').read_text());views=json.loads((A/'knowledge/recipe_views.json').read_text());source='\n'.join(p.read_text() for p in (R/'src/modern/java').rglob('*.java'))
 flags={f for tab in aspects['triggers'].values() for fs in tab.values() for f in fs}|{'!'+a for a in aspects['components']}|{'!gotthaumonomicon','WARP'}
 flags|={f for f in re.findall(r'\.flag\([^\n]*?"([^"\n]+)"',source) if f.startswith(('m_','f_','!'))}
 # Literal calls to ResearchService.flag(player,key), including non-scan world events.
 flags|=set(re.findall(r'ResearchService\.flag\([^,\n]+,"([!\w]+)"',source))
 external=set();errors=[];queries=[];crafts=[];stages={};trace=[]
 for e in entries:
  queries+=e.get('parents',[])
  for i,s in enumerate(e['stages'],1):
   queries+=s.get('required_research',[])
   for item in s.get('required_item',[])+s.get('required_craft',[]):
    if item['id'].startswith('unavailable:') and 'enchanted_placeholder' not in item['legacy']:errors.append(f"Unavailable item: {e['key']}@{i}: {item['id']}")
   crafts += [(e['key'],i,r['id']) for r in s.get('required_craft',[])]
  for a in e.get('addenda',[]):queries+=a.get('required_research',[])
 recipes={}
 for path in D.rglob('*.json'):
  d=json.loads(path.read_text());r=d.get('recipe',d);result=r.get('result',d.get('result',{}));gate=d.get('research','');queries.append(gate)
  if isinstance(result,dict):recipes.setdefault(result.get('id'),[]).append((gate,str(path.relative_to(D))))
 recipes.update({'thaumcraft:arcane_workbench':[('FIRSTSTEPS@1','Salis conversion')],'thaumcraft:crucible':[('UNLOCKALCHEMY@2','Salis conversion')],'thaumcraft:research_table':[('THEORYRESEARCH@1','Scribing tools conversion')]})
 for query in queries:
  for q in re.split(r'\|\||&&',query):
   q=q.lstrip('~');parts=q.split('@');key=parts[0]
   if not key:continue
   if key not in by:
    external.add(key)
    if key not in flags:errors.append('No discovery producer: '+key)
   elif len(parts)>1 and (not parts[1].isdigit() or not 1<=int(parts[1])<=len(by[key]['stages'])+1):errors.append('Invalid stage query: '+q)
 def knows(q):
  q=q.lstrip('~')
  if not q:return True
  if '||' in q:return any(knows(p) for p in q.split('||'))
  if '&&' in q:return all(knows(p) for p in q.split('&&'))
  p=q.split('@');k=p[0]
  if k not in by:return k in flags
  if len(p)==2:return stages.get(k,0)>=int(p[1])
  return stages.get(k,0)>len(by[k]['stages']) or k in flags and len(by[k]['stages'])==1 and free(by[k]['stages'][0])
 def free(s):return not any(s.get(k) for k in ['required_item','required_craft','required_research','required_knowledge'])
 for _ in range(1024):
  changed=False
  for e in entries:
   key=e['key'];stage=stages.get(key,0)
   if stage>len(e['stages']):continue
   if key.startswith('!'):
    if key in flags:stages[key]=len(e['stages'])+1;changed=True
    continue
   if not all(knows(q) for q in e.get('parents',[])):continue
   if e['category']!='BASICS' and not knows('UNLOCK'+e['category']):continue
   if stage==0:
    if 'HIDDEN' in e.get('meta',[]) and not e.get('parents') and key not in flags:continue
    stages[key]=1;changed=True;trace.append(key+'@1');continue
   s=e['stages'][stage-1]
   if not all(knows(q) for q in s.get('required_research',[])):continue
   if not all(any(knows(gate) for gate,path in recipes.get(r['id'],[])) for r in s.get('required_craft',[])):continue
   stages[key]=stage+1;changed=True;trace.append(key+'@'+str(stage+1))
   for sibling in e.get('siblings',[]):
    d=by.get(sibling)
    if d is None:flags.add(sibling)
    elif len(d['stages'])==1 and free(d['stages'][0]) and all(knows(q) for q in d.get('parents',[])):stages[sibling]=2
  if not changed:break
 blocked={k:stages.get(k,0) for k,e in by.items() if not knows(k)}
 if blocked:errors.append('Unreachable research: '+str(blocked))
 missing=[]
 for e in entries:
  for s in e['stages']+e.get('addenda',[]):
   for ref in s.get('recipes',[]):
    if ref not in views:missing.append({'entry':e['key'],'reference':ref})
 aspect_sources={}
 for aspect in sorted({q[1:] for q in external if q.startswith('!') and q[1:] in aspects['components']}):
  aspect_sources[aspect]={kind:[key for key,value in aspects.get(kind,{}).items() if value.get(aspect,0)>0] for kind in ['items','entities','complex']}
  if not any(aspect_sources[aspect].values()):errors.append('Aspect has no discovery source: '+aspect)
 return dict(aspect_discovery_sources=aspect_sources,entries=len(entries),stages=sum(len(e['stages']) for e in entries),addenda=sum(len(e.get('addenda',[])) for e in entries),completed=len(entries)-len(blocked),external_requirements=sorted(external),craft_requirements=len(crafts),unreachable=blocked,errors=sorted(set(errors)),references_without_diagram=missing,scope='Symbolic research/craft-gate reachability with supplied resources, knowledge and legitimate discovery events; not an end-to-end survival or mod-compatibility certification.',stage_trace=trace)
if __name__=='__main__':
 result=audit();out=R/'docs/THAUMONOMICON-AUDIT.json';out.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n');print(json.dumps({k:v for k,v in result.items() if k not in ['stage_trace','external_requirements']},ensure_ascii=False,indent=2));raise SystemExit(bool(result['errors']))
