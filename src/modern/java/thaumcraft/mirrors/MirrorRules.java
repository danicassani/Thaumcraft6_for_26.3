package thaumcraft.mirrors;
import net.minecraft.core.*;
public final class MirrorRules {
 private MirrorRules(){}
 public static int threshold(boolean essence){return essence?64:128;}
 public static boolean inFront(BlockPos origin,BlockPos p,Direction face){int x=p.getX()-origin.getX(),y=p.getY()-origin.getY(),z=p.getZ()-origin.getZ();int depth=x*face.getStepX()+y*face.getStepY()+z*face.getStepZ();return !p.equals(origin)&&Math.abs(x)<=8&&Math.abs(y)<=8&&Math.abs(z)<=8&&depth>=0&&depth<8;}
}
