package thaumcraft.champion;

/** Numeric rules ported from TC6's champion spawn and loot code, kept free of world state for testing. */
public final class ChampionRules {
    private ChampionRules() {}

    /** TC6 whitelist values: 0,1,2,3 become bonus 0,0,1,2. */
    public static int whitelistBonus(int whitelistValue) {
        return Math.max(0, whitelistValue - 1);
    }

    /**
     * Adjusts the raw 0..99 roll exactly like {@code EntityEvents.entitySpawns}.
     * Easy adds 2, hard subtracts 2, Nether/End subtract 2 (or 1 when champions are restricted),
     * and disabling champions adds 2 on every difficulty.
     */
    public static int adjustSpawnRoll(int roll, boolean easy, boolean hard, boolean allowed, boolean netherOrEnd) {
        if (easy || !allowed) roll += 2;
        if (hard) roll -= allowed ? 2 : 0;
        if (netherOrEnd) roll -= allowed ? 2 : 1;
        return roll;
    }

    /** The champion appears when the adjusted roll minus the whitelist bonus is zero or less. */
    public static boolean spawnsChampion(int adjustedRoll, int bonus) {
        return adjustedRoll - bonus <= 0;
    }

    /** Original bag tier: {@code min(2, floor((rand(9) + looting) / 5))}. */
    public static int bagTier(int roll0to8, int looting) {
        return Math.min(2, (roll0to8 + Math.max(0, looting)) / 5);
    }
}
