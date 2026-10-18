package thaumcraft.crystals;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.aura.AuraManager;

/** The original size/generation cycle, with six synchronized support faces. */
public final class CrystalBlock extends Block {
    public static final IntegerProperty SIZE = IntegerProperty.create("size", 0, 3);
    public static final IntegerProperty GENERATION = IntegerProperty.create("gen", 1, 4);
    public final String aspect;
    public CrystalBlock(String aspect, BlockBehaviour.Properties properties) {
        super(properties); this.aspect = aspect;
        var state = stateDefinition.any().setValue(SIZE, 0).setValue(GENERATION, 1);
        for (var property : PipeBlock.PROPERTY_BY_DIRECTION.values()) state = state.setValue(property, false);
        registerDefaultState(state);
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SIZE, GENERATION); PipeBlock.PROPERTY_BY_DIRECTION.values().forEach(builder::add);
    }
    public static boolean support(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE) && state.isFaceSturdy(level, pos, face);
    }
    public BlockState supportedState(BlockState state, BlockGetter level, BlockPos pos) {
        for (Direction side : Direction.values()) {
            BlockPos neighbor = pos.relative(side);
            if (level instanceof LevelReader reader && !reader.hasChunk(neighbor.getX() >> 4, neighbor.getZ() >> 4)) continue;
            state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side), support(level.getBlockState(neighbor), level, neighbor, side.getOpposite()));
        }
        return state;
    }
    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        var attached = supportedState(state, level, pos);
        return PipeBlock.PROPERTY_BY_DIRECTION.values().stream().anyMatch(attached::getValue);
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        var state = supportedState(defaultBlockState(), context.getLevel(), context.getClickedPos());
        return canSurvive(state, context.getLevel(), context.getClickedPos()) ? state : null;
    }
    @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction side, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        if (!canSurvive(state, level, pos)) ticks.scheduleTick(pos, this, 1);
        return supportedState(state, level, pos);
    }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) level.destroyBlock(pos, true);
    }
    @Override protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) { return List.of(CrystalContent.essence(aspect, state.getValue(SIZE) + 1)); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Shapes.empty();
        for (var side : Direction.values()) if (state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side))) {
            shape = Shapes.or(shape, switch(side) {
                case DOWN -> Block.box(0, 0, 0, 16, 8, 16); case UP -> Block.box(0, 8, 0, 16, 16, 16);
                case WEST -> Block.box(0, 0, 0, 8, 16, 16); case EAST -> Block.box(8, 0, 0, 16, 16, 16);
                case NORTH -> Block.box(0, 0, 0, 16, 16, 8); case SOUTH -> Block.box(0, 0, 8, 16, 16, 16);
            });
        }
        return shape.isEmpty() ? Shapes.block() : shape;
    }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3 + state.getValue(GENERATION)) == 0) grow(state, level, pos, random);
    }
    /** One successful growth attempt. No generation or mutation of unloaded chunks. */
    public void grow(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        var aura = AuraManager.get(level, pos); if (aura == null) return;
        boolean tainted = aspect.equals("vitium"); float available = tainted ? aura.flux() : aura.vis();
        int size = state.getValue(SIZE), generation = state.getValue(GENERATION);
        if (available <= 10) {
            boolean touching = false;
            for (Direction direction : Direction.values()) if (level.hasChunkAt(pos.relative(direction)) && level.getBlockState(pos.relative(direction)).is(this)) touching = true;
            if (size > 0 || touching) {
                if (tainted) AuraManager.addFlux(level, pos, 10); else AuraManager.addVis(level, pos, 10);
                level.setBlockAndUpdate(pos, size > 0 ? state.setValue(SIZE, size - 1) : Blocks.AIR.defaultBlockState());
            }
        } else if (available > aura.base() + 10) {
            if (size < 3 && size < 5 - generation + pos.asLong() % 3) {
                if (drain(level, pos, tainted) == 10) level.setBlockAndUpdate(pos, state.setValue(SIZE, size + 1));
            } else if (generation < 4) {
                var target = pos.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
                if (target.equals(pos) || !level.hasChunkAt(target) || random.nextInt(16) != 0) return;
                var targetState = level.getBlockState(target);
                if (!targetState.getFluidState().isEmpty() || !targetState.canBeReplaced() || !canSurvive(defaultBlockState(), level, target)) return;
                if (drain(level, pos, tainted) == 10) {
                    int next = generation + (random.nextInt(6) == 0 ? 0 : 1);
                    level.setBlockAndUpdate(target, supportedState(defaultBlockState().setValue(GENERATION, next), level, target));
                }
            }
        }
    }
    private static float drain(ServerLevel level, BlockPos pos, boolean tainted) { return tainted ? AuraManager.drainFlux(level, pos, 10, false) : AuraManager.drainVis(level, pos, 10, false); }
}
