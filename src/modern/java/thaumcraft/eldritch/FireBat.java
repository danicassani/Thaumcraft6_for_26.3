package thaumcraft.eldritch;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.level.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
/** Original hostile fire bat: native flight, wet damage and a one-in-ten contact explosion. */
public final class FireBat extends Bat {
 private int cooldown;
 public FireBat(EntityType<? extends Bat> t,Level l){super(t,l);setResting(false);}
 public static AttributeSupplier attributes(){return Bat.createAttributes().add(Attributes.MAX_HEALTH,5).add(Attributes.ATTACK_DAMAGE,1).build();}
 public boolean fireImmune(){return true;}
 public boolean hurtServer(ServerLevel l,DamageSource s,float n){return !s.is(DamageTypeTags.IS_EXPLOSION)&&super.hurtServer(l,s,n);}
 protected void customServerAiStep(ServerLevel l){
  if(thaumcraft.config.ThaumcraftConfig.wussMode.get()||l.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL){discard();return;}
  if(isInWaterOrRain()&&tickCount%20==0)hurtServer(l,damageSources().drown(),1);
  setResting(false);if(cooldown>0)cooldown--;var target=l.getNearestPlayer(getX(),getY(),getZ(),12,true);
  if(target==null){super.customServerAiStep(l);return;}
  var delta=target.getEyePosition().subtract(position());var movement=getDeltaMovement().scale(.7).add(delta.normalize().scale(.12));
  if(thaumcraft.thaumaturgy.SpellEngine.loadedLine(l,position(),position().add(movement))&&l.noCollision(this,getBoundingBox().move(movement)))setDeltaMovement(movement);else setDeltaMovement(Vec3.ZERO);
  if(cooldown==0&&distanceToSqr(target)<6.25&&hasLineOfSight(target)){cooldown=20+random.nextInt(20);target.hurtServer(l,damageSources().mobAttack(this),1);if(random.nextInt(10)==0){l.explode(this,getX(),getY(),getZ(),1.5F,Level.ExplosionInteraction.NONE);discard();}}
 }
 protected void addAdditionalSaveData(ValueOutput o){super.addAdditionalSaveData(o);o.putInt("firebat_cooldown",cooldown);}
 protected void readAdditionalSaveData(ValueInput i){super.readAdditionalSaveData(i);cooldown=Math.clamp(i.getIntOr("firebat_cooldown",0),0,40);}
}
