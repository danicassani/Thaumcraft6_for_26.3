package thaumcraft.research;

import java.util.*;
/** Complete research and its discovery predicates without fabricating scans, points or items. */
public final class CheatResearch {
 public record Grant(KnowledgeState knowledge,int permanentWarp,int normalWarp){}
 public static Grant complete(KnowledgeState old){
  var stages=new HashMap<>(old.stages());var flags=new HashSet<>(old.flags());int permanent=0,normal=0;
  for(var entry:ResearchCatalog.ENTRIES){
   if(!ResearchCatalog.knows(old,entry.key())){
    int first=Math.max(0,old.stages().getOrDefault(entry.key(),0)-1);
    for(int i=first;i<entry.stages().size();i++){int warp=entry.stages().get(i).warp();permanent+=(warp+1)/2;normal+=warp/2;}
   }
   stages.merge(entry.key(),entry.stages().size()+1,Math::max);
   entry.parents().forEach(q->predicates(flags,q));entry.siblings().forEach(q->predicates(flags,q));
   for(var stage:entry.stages())stage.research().forEach(q->predicates(flags,q));
   for(var addendum:entry.addenda())addendum.research().forEach(q->predicates(flags,q));
  }
  return new Grant(new KnowledgeState(old.version(),stages,old.scans(),flags,old.crafts(),old.points()),permanent,normal);
 }
 private static void predicates(Set<String> flags,String query){for(var raw:query.split("\\|\\||&&")){String key=raw.startsWith("~")?raw.substring(1):raw;int stage=key.indexOf('@');if(stage>=0)key=key.substring(0,stage);if(!key.isEmpty()&&!ResearchCatalog.BY_KEY.containsKey(key))flags.add(key);}}
 private CheatResearch(){}
}
