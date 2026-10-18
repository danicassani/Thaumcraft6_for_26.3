package thaumcraft.crystals;

import java.util.Map;

/** Original essence identifiers and colors; research/combinations are ported separately. */
public final class CrystalAspects {
    private CrystalAspects() {}
    public static final Map<String, Integer> COLORS;
    static {
        var colors = new java.util.LinkedHashMap<String, Integer>();
        colors.put("aer", 0xffff7e);
        colors.put("terra", 0x56c000);
        colors.put("ignis", 0xff5a01);
        colors.put("aqua", 0x3cd4fc);
        colors.put("ordo", 0xd5d4ec);
        colors.put("perditio", 0x404040);
        colors.put("vacuos", 0x888888);
        colors.put("lux", 0xffffc0);
        colors.put("motus", 0xcdccf4);
        colors.put("gelum", 0xe1ffff);
        colors.put("vitreus", 0x80ffff);
        colors.put("metallum", 0xb5b5cd);
        colors.put("victus", 0xde0005);
        colors.put("mortuus", 0x6a0005);
        colors.put("potentia", 0xc0ffff);
        colors.put("permutatio", 0x578357);
        colors.put("praecantatio", 0xcf00ff);
        colors.put("auram", 0xffc0ff);
        colors.put("alkimia", 0x23ac9d);
        colors.put("vitium", 0x800080);
        colors.put("tenebrae", 0x222222);
        colors.put("alienis", 0x805080);
        colors.put("volatus", 0xe7e7d7);
        colors.put("herba", 0x1ac00);
        colors.put("instrumentum", 0x4040ee);
        colors.put("fabrico", 0x809d80);
        colors.put("machina", 0x8080a0);
        colors.put("vinculum", 0x9a8080);
        colors.put("spiritus", 0xebebfb);
        colors.put("cognitio", 0xf9967f);
        colors.put("sensus", 0xc0ffc0);
        colors.put("aversio", 0xc05050);
        colors.put("praemunio", 0xc0c0);
        colors.put("desiderium", 0xe6be44);
        colors.put("exanimis", 0x3a4000);
        colors.put("bestia", 0x9f6409);
        colors.put("humanus", 0xffd7c0);
        COLORS = java.util.Collections.unmodifiableMap(colors);
    }
}
