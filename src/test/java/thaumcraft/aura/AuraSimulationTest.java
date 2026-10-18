package thaumcraft.aura;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Random;
import org.junit.jupiter.api.Test;

final class AuraSimulationTest {
    @Test void rejectsInvalidStatesAndAmounts() {
        assertThrows(IllegalArgumentException.class, () -> new AuraCell(501, 1, 1));
        for (float bad : new float[]{Float.NaN, Float.POSITIVE_INFINITY, -1}) {
            assertThrows(IllegalArgumentException.class, () -> new AuraCell(200, bad, 1));
            assertThrows(IllegalArgumentException.class, () -> AuraCell.requireAmount(bad));
        }
        assertEquals(0, new AuraCell(0, 0, 10).saturation());
    }
    @Test void coordinatesRoundTripAtNegativeAndIntegerBounds() {
        for (int x : new int[]{0, -1, Integer.MIN_VALUE, Integer.MAX_VALUE})
            for (int z : new int[]{0, -1, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
                long key = AuraCell.key(x, z); assertEquals(x, AuraCell.x(key)); assertEquals(z, AuraCell.z(key));
            }
    }
    @Test void fullMoonRegeneratesAndNewMoonDoesNot() {
        var cells = new HashMap<Long, AuraCell>(); long key = AuraCell.key(0, 0);
        cells.put(key, new AuraCell(200, 100, 0));
        AuraSimulation.step(cells, 0, new Random(42)); assertEquals(100.25F, cells.get(key).vis());
        cells.put(key, new AuraCell(200, 100, 0));
        AuraSimulation.step(cells, 4, new Random(42)); assertEquals(100, cells.get(key).vis());
    }
    @Test void pollutionOccupiesCapacityRatherThanRegeneratingThroughIt() {
        var cells = new HashMap<Long, AuraCell>(); long key = AuraCell.key(0, 0);
        cells.put(key, new AuraCell(200, 100, 150));
        AuraSimulation.step(cells, 0, new Random(42)); assertEquals(100, cells.get(key).vis()); assertEquals(150, cells.get(key).flux());
    }
    @Test void transfersConserveVisAndFluxWithoutLoadingOtherCells() {
        var cells = new HashMap<Long, AuraCell>();
        cells.put(AuraCell.key(0, 0), new AuraCell(200, 200, 100));
        cells.put(AuraCell.key(1, 0), new AuraCell(200, 20, 0));
        AuraSimulation.step(cells, 4, new Random(42));
        assertEquals(2, cells.size());
        assertEquals(220, cells.values().stream().mapToDouble(AuraCell::vis).sum(), .0001);
        assertEquals(100, cells.values().stream().mapToDouble(AuraCell::flux).sum(), .0001);
        assertEquals(21, cells.get(AuraCell.key(1, 0)).vis());
        assertEquals(1, cells.get(AuraCell.key(1, 0)).flux());
    }
    @Test void disabledEmptyAuraDoesNotCreatePollution() {
        var cells = new HashMap<Long, AuraCell>(); long key = AuraCell.key(0, 0);
        cells.put(key, new AuraCell(0, 0, 0)); var random = new Random(4);
        for (int i = 0; i < 1000; i++) AuraSimulation.step(cells, i % 8, random);
        assertEquals(new AuraCell(0, 0, 0), cells.get(key));
    }
    @Test void longSimulationAlwaysRemainsFiniteAndBounded() {
        var cells = new HashMap<Long, AuraCell>();
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) cells.put(AuraCell.key(x, z), new AuraCell(200, x == 0 ? AuraCell.MAX : 0, z == 0 ? AuraCell.MAX : 0));
        var random = new Random(5);
        for (int i = 0; i < 1000; i++) AuraSimulation.step(cells, i % 8, random);
        assertEquals(25, cells.size());
        cells.values().forEach(c -> { assertTrue(Float.isFinite(c.vis()) && c.vis() >= 0 && c.vis() <= AuraCell.MAX); assertTrue(Float.isFinite(c.flux()) && c.flux() >= 0 && c.flux() <= AuraCell.MAX); });
    }
}
