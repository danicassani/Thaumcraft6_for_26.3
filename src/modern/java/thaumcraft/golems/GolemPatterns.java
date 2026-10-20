package thaumcraft.golems;
/** Immutable original 3x3 pattern coordinates, independent of the game registries. */
public final class GolemPatterns {
    private static final int[][] PATTERNS={{0,1,2,3,4,5,6,7,8},{0},{0,1},{0,3},{0,1,3,4},{0,1,2},{0,3,6},{0,1,2,3,4,5},{0,1,3,4,6,7},{0,1,2,3,5,6,7,8}};
 private GolemPatterns(){}
 public static int[] slots(int pattern){return PATTERNS[Math.clamp(pattern,0,9)].clone();}
}
