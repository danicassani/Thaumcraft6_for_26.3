package thaumcraft.eldritch;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.content.SimpleContent;
import thaumcraft.warp.*;

public final class EldritchMob extends Monster {
 public enum Kind {
  BRAINY("brainy_zombie",25,5,.23F,.6F,1.95F),GIANT("giant_brainy_zombie",60,7,.23F,.6F,1.95F),INHABITED("inhabited_zombie",30,5,.23F,.6F,1.95F),CRAB("eldritch_crab",20,4,.3F,.8F,.6F),
  LEADER("cultist_leader",150,5,.32F,.75F,2.25F),GOLEM("eldritch_golem",400,10,.3F,1.75F,3.5F),WARDEN("eldritch_warden",400,10,.33F,1.5F,3.5F),PORTAL("cultist_portal_greater",500,0,0,1.5F,3);
  public final String id;public final float health,damage,speed,width,height;
  Kind(String id,float health,float damage,float speed,float width,float height){this.id=id;this.health=health;this.damage=damage;this.speed=speed;this.width=width;this.height=height;}
  public boolean boss(){return ordinal()>=LEADER.ordinal();}
 }
 private static final EntityDataAccessor<Integer> PHASE=SynchedEntityData.defineId(EldritchMob.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Float> ANGER=SynchedEntityData.defineId(EldritchMob.class,EntityDataSerializers.FLOAT);
 public final Kind kind;
 private final ServerBossEvent boss=new ServerBossEvent(UUID.randomUUID(),Component.empty(),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.PROGRESS);
 private final ServerBossEvent shield=new ServerBossEvent(UUID.randomUUID(),Component.empty(),BossEvent.BossBarColor.BLUE,BossEvent.BossBarOverlay.PROGRESS);
 private boolean charging;
 private int arrival,attack,stage,stageTicks=200,frenzy,beam,enrage;private boolean released,wardBroken;private BlockPos home;
 private final Map<UUID,Float> threat=new HashMap<>();
 public EldritchMob(EntityType<? extends EldritchMob> t,Level l,Kind k){super(t,l);kind=k;if(k==Kind.CRAB)goalSelector.addGoal(1,new LeapAtTargetGoal(this,.5F));xpReward=k.boss()?50:k==Kind.GIANT?15:5;
  if(k.boss()){setPersistenceRequired();arrival=k==Kind.WARDEN?150:k==Kind.GOLEM?100:0;}
  if(k==Kind.PORTAL){setNoGravity(true);setNoAi(true);}if(k==Kind.WARDEN)setAbsorptionAmount(264);
  if(k==Kind.CRAB&&random.nextBoolean())entityData.set(PHASE,1);
  if(k==Kind.LEADER){for(var slot:List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS)){String n=slot==EquipmentSlot.HEAD?"helm":slot==EquipmentSlot.CHEST?"chest":"legs";setItemSlot(slot,new ItemStack(SimpleContent.ITEMS.get("crimson_praetor_"+n).get()));setDropChance(slot,.05F);}setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(SimpleContent.ITEMS.get("crimson_blade").get()));}
 }
 public static AttributeSupplier attributes(Kind k){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,k.health).add(Attributes.ATTACK_DAMAGE,k.damage).add(Attributes.MOVEMENT_SPEED,k.speed).add(Attributes.FOLLOW_RANGE,k.boss()?40:24).add(Attributes.ARMOR,k==Kind.GOLEM?6:k==Kind.WARDEN?4:k==Kind.BRAINY||k==Kind.GIANT?3:0).add(Attributes.KNOCKBACK_RESISTANCE,k.boss()?.95:0).add(Attributes.MAX_ABSORPTION,k==Kind.WARDEN?264:0).build();}
 protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(PHASE,0);b.define(ANGER,0F);}
 public int phase(){return entityData.get(PHASE);}public float anger(){return entityData.get(ANGER);}public int stage(){return stage;}public int arrival(){return arrival;}public int frenzy(){return frenzy;}
 protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(2,new MeleeAttackGoal(this,1,false));goalSelector.addGoal(6,new WaterAvoidingRandomStrollGoal(this,.8));goalSelector.addGoal(7,new LookAtPlayerGoal(this,Player.class,12));targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));}
 public boolean removeWhenFarAway(double distance){return !kind.boss()&&super.removeWhenFarAway(distance);}
 public boolean considersEntityAsAlly(Entity e){return e instanceof EldritchMob&&kind!=Kind.BRAINY&&kind!=Kind.GIANT||e instanceof WarpMonster||super.considersEntityAsAlly(e);}
 public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);if(kind.boss()){boss.addPlayer(p);if(kind==Kind.WARDEN)shield.addPlayer(p);}}
 public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);boss.removePlayer(p);shield.removePlayer(p);}
 protected net.minecraft.sounds.SoundEvent getAmbientSound(){return kind==Kind.CRAB?EldritchSounds.CRAB.get():kind==Kind.PORTAL?EldritchSounds.MONOLITH.get():kind==Kind.WARDEN?EldritchSounds.IDLE.get():super.getAmbientSound();}
 protected net.minecraft.sounds.SoundEvent getDeathSound(){return kind==Kind.CRAB?EldritchSounds.CRAB_DEATH.get():kind==Kind.WARDEN?EldritchSounds.DEATH.get():super.getDeathSound();}
 public boolean fireImmune(){return kind==Kind.PORTAL||super.fireImmune();}
 public boolean canBeAffected(MobEffectInstance e){return kind!=Kind.PORTAL&&!(kind==Kind.CRAB&&e.is(MobEffects.POISON))&&super.canBeAffected(e);}
 public boolean doHurtTarget(ServerLevel l,Entity e){return arrival==0&&frenzy==0&&!ThaumcraftConfig.wussMode.get()&&super.doHurtTarget(l,e);}
 public void travel(Vec3 v){if(kind!=Kind.PORTAL&&arrival==0&&frenzy==0)super.travel(v);}
 public boolean hurtServer(ServerLevel l,DamageSource s,float damage){
  if(arrival>0||frenzy>0||kind==Kind.WARDEN&&s.is(DamageTypes.WITHER))return false;
  if(kind==Kind.GOLEM&&phase()==0&&damage>getHealth()){entityData.set(PHASE,1);arrival=100;l.explode(this,getX(),getEyeY(),getZ(),2,Level.ExplosionInteraction.NONE);return false;}
  if(kind.boss()&&damage>35){damage=35;if(enrage==0){enrage=200;addEffect(new MobEffectInstance(MobEffects.REGENERATION,200,2));addEffect(new MobEffectInstance(MobEffects.STRENGTH,200,3));}}

  if(kind==Kind.GIANT){entityData.set(ANGER,Math.min(2,anger()+.1F));}
  boolean hit=super.hurtServer(l,s,damage);if(hit&&kind==Kind.CRAB&&phase()==1&&getHealth()<=getMaxHealth()/2){entityData.set(PHASE,0);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.3);getAttribute(Attributes.ARMOR).setBaseValue(0);}if(hit&&s.getEntity() instanceof LivingEntity who){threat.merge(who.getUUID(),damage,Float::sum);if(threat.size()>64)threat.clear();}
  if(hit&&kind==Kind.WARDEN&&!wardBroken&&getAbsorptionAmount()<=0){wardBroken=true;frenzy=150;teleportHome(l);}return hit;
 }
 private void teleportHome(ServerLevel l){if(home==null||!l.hasChunkAt(home))return;var old=position();setPos(Vec3.atBottomCenterOf(home));if(!l.noCollision(this))setPos(old);}
 public void tick(){super.tick();if(!(level() instanceof ServerLevel l))return;
  if(ThaumcraftConfig.wussMode.get()||l.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL){discard();return;}if(home==null)home=blockPosition();
  if(arrival>0)arrival--;if(enrage>0)enrage--;if(attack>0)attack--;if(kind==Kind.GOLEM&&phase()==1&&beam<=0)charging=true;if(charging&&++beam>=150)charging=false;
  if(kind.boss()){boss.setName(getDisplayName());boss.setProgress(Math.clamp(getHealth()/getMaxHealth(),0,1));shield.setName(Component.translatable("entity.thaumcraft.eldritch_warden.shield"));shield.setProgress(Math.clamp(getAbsorptionAmount()/264,0,1));if(tickCount%30==0&&kind!=Kind.PORTAL)heal(1);if(tickCount%20==0)retarget(l);}
  if(kind==Kind.GIANT){float a=Math.max(0,anger()-.002F);entityData.set(ANGER,a);getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(a>1?7+(a-1)*5:7);getAttribute(Attributes.SCALE).setBaseValue(a>1?(1+a)/2:1);}
  if(kind==Kind.WARDEN&&tickCount%10==0)SapField.place(l,blockPosition());
  if(kind==Kind.WARDEN&&hurtTime==0&&tickCount%25==0)setAbsorptionAmount(Math.min(264,getAbsorptionAmount()+1));
  if(kind==Kind.PORTAL){setDeltaMovement(Vec3.ZERO);tickPortal(l);return;}
  if(frenzy>0){if(frenzy%10==0){double radius=(150-frenzy)/8D;for(int n=0;n<Math.max(8,(int)(radius*6));n++){double theta=n*Math.PI*2/Math.max(8,(int)(radius*6));SapField.place(l,blockPosition().offset((int)(Math.cos(theta)*radius),0,(int)(Math.sin(theta)*radius)));}l.sendParticles(ParticleTypes.WITCH,getX(),getY()+.2,getZ(),40,radius,.1,radius,0);}frenzy--;return;}
  var target=getTarget();if(kind==Kind.CRAB){if(getVehicle() instanceof LivingEntity victim){if(attack==0){attack=10+random.nextInt(10);victim.hurtServer(l,damageSources().mobAttack(this),4);if(random.nextFloat()<.2F)stopRiding();}}else if(target!=null&&phase()==0&&distanceToSqr(target)<4&&!onGround()&&getY()-target.getY()>=target.getBbHeight()/2&&!target.isVehicle())startRiding(target);}
  if(arrival>0||attack>0||target==null||!target.isAlive()||distanceToSqr(target)>576||!hasLineOfSight(target))return;
  if(kind==Kind.LEADER||kind==Kind.GOLEM||kind==Kind.WARDEN){attack=kind==Kind.GOLEM&&phase()==1?5:40;if(kind==Kind.WARDEN&&random.nextFloat()<.2F){playSound(EldritchSounds.SCREECH.get(),2,1);target.addEffect(new MobEffectInstance(MobEffects.WITHER,400));target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,400));target.push(target.position().subtract(position()).normalize().scale(1.5).add(0,.1,0));if(target instanceof ServerPlayer p)thaumcraft.infusion.CuriosityContent.warp(p,0,0,3+random.nextInt(3));return;}
   if(kind==Kind.GOLEM){if(phase()==0||charging)return;beam-=15+random.nextInt(5);}if(kind==Kind.WARDEN){var blast=new WarpOrb(WarpCreatures.ORB.get(),l);blast.setOwner(this);blast.setPos(getEyePosition());blast.setDeltaMovement(target.getEyePosition().subtract(getEyePosition()).normalize());if(thaumcraft.thaumaturgy.SpellEngine.loadedLine(l,getEyePosition(),target.getEyePosition()))l.addFreshEntity(blast);playSound(EldritchSounds.ATTACK.get(),1,1);return;}var orb=new ClericOrb(WarpCreatures.CLERIC_ORB.get(),l,this,target);orb.setPos(getEyePosition());orb.setDeltaMovement(target.getEyePosition().subtract(getEyePosition()).normalize().scale(.66));if(thaumcraft.thaumaturgy.SpellEngine.loadedLine(l,getEyePosition(),target.getEyePosition()))l.addFreshEntity(orb);playSound(EldritchSounds.ATTACK.get(),1,1);
  }
 }
 private void retarget(ServerLevel l){int players=0;for(var id:threat.keySet())if(l.getEntity(id) instanceof ServerPlayer p&&p.isAlive()&&distanceToSqr(p)<16384)players++;if(kind!=Kind.PORTAL){int count=Math.clamp(players-1,0,5);double before=getMaxHealth();modifier(Attributes.MAX_HEALTH,"eldritch_party_health",count*50);modifier(Attributes.ATTACK_DAMAGE,"eldritch_party_damage",count*.5);if(before!=getMaxHealth())setHealth((float)(getHealth()*getMaxHealth()/before));}float max=0;LivingEntity best=getTarget();for(var entry:threat.entrySet()){var e=l.getEntity(entry.getKey());if(e instanceof LivingEntity living&&e.isAlive()&&distanceToSqr(e)<16384&&entry.getValue()>max){max=entry.getValue();best=living;}}if(best!=null)setTarget(best);}
 private void modifier(net.minecraft.core.Holder<Attribute> attribute,String name,double amount){var a=getAttribute(attribute);var id=SimpleContent.id(name);var old=a.getModifier(id);if(old!=null&&old.amount()==amount)return;a.removeModifier(id);if(amount!=0)a.addPermanentModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_VALUE));}
 private void tickPortal(ServerLevel l){if(stage<12)heal(1);if(--stageTicks>0)return;var p=l.getNearestPlayer(getX(),getY(),getZ(),48,true);if(p==null){stageTicks=30+random.nextInt(30);return;}
  int count=l.getEntitiesOfClass(WarpMonster.class,getBoundingBox().inflate(32),m->m.kind==WarpMonster.Kind.KNIGHT||m.kind==WarpMonster.Kind.CLERIC).size();if(count>=24){stageTicks=40;return;}
  if(stage==12){var leader=new EldritchMob(EldritchContent.TYPES.get(Kind.LEADER).get(),l,Kind.LEADER);if(!spawn(l,leader,p)){stageTicks=20;return;}stageTicks=50+count*40+random.nextInt(50);}else{boolean cleric=random.nextFloat()<.33F;var mob=new WarpMonster((cleric?WarpCreatures.CLERIC:WarpCreatures.KNIGHT).get(),l,cleric?WarpMonster.Kind.CLERIC:WarpMonster.Kind.KNIGHT);if(!spawn(l,mob,p)){stageTicks=20;return;}stageTicks=stage<5?Math.max(1,15+random.nextInt(10-stage)-stage):Math.max(5,count*20+random.nextInt(5+count*20/3));if(stage>12)hurtServer(l,damageSources().fellOutOfWorld(),5+random.nextInt(5));}stage++;
 }
 private boolean spawn(ServerLevel l,Monster mob,Player p){mob.setPos(position().add(random.nextFloat()-random.nextFloat(),.25,random.nextFloat()-random.nextFloat()));if(!thaumcraft.thaumaturgy.SpellEngine.loadedLine(l,mob.getBoundingBox().getMinPosition(),mob.getBoundingBox().getMaxPosition())||!l.noCollision(mob)||!l.addFreshEntity(mob))return false;mob.setTarget(p);l.sendParticles(ParticleTypes.PORTAL,mob.getX(),mob.getY()+1,mob.getZ(),20,.4,.7,.4,.02);return true;}
 public void playerTouch(Player p){super.playerTouch(p);if(kind==Kind.PORTAL&&level() instanceof ServerLevel l&&distanceToSqr(p)<3)p.hurtServer(l,damageSources().indirectMagic(this,this),8);}
 public void die(DamageSource s){if(!dead&&level() instanceof ServerLevel l){if(kind==Kind.PORTAL)l.explode(this,getX(),getY(),getZ(),2,Level.ExplosionInteraction.NONE);if(kind==Kind.INHABITED&&!released){released=true;var crab=new EldritchMob(EldritchContent.TYPES.get(Kind.CRAB).get(),l,Kind.CRAB);crab.setPos(position());l.addFreshEntity(crab);}}super.die(s);}
 protected void addAdditionalSaveData(ValueOutput o){super.addAdditionalSaveData(o);o.putInt("eldritch_phase",phase());o.putFloat("eldritch_anger",anger());o.putInt("eldritch_arrival",arrival);o.putInt("eldritch_attack",attack);o.putInt("eldritch_stage",stage);o.putInt("eldritch_stage_ticks",stageTicks);o.putInt("eldritch_frenzy",frenzy);o.putInt("eldritch_beam",beam);o.putBoolean("eldritch_charging",charging);o.putBoolean("eldritch_released",released);o.putBoolean("eldritch_ward_broken",wardBroken);if(home!=null)o.store("eldritch_home",BlockPos.CODEC,home);}
 protected void readAdditionalSaveData(ValueInput i){super.readAdditionalSaveData(i);entityData.set(PHASE,Math.clamp(i.getIntOr("eldritch_phase",0),0,1));entityData.set(ANGER,Math.clamp(i.getFloatOr("eldritch_anger",0),0,2));arrival=Math.clamp(i.getIntOr("eldritch_arrival",0),0,150);attack=Math.clamp(i.getIntOr("eldritch_attack",0),0,40);stage=Math.max(0,i.getIntOr("eldritch_stage",0));stageTicks=Math.clamp(i.getIntOr("eldritch_stage_ticks",200),0,10000);frenzy=Math.clamp(i.getIntOr("eldritch_frenzy",0),0,150);beam=Math.clamp(i.getIntOr("eldritch_beam",0),0,150);charging=i.getBooleanOr("eldritch_charging",false);released=i.getBooleanOr("eldritch_released",false);wardBroken=i.getBooleanOr("eldritch_ward_broken",false);home=i.read("eldritch_home",BlockPos.CODEC).orElse(null);}
}
