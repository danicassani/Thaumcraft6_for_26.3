package thaumcraft.warp;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
public final class WarpMonster extends Monster {
 public enum Kind{GUARDIAN,KNIGHT,CLERIC,PORTAL}
 public final Kind kind;private long nextCast;
 public WarpMonster(EntityType<? extends WarpMonster> t,Level l,Kind k){super(t,l);kind=k;xpReward=k==Kind.GUARDIAN?20:10;if(k==Kind.PORTAL){setNoGravity(true);setNoAi(true);setPersistenceRequired();}}
 public static AttributeSupplier attributes(Kind k){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,k==Kind.PORTAL?100:k==Kind.GUARDIAN?50:k==Kind.KNIGHT?30:24).add(Attributes.ATTACK_DAMAGE,k==Kind.GUARDIAN?7:k==Kind.PORTAL?0:4).add(Attributes.MOVEMENT_SPEED,k==Kind.GUARDIAN?.28:.3).add(Attributes.FOLLOW_RANGE,k==Kind.GUARDIAN?40:32).add(Attributes.ARMOR,k==Kind.GUARDIAN||k==Kind.PORTAL?4:0).add(Attributes.KNOCKBACK_RESISTANCE,k==Kind.PORTAL?1:0).build();}
 protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(2,new MeleeAttackGoal(this,1,false));goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.8));goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,8));targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));}
 public boolean considersEntityAsAlly(Entity e){return e instanceof WarpMonster||super.considersEntityAsAlly(e);}
 public boolean hurtServer(ServerLevel l,DamageSource s,float n){return super.hurtServer(l,s,kind==Kind.GUARDIAN&&(s.is(DamageTypes.MAGIC)||s.is(DamageTypes.INDIRECT_MAGIC))?n/2:n);}
 public void travel(Vec3 v){if(kind!=Kind.PORTAL)super.travel(v);}
 public void tick(){super.tick();if(!(level() instanceof ServerLevel l))return;if(thaumcraft.config.ThaumcraftConfig.wussMode.get()||l.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL){discard();return;}long now=l.getGameTime();if(kind==Kind.PORTAL&&nextCast==0){nextCast=now+100;return;}if(now<nextCast)return;nextCast=now+(kind==Kind.PORTAL?50+random.nextInt(50):40);
  if(kind==Kind.PORTAL){var p=l.getNearestPlayer(getX(),getY(),getZ(),32,true);int limit=l.getDifficulty()==net.minecraft.world.Difficulty.HARD?6:l.getDifficulty()==net.minecraft.world.Difficulty.NORMAL?4:2;if(p==null||!getSensing().hasLineOfSight(p)||l.getEntitiesOfClass(WarpMonster.class,getBoundingBox().inflate(32),e->e.kind==Kind.KNIGHT||e.kind==Kind.CLERIC).size()>=limit)return;boolean cleric=random.nextFloat()<.33F;var m=new WarpMonster((cleric?WarpCreatures.CLERIC:WarpCreatures.KNIGHT).get(),l,cleric?Kind.CLERIC:Kind.KNIGHT);m.setPos(position().add(random.nextFloat()-random.nextFloat(),.25,random.nextFloat()-random.nextFloat()));if(thaumcraft.thaumaturgy.SpellEngine.loadedLine(l,m.getBoundingBox().getMinPosition(),m.getBoundingBox().getMaxPosition())&&l.noCollision(m)&&l.addFreshEntity(m)){m.setTarget(p);hurtServer(l,damageSources().fellOutOfWorld(),5+random.nextInt(5));}}
  else if(kind!=Kind.KNIGHT){var target=getTarget();if(target==null||!target.isAlive()||distanceToSqr(target)>576||distanceToSqr(target)<9||!getSensing().hasLineOfSight(target)||!thaumcraft.thaumaturgy.SpellEngine.loadedLine(l,getEyePosition(),target.getEyePosition()))return;if(kind==Kind.CLERIC&&random.nextFloat()<.66F){var delta=target.getEyePosition().subtract(getEyePosition());for(int i=0;i<3;i++){var fireball=new net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball(l,this,delta.add(random.nextGaussian()*.15,0,random.nextGaussian()*.15));fireball.setPos(getEyePosition());l.addFreshEntity(fireball);}return;}if(kind==Kind.GUARDIAN&&random.nextFloat()<.15F){target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER,400));if(target instanceof ServerPlayer p)thaumcraft.infusion.CuriosityContent.warp(p,0,0,1+random.nextInt(3));l.sendParticles(net.minecraft.core.particles.ParticleTypes.SONIC_BOOM,getX(),getEyeY(),getZ(),1,0,0,0,0);return;}var orb=new WarpOrb(WarpCreatures.ORB.get(),l);orb.setOwner(this);orb.setPos(getEyePosition());var direction=target.getEyePosition().subtract(getEyePosition()).normalize().scale(.5);orb.setDeltaMovement(direction);l.addFreshEntity(orb);}
 }
 protected void addAdditionalSaveData(ValueOutput o){super.addAdditionalSaveData(o);o.putLong("warp_next_cast",nextCast);}
 protected void readAdditionalSaveData(ValueInput i){super.readAdditionalSaveData(i);nextCast=i.getLongOr("warp_next_cast",0);if(kind==Kind.PORTAL){setNoGravity(true);setNoAi(true);}}
}
