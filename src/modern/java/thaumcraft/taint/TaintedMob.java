package thaumcraft.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import thaumcraft.aura.AuraManager;

/** Seed, crawler and tentacle lifecycles share the native mob save and combat pipeline. */
public final class TaintedMob extends Monster {
 public enum Kind {
  SEED("taint_seed",1.5F,1.25F,75,4,0,true),PRIME("taint_seed_prime",2,2,150,7,0,true),
  CRAWLER("taint_crawler",.5F,.4F,8,2,.275,false),SWARM("taint_swarm",2,2,30,2,.25,false),
  TENTACLE("taintacle",.8F,3,50,7,0,false),SMALL("taintacle_small",.22F,1,5,2,0,false),GIANT("taintacle_giant",1.1F,6,175,9,0,false);
  public final String id;public final float width,height;public final double health,damage,speed;public final boolean seed;
  Kind(String i,float w,float h,double hp,double d,double s,boolean seed){id=i;width=w;height=h;health=hp;damage=d;speed=s;this.seed=seed;}
 }
 public final Kind kind;public int boost;private int remaining=200,attackCooldown;
 public TaintedMob(EntityType<? extends TaintedMob> type,Level l,Kind k){super(type,l);kind=k;xpReward=k==Kind.PRIME?12:k.seed||k==Kind.TENTACLE?8:k==Kind.GIANT?20:k==Kind.SMALL?0:3;setPersistenceRequired();if(k==Kind.SWARM)setNoGravity(true);}
 public static AttributeSupplier attributes(Kind k){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,k.health).add(Attributes.ATTACK_DAMAGE,k.damage).add(Attributes.MOVEMENT_SPEED,k.speed).add(Attributes.FOLLOW_RANGE,32).add(Attributes.KNOCKBACK_RESISTANCE,k.speed==0?1:0).build();}
 protected void registerGoals(){goalSelector.addGoal(1,new FloatGoal(this));goalSelector.addGoal(2,new MeleeAttackGoal(this,1,true));goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.8));goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,16));targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));}
 public boolean considersEntityAsAlly(Entity other){return other instanceof TaintedMob||other instanceof ThaumicSlime||super.considersEntityAsAlly(other);}
 public void move(MoverType type,Vec3 motion){super.move(type,kind!=null&&kind.speed==0?new Vec3(0,Math.min(0,motion.y),0):motion);}
 public boolean supported(){var p=blockPosition().below();return level().hasChunkAt(p)&&level().getBlockState(p).isFaceSturdy(level(),p,net.minecraft.core.Direction.UP);}
 public boolean removeWhenFarAway(double d){return !kind.seed&&kind!=Kind.GIANT;}
 protected void customServerAiStep(ServerLevel l){
  if(thaumcraft.config.ThaumcraftConfig.wussMode.get()){discard();return;}
  if(kind==Kind.SMALL&&--remaining<=0){discard();return;}
  if(kind.seed){getNavigation().stop();var motion=getDeltaMovement();setDeltaMovement(0,motion.y,0);if(!supported())return;
   if(tickCount%20==0){for(var e:l.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(kind==Kind.PRIME?8:4)))if(e!=this&&!e.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD))e.addEffect(new MobEffectInstance(TaintContent.DISEASE.getHolder().orElseThrow(),100,kind==Kind.PRIME?1:0));}
   if(tickCount%20==0||boost>0){var a=AuraManager.get(l,blockPosition());if(boost>0||a!=null&&a.flux()>0){int area=kind==Kind.PRIME?2:1;var p=blockPosition().offset(random.nextInt(6*area+1)-3*area,random.nextInt(2*area+1)-area,random.nextInt(6*area+1)-3*area);if(l.hasChunkAt(p))TaintWorld.spread(l,p,true);}else if(a!=null){hurtServer(l,TaintContent.damage(l,"taint"),.5F);AuraManager.addFlux(l,blockPosition(),.1F);}if(boost>0)boost--;}
   if(tickCount%40==0&&getTarget()!=null&&distanceToSqr(getTarget())<256&&getSensing().hasLineOfSight(getTarget())){var p=getTarget().blockPosition();if(l.hasChunkAt(p.below())&&TaintWorld.taint(l.getBlockState(p.below()))&&l.getEntitiesOfClass(TaintedMob.class,getTarget().getBoundingBox().inflate(2),e->e.kind==Kind.SMALL).isEmpty())spawn(l,Kind.SMALL,p);}
  } else if(kind==Kind.TENTACLE||kind==Kind.SMALL||kind==Kind.GIANT){if(tickCount%20==0&&l.hasChunkAt(blockPosition().below())&&!TaintWorld.taint(l.getBlockState(blockPosition()))&&!TaintWorld.taint(l.getBlockState(blockPosition().below())))hurtServer(l,damageSources().starve(),1);if(kind!=Kind.SMALL&&tickCount%40==0&&getTarget()!=null&&distanceToSqr(getTarget())>16&&distanceToSqr(getTarget())<256){var p=getTarget().blockPosition();if(l.hasChunkAt(p.below())&&TaintWorld.taint(l.getBlockState(p.below()))&&l.getEntitiesOfClass(TaintedMob.class,getTarget().getBoundingBox().inflate(2),e->e.kind==Kind.SMALL).isEmpty())spawn(l,Kind.SMALL,p);}
  } else if(kind==Kind.SWARM){var t=getTarget();if(t!=null){var v=t.getEyePosition().subtract(position()).normalize().scale(.15);if(l.hasChunkAt(BlockPos.containing(position().add(v)))&&l.noCollision(this,getBoundingBox().move(v)))setDeltaMovement(getDeltaMovement().scale(.8).add(v.scale(.2)));}}
  if(kind!=Kind.CRAWLER&&getTarget()!=null&&--attackCooldown<=0){var t=getTarget();double reach=kind==Kind.GIANT?4:kind==Kind.TENTACLE?3:kind.seed?2:1.5;if(distanceToSqr(t)<reach*reach&&getSensing().hasLineOfSight(t)){attackCooldown=30;if(t.hurtServer(l,damageSources().mobAttack(this),(float)kind.damage))t.addEffect(new MobEffectInstance(TaintContent.DISEASE.getHolder().orElseThrow(),100));}}
  super.customServerAiStep(l);
 }
 public static TaintedMob spawn(ServerLevel l,Kind k,BlockPos p){if(thaumcraft.config.ThaumcraftConfig.wussMode.get()||l.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL||!l.hasChunkAt(p))return null;var m=new TaintedMob(TaintContent.MOBS.get(k).get(),l,k);m.setPos(Vec3.atBottomCenterOf(p));return l.noCollision(m)&&l.addFreshEntity(m)?m:null;}
 protected void addAdditionalSaveData(ValueOutput out){super.addAdditionalSaveData(out);out.putInt("taint_boost",boost);out.putInt("taint_remaining",remaining);}
 protected void readAdditionalSaveData(ValueInput in){super.readAdditionalSaveData(in);boost=Math.clamp(in.getIntOr("taint_boost",0),0,1200);remaining=Math.clamp(in.getIntOr("taint_remaining",200),0,200);if(kind==Kind.SWARM)setNoGravity(true);}
}
