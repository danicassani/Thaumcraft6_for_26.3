package thaumcraft.essentia;

import net.minecraft.core.*;
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
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

public final class TubeBlock extends Block implements EntityBlock{
    public enum Kind{NORMAL,VALVE,FILTER,RESTRICT,ONEWAY,BUFFER}
    public static final BooleanProperty[] SIDES={BlockStateProperties.DOWN,BlockStateProperties.UP,BlockStateProperties.NORTH,BlockStateProperties.SOUTH,BlockStateProperties.WEST,BlockStateProperties.EAST};
    public static final EnumProperty<Direction> FACING=BlockStateProperties.FACING;
    public static final BooleanProperty OPEN=BooleanProperty.create("open");
    public final Kind kind;
    public TubeBlock(Kind k,Properties p){super(p);kind=k;var state=stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(OPEN,true);for(var side:SIDES)state=state.setValue(side,false);registerDefaultState(state);}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,OPEN);b.add(SIDES);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getNearestLookingDirection().getOpposite());}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new TubeEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide()?null:(w,p,b,e)->{if(e instanceof TubeEntity tube)tube.serverTick();};}
    @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,LivingEntity by,ItemStack stack){super.setPlacedBy(l,p,s,by,stack);if(l.getBlockEntity(p) instanceof TubeEntity t)t.refreshConnections();}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){double a=kind==Kind.BUFFER?4:6,b=16-a;var shape=Block.box(a,a,a,b,b,b);for(var d:Direction.values())if(s.getValue(SIDES[d.ordinal()]))shape=Shapes.or(shape,switch(d){case DOWN->Block.box(6,0,6,10,8,10);case UP->Block.box(6,8,6,10,16,10);case NORTH->Block.box(6,6,0,10,10,8);case SOUTH->Block.box(6,6,8,10,10,16);case WEST->Block.box(0,6,6,8,10,10);case EAST->Block.box(8,6,6,16,10,10);});return shape;}
    @Override protected InteractionResult useItemOn(ItemStack item,BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){return l.getBlockEntity(p) instanceof TubeEntity t?t.interact(player,hand,hit):InteractionResult.PASS;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){return l.getBlockEntity(p) instanceof TubeEntity t?t.interact(player,InteractionHand.MAIN_HAND,hit):InteractionResult.PASS;}
    @Override protected boolean hasAnalogOutputSignal(BlockState s){return true;}
    @Override protected int getAnalogOutputSignal(BlockState s,Level l,BlockPos p,Direction face){return l.getBlockEntity(p) instanceof TubeEntity t&&t.total()>0?1+14*t.total()/t.capacity():0;}
}
