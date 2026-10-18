package thaumcraft.alchemy;

import java.util.List;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

public final class AlchemyBlock extends Block implements EntityBlock {
    public enum Kind{CRUCIBLE,SMELTER,ALEMBIC,JAR,VOID_JAR}
    public static final BooleanProperty LIT=BlockStateProperties.LIT;
    public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
    public final Kind kind;
    public AlchemyBlock(Kind kind,Properties p){super(p);this.kind=kind;registerDefaultState(stateDefinition.any().setValue(LIT,false).setValue(FACING,Direction.NORTH));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(LIT,FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new AlchemyMachine(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState s,BlockEntityType<T> type){return level.isClientSide()?null:(l,p,b,e)->{if(e instanceof AlchemyMachine m)m.serverTick();};}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(kind){case CRUCIBLE->Shapes.or(Block.box(0,0,0,16,5,16),Block.box(0,5,0,2,16,16),Block.box(14,5,0,16,16,16),Block.box(2,5,0,14,16,2),Block.box(2,5,14,14,16,16));case JAR,VOID_JAR->Block.box(3,0,3,13,12,13);case ALEMBIC->Block.box(2,0,2,14,16,14);default->Shapes.block();};}
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){if(l.getBlockEntity(p) instanceof AlchemyMachine m){if(stack.is(thaumcraft.essentia.EssentiaContent.LABEL.get()))return m.label(player,hand,hit.getDirection());return m.interact(player,hand);}return InteractionResult.TRY_WITH_EMPTY_HAND;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(l.getBlockEntity(p) instanceof AlchemyMachine m)return m.interact(player,InteractionHand.MAIN_HAND);return InteractionResult.SUCCESS;}
    @Override public void setPlacedBy(Level l,BlockPos pos,BlockState s,LivingEntity by,ItemStack stack){super.setPlacedBy(l,pos,s,by,stack);if(l instanceof ServerLevel&&l.getBlockEntity(pos) instanceof AlchemyMachine m)m.placeContents(stack);}
    @Override protected List<ItemStack> getDrops(BlockState s,LootParams.Builder params){if((kind==Kind.JAR||kind==Kind.VOID_JAR)&&params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof AlchemyMachine m)return List.of(m.jarItem());return super.getDrops(s,params);}
    @Override protected boolean hasAnalogOutputSignal(BlockState state){return true;}
    @Override protected int getAnalogOutputSignal(BlockState state,Level level,BlockPos pos,Direction direction){return level.getBlockEntity(pos) instanceof AlchemyMachine m?m.comparator():0;}
}
