package thaumcraft.thaumaturgy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.arrow.SpectralArrow;
import net.minecraft.world.level.Level;
public final class TurretSpectralArrow extends SpectralArrow {
 public TurretSpectralArrow(EntityType<? extends TurretSpectralArrow> t,Level l){super(t,l);}
 protected boolean canHitEntity(Entity e){return !(getOwner() instanceof ServerPlayer p&&e instanceof LivingEntity living&&(SpellEngine.protectedTarget(p,living)||living instanceof CrossbowTurret t&&p.getUUID().equals(t.owner())))&&super.canHitEntity(e);}
 public void tick(){if(!level().isClientSide()&&(!(getOwner() instanceof ServerPlayer p)||p.level()!=level()||!p.isAlive()||p.isSpectator())){discard();return;}super.tick();}
}
