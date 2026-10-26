package thaumcraft.thaumaturgy;

/** Original Rotura work, stagger and local aura cost. No hardness cutoff. */
public final class BreakWork {
    private BreakWork() {}
    public static float durability(float hardness) {
        if (!Float.isFinite(hardness) || hardness < 0) throw new IllegalArgumentException("Unbreakable hardness");
        return (float)Math.sqrt(hardness * 100D);
    }
    public static int delay(float durability,float strength,int targetIndex) {
        return (int)Math.min(1200, durability / strength / 3D * targetIndex);
    }
    public static float cost(FocusProgram.Effect effect) {
        return .25F + (effect.silk() ? .25F : 0) + effect.fortune() * .1F;
    }
    public static int cracks(float remaining,float total) {
        return total == 0 ? 9 : Math.clamp((int)((1 - remaining / total) * 10),0,9);
    }
}
