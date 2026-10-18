package thaumcraft.content;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.*;

public final class SimplePlantBlock extends VegetationBlock {
    private final String name;
    public SimplePlantBlock(String name, BlockBehaviour.Properties p) { super(p); this.name = name; }
    @Override public void animateTick(BlockState s, Level l, BlockPos p, net.minecraft.util.RandomSource random) {
        double x = p.getX() + 0.5, y = p.getY() + 0.6, z = p.getZ() + 0.5;
        if (name.equals("cinderpearl")) {
            if (random.nextBoolean()) {
                l.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
                l.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME, x, y, z, 0, 0, 0);
            }
        } else if (random.nextInt(3) == 0) {
            l.addParticle(new net.minecraft.core.particles.DustParticleOptions(name.equals("vishroom") ? 0x804DCC : 0x66CCCC, 0.5F), x, y, z, 0, 0.01, 0);
        }
    }
    @Override protected boolean mayPlaceOn(BlockState s, BlockGetter l, BlockPos p) {
        if (name.equals("cinderpearl")) return s.is(BlockTags.SAND) || s.is(BlockTags.TERRACOTTA) || s.is(Blocks.DIRT);
        return s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT) || s.is(Blocks.FARMLAND);
    }
    @Override protected boolean canSurvive(BlockState s, LevelReader l, BlockPos p) { return mayPlaceOn(l.getBlockState(p.below()), l, p.below()); }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return Block.box(2, 0, 2, 14, 12, 14); }
    @Override protected void entityInside(BlockState s, Level l, BlockPos p, Entity e, InsideBlockEffectApplier a, boolean precise) {
        if (name.equals("vishroom") && !l.isClientSide() && e instanceof LivingEntity living && l.getRandom().nextInt(5) == 0) living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200));
    }
}
