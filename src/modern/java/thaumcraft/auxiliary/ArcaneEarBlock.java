package thaumcraft.auxiliary;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.util.RandomSource;
public final class ArcaneEarBlock extends Block implements EntityBlock {
 public static final EnumProperty<Direction> FACING=BlockStateProperties.FACING;public static final BooleanProperty ACTIVE=BlockStateProperties.POWERED;public final boolean toggle;
 public ArcaneEarBlock(boolean toggle,Properties p){super(p);this.toggle=toggle;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.UP).setValue(ACTIVE,false));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING,ACTIVE);}
 public BlockState getStateForPlacement(BlockPlaceContext c){var s=defaultBlockState().setValue(FACING,c.getClickedFace());return c.getLevel().hasChunkAt(c.getClickedPos().relative(c.getClickedFace().getOpposite()))&&s.canSurvive(c.getLevel(),c.getClickedPos())?s:null;}
 protected boolean canSurvive(BlockState s,LevelReader l,BlockPos p){var back=p.relative(s.getValue(FACING).getOpposite());return !l.hasChunkAt(back)||l.getBlockState(back).isFaceSturdy(l,back,s.getValue(FACING));}
 protected BlockState updateShape(BlockState s,LevelReader l,ScheduledTickAccess ticks,BlockPos p,Direction d,BlockPos q,BlockState other,RandomSource r){return s.canSurvive(l,p)?s:Blocks.AIR.defaultBlockState();}
 protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(s.getValue(FACING)){case UP->Block.box(2,0,2,14,6,14);case DOWN->Block.box(2,10,2,14,16,14);case NORTH->Block.box(2,2,10,14,14,16);case SOUTH->Block.box(2,2,0,14,14,6);case WEST->Block.box(10,2,2,16,14,14);case EAST->Block.box(0,2,2,6,14,14);};}
 protected boolean isSignalSource(BlockState s){return true;}protected int getSignal(BlockState s,BlockGetter l,BlockPos p,Direction d){return s.getValue(ACTIVE)?15:0;}protected int getDirectSignal(BlockState s,BlockGetter l,BlockPos p,Direction d){return getSignal(s,l,p,d);}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player user,BlockHitResult hit){if(!user.mayBuild()||!l.mayInteract(user,p))return InteractionResult.FAIL;if(!l.isClientSide()&&l.getBlockEntity(p) instanceof ArcaneEarEntity ear){ear.tune();user.sendSystemMessage(net.minecraft.network.chat.Component.translatable("auxiliary.thaumcraft.note",ear.note(),ear.instrument().getSerializedName()));}return InteractionResult.SUCCESS;}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ArcaneEarEntity(p,s);}public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide()?null:(world,p,state,e)->{if(e instanceof ArcaneEarEntity ear)ear.serverTick();};}
}
