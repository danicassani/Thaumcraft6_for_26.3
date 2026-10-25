package thaumcraft.thaumaturgy;
import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.minecraft.sounds.*;
/** Persistent owned construct; ammunition is a real stack, including all potion components. */
public final class CrossbowTurret extends PathfinderMob {
 private static final EntityDataAccessor<Integer> FLAGS=SynchedEntityData.defineId(CrossbowTurret.class,EntityDataSerializers.INT);
 public final SimpleContainer ammunition=new SimpleContainer(1);
 private UUID owner;private long fireAt;private int swingTicks,loadTicks;
 public CrossbowTurret(EntityType<? extends CrossbowTurret> t,Level l){super(t,l);setNoAi(true);setPersistenceRequired();}
 protected void registerGoals(){}
 protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(FLAGS,2);}
 public boolean advanced(){return getType()==TurretContent.ADVANCED.get();}
 public UUID owner(){return owner;}public boolean owned(Player p){return owner!=null&&owner.equals(p.getUUID());}
 public void configure(UUID id,int flags){owner=id;targets(flags);}
 public int targets(){return entityData.get(FLAGS);}public void targets(int flags){entityData.set(FLAGS,advanced()?flags&7:2);}
 public static boolean validAmmo(ItemStack s){return s.is(Items.ARROW)||s.is(Items.TIPPED_ARROW)||s.is(Items.SPECTRAL_ARROW);}
 public long fireAt(){return fireAt;}
 public float shotProgress(float partial){return swingTicks==0?0:Math.min(1,(swingTicks-1+partial)/6F);}
 public float loadProgress(float partial){return loadTicks==0?0:Math.min(1,(loadTicks-1+partial)/10F);}
 public void handleEntityEvent(byte event){if(event==16)swingTicks=1;else if(event==17)loadTicks=1;else super.handleEntityEvent(event);}
 public net.minecraft.world.scores.PlayerTeam getTeam(){var p=owner==null?null:level().getPlayerByUUID(owner);return p==null?super.getTeam():p.getTeam();}
 public boolean eligible(ServerPlayer p,LivingEntity e){return owner!=null&&e!=this&&e.isAlive()&&!e.isSpectator()&&!SpellEngine.protectedTarget(p,e)&&!(e instanceof CrossbowTurret t&&owner.equals(t.owner))&&((e instanceof Enemy)?(targets()&2)!=0:e instanceof Player?(targets()&4)!=0:e instanceof Mob&&(targets()&1)!=0);}
 public boolean paused(){var pos=isPassenger()?getVehicle().blockPosition():blockPosition();var rail=level().getBlockState(pos);if(!rail.is(thaumcraft.content.SimpleContent.BLOCKS.get("activator_rail").get()))rail=level().getBlockState(pos.below());return level().hasNeighborSignal(blockPosition())||rail.is(thaumcraft.content.SimpleContent.BLOCKS.get("activator_rail").get())&&rail.getValue(PoweredRailBlock.POWERED);}
 protected InteractionResult mobInteract(Player p,InteractionHand hand){if(level().isClientSide())return InteractionResult.SUCCESS;if(!owned(p))return InteractionResult.FAIL;
  if(p.isShiftKeyDown()&&p.getItemInHand(hand).isEmpty()){dropAmmo((ServerLevel)level());spawnAtLocation((ServerLevel)level(),new ItemStack(thaumcraft.content.SimpleContent.ITEMS.get(advanced()?"turret_advanced":"turret_basic").get()));discard();}
  else p.openMenu(new SimpleMenuProvider((id,inv,who)->new TurretMenu(id,inv,ammunition,this),getDisplayName()));return InteractionResult.SUCCESS;
 }
 private void reload(ServerLevel l){if(!ammunition.isEmpty())return;var below=blockPosition().below();if(!l.hasChunkAt(below)||!(l.getBlockEntity(below) instanceof net.minecraft.world.level.block.entity.DispenserBlockEntity dispenser)||!l.getBlockState(below).hasProperty(DispenserBlock.FACING)||l.getBlockState(below).getValue(DispenserBlock.FACING)!=Direction.UP)return;for(int i=0;i<dispenser.getContainerSize();i++){var s=dispenser.getItem(i);if(!validAmmo(s))continue;ammunition.setItem(0,dispenser.removeItem(i,Math.min(64,s.getCount())));dispenser.setChanged();l.broadcastEntityEvent(this,(byte)17);playSound(SoundEvents.DISPENSER_DISPENSE);break;}}
 public void tick(){super.tick();if(level().isClientSide()){if(swingTicks>0&&++swingTicks>6)swingTicks=0;if(loadTicks>0&&++loadTicks>10)loadTicks=0;return;}
  var l=(ServerLevel)level();if(tickCount%80==0)heal(1);if(owner==null||paused())return;var entity=l.getPlayerByUUID(owner);if(!(entity instanceof ServerPlayer p)||!p.isAlive()||p.isSpectator())return;reload(l);if(ammunition.isEmpty()||l.getGameTime()<fireAt)return;fireAt=l.getGameTime()+5;
  Vec3 from=position().add(0,advanced()?1:.875,0);LivingEntity target=null;double best=24*24;
  for(var e:l.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(24))){double d=e.position().distanceToSqr(from);if(d>=best||!eligible(p,e)||!SpellEngine.loadedLine(l,from,e.getEyePosition()))continue;var hit=l.clip(new ClipContext(from,e.getEyePosition(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));if(hit.getType()!=HitResult.Type.MISS)continue;target=e;best=d;}
  if(target==null)return;var delta=target.getEyePosition().subtract(from);float yaw=(float)(Math.atan2(delta.z,delta.x)*180/Math.PI)-90,pitch=(float)(-Math.atan2(delta.y,Math.sqrt(delta.x*delta.x+delta.z*delta.z))*180/Math.PI);setYRot(yaw);setYHeadRot(yaw);yBodyRot=yaw;setXRot(pitch);
  var ammo=ammunition.getItem(0);AbstractArrow arrow;if(ammo.is(Items.SPECTRAL_ARROW))arrow=new TurretSpectralArrow(TurretContent.SPECTRAL.get(),l);else {var tipped=new TurretArrow(ThaumaturgyContent.TURRET_ARROW.get(),l);tipped.ammunition(ammo);arrow=tipped;}arrow.setOwner(p);arrow.pickup=AbstractArrow.Pickup.DISALLOWED;arrow.setPos(from);var d=new Vec3(target.getX(),target.getY()+target.getBbHeight()/3.,target.getZ()).subtract(from);double flat=Math.sqrt(d.x*d.x+d.z*d.z);arrow.shoot(d.x,d.y+flat*.2,d.z,1.6F,advanced()?1:3);arrow.setBaseDamage(2.25+Math.sqrt(best)/12);
  if(l.addFreshEntity(arrow)){ammo.shrink(1);ammunition.setChanged();fireAt=l.getGameTime()+(advanced()?20:40);l.broadcastEntityEvent(this,(byte)16);playSound(SoundEvents.ARROW_SHOOT,1,1/(getRandom().nextFloat()*.4F+.8F));}
 }
 public void dropAmmo(ServerLevel l){var s=ammunition.removeItemNoUpdate(0);if(!s.isEmpty())spawnAtLocation(l,s);}
 protected void dropCustomDeathLoot(ServerLevel l,net.minecraft.world.damagesource.DamageSource source,boolean hit){super.dropCustomDeathLoot(l,source,hit);dropAmmo(l);}

 public boolean canBeLeashed(){return false;}
 public void push(double x,double y,double z){super.push(x/20,Math.min(y,.1),z/20);}
 protected void addAdditionalSaveData(ValueOutput o){super.addAdditionalSaveData(o);if(owner!=null)o.putString("turret_owner",owner.toString());o.putInt("turret_targets",targets());o.putLong("turret_fire_at",fireAt);o.store("turret_ammo",ItemStack.OPTIONAL_CODEC,ammunition.getItem(0));}
 protected void readAdditionalSaveData(ValueInput i){super.readAdditionalSaveData(i);try{owner=UUID.fromString(i.getStringOr("turret_owner",""));}catch(IllegalArgumentException e){owner=null;}targets(i.getIntOr("turret_targets",2));fireAt=i.getLongOr("turret_fire_at",0);var s=i.read("turret_ammo",ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);ammunition.setItem(0,validAmmo(s)?s.copyWithCount(Math.min(64,s.getCount())):ItemStack.EMPTY);}
}
