package thaumcraft.equipment;
import java.util.*;
import net.minecraft.core.*;
public final class ArchitectPlane {
 private ArchitectPlane(){}
 public static List<BlockPos> select(BlockPos origin,Direction face,Direction horizontal,int orientation){var out=new ArrayList<BlockPos>();for(int a=-1;a<=1;a++)for(int b=-1;b<=1;b++){int x=0,y=0,z=0;if(orientation==1){y=b;if(face.getAxis()==Direction.Axis.Y){if(horizontal.getAxis()==Direction.Axis.Z)x=a;else z=a;}else if(face.getAxis()==Direction.Axis.Z)z=a;else x=a;}else if(orientation==2){if(face.getAxis()==Direction.Axis.Y){y=b;if(horizontal.getAxis()==Direction.Axis.Z)z=a;else x=a;}else{x=a;z=b;}}else if(face.getAxis()==Direction.Axis.Y){x=a;z=b;}else if(face.getAxis()==Direction.Axis.Z){x=a;y=b;}else{z=a;y=b;}out.add(origin.relative(face).offset(x,y,z));}return List.copyOf(out);}
}
