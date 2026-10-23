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
 * gravity and deals the caster's attack damage on impact. Targets persist by UUID;
 * the current runtime id is synchronized for matching client homing.</p>
 */
public final class ClericOrb extends ThrowableItemProjectile {
    private static final int LIFETIME = 240;
    private static final EntityDataAccessor<Integer> TARGET =
            SynchedEntityData.defineId(ClericOrb.class, EntityDataSerializers.INT);
    private Entity target;
    private java.util.UUID targetUuid;
    private boolean deflected;
    private long expires;

    public ClericOrb(EntityType<? extends ClericOrb> type, Level level) {
        super(type, level);
    }

    public ClericOrb(EntityType<? extends ClericOrb> type, Level level, LivingEntity owner, LivingEntity target) {
        super(type, owner, level, new ItemStack(Items.ENDER_PEARL));
        this.target = target;
        this.targetUuid = target.getUUID();
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
        return (deflected||entity != getOwner()) && !(entity instanceof net.minecraft.world.entity.player.Player p&&(p.isCreative()||p.isSpectator())) && super.canHitEntity(entity);
    }

    @Override
    public void tick() {
        if(level() instanceof ServerLevel server){
            long now=server.getGameTime();if(expires==0)expires=now+LIFETIME;
            if(thaumcraft.config.ThaumcraftConfig.wussMode.get()||now>=expires||expires>now+LIFETIME||!thaumcraft.thaumaturgy.SpellEngine.loadedLine(server,position(),position().add(getDeltaMovement()))){discard();return;}
            if(targetUuid!=null){target=server.getEntity(targetUuid);entityData.set(TARGET,target instanceof LivingEntity&&target.isAlive()?target.getId():0);}
        }else if(target==null||target.isRemoved()||target.getId()!=entityData.get(TARGET)){
            var entity=level().getEntity(entityData.get(TARGET));target=entity instanceof LivingEntity?entity:null;
        }
        super.tick();if(isRemoved())return;

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
        if(thaumcraft.config.ThaumcraftConfig.wussMode.get()){discard();return;}

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
    public boolean hurtServer(ServerLevel level,net.minecraft.world.damagesource.DamageSource source,float amount){
        if(isInvulnerableToBase(source)||source.getEntity()==null)return false;
        if(thaumcraft.config.ThaumcraftConfig.wussMode.get()){discard();return false;}
        setDeltaMovement(source.getEntity().getLookAngle().scale(.9));deflected=true;markHurt();
        playSound(thaumcraft.rifts.RiftContent.ZAP.get(),1,1);return true;
    }
    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putLong("cleric_expires", expires);
        if(targetUuid!=null)out.putString("cleric_target_uuid",targetUuid.toString());
        out.putBoolean("cleric_deflected",deflected);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        expires = in.getLongOr("cleric_expires", 0L);
        target=null;targetUuid=null;entityData.set(TARGET,0);
        try{targetUuid=java.util.UUID.fromString(in.getStringOr("cleric_target_uuid",""));}catch(IllegalArgumentException ignored){}
        deflected=in.getBooleanOr("cleric_deflected",false);
    }
}
