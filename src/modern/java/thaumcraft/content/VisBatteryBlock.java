package thaumcraft.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import thaumcraft.aura.AuraManager;

/** Original ten-unit vis buffer, including powered five-tick release and comparator output. */
public final class VisBatteryBlock extends Block {
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, 10);
    public VisBatteryBlock(BlockBehaviour.Properties properties) {
        super(properties); registerDefaultState(stateDefinition.any().setValue(CHARGE, 0));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(CHARGE); }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) { return state.getValue(CHARGE); }
    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean piston) {
        if (!level.isClientSide() && !oldState.is(this)) level.scheduleTick(pos, this, 1);
    }
    @Override protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean piston) {
        if (!level.isClientSide() && level.hasNeighborSignal(pos)) level.scheduleTick(pos, this, 1);
    }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) { transfer(state, level, pos, random); }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) { transfer(state, level, pos, random); }
    /** Also used by integration fixtures to exercise the real transfer path. */
    public void transfer(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int charge = state.getValue(CHARGE), next = charge, delay = 0;
        var cell = AuraManager.get(level, pos); if (cell == null) return;
        if (level.hasNeighborSignal(pos)) {
            if (charge > 0 && cell.vis() <= thaumcraft.aura.AuraCell.MAX - 1 && AuraManager.addVis(level, pos, 1) == 1) { next--; delay = 5; }
        } else if (charge < 10 && cell.vis() > cell.base() * .9F && cell.vis() > 1) {
            if (AuraManager.drainVis(level, pos, 1, false) == 1) { next++; delay = 100 + random.nextInt(100); }
        } else if (charge > 0 && cell.vis() < cell.base() * .75F) {
            if (AuraManager.addVis(level, pos, 1) == 1) { next--; delay = 20 + random.nextInt(20); }
        }
        if (next != charge) {
            level.setBlock(pos, state.setValue(CHARGE, next), Block.UPDATE_ALL);
            level.updateNeighbourForOutputSignal(pos, this);
            level.scheduleTick(pos, this, delay);
        }
    }
}
