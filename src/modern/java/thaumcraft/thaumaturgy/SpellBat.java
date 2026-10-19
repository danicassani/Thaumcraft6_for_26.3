package thaumcraft.thaumaturgy;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

/** Native animated bat, bounded homing and five spell strikes. No natural spawning or loot. */
public final class SpellBat extends Bat {
    private static final EntityDataAccessor<Boolean> FRIENDLY=SynchedEntityData.defineId(SpellBat.class,EntityDataSerializers.BOOLEAN);
    private FocusProgram program;private UUID owner;private long expires,attackAt;private float strength=1;private int strikes=5;private LivingEntity victim;
    public SpellBat(EntityType<? extends Bat> type,Level level){super(type,level);setResting(false);setNoGravity(true);}
    protected void defineSynchedData(SynchedEntityData.Builder data){super.defineSynchedData(data);data.define(FRIENDLY,false);}
    public UUID ownerId(){return owner;}public long expires(){return expires;}public long attackAt(){return attackAt;}public int strikes(){return strikes;}public FocusProgram program(){return program;}public boolean friendly(){return entityData.get(FRIENDLY);}
    public void configure(ServerPlayer p,FocusProgram spell,Vec3 at,float multiplier){if(!spell.valid()||!spell.medium().equals("spellbat")||!Float.isFinite(multiplier)||multiplier<=0||multiplier>1)throw new IllegalArgumentException("Invalid spellbat");program=spell;owner=p.getUUID();strength=multiplier;expires=p.level().getGameTime()+600;attackAt=p.level().getGameTime();strikes=5;entityData.set(FRIENDLY,spell.friendly());setPos(at);setResting(false);}
    public boolean alliedTo(ServerPlayer p){if(owner==null)return false;if(owner.equals(p.getUUID()))return true;var other=p.level().getServer().getPlayerList().getPlayer(owner);return other!=null&&other.isAlliedTo(p);}
    public net.minecraft.world.scores.PlayerTeam getTeam(){if(owner!=null&&level() instanceof ServerLevel l){var p=l.getServer().getPlayerList().getPlayer(owner);if(p!=null)return p.getTeam();}return super.getTeam();}
    public boolean removeWhenFarAway(double distance){return false;}
    protected boolean shouldDropLoot(){return false;}
    public void tick(){if(!level().isClientSide()){long now=level().getGameTime();if(program==null||owner==null||!program.valid()||!program.medium().equals("spellbat")||expires<=now||expires>now+600||strikes<=0){discard();return;}setResting(false);}super.tick();if(level().isClientSide()&&tickCount%3==0)level().addParticle(friendly()?ParticleTypes.ENCHANT:ParticleTypes.WITCH,getX(),getY()+.4,getZ(),0,.01,0);}
    private boolean visible(ServerLevel l,LivingEntity target){var a=position().add(0,getBbHeight()*.5,0);var b=target.position().add(0,target.getBbHeight()*.5,0);return SpellEngine.loadedLine(l,a,b)&&l.clip(new ClipContext(a,b,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS;}
    private boolean eligible(ServerPlayer p,LivingEntity target){return target.level()==p.level()&&target!=this&&!(target instanceof SpellBat)&&target.isAlive()&&!target.isSpectator()&&distanceToSqr(target)<=144&&(friendly()?SpellEngine.friendly(p,target):!SpellEngine.protectedTarget(p,target))&&(!friendly()||!program.impactEffects().stream().allMatch(e->e.kind().equals("heal"))||target.getHealth()<target.getMaxHealth());}
    protected void customServerAiStep(ServerLevel l){
        var p=owner==null?null:l.getServer().getPlayerList().getPlayer(owner);if(p==null||p.level()!=l||!p.isAlive()||p.isSpectator()){victim=null;setDeltaMovement(Vec3.ZERO);return;}
        if(victim!=null&&(!eligible(p,victim)||!visible(l,victim)))victim=null;
        if(tickCount%10==0&&victim==null){double nearest=Double.MAX_VALUE;for(var candidate:l.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(12))){double d=distanceToSqr(candidate);if(d<nearest&&eligible(p,candidate)&&visible(l,candidate)){nearest=d;victim=candidate;}}}
        var destination=victim==null?p.position().add(0,1.5,0):victim.position().add(0,victim.getBbHeight()*.66,0);var delta=destination.subtract(position());var motion=delta.lengthSqr()<.3?Vec3.ZERO:getDeltaMovement().scale(.6).add(delta.normalize().scale(.14));
        var box=getBoundingBox().expandTowards(motion).inflate(.05);if(!SpellEngine.loadedLine(l,new Vec3(box.minX,box.minY,box.minZ),new Vec3(box.maxX,box.maxY,box.maxZ))||!l.noCollision(this,getBoundingBox().move(motion)))motion=Vec3.ZERO;
        setDeltaMovement(motion);if(motion.lengthSqr()>.001)setYRot((float)(Math.atan2(motion.z,motion.x)*180/Math.PI)-90);
        long now=l.getGameTime();if(victim!=null&&now>=attackAt&&distanceToSqr(victim)<6.25&&getBoundingBox().maxY>victim.getBoundingBox().minY&&getBoundingBox().minY<victim.getBoundingBox().maxY&&visible(l,victim)){
            attackAt=now+40;strikes--;SpellEngine.apply(p,program,new EntityHitResult(victim),strength*.33F);if(strikes<=0||getHealth()<=1)discard();else setHealth(getHealth()-1);
        }
    }
    protected void addAdditionalSaveData(ValueOutput out){super.addAdditionalSaveData(out);if(program!=null)out.store("program",FocusProgram.CODEC,program);if(owner!=null)out.putString("owner",owner.toString());out.putLong("expires",expires);out.putLong("attack_at",attackAt);out.putInt("strikes",strikes);out.putFloat("strength",strength);}
    protected void readAdditionalSaveData(ValueInput in){super.readAdditionalSaveData(in);program=in.read("program",FocusProgram.CODEC).orElse(null);try{owner=UUID.fromString(in.getStringOr("owner",""));}catch(IllegalArgumentException e){owner=null;}expires=in.getLongOr("expires",0);attackAt=in.getLongOr("attack_at",expires);strikes=Math.clamp(in.getIntOr("strikes",0),0,5);strength=in.getFloatOr("strength",1);if(!Float.isFinite(strength)||strength<=0||strength>1)program=null;if(program!=null)entityData.set(FRIENDLY,program.friendly());setResting(false);setNoGravity(true);}
}
