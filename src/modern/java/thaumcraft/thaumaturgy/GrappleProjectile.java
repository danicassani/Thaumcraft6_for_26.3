package thaumcraft.thaumaturgy;
import net.minecraft.core.*;
import net.minecraft.network.syncher.*;
import net.minecraft.world.InteractionHand;
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
    private static final EntityDataAccessor<Boolean> OFF_HAND=SynchedEntityData.defineId(GrappleProjectile.class,EntityDataSerializers.BOOLEAN),ANCHORED=SynchedEntityData.defineId(GrappleProjectile.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Long> ANCHORED_AT=SynchedEntityData.defineId(GrappleProjectile.class,EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> FLIGHT_AGE=SynchedEntityData.defineId(GrappleProjectile.class,EntityDataSerializers.INT);
    private BlockPos anchor;private BlockState support;private long expires;private boolean bound,boosted;
    protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(OFF_HAND,false);b.define(ANCHORED,false);b.define(ANCHORED_AT,0L);b.define(FLIGHT_AGE,0);}
    public void launchHand(InteractionHand hand){entityData.set(OFF_HAND,hand==InteractionHand.OFF_HAND);bound=true;}
    public InteractionHand hand(){return entityData.get(OFF_HAND)?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND;}
    public float anchorAge(float partial){return anchored()?Math.max(0,level().getGameTime()-entityData.get(ANCHORED_AT)+partial):0;}
    public int flightAge(){return entityData.get(FLIGHT_AGE);}
    private boolean holdsLinkedGun(ServerPlayer p){var gun=p.getItemInHand(hand());if(!bound){var main=p.getMainHandItem();if(!(main.getItem() instanceof GrappleGun)&&!(p.getOffhandItem().getItem() instanceof GrappleGun))return false;launchHand(main.getItem() instanceof GrappleGun?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND);gun=p.getItemInHand(hand());gun.set(ThaumaturgyContent.GRAPPLE_LINK.get(),getUUID());}return gun.getItem() instanceof GrappleGun&&getUUID().equals(gun.get(ThaumaturgyContent.GRAPPLE_LINK.get()));}
    public void remove(RemovalReason reason){if(!level().isClientSide()&&reason.shouldDestroy()&&getOwner() instanceof ServerPlayer player){for(var hand:InteractionHand.values()){var held=player.getItemInHand(hand);if(getUUID().equals(held.get(ThaumaturgyContent.GRAPPLE_LINK.get())))held.remove(ThaumaturgyContent.GRAPPLE_LINK.get());}var inv=player.getInventory();for(int i=0;i<inv.getContainerSize();i++){var stack=inv.getItem(i);if(getUUID().equals(stack.get(ThaumaturgyContent.GRAPPLE_LINK.get())))stack.remove(ThaumaturgyContent.GRAPPLE_LINK.get());}}super.remove(reason);}
    public GrappleProjectile(EntityType<? extends GrappleProjectile> t,Level l){super(t,l);}
    protected Item getDefaultItem(){return SimpleContent.ITEMS.get("grapple_gun_tip").get();}
    public boolean anchored(){return entityData.get(ANCHORED);}
    public void tick(){
        if(level().isClientSide()){if(anchored()){setDeltaMovement(Vec3.ZERO);setNoGravity(true);}super.tick();return;}
        if(expires==0)expires=level().getGameTime()+30;
        if(!(getOwner() instanceof ServerPlayer p)||p.level()!=level()||!p.isAlive()||p.isSpectator()||p.isShiftKeyDown()||level().getGameTime()>=expires||p.distanceToSqr(this)>1024||!holdsLinkedGun(p)){discard();return;}
        if(anchor==null){if(!SpellEngine.loadedLine(level(),position().add(-.5,0,-.5),position().add(getDeltaMovement()).add(.5,1,.5))){discard();return;}super.tick();return;}
        if(!level().hasChunkAt(anchor)||!level().getBlockState(anchor).equals(support)||!SpellEngine.loadedLine(level(),p.getEyePosition(),position())){discard();return;}
        var ray=level().clip(new ClipContext(p.getEyePosition(),position(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));if(ray.getType()==HitResult.Type.BLOCK&&!ray.getBlockPos().equals(anchor)){discard();return;}
        var delta=position().subtract(p.getEyePosition());if(delta.length()<1.5){p.fallDistance=0;discard();return;}var velocity=p.getDeltaMovement().add(delta.normalize().scale(.12)).add(0,.033+(boosted?0:.4),0);boosted=true;if(velocity.length()>1.2)velocity=velocity.normalize().scale(1.2);p.setDeltaMovement(velocity);p.syncVelocity=true;p.fallDistance=0;
    }
    protected void onHit(HitResult hit){if(level().isClientSide())return;if(hit instanceof BlockHitResult b&&getOwner() instanceof ServerPlayer p){var q=b.getBlockPos();if(!level().hasChunkAt(q)||!level().mayInteract(p,q)||!p.mayUseItemAt(q,b.getDirection(),p.getItemInHand(hand()))||level().getBlockState(q).getCollisionShape(level(),q).isEmpty()){discard();return;}anchor=q.immutable();entityData.set(ANCHORED,true);entityData.set(ANCHORED_AT,level().getGameTime());entityData.set(FLIGHT_AGE,Math.min(30,tickCount));support=level().getBlockState(q);var f=b.getDirection();setPos(hit.getLocation().add(f.getStepX()*.03,f.getStepY()*.03,f.getStepZ()*.03));setDeltaMovement(Vec3.ZERO);setNoGravity(true);expires=level().getGameTime()+60;syncVelocity=true;}else discard();}
    protected void addAdditionalSaveData(ValueOutput o){super.addAdditionalSaveData(o);o.putLong("expires",expires);o.putBoolean("offhand",hand()==InteractionHand.OFF_HAND);o.putBoolean("bound",bound);o.putBoolean("boosted",boosted);o.putLong("anchored_at",entityData.get(ANCHORED_AT));o.putInt("flight_age",flightAge());if(anchor!=null){o.putLong("anchor",anchor.asLong());o.store("support",BlockState.CODEC,support);}}
    protected void readAdditionalSaveData(ValueInput i){super.readAdditionalSaveData(i);expires=i.getLongOr("expires",0);long a=i.getLongOr("anchor",Long.MIN_VALUE);anchor=a==Long.MIN_VALUE?null:BlockPos.of(a);support=i.read("support",BlockState.CODEC).orElse(null);if(anchor!=null&&support==null)anchor=null;entityData.set(OFF_HAND,i.getBooleanOr("offhand",false));bound=i.getBooleanOr("bound",false);boosted=i.getBooleanOr("boosted",false);entityData.set(ANCHORED,anchor!=null);entityData.set(ANCHORED_AT,i.getLongOr("anchored_at",level().getGameTime()));entityData.set(FLIGHT_AGE,Math.clamp(i.getIntOr("flight_age",0),0,30));}
}
