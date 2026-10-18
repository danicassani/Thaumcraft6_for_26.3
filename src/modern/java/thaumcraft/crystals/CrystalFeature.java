package thaumcraft.crystals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.Feature;


import thaumcraft.config.ThaumcraftConfig;

/** Cave clusters on solid mineral faces. Flux crystals are grown from flux, not free worldgen. */
public final class CrystalFeature implements Feature {
    public static final com.mojang.serialization.MapCodec<CrystalFeature> CODEC = com.mojang.serialization.MapCodec.unit(new CrystalFeature());
    @Override public com.mojang.serialization.MapCodec<CrystalFeature> codec() { return CODEC; }
    @Override public boolean place(net.minecraft.world.level.WorldGenLevel level, net.minecraft.world.level.chunk.ChunkGenerator generator, net.minecraft.util.RandomSource random, BlockPos origin) {
        if (!ThaumcraftConfig.generateCrystals.get()) return false;
        String aspect = CrystalContent.NATURAL[random.nextInt(6)];
        if (random.nextInt(3) == 0) {
            var biome = level.getBiome(origin).value();
            if (biome.getBaseTemperature() > 1) aspect = "ignis";
            else if (biome.getBaseTemperature() < .2F) aspect = "ordo";
            else if (biome.hasPrecipitation()) aspect = "aqua";
        }
        var block = CrystalContent.BLOCKS.get(aspect).get(); int placed = 0;
        for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++) for (int z = -1; z <= 1; z++) {
            var pos = origin.offset(x, y, z);
            if (random.nextInt(3) == 0 || !level.getBlockState(pos).isAir() || !block.defaultBlockState().canSurvive(level, pos)) continue;
            level.setBlock(pos, block.supportedState(block.defaultBlockState().setValue(CrystalBlock.SIZE, 1 + random.nextInt(3)), level, pos), 2); placed++;
        }
        return placed > 0;
    }
}
