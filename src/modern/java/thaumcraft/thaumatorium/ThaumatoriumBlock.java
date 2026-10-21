package thaumcraft.thaumatorium;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
public final class ThaumatoriumBlock extends Block implements EntityBlock{
 public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
 public final boolean top;
 public ThaumatoriumBlock(boolean t,Properties p){super(p);top=t;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Thaumatorium(p,s);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide()?null:(w,p,b,e)->{if(e instanceof Thaumatorium m)m.serverTick();};}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player user,BlockHitResult h){if(!l.isClientSide()&&l.getBlockEntity(p) instanceof Thaumatorium part){var m=part.controller();if(m!=null)user.openMenu(m);}return InteractionResult.SUCCESS;}
 protected boolean hasAnalogOutputSignal(BlockState s){return !top;}
 protected int getAnalogOutputSignal(BlockState s,Level l,BlockPos p,Direction face){return l.getBlockEntity(p) instanceof Thaumatorium m?net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromContainer(m.inventory):0;}
}
