package thaumcraft.aura;

import java.util.ArrayList;
import java.util.Map;
import java.util.random.RandomGenerator;

/** Original one-second lunar cycle and neighbor transfers, run on the server thread. */
public final class AuraSimulation {
    private static final float[] REGEN = {.25F, .15F, .1F, .05F, 0, .05F, .1F, .15F};
    private static final float[] MAXIMUM = {1.15F, 1.05F, 1, .95F, .85F, .95F, 1, 1.05F};
    private AuraSimulation() {}

    /** Only the supplied loaded chunks participate. No chunk loading or background mutation. */
    public static void step(Map<Long, AuraCell> cells, int moon, RandomGenerator random) {
        if (moon < 0 || moon >= 8) throw new IllegalArgumentException("Invalid moon phase");
        var keys = new ArrayList<>(cells.keySet());
        keys.sort(Long::compare); // Stable across save/reload and map implementation changes.
        for (long key : keys) {
            AuraCell cell = cells.get(key);
            float vis = cell.vis(), flux = cell.flux(), base = cell.base() * MAXIMUM[moon];
            if (cell.base() == 0 && vis == 0 && flux == 0) continue;
            Long visNeighbor = null, fluxNeighbor = null;
            float lowestVis = Float.MAX_VALUE, lowestFlux = Float.MAX_VALUE;
            int[] directions = {0, 1, 2, 3};
            for (int i = 3; i > 0; i--) { int j = random.nextInt(i + 1), old = directions[i]; directions[i] = directions[j]; directions[j] = old; }
            for (int direction : directions) {
                int dx = direction == 0 ? 1 : direction == 1 ? -1 : 0;
                int dz = direction == 2 ? 1 : direction == 3 ? -1 : 0;
                long neighborKey = AuraCell.key(AuraCell.x(key) + dx, AuraCell.z(key) + dz);
                var neighbor = cells.get(neighborKey);
                if (neighbor == null) continue;
                if (neighbor.vis() < lowestVis && neighbor.vis() + neighbor.flux() < neighbor.base() * MAXIMUM[moon]) {
                    visNeighbor = neighborKey; lowestVis = neighbor.vis();
                }
                if (neighbor.flux() < lowestFlux) { fluxNeighbor = neighborKey; lowestFlux = neighbor.flux(); }
            }
            if (visNeighbor != null && lowestVis < vis && lowestVis / vis < .75F) {
                float amount = Math.min(vis - lowestVis, 1);
                vis -= amount;
                cells.put(visNeighbor, cells.get(visNeighbor).withVis(lowestVis + amount));
            }
            if (fluxNeighbor != null && flux > Math.max(5, cell.base() / 10F) && lowestFlux < flux / 1.75F) {
                float amount = Math.min(flux - lowestFlux, 1);
                flux -= amount;
                cells.put(fluxNeighbor, cells.get(fluxNeighbor).withFlux(lowestFlux + amount));
            }
            float phaseFlux = .25F - REGEN[moon];
            if (vis + flux < base) vis += Math.min(base - vis - flux, REGEN[moon]);
            else if (vis > base * 1.25F && random.nextFloat() < .1F) { vis -= phaseFlux; flux += phaseFlux; }
            else if (vis <= base * .1F && vis >= flux && random.nextFloat() < .1F) flux += phaseFlux;
            cells.put(key, new AuraCell(cell.base(), Math.clamp(vis, 0, AuraCell.MAX), Math.clamp(flux, 0, AuraCell.MAX)));
        }
    }
}
