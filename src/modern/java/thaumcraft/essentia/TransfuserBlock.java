package thaumcraft.essentia;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.world.*;
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
public final class TransfuserBlock extends Block implements EntityBlock {
 public static final EnumProperty<Direction> FACING=BlockStateProperties.FACING;public final boolean filling;
 public TransfuserBlock(boolean filling,Properties props){super(props);this.filling=filling;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.UP));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
 public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getClickedFace());}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new TransfuserEntity(p,s);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){return l.isClientSide()?null:(world,p,state,be)->{if(be instanceof TransfuserEntity t)t.serverTick();};}
 protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(s.getValue(FACING)){case DOWN->Block.box(4,8,4,12,16,12);case UP->Block.box(4,0,4,12,8,12);case NORTH->Block.box(4,4,8,12,12,16);case SOUTH->Block.box(4,4,0,12,12,8);case WEST->Block.box(8,4,4,16,12,12);case EAST->Block.box(0,4,4,8,12,12);};}
 protected InteractionResult useItemOn(ItemStack held,BlockState s,Level l,BlockPos at,Player player,InteractionHand hand,BlockHitResult hit){if(held.is(thaumcraft.research.ResearchContent.METER.get()))return InteractionResult.PASS;if(held.is(EssentiaContent.RESONATOR.get())||held.getItem() instanceof thaumcraft.thaumaturgy.CasterItem){if(player.isShiftKeyDown())return rotate(l,at,player);return inspect(l,at,player);}return InteractionResult.PASS;}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player p,BlockHitResult hit){return inspect(l,at,p);}
 public static InteractionResult rotate(Level l,BlockPos at,Player p){if(l.isClientSide())return InteractionResult.SUCCESS;if(!p.mayBuild()||!l.mayInteract(p,at)||p.distanceToSqr(Vec3.atCenterOf(at))>64||!(l.getBlockEntity(at) instanceof TransfuserEntity t)||!t.supported())return InteractionResult.FAIL;var s=l.getBlockState(at);l.setBlockAndUpdate(at,s.setValue(FACING,Direction.values()[(s.getValue(FACING).ordinal()+1)%6]));return InteractionResult.SUCCESS;}
 private InteractionResult inspect(Level l,BlockPos at,Player p){if(!l.isClientSide()&&l.getBlockEntity(at) instanceof TransfuserEntity t){EssentiaContent.inspect(p,t,t.facing().getOpposite());p.sendSystemMessage(net.minecraft.network.chat.Component.translatable(t.supported()?"essentia.thaumcraft.transfuser.status":"alchemy.thaumcraft.invalid",t.snapshot().transferred(),t.snapshot().pending().isEmpty()?"—":net.minecraft.network.chat.Component.translatable("tc.aspect."+t.snapshot().pending())));}return InteractionResult.SUCCESS;}
 protected List<ItemStack> getDrops(BlockState s,LootParams.Builder params){return params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof TransfuserEntity t?List.of(t.droppedItem()):super.getDrops(s,params);}
}
