package thaumcraft.trees;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;

/** Geometry from TC6's greatwood/silverwood generators, isolated from world writes. */
public final class TreeShape {
    public final Map<BlockPos,Axis> logs=new LinkedHashMap<>();
    public final Set<BlockPos> leaves=new LinkedHashSet<>();
    private TreeShape() {}
    public static TreeShape greatwood(long seed) { return branching(seed,true); }
    public static TreeShape magicOak(long seed) { return branching(seed,false); }
    private static TreeShape branching(long seed,boolean great) {
        var shape=new TreeShape();var random=new Random(seed);
        int limit=11+random.nextInt(11),height=(int)(limit*(great?.618:.6618));
        for (int pass=0;pass<(great?2:1);pass++) {
            int baseY=pass*height;double width=great?(pass==0?1.2:1.66):1.25;
            int perLayer=Math.max(1,(int)(1.382+Math.pow(.9*limit/13,2)));
            var nodes=new ArrayList<BlockPos>();nodes.add(new BlockPos(0,baseY+limit-4,0));
            for (int y=limit-5;y>=limit*.3;y--) {
                double half=limit/2D,radius=Math.sqrt(half*half-Math.pow(half-y,2))*.5;
                for (int n=0;n<perLayer;n++) {
                    double distance=width*radius*(random.nextFloat()+.328),angle=random.nextFloat()*2*Math.PI;
                    nodes.add(new BlockPos((int)Math.floor(distance*Math.sin(angle)+.5),baseY+y,(int)Math.floor(distance*Math.cos(angle)+.5)));
                }
            }
            for (var node:nodes) {
                int branchY=Math.min(baseY+height,(int)(node.getY()-Math.hypot(node.getX(),node.getZ())*(great?.38:.381)));
                if (branchY-baseY<limit*.2) continue;
                shape.line(new BlockPos(0,branchY,0),node);
                for (int y=0;y<4;y++) shape.disk(node.above(y),y==0||y==3?2:3);
            }
            for (int x=0;x<(great?2:1);x++) for (int z=0;z<(great?2:1);z++) shape.line(new BlockPos(x,baseY,z),new BlockPos(x,baseY+height,z));
        }
        shape.leaves.removeAll(shape.logs.keySet());return shape;
    }
    public static TreeShape silverwood(long seed) {
        var shape=new TreeShape();var random=new Random(seed);int height=8+random.nextInt(5);
        int top=height+3+random.nextInt(3);
        for (int y=height-5;y<=top;y++) {
            int center=Math.clamp(y,height-3,height);
            for (int x=-5;x<=5;x++) for (int z=-5;z<=5;z++)
                if (x*x+(y-center)*(y-center)+z*z<10+random.nextInt(8)) shape.leaves.add(new BlockPos(x,y,z));
        }
        for (int y=0;y<height;y++) for (var p:List.of(new BlockPos(0,y,0),new BlockPos(-1,y,0),new BlockPos(1,y,0),new BlockPos(0,y,-1),new BlockPos(0,y,1))) shape.logs.put(p,Axis.Y);
        shape.logs.put(new BlockPos(0,height,0),Axis.Y);
        for (int x:new int[]{-1,1}) for (int z:new int[]{-1,1}) {
            shape.logs.put(new BlockPos(x,0,z),Axis.Y);
            if (random.nextInt(3)!=0) shape.logs.put(new BlockPos(x,1,z),Axis.Y);
            shape.logs.put(new BlockPos(x,height-4,z),Axis.Y);
            if (random.nextInt(3)==0) shape.logs.put(new BlockPos(x,height-5,z),Axis.Y);
        }
        for (int sign:new int[]{-1,1}) {
            for (int y:new int[]{0,height-4}) {shape.logs.put(new BlockPos(sign*2,y,0),Axis.X);shape.logs.put(new BlockPos(0,y,sign*2),Axis.Z);}
            shape.logs.put(new BlockPos(sign*2,-1,0),Axis.Y);shape.logs.put(new BlockPos(0,-1,sign*2),Axis.Y);
        }
        shape.leaves.removeAll(shape.logs.keySet());return shape;
    }
    private void disk(BlockPos center,int radius) {
        for (int x=-radius;x<=radius;x++) for (int z=-radius;z<=radius;z++)
            if (Math.pow(Math.abs(x)+.5,2)+Math.pow(Math.abs(z)+.5,2)<=radius*radius) leaves.add(center.offset(x,0,z));
    }
    private void line(BlockPos from,BlockPos to) {
        int dx=to.getX()-from.getX(),dy=to.getY()-from.getY(),dz=to.getZ()-from.getZ();
        int steps=Math.max(Math.abs(dx),Math.max(Math.abs(dy),Math.abs(dz)));
        Axis axis=Math.abs(dx)>=Math.abs(dz)&&Math.abs(dx)>Math.abs(dy)?Axis.X:Math.abs(dz)>Math.abs(dy)?Axis.Z:Axis.Y;
        for (int i=0;i<=steps;i++) {
            double t=steps==0?0:(double)i/steps;
            logs.put(new BlockPos((int)Math.floor(from.getX()+dx*t+.5),(int)Math.floor(from.getY()+dy*t+.5),(int)Math.floor(from.getZ()+dz*t+.5)),axis);
        }
    }
}
