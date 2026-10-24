package thaumcraft.eldritch;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
/** Keeps native zombie movement, sunlight, equipment and targeting instead of emulating a zombie as a generic monster. */
public final class BrainyZombie extends Zombie {
 private static final EntityDataAccessor<Float> ANGER=SynchedEntityData.defineId(BrainyZombie.class,EntityDataSerializers.FLOAT);
 public final EldritchMob.Kind kind;private boolean released;
 public BrainyZombie(EntityType<? extends Zombie> t,Level l,EldritchMob.Kind k){super(t,l);kind=k;if(k==EldritchMob.Kind.GIANT)goalSelector.addGoal(2,new net.minecraft.world.entity.ai.goal.LeapAtTargetGoal(this,.4F));xpReward=k==EldritchMob.Kind.GIANT?15:5;if(k==EldritchMob.Kind.INHABITED){setItemSlot(EquipmentSlot.HEAD,new ItemStack(thaumcraft.content.SimpleContent.ITEMS.get("crimson_plate_helm").get()));for(var slot:java.util.List.of(EquipmentSlot.CHEST,EquipmentSlot.LEGS))if(random.nextFloat()<(l.getDifficulty()==net.minecraft.world.Difficulty.HARD?.9:.6))setItemSlot(slot,new ItemStack(thaumcraft.content.SimpleContent.ITEMS.get(slot==EquipmentSlot.CHEST?"crimson_plate_chest":"crimson_plate_legs").get()));}}
 public static AttributeSupplier attributes(EldritchMob.Kind k){return Zombie.createAttributes().add(Attributes.MAX_HEALTH,k.health).add(Attributes.ATTACK_DAMAGE,k.damage).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE,0).add(Attributes.ARMOR,k==EldritchMob.Kind.INHABITED?2:3).build();}
 protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ANGER,0F);}
 public float anger(){return entityData.get(ANGER);}
 public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor l,net.minecraft.world.DifficultyInstance difficulty,EntitySpawnReason reason,SpawnGroupData group){var result=super.finalizeSpawn(l,difficulty,reason,group);getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0);return result;}
 protected boolean convertsInWater(){return false;}
 public boolean hurtServer(ServerLevel l,DamageSource s,float n){boolean hit=super.hurtServer(l,s,n);if(hit&&kind==EldritchMob.Kind.GIANT)entityData.set(ANGER,Math.min(2,anger()+.1F));return hit;}
 public boolean doHurtTarget(ServerLevel l,Entity e){return !thaumcraft.config.ThaumcraftConfig.wussMode.get()&&super.doHurtTarget(l,e);}
 public void tick(){super.tick();if(level() instanceof ServerLevel){if(thaumcraft.config.ThaumcraftConfig.wussMode.get()){discard();return;}if(kind==EldritchMob.Kind.GIANT){float a=anger();if(a>1){a-=.002F;entityData.set(ANGER,a);}getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7+(a-1)*5);getAttribute(Attributes.SCALE).setBaseValue(1+a);}}}
 public void die(DamageSource source){if(!dead&&!released&&kind==EldritchMob.Kind.INHABITED&&level() instanceof ServerLevel l){released=true;var crab=new EldritchMob(EldritchContent.TYPES.get(EldritchMob.Kind.CRAB).get(),l,EldritchMob.Kind.CRAB);crab.setPos(position());l.addFreshEntity(crab);}super.die(source);}
 protected void addAdditionalSaveData(ValueOutput o){super.addAdditionalSaveData(o);o.putFloat("brainy_anger",anger());o.putBoolean("brainy_released",released);}
 protected void readAdditionalSaveData(ValueInput i){super.readAdditionalSaveData(i);entityData.set(ANGER,Math.clamp(i.getFloatOr("brainy_anger",0),0,2));released=i.getBooleanOr("brainy_released",false);}
}
