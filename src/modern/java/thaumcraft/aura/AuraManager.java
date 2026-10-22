package thaumcraft.aura;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;

/** All authoritative reads/writes and simulation run on the owning server thread. */
public final class AuraManager {
    private static final Map<ServerLevel, Set<Long>> LOADED = new ConcurrentHashMap<>();
    private static final java.util.List<Map.Entry<String, Float>> BIOME_WEIGHTS = java.util.List.of(
        Map.entry("is_ocean", .33F), Map.entry("is_river", .4F), Map.entry("is_nether", .125F),
        Map.entry("is_end", .125F), Map.entry("is_badlands", .33F), Map.entry("is_taiga", .33F),
        Map.entry("is_forest", .5F), Map.entry("is_beach", .3F), Map.entry("is_jungle", .6F),
        Map.entry("is_savanna", .25F), Map.entry("is_mountain", .3F));
    private AuraManager() {}
    public static void registerEvents() {
        ChunkEvent.Load.BUS.addListener(event -> {
            if (event.getChunk() instanceof LevelChunk chunk && chunk.getLevel() instanceof ServerLevel level)
                LOADED.computeIfAbsent(level, k -> ConcurrentHashMap.newKeySet()).add(chunk.getPos().pack());
        });
        ChunkEvent.Unload.BUS.addListener(event -> {
            if (event.getChunk() instanceof LevelChunk chunk && chunk.getLevel() instanceof ServerLevel level) {
                var chunks = LOADED.get(level); if (chunks != null) chunks.remove(chunk.getPos().pack());
            }
        });
        LevelEvent.Unload.BUS.addListener(event -> { if (event.getLevel() instanceof ServerLevel level) LOADED.remove(level); });
        TickEvent.LevelTickEvent.Post.BUS.addListener(event -> {
            if (event.level() instanceof ServerLevel level && level.tickRateManager().runsNormally() && level.getGameTime() % 20 == 0) tick(level);
        });
    }
    private static void checkThread(ServerLevel level) {
        if (!level.getServer().isSameThread()) throw new IllegalStateException("Aura accessed outside the server thread");
    }
    /** Null means the chunk is unloaded; this method never loads or generates terrain. */
    public static AuraCell get(ServerLevel level, BlockPos pos) { return get(level, pos.getX() >> 4, pos.getZ() >> 4); }
    public static AuraCell get(ServerLevel level, int x, int z) {
        checkThread(level);
        if (level.getChunkSource().getChunkNow(x, z) == null) return null;
        var data = AuraData.get(level); long key = AuraCell.key(x, z);
        var cell = data.get(key);
        if (cell == null) { cell = generate(level, x, z); data.put(key, cell); }
        return cell;
    }
    public static float addVis(ServerLevel level, BlockPos pos, float amount) { return change(level, pos, amount, false, false, false); }
    public static float addFlux(ServerLevel level, BlockPos pos, float amount) { return change(level, pos, amount, true, false, false); }
    public static float drainVis(ServerLevel level, BlockPos pos, float amount, boolean simulate) { return change(level, pos, amount, false, true, simulate); }
    public static float drainFlux(ServerLevel level, BlockPos pos, float amount, boolean simulate) { return change(level, pos, amount, true, true, simulate); }
    private static float change(ServerLevel level, BlockPos pos, float amount, boolean flux, boolean drain, boolean simulate) {
        AuraCell.requireAmount(amount);
        var cell = get(level, pos); if (cell == null) return 0;
        float value = flux ? cell.flux() : cell.vis();
        float moved = Math.min(amount, drain ? value : AuraCell.MAX - value);
        if (!simulate && moved > 0) {
            float result = value + (drain ? -moved : moved);
            AuraData.get(level).put(AuraCell.key(pos.getX() >> 4, pos.getZ() >> 4), flux ? cell.withFlux(result) : cell.withVis(result));
        }
        return moved;
    }
    public static void tick(ServerLevel level) {
        checkThread(level);
        var loaded = LOADED.get(level); if (loaded == null || loaded.isEmpty()) return;
        var working = new HashMap<Long, AuraCell>();
        for (long key : loaded) {
            var cell = get(level, AuraCell.x(key), AuraCell.z(key));
            if (cell != null) working.put(key, cell);
        }
        int phase = level.environmentAttributes().getDimensionValue(net.minecraft.world.attribute.EnvironmentAttributes.MOON_PHASE).index();
        AuraSimulation.step(working, phase, new Random(level.getSeed() ^ level.getGameTime()));
        var data = AuraData.get(level);
        working.forEach(data::put);
        // At most one creation attempt per dimension and second, like the original trigger map.
        for(var entry:working.entrySet()) {
            var cell=entry.getValue();
            if(cell.flux()>cell.base()*.75F && level.getRandom().nextFloat()<cell.flux()/500F/10F) {
                thaumcraft.rifts.RiftSpawning.trySpawn(level,AuraCell.x(entry.getKey()),AuraCell.z(entry.getKey()));break;
            }
        }
    }
    private static AuraCell generate(ServerLevel level, int x, int z) {
        if (!thaumcraft.config.ThaumcraftConfig.generateAura.get()) return new AuraCell(0, 0, 0);
        int[][] offsets = {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        float life = 0;
        var source = level.getChunkSource();
        var resolver = source.getGenerator().getBiomeSource().createUncachedResolver(source.randomState());
        // Sample the generator, not neighboring chunks: initialization must not load terrain.
        for (var offset : offsets) {
            Holder<Biome> biome = resolver.getNoiseBiome(
                (x + offset[0]) * 4 + 2, 12, (z + offset[1]) * 4 + 2);
            life += biomeWeight(biome);
        }
        var random = new Random(level.getSeed() ^ (x * 341873128712L) ^ (z * 132897987541L));
        int base = Math.clamp((int)(life / 5 * 500 * (1 + random.nextGaussian() * .1)), 0, 500);
        return new AuraCell(base, base, 0);
    }
    private static float biomeWeight(Holder<Biome> biome) {
        if (biome.is(thaumcraft.worldgen.MagicalForest.KEY)) return .625F;
        if (biome.is(Identifier.parse("minecraft:mushroom_fields"))) return .75F;
        float sum = 0; int count = 0;
        for (var entry : BIOME_WEIGHTS) {
            if (biome.is(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("minecraft", entry.getKey())))) { sum += entry.getValue(); count++; }
        }
        if (count > 0) return sum / count;
        if (biome.value().getBaseTemperature() < .2F) return .25F;
        if (!biome.value().hasPrecipitation()) return .125F;
        if (biome.is(Identifier.parse("minecraft:plains")) || biome.is(Identifier.parse("minecraft:sunflower_plains"))) return .3F;
        return .5F;
    }
}
