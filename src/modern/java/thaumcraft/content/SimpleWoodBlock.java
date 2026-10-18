package thaumcraft.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

class SimpleWoodBlock extends Block {
    SimpleWoodBlock(BlockBehaviour.Properties p) { super(p); }
    @Override public int getFlammability(BlockState s, BlockGetter l, BlockPos p, Direction f) { return 20; }
    @Override public int getFireSpreadSpeed(BlockState s, BlockGetter l, BlockPos p, Direction f) { return 5; }
}
class SimpleLogBlock extends RotatedPillarBlock {
    SimpleLogBlock(BlockBehaviour.Properties p) { super(p); }
    @Override public int getFlammability(BlockState s, BlockGetter l, BlockPos p, Direction f) { return 5; }
    @Override public int getFireSpreadSpeed(BlockState s, BlockGetter l, BlockPos p, Direction f) { return 5; }
}
class SimpleStairBlock extends StairBlock {
    private final boolean wood;
    SimpleStairBlock(BlockState base, BlockBehaviour.Properties p, boolean wood) { super(base, p); this.wood = wood; }
    @Override public int getFlammability(BlockState s, BlockGetter l, BlockPos p, Direction f) { return wood ? 20 : 0; }
    @Override public int getFireSpreadSpeed(BlockState s, BlockGetter l, BlockPos p, Direction f) { return wood ? 5 : 0; }
}
class SimpleSlabBlock extends SlabBlock {
    private final boolean wood;
    SimpleSlabBlock(BlockBehaviour.Properties p, boolean wood) { super(p); this.wood = wood; }
    @Override public int getFlammability(BlockState s, BlockGetter l, BlockPos p, Direction f) { return wood ? 20 : 0; }
    @Override public int getFireSpreadSpeed(BlockState s, BlockGetter l, BlockPos p, Direction f) { return wood ? 5 : 0; }
}
