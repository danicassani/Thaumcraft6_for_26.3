package thaumcraft.visual;
import net.minecraft.core.Direction;
/** Legacy jar-label variation, including Java's signed remainder at negative coordinates. */
public final class JarLabelGeometry {
 private JarLabelGeometry(){}
 public static float degrees(String aspect,int x,Direction facing,boolean crooked){
  if(!crooked||aspect.isEmpty())return 0;
  int legacy=switch(facing){case NORTH->2;case SOUTH->3;case WEST->4;case EAST->5;default->throw new IllegalArgumentException("Jar labels require a horizontal face");};
  return (aspect.hashCode()+x+legacy)%4-2;
 }
 public static java.util.List<net.minecraft.world.phys.Vec3> quad(Direction face,float l,float r,float b,float t,float depth){
  return switch(face){
   case NORTH->java.util.List.of(new net.minecraft.world.phys.Vec3(l,b,depth),new net.minecraft.world.phys.Vec3(l,t,depth),new net.minecraft.world.phys.Vec3(r,t,depth),new net.minecraft.world.phys.Vec3(r,b,depth));
   case SOUTH->java.util.List.of(new net.minecraft.world.phys.Vec3(r,b,1-depth),new net.minecraft.world.phys.Vec3(r,t,1-depth),new net.minecraft.world.phys.Vec3(l,t,1-depth),new net.minecraft.world.phys.Vec3(l,b,1-depth));
   case WEST->java.util.List.of(new net.minecraft.world.phys.Vec3(depth,b,r),new net.minecraft.world.phys.Vec3(depth,t,r),new net.minecraft.world.phys.Vec3(depth,t,l),new net.minecraft.world.phys.Vec3(depth,b,l));
   case EAST->java.util.List.of(new net.minecraft.world.phys.Vec3(1-depth,b,l),new net.minecraft.world.phys.Vec3(1-depth,t,l),new net.minecraft.world.phys.Vec3(1-depth,t,r),new net.minecraft.world.phys.Vec3(1-depth,b,r));
   default->throw new IllegalArgumentException("Horizontal label required");
  };
 }
}
