package thaumcraft.treasure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

/** Original break-to-open containers; Silk Touch keeps an unopened block. */
public final class TreasureBlock extends Block {
    private final boolean urn;
    public TreasureBlock(boolean urn,Properties properties) { super(properties);this.urn=urn; }
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {
        return urn ? Block.box(2,1,2,14,13,14) : Block.box(1,0,1,15,14,15);
    }
}
