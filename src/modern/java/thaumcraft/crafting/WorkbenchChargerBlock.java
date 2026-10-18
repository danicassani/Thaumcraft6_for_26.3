package thaumcraft.crafting;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class WorkbenchChargerBlock extends Block {
    public WorkbenchChargerBlock(Properties properties) { super(properties); }
    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) { return level.getBlockState(pos.below()).is(ArcaneContent.WORKBENCH.get()); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return canSurvive(defaultBlockState(),context.getLevel(),context.getClickedPos())?defaultBlockState():null; }
    @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction side, BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
        if(!canSurvive(state,level,pos))ticks.scheduleTick(pos,this,1);return super.updateShape(state,level,ticks,pos,side,neighbourPos,neighbour,random);
    }
    @Override protected void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random) { if(!canSurvive(state,level,pos))level.destroyBlock(pos,true); }
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) { return Shapes.block(); }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        if(!level.isClientSide() && level.getBlockEntity(pos.below()) instanceof ArcaneWorkbenchEntity table)player.openMenu(table);return InteractionResult.SUCCESS;
    }
}
