package thaumcraft.rifts;
import net.minecraft.core.*;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import thaumcraft.thaumaturgy.*;
/** Environmental flux cloud: no fake caster or disconnected-player dependency. */
public final class RiftCloud extends Entity{
 private static final EntityDataAccessor<Integer> RADIUS=SynchedEntityData.defineId(RiftCloud.class,EntityDataSerializers.INT);
 private long expires,nextPulse;private boolean valid;
 public RiftCloud(EntityType<? extends RiftCloud> t,Level l){super(t,l);setNoGravity(true);}
 protected void defineSynchedData(SynchedEntityData.Builder d){d.define(RADIUS,1);}
 public int radius(){return entityData.get(RADIUS);}public long expires(){return expires;}
 public void configure(ServerLevel l,Vec3 p,int radius,int seconds){if(l!=level()||radius<1||radius>3||seconds<5||seconds>30||!Double.isFinite(p.x)||!Double.isFinite(p.y)||!Double.isFinite(p.z))throw new IllegalArgumentException("Invalid rift cloud");setPos(p);entityData.set(RADIUS,radius);expires=l.getGameTime()+seconds*20L;nextPulse=l.getGameTime()+5;valid=true;}
 public void move(MoverType t,Vec3 v){}
 public boolean hurtServer(ServerLevel l,DamageSource d,float n){return false;}
 public void tick(){super.tick();if(!(level() instanceof ServerLevel l))return;long now=l.getGameTime();if(!valid||expires<=now||expires>now+600||thaumcraft.config.ThaumcraftConfig.wussMode.get()){discard();return;}if(now<nextPulse)return;nextPulse=now+5;var c=position();var targets=l.getEntitiesOfClass(LivingEntity.class,new AABB(c,c).inflate(radius()),e->e.isAlive()&&!e.isSpectator()&&!(e instanceof Player p&&p.getAbilities().invulnerable)&&c.distanceToSqr(e.getX(),e.getY()+e.getBbHeight()*.5,e.getZ())<=radius()*radius());var cooldowns=FocusCooldowns.get(l);int seen=0;for(var e:targets){if(++seen>64)break;var end=e.position().add(0,e.getBbHeight()*.5,0);if(SpellEngine.loadedLine(l,c,end)&&l.clip(new ClipContext(c,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS&&cooldowns.acquire("entity:"+e.getUUID(),now))e.hurtServer(l,thaumcraft.taint.TaintContent.damage(l,"rift_flux"),2);}}
 protected void addAdditionalSaveData(ValueOutput o){o.putInt("cloud_version",1);o.putInt("radius",radius());o.putLong("expires",expires);o.putLong("next_pulse",nextPulse);}
 protected void readAdditionalSaveData(ValueInput i){int radius=i.getIntOr("radius",0);expires=i.getLongOr("expires",0);nextPulse=i.getLongOr("next_pulse",0);valid=i.getIntOr("cloud_version",0)==1&&radius>=1&&radius<=3;if(valid)entityData.set(RADIUS,radius);}
}
