package thaumcraft.storage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
public final class HungryChestBlock extends ChestBlock {
 public HungryChestBlock(BlockBehaviour.Properties p){super(()->HungryChest.TYPE.get(),net.minecraft.sounds.SoundEvents.CHEST_OPEN,net.minecraft.sounds.SoundEvents.CHEST_CLOSE,p);}
 public boolean chestCanConnectTo(BlockState state){return false;}
 public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new HungryChest(pos,state);}
 protected MenuProvider getMenuProvider(BlockState state,Level level,BlockPos pos){return level.getBlockEntity(pos) instanceof HungryChest chest?chest:null;}
 public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState state,Level level,BlockPos pos,boolean ignore){return level.getBlockEntity(pos) instanceof HungryChest chest?new DoubleBlockCombiner.NeighborCombineResult.Single<>(chest):DoubleBlockCombiner.Combiner::acceptNone;}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){return l.isClientSide()?super.getTicker(l,s,type):createTickerHelper(type,HungryChest.TYPE.get(),(world,pos,state,chest)->chest.collect());}
}
