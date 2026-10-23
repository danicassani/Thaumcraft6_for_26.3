package thaumcraft.warp;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of TC6's {@code EntityGolemOrb} as used by the cultist cleric.
 *
 * <p>The original orb homes towards its target for up to 240 ticks, has no
 * gravity and deals the caster's attack damage on impact. The client does not
 * need the target because movement is simulated on the server.</p>
 */
public final class ClericOrb extends ThrowableItemProjectile {
    private static final int LIFETIME = 240;
    private static final EntityDataAccessor<Integer> TARGET =
            SynchedEntityData.defineId(ClericOrb.class, EntityDataSerializers.INT);
    private Entity target;
    private long expires;

    public ClericOrb(EntityType<? extends ClericOrb> type, Level level) {
        super(type, level);
    }

    public ClericOrb(EntityType<? extends ClericOrb> type, Level level, LivingEntity owner, LivingEntity target) {
        super(type, owner, level, new ItemStack(Items.ENDER_PEARL));
        this.target = target;
        entityData.set(TARGET, target.getId());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TARGET, 0);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0D;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity != getOwner() && super.canHitEntity(entity);
    }

    @Override
    public void tick() {
        super.tick();
        if (isRemoved()) return;

        if (!level().isClientSide()) {
            long now = level().getGameTime();
            if (expires == 0L) expires = now + LIFETIME;
            if (now >= expires) {
                discard();
                return;
            }
        }

        // Resolve the tracked target on both sides; the original sent its entity id in spawn data.
        if (target == null) {
            int id = entityData.get(TARGET);
            if (id != 0 && level().getEntity(id) instanceof LivingEntity living) target = living;
        }

        // TC6 homes every tick by adding distance-scaled acceleration and clamping each axis.
        if (target != null && target.isAlive()) {
            double distance = distanceToSqr(target);
            if (distance > 0.0D) {
                double dx = (target.getX() - getX()) / distance;
                double dy = (target.getBoundingBox().minY + target.getBbHeight() * 0.6D - getY()) / distance;
                double dz = (target.getZ() - getZ()) / distance;
                Vec3 motion = getDeltaMovement().add(dx * 0.2D, dy * 0.2D, dz * 0.2D);
                setDeltaMovement(
                        Math.clamp(motion.x, -0.25D, 0.25D),
                        Math.clamp(motion.y, -0.25D, 0.25D),
                        Math.clamp(motion.z, -0.25D, 0.25D));
            }
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!(level() instanceof ServerLevel server)) return;

        if (hit instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof LivingEntity victim
                && getOwner() instanceof LivingEntity owner) {
            victim.hurtServer(server, damageSources().indirectMagic(this, owner),
                    (float) owner.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE));
        }
        server.sendParticles(ParticleTypes.PORTAL, getX(), getY(), getZ(), 12, 0.2D, 0.2D, 0.2D, 0.05D);
        playSound(thaumcraft.rifts.RiftContent.ZAP.get(), 1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
        discard();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putLong("cleric_expires", expires);
        out.putInt("cleric_target", entityData.get(TARGET));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        expires = in.getLongOr("cleric_expires", 0L);
        entityData.set(TARGET, in.getIntOr("cleric_target", 0));
    }
}
