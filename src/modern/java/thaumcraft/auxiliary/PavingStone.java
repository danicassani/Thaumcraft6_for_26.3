package thaumcraft.auxiliary;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
public final class PavingStone extends Block {
 final boolean barrier;
 public PavingStone(boolean barrier,Properties p){super(p);this.barrier=barrier;}
 protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Block.box(0,0,0,16,15,16);}
 public void stepOn(Level l,BlockPos p,BlockState s,Entity e){if(!barrier&&!l.isClientSide()&&e instanceof LivingEntity living){living.addEffect(new MobEffectInstance(MobEffects.SPEED,40,1,false,false));living.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST,40,0,false,false));}super.stepOn(l,p,s,e);}
 protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moving){super.onPlace(s,l,p,old,moving);if(barrier&&!l.isClientSide())l.scheduleTick(p,this,1);}
 protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource r){if(!barrier)return;for(int i=1;i<=2;i++){var q=p.above(i);if(l.hasChunkAt(q)&&l.isEmptyBlock(q))l.setBlockAndUpdate(q,AuxiliaryContent.FIELD.get().defaultBlockState());}if(!l.hasNeighborSignal(p))for(var e:l.getEntitiesOfClass(LivingEntity.class,new net.minecraft.world.phys.AABB(p).expandTowards(0,2,0).inflate(.1)))if(!(e instanceof net.minecraft.world.entity.player.Player)&&!e.onGround()){double yaw=Math.toRadians(e.getYRot()+180);e.push(-Math.sin(yaw)*.2,-.1,Math.cos(yaw)*.2);}l.scheduleTick(p,this,5);}
}
