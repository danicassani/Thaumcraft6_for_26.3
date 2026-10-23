package thaumcraft.warp;

/**
 * Numeric rules copied from TC6's cultist drops in {@code EntityEvents.livingDrops}.
 *
 * <p>Kept free of world state so the exact probabilities can be tested.</p>
 */
public final class CultistRules {
    private CultistRules() {}

    /**
     * Original denominator for the crimson curiosity drop:
     * 4 normally, 20 after learning {@code !CrimsonCultist@2}, and 50 while
     * carrying a curiosity of the {@code rites} variant (metadata 6).
     */
    public static int curioDenominator(boolean knowsCrimsonCultist, boolean carryingRites) {
        if (carryingRites) return 50;
        return knowsCrimsonCultist ? 20 : 4;
    }

    /** The curiosity drops when a {@code nextInt(denominator)} roll is zero. */
    public static boolean dropsCurio(int roll, int denominator) {
        return denominator > 0 && roll == 0;
    }

    /**
     * Original brain drop: {@code rand.nextInt(10) - looting < 1} on a recently
     * hit zombie. Brainy zombies are excluded by the caller because that variant
     * is not ported yet.
     */
    public static boolean dropsBrain(int roll0to9, int looting) {
        return roll0to9 - Math.max(0, looting) < 1;
    }
}
