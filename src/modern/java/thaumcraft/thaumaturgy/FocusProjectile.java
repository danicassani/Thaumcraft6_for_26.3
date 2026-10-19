package thaumcraft.thaumaturgy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
public final class FocusProjectile extends ThrowableItemProjectile {
    public FocusProjectile(EntityType<? extends FocusProjectile> t,Level l){super(t,l);}
    protected Item getDefaultItem(){return thaumcraft.content.SimpleContent.ITEMS.get("focus_1").get();}
    protected double getDefaultGravity(){return 0;}
    public void tick(){super.tick();if(!level().isClientSide()&&tickCount>100)discard();}
    protected void onHit(HitResult hit){super.onHit(hit);if(!level().isClientSide()){var program=getItem().get(ThaumaturgyContent.PROGRAM.get());if(program!=null&&getOwner() instanceof ServerPlayer p&&p.level()==level())SpellEngine.apply(p,program,hit);discard();}}
}
