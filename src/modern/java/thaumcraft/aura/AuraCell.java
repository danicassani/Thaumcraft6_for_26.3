package thaumcraft.aura;

/** Immutable per-chunk state; mutable callers cannot evade the finite-value invariant. */
public record AuraCell(int base, float vis, float flux) {
    public static final float MAX = 32766.0F;
    public AuraCell {
        if (base < 0 || base > 500 || !valid(vis) || !valid(flux))
            throw new IllegalArgumentException("Invalid aura state");
    }
    private static boolean valid(float value) { return Float.isFinite(value) && value >= 0 && value <= MAX; }
    public static void requireAmount(float amount) {
        if (!Float.isFinite(amount) || amount < 0) throw new IllegalArgumentException("Aura amount must be finite and nonnegative");
    }
    public AuraCell withVis(float amount) { return new AuraCell(base, Math.clamp(amount, 0, MAX), flux); }
    public AuraCell withFlux(float amount) { return new AuraCell(base, vis, Math.clamp(amount, 0, MAX)); }
    public float saturation() { return base == 0 ? 0 : flux / base; }
    public static long key(int x, int z) { return (x & 0xffffffffL) | ((z & 0xffffffffL) << 32); }
    public static int x(long key) { return (int)key; }
    public static int z(long key) { return (int)(key >> 32); }
}
