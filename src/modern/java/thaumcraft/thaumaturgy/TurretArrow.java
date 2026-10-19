package thaumcraft.thaumaturgy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.level.Level;
public final class TurretArrow extends Arrow {
    public TurretArrow(EntityType<? extends TurretArrow> t,Level l){super(t,l);}
    protected boolean canHitEntity(Entity e){return !(getOwner() instanceof ServerPlayer p&&e instanceof LivingEntity living&&SpellEngine.protectedTarget(p,living))&&super.canHitEntity(e);}
    public void tick(){if(!level().isClientSide()&&(!(getOwner() instanceof ServerPlayer p)||p.level()!=level()||!p.isAlive()||p.isSpectator())){discard();return;}super.tick();}
}
