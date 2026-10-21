package thaumcraft.taint;
/** Original numerical rules, shared by server mechanics and regression tests. */
public final class TaintRules{
 private TaintRules(){}
 public static double spreadChance(double rate,float flux,float base){return Math.clamp(rate/100*(.001+(base>0?flux/base:0)*2),0,1);}
 public static int wardTicks(int permanent){return Math.min(32000,200000/Math.max(1,(int)Math.sqrt(Math.clamp(permanent,0,500))));}
 public static int diseaseInterval(int amplifier){return Math.max(1,40>>Math.clamp(amplifier,0,6));}
 public static int deathDamage(int fluidLevel){return Math.max(1,5-Math.clamp(fluidLevel,0,3));}
 public static boolean seedEdge(double distanceSquared,int radius){return distanceSquared<radius*(double)radius&&distanceSquared>radius*.8*(radius*.8);}
 public static int gooAfterDecay(int quanta){return Math.max(0,Math.min(8,quanta)-1);}
}
