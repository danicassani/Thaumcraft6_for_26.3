package thaumcraft.thaumaturgy;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;

public final class FocusProjectile extends ThrowableItemProjectile {
    public FocusProjectile(EntityType<? extends FocusProjectile> t,Level l){super(t,l);}
    private float strength=1;
    private long expires;
    private LivingEntity target;
    public void strength(float value){if(!Float.isFinite(value)||value<=0||value>1)throw new IllegalArgumentException("Invalid spell strength");strength=value;}
    public long expires(){return expires;}
    public FocusProgram program(){return getItem().get(ThaumaturgyContent.PROGRAM.get());}
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput out){super.addAdditionalSaveData(out);out.putFloat("focus_strength",strength);out.putLong("focus_expires",expires);}
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput in){super.readAdditionalSaveData(in);float value=in.getFloatOr("focus_strength",1);strength=Float.isFinite(value)&&value>0&&value<=1?value:1;expires=in.getLongOr("focus_expires",0);}
    protected Item getDefaultItem(){return thaumcraft.content.SimpleContent.ITEMS.get("focus_1").get();}
    protected double getDefaultGravity(){var p=program();return p==null||p.projectile().speed()==0&&p.projectile().option()==0?0:p.projectile().option()>1?.005:.01;}
    public void tick(){
        if(!level().isClientSide()){
            var program=program();if(program==null||!program.valid()){discard();return;}
            if(expires==0)expires=level().getGameTime()+(program.projectile().speed()==0&&program.projectile().option()==0?100:1200);
            if(level().getGameTime()>=expires){discard();return;}
            if(getOwner()==null)return;
            if(!(getOwner() instanceof ServerPlayer p)||p.level()!=level()||!p.isAlive()||p.isSpectator()){discard();return;}
            int option=program.projectile().option();if(option>1)seek(p,option==3);
            var end=position().add(getDeltaMovement()).add(0,-.02,0);if(!SpellEngine.loadedLine(level(),position().add(-.5,0,-.5),end.add(.5,1,.5))||!level().getWorldBorder().isWithinBounds(net.minecraft.core.BlockPos.containing(end))){discard();return;}
        }
        super.tick();
    }
    private boolean eligible(ServerPlayer p,LivingEntity e,boolean friendly){if(e.level()!=level()||!e.isAlive()||e.isSpectator()||e.distanceToSqr(this)>256)return false;boolean ally=SpellEngine.friendly(p,e);if(friendly?!ally:SpellEngine.protectedTarget(p,e))return false;var to=e.getBoundingBox().getCenter();if(!SpellEngine.loadedLine(level(),position(),to))return false;return level().clip(new ClipContext(position(),to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS;}
    private void seek(ServerPlayer p,boolean friendly){
        if(target!=null&&!eligible(p,target,friendly))target=null;
        if(target==null&&(tickCount<=1||tickCount%5==0)){double distance=257;var heading=getDeltaMovement().normalize();for(var e:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(16))){if(e==p||!eligible(p,e,friendly)||heading.dot(e.getBoundingBox().getCenter().subtract(position()).normalize())<.2)continue;double d=e.distanceToSqr(this);if(d<distance){distance=d;target=e;}}}
        if(target!=null){var velocity=getDeltaMovement();var toward=target.getBoundingBox().getCenter().subtract(position()).normalize();setDeltaMovement(velocity.normalize().add(toward.scale(.275)).normalize().scale(velocity.length()));syncVelocity=true;}
    }
    protected boolean canHitEntity(Entity e){var p=program();if(p!=null&&p.projectile().option()>1&&getOwner() instanceof ServerPlayer owner&&e instanceof LivingEntity living){boolean ally=SpellEngine.friendly(owner,living);if(p.projectile().option()==3?!ally:SpellEngine.protectedTarget(owner,living))return false;}return super.canHitEntity(e);}
    protected void onHit(HitResult hit){
        var program=program();
        if(program!=null&&program.projectile().option()==1&&hit instanceof BlockHitResult b){var v=getDeltaMovement();var axis=b.getDirection().getAxis();setDeltaMovement(v.x*(axis==net.minecraft.core.Direction.Axis.X?-.9:.9),v.y*(axis==net.minecraft.core.Direction.Axis.Y?-.81:.9),v.z*(axis==net.minecraft.core.Direction.Axis.Z?-.9:.9));setPos(hit.getLocation().add(new Vec3(b.getDirection().getStepX(),b.getDirection().getStepY(),b.getDirection().getStepZ()).scale(.02)));syncVelocity=true;if(!level().isClientSide()&&getDeltaMovement().length()<.2)discard();return;}
        super.onHit(hit);if(!level().isClientSide()){if(program!=null&&getOwner() instanceof ServerPlayer p&&p.level()==level())SpellEngine.apply(p,program,hit,strength);discard();}
    }
}
