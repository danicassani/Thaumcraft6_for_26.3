package thaumcraft.thaumaturgy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import thaumcraft.rifts.FluxRiftEntity;
import thaumcraft.content.SimpleContent;
public final class CausalityProjectile extends ThrowableItemProjectile {
    private long expires;
    public CausalityProjectile(EntityType<? extends CausalityProjectile> t,Level l){super(t,l);}
    protected Item getDefaultItem(){return SimpleContent.ITEMS.get("causality_collapser").get();}
    public void tick(){if(!level().isClientSide()){if(expires==0)expires=level().getGameTime()+1200;if(level().getGameTime()>=expires||!SpellEngine.loadedLine(level(),position().add(-.5,0,-.5),position().add(getDeltaMovement()).add(.5,1,.5))){discard();return;}}super.tick();}
    protected void onHit(HitResult hit){super.onHit(hit);if(level() instanceof ServerLevel l){for(var r:l.getEntitiesOfClass(FluxRiftEntity.class,getBoundingBox().inflate(3)))if(r.distanceToSqr(this)<=9)r.beginCollapse();l.explode(this,getX(),getY(),getZ(),2,Level.ExplosionInteraction.TNT);discard();}}
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput o){super.addAdditionalSaveData(o);o.putLong("expires",expires);}
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput i){super.readAdditionalSaveData(i);expires=i.getLongOr("expires",0);}
}
