package thaumcraft.thaumaturgy;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import thaumcraft.content.SimpleContent;
public final class GrappleProjectile extends ThrowableItemProjectile {
    private BlockPos anchor;private BlockState support;private long expires;
    public GrappleProjectile(EntityType<? extends GrappleProjectile> t,Level l){super(t,l);}
    protected Item getDefaultItem(){return SimpleContent.ITEMS.get("grapple_gun_tip").get();}
    public boolean anchored(){return anchor!=null;}
    public void tick(){
        if(level().isClientSide()){super.tick();return;}
        if(expires==0)expires=level().getGameTime()+30;
        if(!(getOwner() instanceof ServerPlayer p)||p.level()!=level()||!p.isAlive()||p.isSpectator()||p.isShiftKeyDown()||level().getGameTime()>=expires||p.distanceToSqr(this)>1024||!(p.getMainHandItem().getItem() instanceof GrappleGun||p.getOffhandItem().getItem() instanceof GrappleGun)){discard();return;}
        if(anchor==null){if(!SpellEngine.loadedLine(level(),position().add(-.5,0,-.5),position().add(getDeltaMovement()).add(.5,1,.5))){discard();return;}super.tick();return;}
        if(!level().hasChunkAt(anchor)||!level().getBlockState(anchor).equals(support)||!SpellEngine.loadedLine(level(),p.getEyePosition(),position())){discard();return;}
        var ray=level().clip(new ClipContext(p.getEyePosition(),position(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));if(ray.getType()==HitResult.Type.BLOCK&&!ray.getBlockPos().equals(anchor)){discard();return;}
        var delta=position().subtract(p.getEyePosition());if(delta.length()<1.5){p.fallDistance=0;discard();return;}var velocity=p.getDeltaMovement().add(delta.normalize().scale(.12));if(velocity.length()>1.2)velocity=velocity.normalize().scale(1.2);p.setDeltaMovement(velocity);p.syncVelocity=true;p.fallDistance=0;
        if(p.tickCount%4==0)for(int i=0;i<12;i++){var point=p.getEyePosition().add(delta.scale(i/12.));p.level().sendParticles(ParticleTypes.CRIT,point.x,point.y,point.z,1,0,0,0,0);}
    }
    protected void onHit(HitResult hit){if(level().isClientSide())return;if(hit instanceof BlockHitResult b&&getOwner() instanceof ServerPlayer p){var q=b.getBlockPos();if(!level().hasChunkAt(q)||!level().mayInteract(p,q)||!p.mayUseItemAt(q,b.getDirection(),p.getMainHandItem())||level().getBlockState(q).getCollisionShape(level(),q).isEmpty()){discard();return;}anchor=q.immutable();support=level().getBlockState(q);var f=b.getDirection();setPos(hit.getLocation().add(f.getStepX()*.03,f.getStepY()*.03,f.getStepZ()*.03));setDeltaMovement(Vec3.ZERO);setNoGravity(true);expires=level().getGameTime()+60;syncVelocity=true;}else discard();}
    protected void addAdditionalSaveData(ValueOutput o){super.addAdditionalSaveData(o);o.putLong("expires",expires);if(anchor!=null){o.putLong("anchor",anchor.asLong());o.store("support",BlockState.CODEC,support);}}
    protected void readAdditionalSaveData(ValueInput i){super.readAdditionalSaveData(i);expires=i.getLongOr("expires",0);long a=i.getLongOr("anchor",Long.MIN_VALUE);anchor=a==Long.MIN_VALUE?null:BlockPos.of(a);support=i.read("support",BlockState.CODEC).orElse(null);if(anchor!=null&&support==null)anchor=null;}
}
