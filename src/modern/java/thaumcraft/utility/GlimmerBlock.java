package thaumcraft.utility;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
public final class GlimmerBlock extends Block implements EntityBlock {
 public GlimmerBlock(Properties p){super(p);}protected RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new GlimmerEntity(p,s);}protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moving){}protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource r){if(l.getBlockEntity(p) instanceof GlimmerEntity glow)glow.validateOwner();if(l.getBlockState(p).is(this)&&l.getBlockEntity(p) instanceof GlimmerEntity glow&&glow.owned())l.scheduleTick(p,this,20);}
}
