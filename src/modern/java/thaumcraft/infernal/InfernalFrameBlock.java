package thaumcraft.infernal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class InfernalFrameBlock extends Block implements EntityBlock{
 public InfernalFrameBlock(Properties p){super(p);}
 protected RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new InfernalFrame(p,s);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide()?null:(w,p,b,e)->{if(e instanceof InfernalFrame f)f.serverTick();};}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player user,BlockHitResult hit){if(!l.isClientSide()&&l.getBlockEntity(p) instanceof InfernalFrame f&&f.controller!=null&&l.hasChunkAt(f.controller)&&l.getBlockEntity(f.controller) instanceof InfernalFurnace oven)oven.inspect(user);return InteractionResult.SUCCESS;}
}
