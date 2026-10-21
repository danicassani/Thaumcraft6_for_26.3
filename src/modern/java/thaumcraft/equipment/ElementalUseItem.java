package thaumcraft.equipment;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
/** Continuous use is driven by the native active-hand protocol, never a client velocity packet. */
public final class ElementalUseItem extends Item {
 private final boolean sword;
 public ElementalUseItem(Properties p,boolean sword){super(p);this.sword=sword;}
 public int getUseDuration(ItemStack stack,LivingEntity user){return 72000;}
 public InteractionResult use(Level level,Player player,InteractionHand hand){if(!player.isAlive()||player.isSpectator())return InteractionResult.FAIL;player.startUsingItem(hand);return InteractionResult.CONSUME;}
 public void onUseTick(Level level,LivingEntity user,ItemStack stack,int remaining){
  if(!user.isAlive()||user.isPassenger())return;
  if(sword){var v=user.getDeltaMovement();double y=v.y;if(y<0){y/=1.2;user.fallDistance/=1.2F;}y+=.08;if(y>.5)y=.2;user.setDeltaMovement(v.x,y,v.z);if(user instanceof ServerPlayer p&&p.connection!=null)p.connection.resetFlyingTicks();
   if(level instanceof ServerLevel l){for(var target:l.getEntitiesOfClass(LivingEntity.class,user.getBoundingBox().inflate(2.5),e->e!=user&&e.isAlive()&&!(e instanceof Player)&&!e.isAlliedTo(user))){var delta=target.position().subtract(user.position());target.setDeltaMovement(target.getDeltaMovement().add(delta.scale(1/(2.5*(delta.length()+.1)))));target.syncVelocity=true;}if((72000-remaining)%4==0)l.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,user.getX(),user.getY()+.3,user.getZ(),6,1,.2,1,.02);if((72000-remaining)%20==0){stack.hurtAndBreak(1,user,user.getUsedItemHand());l.playSound(null,user.blockPosition(),net.minecraft.sounds.SoundEvents.BREEZE_WHIRL,net.minecraft.sounds.SoundSource.PLAYERS,.5F,1);}}
  }else if(level instanceof ServerLevel l){for(var item:l.getEntitiesOfClass(ItemEntity.class,user.getBoundingBox().inflate(10),e->e.isAlive()&&e.distanceToSqr(user)<=100)){var delta=new Vec3(item.getX()-user.getX(),item.getY()-user.getY()+user.getBbHeight()/2,item.getZ()-user.getZ());if(delta.lengthSqr()<1e-8)continue;var v=item.getDeltaMovement().subtract(delta.normalize().scale(.3)).add(0,.1,0);item.setDeltaMovement(Math.clamp(v.x,-.25,.25),Math.clamp(v.y,-.25,.25),Math.clamp(v.z,-.25,.25));item.syncVelocity=true;if((72000-remaining)%5==0)l.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,item.getX(),item.getY(),item.getZ(),1,.1,.1,.1,.01);}}
 }
}
