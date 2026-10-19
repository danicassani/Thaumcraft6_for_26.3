package thaumcraft.thaumaturgy;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

/** Static cloud or mine. Absolute deadlines continue to elapse while chunks/owners are absent. */
public final class FocusFieldEntity extends Entity {
    private static final EntityDataAccessor<Boolean> MINE=SynchedEntityData.defineId(FocusFieldEntity.class,EntityDataSerializers.BOOLEAN),ARMED=SynchedEntityData.defineId(FocusFieldEntity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> RADIUS=SynchedEntityData.defineId(FocusFieldEntity.class,EntityDataSerializers.INT);
    private FocusProgram program;private UUID owner;private long expires,armedAt,nextPulse;private float strength=1;
    private BlockPos support;private net.minecraft.world.level.block.state.BlockState supportingState;
    public FocusFieldEntity(EntityType<? extends FocusFieldEntity> type,Level level){super(type,level);setNoGravity(true);}
    protected void defineSynchedData(SynchedEntityData.Builder data){data.define(MINE,false);data.define(ARMED,false);data.define(RADIUS,1);}
    public boolean mine(){return entityData.get(MINE);}public boolean armed(){return entityData.get(ARMED);}public int radius(){return entityData.get(RADIUS);}public UUID ownerId(){return owner;}public long expires(){return expires;}public FocusProgram program(){return program;}
    public void configure(ServerPlayer p,FocusProgram program,Vec3 position,float strength,BlockPos support){if(!program.valid()||(!program.medium().equals("cloud")&&!program.medium().equals("mine"))||!Float.isFinite(strength)||strength<=0||strength>1)throw new IllegalArgumentException("Invalid persistent focus field");this.program=program;owner=p.getUUID();this.strength=strength;entityData.set(MINE,program.medium().equals("mine"));entityData.set(RADIUS,program.radius());setPos(position);long now=p.level().getGameTime();armedAt=now+40;nextPulse=now+5;expires=now+(mine()?1200:program.seconds()*20);this.support=support==null?null:support.immutable();supportingState=support==null?null:p.level().getBlockState(support);}
    public void move(MoverType type,Vec3 movement){}
    public boolean hurtServer(ServerLevel level,DamageSource source,float amount){return false;}
    public void tick(){super.tick();if(level().isClientSide()){if(tickCount%3==0){double r=mine()?.15:radius();for(int i=0;i<(mine()?1:radius()+1);i++)level().addParticle(mine()?ParticleTypes.ENCHANT:ParticleTypes.WITCH,getX()+(random.nextDouble()-.5)*r*2,getY()+(random.nextDouble()-.5)*r,getZ()+(random.nextDouble()-.5)*r*2,0,.01,0);}return;}
        var l=(ServerLevel)level();long now=l.getGameTime();if(program==null||owner==null||expires<=now||expires>now+1200){discard();return;}
        if(mine()){if(support==null||supportingState==null||!l.hasChunkAt(support)||!l.getBlockState(support).equals(supportingState)){discard();return;}entityData.set(ARMED,now>=armedAt);if(!armed())return;}
        if(now<nextPulse)return;nextPulse=now+5;var p=l.getServer().getPlayerList().getPlayer(owner);if(p==null||p.level()!=l||!p.isAlive())return;
        var center=position();double r=mine()?1:radius();var box=new AABB(center.x-r,center.y-r,center.z-r,center.x+r,center.y+r,center.z+r);var targets=l.getEntitiesOfClass(LivingEntity.class,box,e->e.isAlive()&&!e.isSpectator()&&center.distanceToSqr(e.getX(),e.getY()+e.getBbHeight()*.5,e.getZ())<=r*r);
        var eligible=new ArrayList<LivingEntity>();for(var target:targets){if(mine()&&(program.friendly()!=SpellEngine.friendly(p,target)))continue;if(!visible(l,center,target.getEyePosition(),this))continue;eligible.add(target);if(eligible.size()==16)break;}
        if(mine()){if(eligible.isEmpty())return;discard();for(var target:eligible)SpellEngine.apply(p,program,new EntityHitResult(target),strength);l.sendParticles(ParticleTypes.ENCHANT,getX(),getY(),getZ(),20,.3,.3,.3,.05);return;}
        var cooldowns=FocusCooldowns.get(l);for(var target:eligible)if(cooldowns.acquire("entity:"+target.getUUID(),now))SpellEngine.apply(p,program,new EntityHitResult(target),strength*.5F);
        // Bounded native ray samples, never an unbounded cube scan or chunk load.
        for(int i=0;i<radius();i++){var direction=new Vec3(random.nextGaussian(),random.nextGaussian(),random.nextGaussian()).normalize();var end=center.add(direction.scale(radius()));if(!SpellEngine.loadedLine(l,center,end))continue;var hit=l.clip(new ClipContext(center,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));if(hit.getType()!=HitResult.Type.MISS&&cooldowns.acquire("block:"+hit.getBlockPos().asLong(),now))SpellEngine.apply(p,program,hit,strength*.5F);}
    }
    private static boolean visible(ServerLevel l,Vec3 from,Vec3 to,Entity source){return SpellEngine.loadedLine(l,from,to)&&l.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,source)).getType()==HitResult.Type.MISS;}
    protected void addAdditionalSaveData(ValueOutput out){if(program!=null)out.store("program",FocusProgram.CODEC,program);if(owner!=null)out.putString("owner",owner.toString());out.putLong("expires",expires);out.putLong("armed_at",armedAt);out.putLong("next_pulse",nextPulse);out.putFloat("strength",strength);if(support!=null)out.putLong("support",support.asLong());if(supportingState!=null)out.store("supporting_state",net.minecraft.world.level.block.state.BlockState.CODEC,supportingState);}
    protected void readAdditionalSaveData(ValueInput in){program=in.read("program",FocusProgram.CODEC).orElse(null);try{owner=UUID.fromString(in.getStringOr("owner",""));}catch(IllegalArgumentException e){owner=null;}expires=in.getLongOr("expires",0);armedAt=in.getLongOr("armed_at",expires);nextPulse=in.getLongOr("next_pulse",0);strength=in.getFloatOr("strength",1);if(!Float.isFinite(strength)||strength<=0||strength>1)program=null;if(program!=null){entityData.set(MINE,program.medium().equals("mine"));entityData.set(RADIUS,program.radius());if(!mine()&&!program.medium().equals("cloud"))program=null;}long savedSupport=in.getLongOr("support",Long.MIN_VALUE);support=savedSupport==Long.MIN_VALUE?null:BlockPos.of(savedSupport);supportingState=in.read("supporting_state",net.minecraft.world.level.block.state.BlockState.CODEC).orElse(null);}
}
