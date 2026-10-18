package thaumcraft.alchemy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
public final class AlumentumProjectile extends ThrowableItemProjectile {
    public AlumentumProjectile(EntityType<? extends AlumentumProjectile> type,Level level){super(type,level);}
    @Override protected Item getDefaultItem(){return AlchemyContent.ALUMENTUM.get();}
    @Override protected void onHit(HitResult result){super.onHit(result);if(level() instanceof ServerLevel){level().explode(this,getX(),getY(),getZ(),1.1F,Level.ExplosionInteraction.TNT);discard();}}
}
