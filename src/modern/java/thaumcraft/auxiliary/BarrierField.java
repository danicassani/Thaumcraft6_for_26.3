package thaumcraft.auxiliary;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
public final class BarrierField extends Block {
 public BarrierField(Properties p){super(p);}
 public static BlockPos anchor(BlockGetter l,BlockPos p){if(l.getBlockState(p.below()).is(AuxiliaryContent.BARRIER.get()))return p.below();if(l.getBlockState(p.below()).is(AuxiliaryContent.FIELD.get())&&l.getBlockState(p.below(2)).is(AuxiliaryContent.BARRIER.get()))return p.below(2);return null;}
 private static boolean hasPlayer(LivingEntity e){for(var passenger:e.getIndirectPassengers())if(passenger instanceof Player)return true;return false;}
 protected RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
 protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
 protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){var at=anchor(l,p);if(at==null||!(c instanceof EntityCollisionContext ec)||!(ec.getEntity() instanceof LivingEntity e)||e instanceof Player||hasPlayer(e))return Shapes.empty();return l instanceof Level world&&world.hasNeighborSignal(at)?Shapes.empty():Shapes.block();}
 protected BlockState updateShape(BlockState s,LevelReader l,ScheduledTickAccess ticks,BlockPos p,Direction d,BlockPos q,BlockState other,RandomSource r){return anchor(l,p)==null?Blocks.AIR.defaultBlockState():s;}
 protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moving){if(!l.isClientSide())l.scheduleTick(p,this,20);}
 protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource r){if(anchor(l,p)==null)l.removeBlock(p,false);else l.scheduleTick(p,this,20);}
}
