package thaumcraft.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class WorkbenchBlock extends Block implements EntityBlock {
    public WorkbenchBlock(Properties properties) { super(properties); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ArcaneWorkbenchEntity(pos,state); }
    @Override protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) { return Block.box(0,0,0,16,16,16); }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if(!level.isClientSide() && level.getBlockEntity(pos) instanceof ArcaneWorkbenchEntity table){
            // Recognize workbenches already built in earlier port versions.
            if(thaumcraft.research.ResearchService.knows(player,"FIRSTSTEPS@1"))thaumcraft.research.ResearchService.recordCraft(player,new net.minecraft.world.item.ItemStack(this));
            player.openMenu(table);
        }
        return InteractionResult.SUCCESS;
    }
}
