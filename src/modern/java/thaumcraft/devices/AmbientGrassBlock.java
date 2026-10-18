package thaumcraft.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Vanilla grass lifecycle and original low-light ambient motes. No aura cost. */
public final class AmbientGrassBlock extends GrassBlock {
    public AmbientGrassBlock(BlockBehaviour.Properties properties) { super(properties); }
    @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) <= 7 && random.nextInt(4) == 0)
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + random.nextDouble(), pos.getY() + 1.1, pos.getZ() + random.nextDouble(), 0, .005, 0);
    }
}
