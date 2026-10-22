package thaumcraft.mirrors;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
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
import net.minecraft.util.RandomSource;
public final class MirrorBlock extends Block implements EntityBlock {
 public static final EnumProperty<Direction> FACING=BlockStateProperties.FACING;public static final BooleanProperty ACTIVE=BlockStateProperties.ENABLED;public final boolean essence;
 public MirrorBlock(boolean essence,Properties p){super(p);this.essence=essence;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.UP).setValue(ACTIVE,false));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,ACTIVE);}
 protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(s.getValue(FACING)){case UP->Block.box(0,0,0,16,2,16);case DOWN->Block.box(0,14,0,16,16,16);case NORTH->Block.box(0,0,14,16,16,16);case SOUTH->Block.box(0,0,0,16,16,2);case EAST->Block.box(0,0,0,2,16,16);case WEST->Block.box(14,0,0,16,16,16);};}
 public BlockState getStateForPlacement(BlockPlaceContext c){var s=defaultBlockState().setValue(FACING,c.getClickedFace());return c.getLevel().hasChunkAt(c.getClickedPos().relative(c.getClickedFace().getOpposite()))&&s.canSurvive(c.getLevel(),c.getClickedPos())?s:null;}
 protected boolean canSurvive(BlockState s,LevelReader l,BlockPos p){var back=s.getValue(FACING).getOpposite();return !l.hasChunkAt(p.relative(back))||l.getBlockState(p.relative(back)).isFaceSturdy(l,p.relative(back),back.getOpposite());}
 protected BlockState updateShape(BlockState s,LevelReader l,ScheduledTickAccess ticks,BlockPos p,Direction face,BlockPos other,BlockState neighbor,RandomSource random){return !s.canSurvive(l,p)?Blocks.AIR.defaultBlockState():super.updateShape(s,l,ticks,p,face,other,neighbor,random);}
 public void setPlacedBy(Level l,BlockPos pos,BlockState state,LivingEntity by,ItemStack stack){super.setPlacedBy(l,pos,state,by,stack);if(l instanceof ServerLevel&&l.getBlockEntity(pos) instanceof MirrorEntity mirror){var link=stack.get(MirrorContent.LINK.get());if(link!=null)mirror.remember(link);}}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new MirrorEntity(p,s);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide()?null:(world,p,state,e)->{if(e instanceof MirrorEntity mirror)mirror.serverTick();};}
 protected List<ItemStack> getDrops(BlockState s,LootParams.Builder params){return params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof MirrorEntity m?List.of(m.droppedItem()):super.getDrops(s,params);}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(!l.isClientSide()&&l.getBlockEntity(p) instanceof MirrorEntity m)player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(m.peer()!=null?"mirror.thaumcraft.connected":"mirror.thaumcraft.waiting"));return InteractionResult.SUCCESS;}
}
