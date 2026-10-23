package thaumcraft.champion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/** Verifies the pure numbers copied from TC6 champion spawning and drops. */
class ChampionRulesTest {
    @Test void whitelistValueZeroAndOneBothGiveNoBonus() {
        assertEquals(0, ChampionRules.whitelistBonus(0));
        assertEquals(0, ChampionRules.whitelistBonus(1));
    }

    @Test void whitelistValueTwoAndThreeGiveOneAndTwo() {
        assertEquals(1, ChampionRules.whitelistBonus(2));
        assertEquals(2, ChampionRules.whitelistBonus(3));
    }

    @Test void permanentDisabledChampionsStillAddTwo() {
        // allowChampionMobs=false adds 2 (like easy difficulty) but hard no longer subtracts.
        assertEquals(12, ChampionRules.adjustSpawnRoll(10, false, false, false, false));
        // TC6 still adds the "disabled" +2 on hard, it just stops subtracting 2 for hard.
        assertEquals(12, ChampionRules.adjustSpawnRoll(10, false, true, false, false));
        assertEquals(11, ChampionRules.adjustSpawnRoll(10, false, false, false, true));
    }

    @Test void easyAndHardShiftTheRoll() {
        assertEquals(2, ChampionRules.adjustSpawnRoll(0, true, false, true, false));
        assertEquals(0, ChampionRules.adjustSpawnRoll(2, false, true, true, false));
        assertEquals(0, ChampionRules.adjustSpawnRoll(2, false, false, true, true));
    }

    @Test void onlyRollsAtOrBelowZeroSpawnAChampion() {
        assertTrue(ChampionRules.spawnsChampion(0, 0));
        assertTrue(ChampionRules.spawnsChampion(-1, 0));
        assertFalse(ChampionRules.spawnsChampion(1, 0));
        assertTrue(ChampionRules.spawnsChampion(1, 1));
        assertFalse(ChampionRules.spawnsChampion(2, 1));
    }

    @Test void bagTierUsesLootingAndCapsAtTwo() {
        assertEquals(0, ChampionRules.bagTier(0, 0));
        assertEquals(0, ChampionRules.bagTier(4, 0));
        assertEquals(1, ChampionRules.bagTier(5, 0));
        assertEquals(1, ChampionRules.bagTier(0, 5));
        assertEquals(2, ChampionRules.bagTier(1, 9));
        assertEquals(2, ChampionRules.bagTier(8, 100));
    }
}
