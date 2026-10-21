package thaumcraft.thaumatorium;
import net.minecraft.core.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
public final class BrainBoxBlock extends Block{
 public static final EnumProperty<Direction> FACING=BlockStateProperties.FACING;
 public BrainBoxBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.DOWN));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING);}
 public BlockState getStateForPlacement(BlockPlaceContext c){var s=defaultBlockState().setValue(FACING,c.getClickedFace().getOpposite());return canSurvive(s,c.getLevel(),c.getClickedPos())?s:null;}
 protected boolean canSurvive(BlockState s,LevelReader l,BlockPos p){var d=s.getValue(FACING);var target=l.getBlockState(p.relative(d));return target.getBlock() instanceof ThaumatoriumBlock&&d.getOpposite()!=target.getValue(ThaumatoriumBlock.FACING)&&d!=Direction.UP;}
 protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moved){super.onPlace(s,l,p,old,moved);if(!l.isClientSide())l.scheduleTick(p,this,20);}
 protected void tick(BlockState s,net.minecraft.server.level.ServerLevel l,BlockPos p,net.minecraft.util.RandomSource random){if(!l.hasChunkAt(p.relative(s.getValue(FACING)))){l.scheduleTick(p,this,20);return;}if(!canSurvive(s,l,p)){l.destroyBlock(p,true);return;}l.scheduleTick(p,this,20);}
 protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Block.box(3,3,3,13,13,13);}
}
