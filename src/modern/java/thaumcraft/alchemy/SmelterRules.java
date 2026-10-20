package thaumcraft.alchemy;
/** Original tier efficiencies and bounded upgrade calculations, independent of world state. */
public final class SmelterRules {
 private SmelterRules(){}
 public static int efficiency(int tier){return switch(tier){case 1->90;case 2->95;default->80;};}
 public static boolean retains(int tier,boolean flux,float roll){return roll<=efficiency(tier)/100F*(flux?.66F:1);}
 public static int cookTicks(int aspects,int bellows){return Math.max(1,(int)(aspects*2L*(1-.125*Math.clamp(bellows,0,3))));}
 public static int distillTicks(int tier,boolean alumentum){int base=tier==1?10:15;return alumentum?Math.max(1,(int)(base*.8F)):base;}
 public static boolean vents(int count,java.util.function.DoubleSupplier random){for(int i=0;i<Math.clamp(count,0,3);i++)if(random.getAsDouble()<1D/3)return true;return false;}
}
