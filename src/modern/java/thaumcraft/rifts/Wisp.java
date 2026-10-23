package thaumcraft.rifts;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import thaumcraft.crystals.*;
import thaumcraft.thaumaturgy.SpellEngine;
public final class Wisp extends Monster{
 private static final EntityDataAccessor<String> ASPECT=SynchedEntityData.defineId(Wisp.class,EntityDataSerializers.STRING);
 private static final EntityDataAccessor<Integer> ZAP=SynchedEntityData.defineId(Wisp.class,EntityDataSerializers.INT);
 private boolean aspectChosen;private int aggro,charge,zapTicks;private BlockPos destination;
 public Wisp(EntityType<? extends Wisp> t,Level l){super(t,l);setNoGravity(true);xpReward=5;}
 public static Wisp spawn(ServerLevel l,Vec3 position,boolean flux){if(!Double.isFinite(position.x)||!Double.isFinite(position.y)||!Double.isFinite(position.z))return null;var p=BlockPos.containing(position);if(thaumcraft.config.ThaumcraftConfig.wussMode.get()||!thaumcraft.config.ThaumcraftConfig.allowSpawnWisp.get()||l.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL||!l.isInWorldBounds(p)||!l.isInWorldBounds(p.above())||!l.hasChunkAt(p)||!l.getFluidState(p).isEmpty()||!l.getFluidState(p.above()).isEmpty())return null;if(!WispSpawning.light(l,p,l.getRandom())||l.getEntitiesOfClass(Wisp.class,new AABB(p).inflate(16)).size()>=8)return null;var w=new Wisp(RiftContent.WISP.get(),l);w.setPos(position);if(flux)w.aspect("vitium");else w.chooseAspect();return l.noCollision(w)&&l.addFreshEntity(w)?w:null;}
 public static AttributeSupplier attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,22).add(Attributes.ATTACK_DAMAGE,3).add(Attributes.MOVEMENT_SPEED,.15).add(Attributes.FOLLOW_RANGE,16).build();}
 protected void registerGoals(){}
 public boolean canBreatheUnderwater(){return true;}
 public boolean causeFallDamage(double distance,float multiplier,DamageSource source){return false;}
 protected void defineSynchedData(SynchedEntityData.Builder d){super.defineSynchedData(d);d.define(ASPECT,"aer");d.define(ZAP,-1);}
 public String aspect(){return entityData.get(ASPECT);}public void aspect(String a){if(!CrystalAspects.COLORS.containsKey(a))throw new IllegalArgumentException("Unknown wisp aspect");entityData.set(ASPECT,a);aspectChosen=true;}
 public int charge(){return charge;}public int zapTicks(){return zapTicks;}public Entity zapTarget(){return level().getEntity(entityData.get(ZAP));}
 public void chooseAspect(){var keys=new ArrayList<>(CrystalAspects.COLORS.keySet());int i=random.nextInt(10)!=0?random.nextInt(6):6+random.nextInt(keys.size()-6);aspect(keys.get(i));}
 public int getMaxSpawnClusterSize(){return 2;}
 public boolean checkSpawnRules(LevelAccessor level,EntitySpawnReason reason){return WispSpawning.enabled(level)&&this.level() instanceof ServerLevel l&&l.getEntitiesOfClass(Wisp.class,getBoundingBox().inflate(16),w->w.isAlive()).size()<8;}
 public SpawnGroupData finalizeSpawn(ServerLevelAccessor level,net.minecraft.world.DifficultyInstance difficulty,EntitySpawnReason reason,SpawnGroupData group){var result=super.finalizeSpawn(level,difficulty,reason,group);if(!aspectChosen)chooseAspect();return result;}
 public boolean considersEntityAsAlly(Entity e){return e instanceof Wisp||super.considersEntityAsAlly(e);}
 private boolean eligible(LivingEntity e){return e!=null&&e.isAlive()&&e.level()==level()&&!e.isSpectator()&&!isAlliedTo(e)&&!(e instanceof Player p&&p.getAbilities().invulnerable)&&distanceToSqr(e)<=1024;}
 public boolean visible(ServerLevel l,LivingEntity target){var a=getEyePosition();var b=target.getEyePosition();return SpellEngine.loadedLine(l,a,b)&&l.clip(new ClipContext(a,b,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS;}
 public boolean hurtServer(ServerLevel l,DamageSource source,float damage){boolean hit=super.hurtServer(l,source,damage);if(hit&&source.getEntity() instanceof LivingEntity e&&eligible(e)){setTarget(e);aggro=200;}return hit;}
 public boolean zap(ServerLevel l,LivingEntity e){if(l!=level()||!eligible(e)||distanceToSqr(e)>=256||!visible(l,e)||thaumcraft.config.ThaumcraftConfig.wussMode.get())return false;entityData.set(ZAP,e.getId());l.broadcastEntityEvent(this,(byte)4);playSound(RiftContent.ZAP.get(),1,1.1F);boolean moving=Math.abs(e.getDeltaMovement().x)>.1||Math.abs(e.getDeltaMovement().y)>.1||Math.abs(e.getDeltaMovement().z)>.1;return random.nextFloat()<(moving?.4F:.66F)&&e.hurtServer(l,damageSources().mobAttack(this),(float)getAttributeValue(Attributes.ATTACK_DAMAGE)+(moving?0:1));}
 protected void customServerAiStep(ServerLevel l){if(thaumcraft.config.ThaumcraftConfig.wussMode.get()){discard();return;}if(aggro>0)aggro--;var target=getTarget();if(!eligible(target)){setTarget(null);target=null;}if(random.nextInt(1000)==0&&(target==null||aggro<=0)){var player=l.getNearestPlayer(this,16);if(eligible(player)){setTarget(player);target=player;aggro=50;}}
  boolean see=target!=null&&visible(l,target);Vec3 goal=null;if(see&&distanceToSqr(target)>128)goal=target.getEyePosition();else if(!see){if(destination!=null&&(!l.hasChunkAt(destination)||!l.getBlockState(destination).isAir()||!l.isInWorldBounds(destination)))destination=null;if(destination==null||random.nextInt(30)==0||position().distanceToSqr(Vec3.atCenterOf(destination))<4){var p=blockPosition().offset(random.nextInt(7)-random.nextInt(7),random.nextInt(6)-2,random.nextInt(7)-random.nextInt(7));if(l.hasChunkAt(p)&&l.isInWorldBounds(p)&&l.getBlockState(p).isAir())destination=p;}if(destination!=null)goal=Vec3.atCenterOf(destination);}
  if(goal!=null){var d=goal.subtract(position());var desired=new Vec3(Math.signum(d.x)*.5,Math.signum(d.y)*.7,Math.signum(d.z)*.5);setDeltaMovement(getDeltaMovement().lerp(desired,.1));}
  if(see&&distanceToSqr(target)<256){lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,target.getEyePosition());if(++charge>=20){zap(l,target);charge=-20+random.nextInt(20);}}else if(charge>0)charge--;
 }
 public void travel(Vec3 input){var motion=getDeltaMovement().multiply(1,.6,1);var next=getBoundingBox().expandTowards(motion).inflate(.05);if(SpellEngine.loadedLine(level(),new Vec3(next.minX,next.minY,next.minZ),new Vec3(next.maxX,next.maxY,next.maxZ))&&level().noCollision(this,getBoundingBox().move(motion)))move(MoverType.SELF,motion);else motion=Vec3.ZERO;setDeltaMovement(motion.scale(.91));}
 public void tick(){super.tick();if(zapTicks>0)zapTicks--;}
 public void handleEntityEvent(byte event){if(event==4)zapTicks=6;else super.handleEntityEvent(event);}
 protected SoundEvent getAmbientSound(){return RiftContent.LIVE.get();}protected SoundEvent getHurtSound(DamageSource d){return SoundEvents.LAVA_EXTINGUISH;}protected SoundEvent getDeathSound(){return RiftContent.DEAD.get();}protected float getSoundVolume(){return .25F;}
 protected void dropCustomDeathLoot(ServerLevel l,DamageSource d,boolean player){super.dropCustomDeathLoot(l,d,player);spawnAtLocation(l,CrystalContent.essence(aspect(),1));}
 protected void addAdditionalSaveData(ValueOutput o){super.addAdditionalSaveData(o);o.putString("wisp_aspect",aspect());o.putInt("wisp_charge",charge);o.putInt("wisp_aggro",aggro);}
 protected void readAdditionalSaveData(ValueInput i){super.readAdditionalSaveData(i);String a=i.getStringOr("wisp_aspect","aer");aspect(CrystalAspects.COLORS.containsKey(a)?a:"aer");charge=Math.clamp(i.getIntOr("wisp_charge",0),-20,19);aggro=Math.clamp(i.getIntOr("wisp_aggro",0),0,200);setNoGravity(true);}
}
