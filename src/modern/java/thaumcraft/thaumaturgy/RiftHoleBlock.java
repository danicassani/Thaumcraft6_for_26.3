package thaumcraft.thaumaturgy;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.world.phys.HitResult;
/** Collisionless, supporting placeholder: no item, loot, piston movement or fluid replacement. */
public final class RiftHoleBlock extends Block {
    public RiftHoleBlock(Properties properties){super(properties);}
    protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    protected VoxelShape getBlockSupportShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.block();}
    protected boolean canBeReplaced(BlockState s,BlockPlaceContext c){return false;}
    protected boolean canBeReplaced(BlockState s,Fluid fluid){return false;}
    protected RenderShape getRenderShape(BlockState state){return RenderShape.INVISIBLE;}
    public ItemStack getCloneItemStack(BlockState state,HitResult hit,LevelReader l,BlockPos p,Player player){return ItemStack.EMPTY;}
    public void animateTick(BlockState s,Level l,BlockPos p,RandomSource random){l.addParticle(net.minecraft.core.particles.ParticleTypes.PORTAL,p.getX()+random.nextDouble(),p.getY()+random.nextDouble(),p.getZ()+random.nextDouble(),0,0,0);}
}
