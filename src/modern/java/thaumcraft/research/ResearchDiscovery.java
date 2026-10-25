package thaumcraft.research;
/** Backfill newly corrected discovery triggers without repeating scans or awarding points. */
public final class ResearchDiscovery {
 public static KnowledgeState reconcile(KnowledgeState state,boolean lastingWarp){var next=state;for(var scan:state.scans()){boolean entity=scan.startsWith("entity:");if(!entity&&!scan.startsWith("item:"))continue;String id=scan.substring(entity?7:5).split("/",2)[0];for(var flag:AspectCatalog.triggers(entity,id))if(!next.flags().contains(flag))next=next.flag(flag);}if(lastingWarp&&ResearchCatalog.knows(next,"FIRSTSTEPS")&&!next.flags().contains("WARP"))next=next.flag("WARP");return next;}
 private ResearchDiscovery(){}
}
