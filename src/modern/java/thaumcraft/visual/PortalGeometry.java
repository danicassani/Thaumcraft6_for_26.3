package thaumcraft.visual;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import thaumcraft.rifts.RiftShape;
/** Read-only visual equivalents of the legacy portal surface and six-sided rift cone. */
public final class PortalGeometry {
 public static List<Vec3> mirror(Direction face){var list=new ArrayList<Vec3>();double d=1.016/16;for(var p:List.of(new Vec3(1D/16,d,1D/16),new Vec3(1D/16,d,15D/16),new Vec3(15D/16,d,15D/16),new Vec3(15D/16,d,1D/16))){double x=p.x,y=p.y,z=p.z;list.add(switch(face){case UP->p;case DOWN->new Vec3(x,1-y,1-z);case NORTH->new Vec3(x,z,1-y);case SOUTH->new Vec3(1-x,z,y);case EAST->new Vec3(y,z,1-x);case WEST->new Vec3(1-y,z,x);});}if(list.get(1).subtract(list.get(0)).cross(list.get(2).subtract(list.get(0))).dot(new Vec3(face.getStepX(),face.getStepY(),face.getStepZ()))<0)Collections.reverse(list);return List.copyOf(list);}
 public static List<Vec3[]> rift(List<RiftShape.Point> points,float age,float stability,float scale){var vertices=new ArrayList<Vec3[]>();float stab=Math.clamp(1-stability/50,0,1.5F);for(int i=0;i<points.size()-1;i++){var a=points.get(i);var b=points.get(i+1);var at=wobble(a.position(),age,i,points.size(),stab);var bt=wobble(b.position(),age,i+1,points.size(),stab);var tangent=bt.subtract(at).normalize();if(tangent.lengthSqr()<.000001)continue;var u=tangent.cross(Math.abs(tangent.y)<.9?new Vec3(0,1,0):new Vec3(1,0,0)).normalize();var v=tangent.cross(u).normalize();double aw=a.width()*width(age,i,points.size(),stab)*scale,bw=b.width()*width(age,i+1,points.size(),stab)*scale;for(int side=0;side<6;side++){double angle=side*Math.PI/3,next=(side+1)*Math.PI/3;var p=u.scale(Math.cos(angle)).add(v.scale(Math.sin(angle)));var q=u.scale(Math.cos(next)).add(v.scale(Math.sin(next)));vertices.add(new Vec3[]{at.add(p.scale(aw)),at.add(q.scale(aw)),bt.add(q.scale(bw)),bt.add(p.scale(bw))});}}return List.copyOf(vertices);}
 private static float phase(float age,int i,int count){return age+(i<count/2?i*10:i>count/2?-i*10:0);}
 private static double width(float age,int i,int count,float stab){return 1-Math.sin(phase(age,i,count)/8)*.1*stab;}
 private static Vec3 wobble(Vec3 p,float age,int i,int count,float stab){float t=phase(age,i,count);return p.add(Math.sin(t/50)*.1*stab,Math.sin(t/60)*.1*stab,Math.sin(t/70)*.1*stab);}
 private PortalGeometry(){}
}
