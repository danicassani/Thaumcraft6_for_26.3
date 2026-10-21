package thaumcraft.rifts;

/** Original drop probability and fall-through collapse effects, independent of rendering. */
public final class RiftCollapse {
    private RiftCollapse(){}
    public enum Stability { VERY_UNSTABLE, UNSTABLE, STABLE, VERY_STABLE }
    public static Stability stability(float s){return s>50?Stability.VERY_STABLE:s>=0?Stability.STABLE:s> -25?Stability.UNSTABLE:Stability.VERY_UNSTABLE;}
    public static int seeds(int size){return (int)Math.sqrt(Math.clamp(size,1,100));}
    public static boolean pearl(int size,int roll){if(roll<0||roll>=100)throw new IllegalArgumentException("Bad collapse roll");return roll<seeds(size);}
    // Preserve the original squared-distance / 32 formula, including its short effective radius.
    public static int exposure(double distanceSquared,int strength){if(!Double.isFinite(distanceSquared)||distanceSquared<0)return 0;return Math.max(0,(int)((1-distanceSquared/32)*strength));}
}
