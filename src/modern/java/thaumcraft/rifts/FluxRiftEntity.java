package thaumcraft.rifts;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import thaumcraft.aura.*;
import thaumcraft.content.SimpleContent;

public final class FluxRiftEntity extends Entity {
    private static final EntityDataAccessor<Integer> SEED=SynchedEntityData.defineId(FluxRiftEntity.class,EntityDataSerializers.INT), SIZE=SynchedEntityData.defineId(FluxRiftEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> STABILITY=SynchedEntityData.defineId(FluxRiftEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> COLLAPSE=SynchedEntityData.defineId(FluxRiftEntity.class,EntityDataSerializers.BOOLEAN);
    private int maxSize=5,shapeSize=-1,shapeSeed;
    private List<RiftShape.Point> shape=List.of();
    public FluxRiftEntity(EntityType<? extends FluxRiftEntity> type,Level level) {super(type,level);setNoGravity(true);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder data) {data.define(SEED,1);data.define(SIZE,5);data.define(STABILITY,0F);data.define(COLLAPSE,false);}
    public int size() {return entityData.get(SIZE);} public int seed() {return entityData.get(SEED);} public float stability() {return entityData.get(STABILITY);} public boolean collapsing() {return entityData.get(COLLAPSE);}
    public void setSize(int size) {entityData.set(SIZE,Math.clamp(size,1,100));maxSize=Math.max(maxSize,size());}
    public void setSeed(int seed) {entityData.set(SEED,seed);}
    public void setStability(float value) {if(!Float.isFinite(value))throw new IllegalArgumentException("Non-finite rift stability");entityData.set(STABILITY,Math.clamp(value,-100,100));}
    public void beginCollapse() {if(!collapsing()){maxSize=size();entityData.set(COLLAPSE,true);}}
    public List<RiftShape.Point> shape() {
        if(shapeSize!=size()||shapeSeed!=seed()) {shape=RiftShape.create(seed(),size());shapeSize=size();shapeSeed=seed();}
        return shape;
    }
    private void bounds() {
        var points=shape();double minX=0,minY=0,minZ=0,maxX=0,maxY=0,maxZ=0;
        for(var point:points) {var p=point.position();double w=point.width()+.1;minX=Math.min(minX,p.x-w);minY=Math.min(minY,p.y-w);minZ=Math.min(minZ,p.z-w);maxX=Math.max(maxX,p.x+w);maxY=Math.max(maxY,p.y+w);maxZ=Math.max(maxZ,p.z+w);}
        setBoundingBox(new AABB(getX()+minX,getY()+minY,getZ()+minZ,getX()+maxX,getY()+maxY,getZ()+maxZ));
    }
    @Override public void move(MoverType type,Vec3 vector) {}
    @Override public boolean hurtServer(ServerLevel level,DamageSource source,float amount) {return false;}
    @Override public void tick() {
        super.tick();bounds();
        if(!(level() instanceof ServerLevel server))return;
        var points=shape();int i=random.nextInt(points.size()-1);var a=position().add(points.get(i).position());var b=position().add(points.get(i+1).position());
        // Check the entire short segment before clipping: never fetch a neighboring unloaded chunk.
        if(server.hasChunkAt(BlockPos.containing(a))&&server.hasChunkAt(BlockPos.containing(b))) {
            var hit=server.clip(new ClipContext(a,b,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
            if(hit.getType()==HitResult.Type.BLOCK) {var pos=hit.getBlockPos();var state=server.getBlockState(pos);if(!state.isAir()&&state.getDestroySpeed(server,pos)>=0)server.destroyBlock(pos,false,this);}
        }
        for(var entity:server.getEntities(this,new AABB(a,a).inflate(.5))) {
            if(entity instanceof FluxRiftEntity || (entity instanceof Player player && player.getAbilities().instabuild))continue;
            if(entity instanceof ItemEntity)entity.discard();else entity.hurtServer(server,damageSources().fellOutOfWorld(),2);
        }
        if(size()<=1 && !collapsing())beginCollapse();
        if(collapsing()) {
            if(random.nextBoolean())AuraManager.addVis(server,blockPosition(),1);else AuraManager.addFlux(server,blockPosition(),1);
            if(size()<=1) {completeCollapse(server);return;}setSize(size()-1);
        }
        if(tickCount%120==0)setStability(stability()-.2F);
        if(tickCount%600==getId()%600 && !collapsing()) {
            float cost=(float)Math.sqrt(size()*2);var cell=AuraManager.get(server,blockPosition());
            if(cell!=null && cell.flux()>=cost && size()<100 && stability()<=50) {AuraManager.drainFlux(server,blockPosition(),cost,false);setSize(size()+1);}
            if(stability()<0 && random.nextInt(1000)<Math.abs(stability())+size())corruptionEvent(server,random.nextInt(101));
        }
        if(tickCount%20==0)server.sendParticles(ParticleTypes.REVERSE_PORTAL,getX(),getY(),getZ(),2,.3,.3,.3,.01);
    }
    /** Original weights retained: the wisp/cloud slots remain reserved for their own port. */
    public boolean corruptionEvent(ServerLevel server,int roll) {
        if(collapsing()||thaumcraft.config.ThaumcraftConfig.wussMode.get())return false;
        if(roll>=50&&roll<60){
            if(thaumcraft.taint.TaintWorld.seed(server,blockPosition())!=null)return false;
            var p=blockPosition().offset((int)(random.nextGaussian()*5),(int)(random.nextGaussian()*5),(int)(random.nextGaussian()*5));
            if(!server.hasChunkAt(p.below())||!server.getBlockState(p.below()).isFaceSturdy(server,p.below(),net.minecraft.core.Direction.UP))return false;
            var seed=thaumcraft.taint.TaintWorld.spawnSeed(server,p,true,size());if(seed==null)return false;
            AuraManager.addFlux(server,blockPosition(),size()/2);discard();return true;
        }
        if(roll>=60&&roll<80){boolean changed=false;for(var e:server.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(16)))if(!(e instanceof Player p&&(p.isCreative()||p.isSpectator()))){e.addEffect(new net.minecraft.world.effect.MobEffectInstance(thaumcraft.taint.TaintContent.INFECTIOUS.getHolder().orElseThrow(),3000,2));changed=true;}if(changed)setStability(stability()+10);return changed;}
        if(roll==100){beginCollapse();return true;}return false;
    }
    private void completeCollapse(ServerLevel level) {
        if(random.nextInt(100)<(int)Math.sqrt(maxSize))level.addFreshEntity(new ItemEntity(level,getX(),getY(),getZ(),new ItemStack(SimpleContent.ITEMS.get("primordial_nugget").get(),2+random.nextInt(4))));
        var seeds=new ItemStack(SimpleContent.ITEMS.get("void_seed").get(),(int)Math.sqrt(maxSize));
        if(!seeds.isEmpty())level.addFreshEntity(new ItemEntity(level,getX(),getY(),getZ(),seeds));
        level.sendParticles(ParticleTypes.PORTAL,getX(),getY(),getZ(),60,.6,.6,.6,.2);discard();
    }
    @Override protected void addAdditionalSaveData(ValueOutput out) {out.putInt("rift_size",size());out.putInt("rift_seed",seed());out.putFloat("stability",stability());out.putBoolean("collapse",collapsing());out.putInt("max_size",maxSize);}
    @Override protected void readAdditionalSaveData(ValueInput in) {setSize(in.getIntOr("rift_size",5));setSeed(in.getIntOr("rift_seed",1));float stability=in.getFloatOr("stability",0);setStability(Float.isFinite(stability)?stability:0);entityData.set(COLLAPSE,in.getBooleanOr("collapse",false));maxSize=Math.clamp(in.getIntOr("max_size",size()),1,100);}
}
