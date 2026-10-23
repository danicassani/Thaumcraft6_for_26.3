package thaumcraft.pech;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
public final class PechBolt extends ThrowableItemProjectile {
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> EFFECT=net.minecraft.network.syncher.SynchedEntityData.defineId(PechBolt.class,net.minecraft.network.syncher.EntityDataSerializers.INT);
    private long expires;
    public PechBolt(EntityType<? extends PechBolt> type,Level level){super(type,level);}
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(EFFECT,0);}
    public void effect(int value){if(value<0||value>4)throw new IllegalArgumentException("Unknown pech spell");entityData.set(EFFECT,value);}
    public int effect(){return entityData.get(EFFECT);}
    protected Item getDefaultItem(){return thaumcraft.content.SimpleContent.ITEMS.get("pech_wand").get();}protected double getDefaultGravity(){return 0;}
    protected boolean canHitEntity(Entity e){return !(e instanceof Pech)&&!(e instanceof Player p&&(p.isCreative()||p.isSpectator()))&&super.canHitEntity(e);}
    public void tick(){if(!level().isClientSide()){long now=level().getGameTime();if(expires==0)expires=now+100;if(now>=expires||expires>now+100||!(getOwner() instanceof Pech p)||!p.isAlive()||thaumcraft.config.ThaumcraftConfig.wussMode.get()||!thaumcraft.thaumaturgy.SpellEngine.loadedLine(level(),position(),position().add(getDeltaMovement()))){discard();return;}}super.tick();}
    protected void onHit(HitResult hit){super.onHit(hit);if(!(level() instanceof ServerLevel l))return;if(hit instanceof EntityHitResult h&&h.getEntity() instanceof LivingEntity victim&&!(victim instanceof Pech)&&!(victim instanceof Player p&&(p.isCreative()||p.isSpectator()))&&getOwner() instanceof Pech owner){switch(effect()){case 0->{victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,100));victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,100));}case 1,2->victim.hurtServer(l,damageSources().indirectMagic(this,owner),3);case 3->{victim.hurtServer(l,damageSources().indirectMagic(this,owner),1);var d=getDeltaMovement().normalize();victim.push(d.x*.3,.3,d.z*.3);}case 4->{victim.hurtServer(l,damageSources().indirectMagic(this,owner),2);victim.igniteForSeconds(2);}}}l.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,getX(),getY(),getZ(),10,.15,.15,.15,.03);discard();}
    protected void addAdditionalSaveData(ValueOutput o){super.addAdditionalSaveData(o);o.putInt("pech_spell",effect());o.putLong("pech_expires",expires);}protected void readAdditionalSaveData(ValueInput i){super.readAdditionalSaveData(i);effect(Math.clamp(i.getIntOr("pech_spell",0),0,4));expires=i.getLongOr("pech_expires",0);}
}
