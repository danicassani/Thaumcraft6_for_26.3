package thaumcraft.visual;
/** The original crucible's quantity-based purple staining applied to native water tint. */
public final class CrucibleWater {
 private CrucibleWater(){}
 public static int color(int water,int amount){
  float staining=amount<=0?0:.5F+Math.min(500,amount)/1000F;
  int red=(int)(((water>>16)&255)*(1-staining/3)),green=(int)(((water>>8)&255)*(1-staining)),blue=(int)((water&255)*(1-staining/2));
  return 0xff000000|(red<<16)|(green<<8)|blue;
 }
}
