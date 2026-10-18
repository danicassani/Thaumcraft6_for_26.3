package thaumcraft.aura;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelResource;

/** One versioned SavedData file per dimension. Unloaded chunks remain stored but never simulated. */
public final class AuraData extends SavedData {
    public record Entry(int x, int z, AuraCell cell) {}
    private static final Codec<Float> AMOUNT = Codec.floatRange(0, AuraCell.MAX).validate(value ->
        Float.isFinite(value) ? com.mojang.serialization.DataResult.success(value) : com.mojang.serialization.DataResult.error(() -> "Non-finite aura"));
    private static final Codec<Entry> ENTRY_CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.INT.fieldOf("x").forGetter(Entry::x), Codec.INT.fieldOf("z").forGetter(Entry::z),
        Codec.intRange(0, 500).fieldOf("base").forGetter(e -> e.cell().base()),
        AMOUNT.fieldOf("vis").forGetter(e -> e.cell().vis()), AMOUNT.fieldOf("flux").forGetter(e -> e.cell().flux())
    ).apply(i, (x, z, base, vis, flux) -> new Entry(x, z, new AuraCell(base, vis, flux))));
    private static final Codec<AuraData> FORMAT_CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.intRange(1, 1).fieldOf("version").forGetter(data -> 1),
        ENTRY_CODEC.listOf().fieldOf("chunks").forGetter(AuraData::entries)
    ).apply(i, (version, entries) -> new AuraData(entries)));
    // Reject partial parses: a bad entry or future version must never erase the rest of a file.
    public static final Codec<AuraData> CODEC = Codec.of(FORMAT_CODEC, new com.mojang.serialization.Decoder<>() {
        @Override public <T> com.mojang.serialization.DataResult<com.mojang.datafixers.util.Pair<AuraData, T>> decode(com.mojang.serialization.DynamicOps<T> ops, T input) {
            try {
                var result = FORMAT_CODEC.decode(ops, input);
                return result.result().map(com.mojang.serialization.DataResult::success)
                    .orElseGet(() -> com.mojang.serialization.DataResult.error(() -> result.error().orElseThrow().message()));
            } catch (IllegalArgumentException error) {
                return com.mojang.serialization.DataResult.error(error::getMessage);
            }
        }
    });
    public static final SavedDataType<AuraData> TYPE = new SavedDataType<>(Identifier.parse("thaumcraft:aura"), AuraData::new, CODEC, null);
    private final Map<Long, AuraCell> cells = new HashMap<>();
    public AuraData() {}
    private AuraData(List<Entry> entries) {
        for (var e : entries) {
            if (cells.putIfAbsent(AuraCell.key(e.x(), e.z()), e.cell()) != null) throw new IllegalArgumentException("Duplicate aura chunk");
        }
    }
    private List<Entry> entries() {
        return cells.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e -> new Entry(AuraCell.x(e.getKey()), AuraCell.z(e.getKey()), e.getValue())).toList();
    }
    public AuraCell get(long key) { return cells.get(key); }
    public void put(long key, AuraCell cell) {
        if (!cell.equals(cells.put(key, cell))) setDirty();
    }
    public static AuraData get(ServerLevel level) {
        var data = level.getDataStorage().get(TYPE);
        if (data != null) return data;
        // Vanilla computeIfAbsent otherwise replaces unreadable/future-format data with a new file.
        var path = DimensionType.getStorageFolder(level.dimension(), level.getServer().getWorldPath(LevelResource.ROOT))
            .resolve("data/thaumcraft/aura.dat");
        if (Files.exists(path)) throw new IllegalStateException("Cannot decode aura data; preserving existing file: " + path);
        return level.getDataStorage().computeIfAbsent(TYPE);
    }
}
