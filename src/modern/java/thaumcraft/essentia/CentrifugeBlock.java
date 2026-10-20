package thaumcraft.essentia;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
public final class CentrifugeBlock extends Block implements EntityBlock{
 public static final IntegerProperty PHASE=IntegerProperty.create("phase",0,7);
 public static final BooleanProperty ACTIVE=BooleanProperty.create("active");
 public CentrifugeBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(PHASE,0).setValue(ACTIVE,false));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(PHASE,ACTIVE);}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new CentrifugeEntity(p,s);}
 @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide()?null:(w,p,b,e)->{if(e instanceof CentrifugeEntity c)c.serverTick();};}
 @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.or(Block.box(4,0,4,12,4,12),Block.box(0,5,6,16,11,10),Block.box(4,12,4,12,16,12));}
 private InteractionResult inspect(Level l,BlockPos at,Player p){if(!(l.getBlockEntity(at) instanceof CentrifugeEntity c))return InteractionResult.PASS;if(!l.isClientSide()){EssentiaContent.inspect(p,c,Direction.UP);p.sendSystemMessage(net.minecraft.network.chat.Component.translatable(c.supported()?"essentia.thaumcraft.centrifuge.status":"alchemy.thaumcraft.invalid",c.snapshot().input().isEmpty()?"—":net.minecraft.network.chat.Component.translatable("tc.aspect."+c.snapshot().input()),c.snapshot().remaining(),c.powered()?"⏸":"▶"));}return InteractionResult.SUCCESS;}
 @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player p,BlockHitResult hit){return inspect(l,at,p);}
 @Override protected boolean hasAnalogOutputSignal(BlockState s){return true;}
 @Override protected int getAnalogOutputSignal(BlockState s,Level l,BlockPos p,Direction face){return l.getBlockEntity(p) instanceof CentrifugeEntity c&&c.supported()&&!c.snapshot().output().isEmpty()?15:0;}
}
