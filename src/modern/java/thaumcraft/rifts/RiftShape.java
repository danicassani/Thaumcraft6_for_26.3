package thaumcraft.rifts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.phys.Vec3;

/** Seeded two-sided walk from TC6, shared by collisions and the client mesh. */
public final class RiftShape {
    public record Point(Vec3 position,float width) {}
    private RiftShape() {}
    public static List<Point> create(int seed,int size) {
        var points=new ArrayList<Point>();var random=new Random(seed);
        var right=new Vec3(random.nextGaussian(),random.nextGaussian(),random.nextGaussian()).normalize();var left=right.scale(-1);
        var r=Vec3.ZERO;var l=Vec3.ZERO;int steps=(int)Math.ceil(Math.clamp(size,1,100)/3F);float width=Math.clamp(size,1,100)/300F,dec=width/steps;
        for(int i=0;i<steps;i++) {
            width-=dec;
            right=right.xRot((float)(random.nextGaussian()*.33)).yRot((float)(random.nextGaussian()*.33));r=r.add(right.scale(.2));points.add(new Point(r,Math.max(0,width)));
            left=left.xRot((float)(random.nextGaussian()*.33)).yRot((float)(random.nextGaussian()*.33));l=l.add(left.scale(.2));points.addFirst(new Point(l,Math.max(0,width)));
        }
        points.add(new Point(r.add(right.scale(.1)),0));points.addFirst(new Point(l.add(left.scale(.1)),0));return List.copyOf(points);
    }
}
