package thaumcraft.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.*;

public final class LuminousBlock extends Block {
    private final boolean nitor;
    private final int color;
    public LuminousBlock(boolean nitor, int color, BlockBehaviour.Properties properties) { super(properties); this.nitor = nitor; this.color = color; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return nitor ? Block.box(5.28, 5.28, 5.28, 10.56, 10.56, 10.56) : Block.box(6, 0, 6, 10, 8, 10);
    }
    @Override protected RenderShape getRenderShape(BlockState state) { return nitor ? RenderShape.INVISIBLE : RenderShape.MODEL; }
    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) { return nitor || level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP); }
    @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction side, BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
        if (!nitor && !canSurvive(state, level, pos)) { ticks.scheduleTick(pos, this, 1); }
        return super.updateShape(state, level, ticks, pos, side, neighbourPos, neighbour, random);
    }
    @Override protected void tick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) level.destroyBlock(pos, true);
    }
    @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (nitor) {
            for (int i = 0; i < 3; i++) level.addParticle(new DustParticleOptions(color, 1.2F), pos.getX() + 0.5 + random.nextGaussian() * 0.07, pos.getY() + 0.5 + random.nextGaussian() * 0.07, pos.getZ() + 0.5 + random.nextGaussian() * 0.07, 0, 0.01, 0);
        } else {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 0, 0, 0);
            level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 0, 0, 0);
        }
    }
}
