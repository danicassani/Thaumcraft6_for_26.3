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
 private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> PORTAL_ACTIVE=net.minecraft.network.syncher.SynchedEntityData.defineId(WarpMonster.class,net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
 public final Kind kind;private long nextCast;private int portalAge,pulse;
 protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(PORTAL_ACTIVE,false);}
 public boolean portalActive(){return entityData.get(PORTAL_ACTIVE);}
 public int portalAge(){return portalAge;}
 public int portalPulse(){return pulse;}
 public boolean fireImmune(){return kind==Kind.PORTAL||super.fireImmune();}
 public boolean isPushable(){return kind!=Kind.PORTAL&&super.isPushable();}
 public void push(Entity e){if(kind!=Kind.PORTAL)super.push(e);}
 public void playerTouch(Player p){super.playerTouch(p);if(kind==Kind.PORTAL&&level() instanceof ServerLevel l&&distanceToSqr(p)<3&&p.hurtServer(l,damageSources().indirectMagic(this,this),4))playSound(thaumcraft.rifts.RiftContent.ZAP.get(),1,1);}
 public boolean canBeAffected(net.minecraft.world.effect.MobEffectInstance effect){return kind!=Kind.PORTAL&&super.canBeAffected(effect);}
 public boolean causeFallDamage(double distance,float multiplier,DamageSource source){return kind!=Kind.PORTAL&&super.causeFallDamage(distance,multiplier,source);}
 public void die(DamageSource source){if(kind==Kind.PORTAL&&!dead&&level() instanceof ServerLevel l)l.explode(this,getX(),getY(),getZ(),1.5F,Level.ExplosionInteraction.NONE);super.die(source);}
 public void handleEntityEvent(byte event){if(kind==Kind.PORTAL&&event==16)pulse=10;else super.handleEntityEvent(event);}
 private void tickPortal(ServerLevel l){
  if(!portalActive()){if(tickCount%10==0&&l.getNearestPlayer(getX(),getY(),getZ(),32,true)!=null){entityData.set(PORTAL_ACTIVE,true);nextCast=l.getGameTime()+100;playSound(net.minecraft.sounds.SoundEvents.PORTAL_TRIGGER,.5F,1);}return;}
  long now=l.getGameTime();if(nextCast==0)nextCast=now+100;if(now<nextCast)return;nextCast=now+50+random.nextInt(50);
  var p=l.getNearestPlayer(getX(),getY(),getZ(),32,true);int limit=l.getDifficulty()==net.minecraft.world.Difficulty.HARD?6:l.getDifficulty()==net.minecraft.world.Difficulty.NORMAL?4:2;
  if(p==null||!hasLineOfSight(p)||l.getEntitiesOfClass(WarpMonster.class,getBoundingBox().inflate(32),e->e.isAlive()&&(e.kind==Kind.KNIGHT||e.kind==Kind.CLERIC)).size()>=limit)return;
  boolean cleric=random.nextFloat()<.33F;var m=new WarpMonster((cleric?WarpCreatures.CLERIC:WarpCreatures.KNIGHT).get(),l,cleric?Kind.CLERIC:Kind.KNIGHT);
  m.setPos(position().add(random.nextFloat()-random.nextFloat(),.25,random.nextFloat()-random.nextFloat()));
  if(thaumcraft.thaumaturgy.SpellEngine.loadedLine(l,m.getBoundingBox().getMinPosition(),m.getBoundingBox().getMaxPosition())&&l.noCollision(m)&&l.addFreshEntity(m)){m.setTarget(p);l.broadcastEntityEvent(this,(byte)16);l.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,m.getX(),m.getY()+1,m.getZ(),12,.3,.7,.3,.02);playSound(net.minecraft.sounds.SoundEvents.EVOKER_CAST_SPELL,.5F,1);hurtServer(l,damageSources().fellOutOfWorld(),5+random.nextInt(5));}
 }

 public WarpMonster(EntityType<? extends WarpMonster> t,Level l,Kind k){super(t,l);kind=k;xpReward=k==Kind.GUARDIAN?20:10;if(k==Kind.KNIGHT||k==Kind.CLERIC)equipCult();if(k==Kind.PORTAL){setNoGravity(true);setNoAi(true);setPersistenceRequired();}}
 private void equipCult(){String family=kind==Kind.CLERIC?"crimson_robe_":"crimson_plate_";for(var pair:java.util.Map.of("helm",EquipmentSlot.HEAD,"chest",EquipmentSlot.CHEST,"legs",EquipmentSlot.LEGS).entrySet()){setItemSlot(pair.getValue(),new net.minecraft.world.item.ItemStack(thaumcraft.content.SimpleContent.ITEMS.get(family+pair.getKey()).get()));setDropChance(pair.getValue(),.05F);}setItemSlot(EquipmentSlot.FEET,new net.minecraft.world.item.ItemStack(thaumcraft.content.SimpleContent.ITEMS.get("crimson_boots").get()));setDropChance(EquipmentSlot.FEET,.05F);if(kind==Kind.KNIGHT){setItemSlot(EquipmentSlot.MAINHAND,new net.minecraft.world.item.ItemStack(thaumcraft.content.SimpleContent.ITEMS.get("thaumium_sword").get()));setDropChance(EquipmentSlot.MAINHAND,.05F);}}
 public static AttributeSupplier attributes(Kind k){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,k==Kind.PORTAL?100:k==Kind.GUARDIAN?50:k==Kind.KNIGHT?30:24).add(Attributes.ATTACK_DAMAGE,k==Kind.GUARDIAN?7:k==Kind.PORTAL?0:4).add(Attributes.MOVEMENT_SPEED,k==Kind.GUARDIAN?.28:.3).add(Attributes.FOLLOW_RANGE,k==Kind.GUARDIAN?40:32).add(Attributes.ARMOR,k==Kind.GUARDIAN||k==Kind.PORTAL?4:0).add(Attributes.KNOCKBACK_RESISTANCE,k==Kind.PORTAL?1:0).build();}
 protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(2,new MeleeAttackGoal(this,1,false));goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.8));goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,8));targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));}
 public boolean considersEntityAsAlly(Entity e){return e instanceof WarpMonster||super.considersEntityAsAlly(e);}
 public boolean hurtServer(ServerLevel l,DamageSource s,float n){return super.hurtServer(l,s,kind==Kind.GUARDIAN&&(s.is(DamageTypes.MAGIC)||s.is(DamageTypes.INDIRECT_MAGIC))?n/2:n);}
 public void travel(Vec3 v){if(kind!=Kind.PORTAL)super.travel(v);}
 public void tick(){super.tick();if(kind==Kind.PORTAL){if(portalActive())portalAge++;if(pulse>0)pulse--;setDeltaMovement(Vec3.ZERO);}if(!(level() instanceof ServerLevel l))return;if(thaumcraft.config.ThaumcraftConfig.wussMode.get()||l.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL){discard();return;}if(kind==Kind.PORTAL){tickPortal(l);return;}long now=l.getGameTime();if(now<nextCast)return;nextCast=now+40;
  if(kind!=Kind.KNIGHT){var target=getTarget();if(target==null||!target.isAlive()||distanceToSqr(target)>576||distanceToSqr(target)<9||!getSensing().hasLineOfSight(target)||!thaumcraft.thaumaturgy.SpellEngine.loadedLine(l,getEyePosition(),target.getEyePosition()))return;if(kind==Kind.CLERIC&&random.nextFloat()<.66F){var delta=target.getEyePosition().subtract(getEyePosition());for(int i=0;i<3;i++){var fireball=new net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball(l,this,delta.add(random.nextGaussian()*.15,0,random.nextGaussian()*.15));fireball.setPos(getEyePosition());l.addFreshEntity(fireball);}return;}if(kind==Kind.GUARDIAN&&random.nextFloat()<.15F){target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER,400));if(target instanceof ServerPlayer p)thaumcraft.infusion.CuriosityContent.warp(p,0,0,1+random.nextInt(3));l.sendParticles(net.minecraft.core.particles.ParticleTypes.SONIC_BOOM,getX(),getEyeY(),getZ(),1,0,0,0,0);return;}if(kind==Kind.CLERIC){var orb=new ClericOrb(WarpCreatures.CLERIC_ORB.get(),l,this,target);var aim=target.getEyePosition().add(target.getDeltaMovement().scale(10)).subtract(getEyePosition());aim=aim.add(random.nextGaussian()*.0225,random.nextGaussian()*.0225,random.nextGaussian()*.0225).normalize();orb.setPos(getEyePosition());orb.setDeltaMovement(aim.scale(.66));l.addFreshEntity(orb);playSound(net.minecraft.sounds.SoundEvents.EVOKER_CAST_SPELL,.6F,1);return;}var orb=new WarpOrb(WarpCreatures.ORB.get(),l);orb.setOwner(this);orb.setPos(getEyePosition());var direction=target.getEyePosition().subtract(getEyePosition()).normalize().scale(.5);orb.setDeltaMovement(direction);l.addFreshEntity(orb);}
 }
 protected void addAdditionalSaveData(ValueOutput o){super.addAdditionalSaveData(o);o.putLong("warp_next_cast",nextCast);if(kind==Kind.PORTAL){o.putBoolean("portal_active",portalActive());o.putInt("portal_age",portalAge);}}
 protected void readAdditionalSaveData(ValueInput i){super.readAdditionalSaveData(i);nextCast=i.getLongOr("warp_next_cast",0);if(kind==Kind.PORTAL){entityData.set(PORTAL_ACTIVE,i.getBooleanOr("portal_active",nextCast!=0));portalAge=Math.max(0,i.getIntOr("portal_age",0));setNoGravity(true);setNoAi(true);setPersistenceRequired();}}
}
