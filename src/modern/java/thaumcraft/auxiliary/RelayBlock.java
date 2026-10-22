package thaumcraft.auxiliary;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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
public final class RelayBlock extends DiodeBlock implements EntityBlock {
 public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;public static final BooleanProperty ACTIVE=BlockStateProperties.POWERED;
 public RelayBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(ACTIVE,false));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING,ACTIVE);}
 public BlockState getStateForPlacement(BlockPlaceContext c){var face=c.getHorizontalDirection();if(!c.isSecondaryUseActive())face=face.getOpposite();var s=defaultBlockState().setValue(FACING,face);return s.canSurvive(c.getLevel(),c.getClickedPos())?s:null;}
 protected boolean canSurvive(BlockState s,LevelReader l,BlockPos p){return !l.hasChunkAt(p.below())||l.getBlockState(p.below()).isFaceSturdy(l,p.below(),Direction.UP);}
 protected BlockState updateShape(BlockState s,LevelReader l,ScheduledTickAccess ticks,BlockPos p,Direction d,BlockPos q,BlockState other,RandomSource r){return s.canSurvive(l,p)?s:Blocks.AIR.defaultBlockState();}
 protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Block.box(0,0,0,16,7,16);}
 protected int getDelay(BlockState s){return 2;}
 protected int getOutputSignal(BlockGetter l,BlockPos p,BlockState s){return l.getBlockEntity(p) instanceof RelayEntity relay?relay.output():15;}
 protected boolean shouldTurnOn(Level l,BlockPos p,BlockState s){return l.hasChunkAt(p.relative(s.getValue(FACING)))&&getInputSignal(l,p,s)>=(l.getBlockEntity(p) instanceof RelayEntity relay?relay.input():1);}
 public void reevaluate(Level l,BlockPos p,BlockState s){checkTickOnNeighbor(l,p,s);}
 protected void updateNeighborsInFront(Level l,BlockPos p,BlockState s){if(l.hasChunkAt(p.relative(s.getValue(FACING).getOpposite())))super.updateNeighborsInFront(l,p,s);}
 public boolean shouldPrioritize(BlockGetter l,BlockPos p,BlockState s){return l instanceof LevelReader reader&&reader.hasChunkAt(p.relative(s.getValue(FACING).getOpposite()))&&super.shouldPrioritize(l,p,s);}
 public void notifyOutput(Level l,BlockPos p,BlockState s){updateNeighborsInFront(l,p,s);}
 protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource r){super.tick(s,l,p,r);if(l.getBlockState(p).getValue(ACTIVE)!=s.getValue(ACTIVE))notifyOutput(l,p,l.getBlockState(p));}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player user,BlockHitResult hit){if(!user.mayBuild()||!l.mayInteract(user,p))return InteractionResult.FAIL;var face=s.getValue(FACING);var v=hit.getLocation().subtract(Vec3.atLowerCornerOf(p));double front=(v.x-.5)*face.getStepX()+(v.z-.5)*face.getStepZ();if(!l.isClientSide()&&l.getBlockEntity(p) instanceof RelayEntity relay){relay.adjust(front>0);l.playSound(null,p,net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),net.minecraft.sounds.SoundSource.BLOCKS,.5F,1);user.sendSystemMessage(net.minecraft.network.chat.Component.translatable("auxiliary.thaumcraft.relay",relay.input(),relay.output()));}return InteractionResult.SUCCESS;}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new RelayEntity(p,s);}
}
