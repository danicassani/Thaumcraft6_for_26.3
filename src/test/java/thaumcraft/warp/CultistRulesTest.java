package thaumcraft.warp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/** Verifies the pure numbers copied from TC6 cultist and zombie drops. */
class CultistRulesTest {
    @Test void curioDenominatorFollowsResearchAndHeldCurio() {
        assertEquals(4, CultistRules.curioDenominator(false, false));
        assertEquals(20, CultistRules.curioDenominator(true, false));
        assertEquals(50, CultistRules.curioDenominator(false, true));
        assertEquals(50, CultistRules.curioDenominator(true, true));
    }

    @Test void curioOnlyDropsOnZeroRoll() {
        assertTrue(CultistRules.dropsCurio(0, 4));
        assertFalse(CultistRules.dropsCurio(1, 4));
        assertFalse(CultistRules.dropsCurio(0, 0));
    }

    @Test void brainUsesOriginalLootingThreshold() {
        assertTrue(CultistRules.dropsBrain(0, 0));
        assertFalse(CultistRules.dropsBrain(1, 0));
        assertTrue(CultistRules.dropsBrain(1, 1));
        assertTrue(CultistRules.dropsBrain(9, 9));
        assertFalse(CultistRules.dropsBrain(9, 0));
    }

    @Test void negativeLootingNeverImprovesTheDrop() {
        assertFalse(CultistRules.dropsBrain(1, -3));
    }
}
