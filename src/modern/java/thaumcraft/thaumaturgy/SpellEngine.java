package thaumcraft.thaumaturgy;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
public final class SpellEngine {
    public static HitResult aim(ServerPlayer p,double range){var start=p.getEyePosition();var end=start.add(p.getLookAngle().scale(range));HitResult hit=p.level().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));double distance=start.distanceToSqr(hit.getLocation());for(var entity:p.level().getEntities(p,p.getBoundingBox().expandTowards(p.getLookAngle().scale(range)).inflate(1),e->e instanceof LivingEntity&&e.isAlive()&&!e.isSpectator())){var intersection=entity.getBoundingBox().inflate(.15).clip(start,end);if(intersection.isPresent()&&start.distanceToSqr(intersection.get())<distance){distance=start.distanceToSqr(intersection.get());hit=new EntityHitResult(entity,intersection.get());}}return hit;}
    public static void cast(ServerPlayer p,FocusProgram program){if(program.medium().equals("projectile")){var shot=new FocusProjectile(ThaumaturgyContent.PROJECTILE.get(),p.level());shot.setOwner(p);shot.setPos(p.getX(),p.getEyeY()-.1,p.getZ());var stack=new ItemStack(thaumcraft.content.SimpleContent.ITEMS.get("focus_1").get());stack.set(ThaumaturgyContent.PROGRAM.get(),program);shot.setItem(stack);shot.shootFromRotation(p,p.getXRot(),p.getYRot(),0,1.5F,0);p.level().addFreshEntity(shot);}else{var hit=aim(p,program.medium().equals("touch")?4:32);apply(p,program,hit);var from=p.getEyePosition();var delta=hit.getLocation().subtract(from);int steps=Math.min(128,(int)(delta.length()*4));for(int i=1;i<=steps;i++){var v=from.add(delta.scale((double)i/Math.max(1,steps)));p.level().sendParticles(ParticleTypes.ENCHANT,v.x,v.y,v.z,1,0,0,0,0);}}}
    public static void apply(ServerPlayer p,FocusProgram program,HitResult hit){
        var l=p.level();if(hit.getType()==HitResult.Type.MISS)return;LivingEntity target=hit instanceof EntityHitResult e&&e.getEntity() instanceof LivingEntity living?living:null;
        boolean protectedTarget=target!=null&&(target.isAlliedTo(p)||target instanceof net.minecraft.world.entity.player.Player other&&(!l.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.PVP)||!p.canHarmPlayer(other)||other.getAbilities().instabuild));
        for(var e:program.effects()){
            int power=e.power();if(target!=null){if(protectedTarget&&!e.kind().equals("heal"))continue;switch(e.kind()){
                case "heal"->{if(target.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD))target.hurtServer(l,l.damageSources().indirectMagic(p,p),power*2);else target.heal(power*2);}
                case "fire"->{target.hurtServer(l,l.damageSources().indirectMagic(p,p),power*2);target.igniteForSeconds(e.duration()*2);}
                case "frost"->{target.hurtServer(l,l.damageSources().indirectMagic(p,p),power*2);target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,40*e.duration(),power/2));}
                case "air"->{target.hurtServer(l,l.damageSources().indirectMagic(p,p),power);target.push(p.getLookAngle().x*power*.3,.2+power*.1,p.getLookAngle().z*power*.3);}
                case "earth","flux"->target.hurtServer(l,l.damageSources().indirectMagic(p,p),power*3);
                case "curse"->{target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,100*e.duration(),power/2));target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,100*e.duration(),power/2));}
            }}else if(hit instanceof BlockHitResult b){var q=b.getBlockPos();if(!l.hasChunkAt(q)||!l.mayInteract(p,q)||!p.mayUseItemAt(q,b.getDirection(),p.getMainHandItem()))continue;var state=l.getBlockState(q);switch(e.kind()){
                case "break"->{if(l.getBlockEntity(q)==null&&state.getDestroySpeed(l,q)>=0&&state.getDestroySpeed(l,q)<=power*4)p.gameMode.destroyBlock(q);}
                case "exchange"->{var replacement=p.getOffhandItem();if(!(replacement.getItem() instanceof BlockItem block)||l.getBlockEntity(q)!=null||state.getDestroySpeed(l,q)<0||state.getDestroySpeed(l,q)>power*4)continue;var intended=block.getBlock().defaultBlockState();if(intended.hasBlockEntity()||!intended.canSurvive(l,q)||!l.isUnobstructed(intended,q,net.minecraft.world.phys.shapes.CollisionContext.empty()))continue;if(p.gameMode.destroyBlock(q)&&l.getBlockState(q).isAir()){replacement.useOn(new net.minecraft.world.item.context.UseOnContext(p,net.minecraft.world.InteractionHand.OFF_HAND,new BlockHitResult(Vec3.atCenterOf(q),Direction.UP,q,false)));}}
                case "fire"->{var at=q.relative(b.getDirection());if(l.hasChunkAt(at)&&l.mayInteract(p,at)&&p.mayUseItemAt(at,b.getDirection(),p.getMainHandItem())&&l.isEmptyBlock(at)){var fire=BaseFireBlock.getState(l,at);if(fire.canSurvive(l,at)){var snapshot=net.minecraftforge.common.util.BlockSnapshot.create(l.dimension(),l,at);if(!net.minecraftforge.event.ForgeEventFactory.onBlockPlace(p,snapshot,b.getDirection()))l.setBlockAndUpdate(at,fire);}}}
            }}
        }
        var at=hit.getLocation();l.sendParticles(ParticleTypes.ENCHANT,at.x,at.y,at.z,15,.2,.2,.2,.1);
    }
}
