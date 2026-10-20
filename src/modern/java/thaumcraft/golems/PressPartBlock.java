package thaumcraft.golems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
public final class PressPartBlock extends Block implements EntityBlock {
 public PressPartBlock(Properties p){super(p);}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new PressPart(p,s);}
 protected RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
 protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return l.getBlockEntity(p) instanceof PressPart part?part.original.getShape(l,p,c):Shapes.block();}
 protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return getShape(s,l,p,c);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide()?null:(a,b,c,e)->{if(e instanceof PressPart part)part.serverTick();};}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player owner,BlockHitResult hit){if(!l.isClientSide()&&l.getBlockEntity(p) instanceof PressPart part&&part.controller!=null&&l.hasChunkAt(part.controller)&&l.getBlockEntity(part.controller) instanceof GolemBuilder press)owner.openMenu(press);return InteractionResult.SUCCESS;}
}
