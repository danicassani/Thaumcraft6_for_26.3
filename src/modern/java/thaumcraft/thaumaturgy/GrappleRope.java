package thaumcraft.thaumaturgy;
import java.util.*;
import net.minecraft.world.phys.Vec3;
/** Finite cable samples with exact endpoints. Flight waves settle geometrically after anchoring. */
public final class GrappleRope {
 public static final int MAX_SEGMENTS=128;public static final float RADIUS=.025F;
 public static List<Vec3> points(Vec3 end,float age,boolean anchored,float anchorAge,int flightAge){
  double length=end.length();if(!Double.isFinite(length)||length<.001||length>40||!Float.isFinite(age)||!Float.isFinite(anchorAge))return List.of();
  int steps=Math.clamp((int)Math.ceil(length*5),2,MAX_SEGMENTS);double amplitude=.02*Math.clamp(anchored?flightAge:age,0,30)*(anchored?Math.pow(.66,Math.max(0,anchorAge)):1);var result=new ArrayList<Vec3>(steps+1);result.add(Vec3.ZERO);
  for(int i=1;i<steps;i++){double t=i/(double)steps,dist=t*length*5,wave=amplitude*Math.sin(Math.PI*t);result.add(end.scale(t).add(Math.sin(dist/10)*wave,Math.sin(dist/8)*wave,Math.sin(dist/6)*wave));}result.add(end);return List.copyOf(result);
 }
 private GrappleRope(){}
}
