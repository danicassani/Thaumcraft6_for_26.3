package thaumcraft.condenser;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
public final class CondenserBlock extends Block implements EntityBlock{
 public static final BooleanProperty ENABLED=BlockStateProperties.ENABLED,ACTIVE=BooleanProperty.create("active");
 public CondenserBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(ENABLED,true).setValue(ACTIVE,false));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(ENABLED,ACTIVE);}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Condenser(p,s);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide()?null:(w,p,b,e)->{if(e instanceof Condenser c)c.serverTick();};}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player user,BlockHitResult hit){if(!l.isClientSide()&&l.getBlockEntity(p) instanceof Condenser c)c.inspect(user);return InteractionResult.SUCCESS;}
 protected boolean hasAnalogOutputSignal(BlockState s){return true;}
 protected int getAnalogOutputSignal(BlockState s,Level l,BlockPos p,Direction d){return l.getBlockEntity(p) instanceof Condenser c&&c.supported()&&c.snapshot().output()>0?1+14*c.snapshot().output()/100:0;}
}
