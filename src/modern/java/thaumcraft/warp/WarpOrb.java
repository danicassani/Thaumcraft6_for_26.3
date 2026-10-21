package thaumcraft.warp;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
public final class WarpOrb extends ThrowableItemProjectile {
 private long expires;
 public WarpOrb(EntityType<? extends WarpOrb> t,Level l){super(t,l);}
 protected Item getDefaultItem(){return Items.ENDER_PEARL;}protected double getDefaultGravity(){return 0;}
 protected boolean canHitEntity(Entity e){return !(e instanceof WarpMonster)&&super.canHitEntity(e);}
 public void tick(){if(!level().isClientSide()){long now=level().getGameTime();if(expires==0)expires=now+100;if(thaumcraft.config.ThaumcraftConfig.wussMode.get()||now>=expires||expires>now+100||!thaumcraft.thaumaturgy.SpellEngine.loadedLine(level(),position().add(-.3,0,-.3),position().add(getDeltaMovement()).add(.3,.6,.3))){discard();return;}}super.tick();}
 protected void onHit(HitResult hit){super.onHit(hit);if(level() instanceof ServerLevel l){if(getOwner() instanceof LivingEntity owner){for(var e:l.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(2),e->e!=owner&&!(e instanceof WarpMonster)&&!e.isInvertedHealAndHarm()&&!(e instanceof net.minecraft.world.entity.player.Player p&&(p.isCreative()||p.isSpectator())))){e.hurtServer(l,damageSources().indirectMagic(this,owner),(float)owner.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)*.666F);e.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,160));}}l.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,getX(),getY(),getZ(),12,.2,.2,.2,.05);discard();}}
 protected void addAdditionalSaveData(ValueOutput o){super.addAdditionalSaveData(o);o.putLong("eldritch_expires",expires);}protected void readAdditionalSaveData(ValueInput i){super.readAdditionalSaveData(i);expires=i.getLongOr("eldritch_expires",0);}
}
