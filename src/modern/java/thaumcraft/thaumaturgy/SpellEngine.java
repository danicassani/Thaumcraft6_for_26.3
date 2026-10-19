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
    public static HitResult aim(ServerPlayer p,double range){return aim(p,p.getLookAngle(),range);}
    public static boolean loadedLine(net.minecraft.world.level.Level l,Vec3 from,Vec3 to){int x0=BlockPos.containing(from).getX()>>4,x1=BlockPos.containing(to).getX()>>4,z0=BlockPos.containing(from).getZ()>>4,z1=BlockPos.containing(to).getZ()>>4;for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)if(!l.hasChunk(x,z))return false;return true;}
    public static HitResult aim(ServerPlayer p,Vec3 direction,double range){var start=p.getEyePosition();var end=start.add(direction.scale(range));if(!loadedLine(p.level(),start,end))return BlockHitResult.miss(start,Direction.UP,BlockPos.containing(start));HitResult hit=p.level().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));double distance=start.distanceToSqr(hit.getLocation());for(var entity:p.level().getEntities(p,p.getBoundingBox().expandTowards(direction.scale(range)).inflate(1),e->e instanceof LivingEntity&&e.isAlive()&&!e.isSpectator())){var intersection=entity.getBoundingBox().inflate(.15).clip(start,end);if(intersection.isPresent()&&start.distanceToSqr(intersection.get())<distance){distance=start.distanceToSqr(intersection.get());hit=new EntityHitResult(entity,intersection.get());}}return hit;}
    public static boolean friendly(ServerPlayer p,LivingEntity target){return target==p||target.isAlliedTo(p)||target instanceof TamableAnimal animal&&p.getUUID().equals(animal.getOwnerReference()==null?null:animal.getOwnerReference().getUUID());}
    public static void cast(ServerPlayer p,FocusProgram program){if(!program.valid())return;var direction=p.getLookAngle();switch(program.modifier()){
        case "scatter"->{for(int i=0;i<program.forks();i++){var random=p.getRandom();var scattered=direction.add(random.nextGaussian()*.0075*program.cone(),random.nextGaussian()*.0075*program.cone(),random.nextGaussian()*.0075*program.cone()).normalize();deliver(p,program.path(0),scattered,2F/program.forks());}}
        case "split_trajectory"->{deliver(p,program.path(0),direction,.75F);deliver(p,program.path(1),direction,.75F);}
        case "split_target"->deliver(p,program,direction,.75F);
        default->deliver(p,program,direction,1);
    }}
    private static void deliver(ServerPlayer p,FocusProgram program,Vec3 direction,float strength){
        if(program.medium().equals("plan")){var from=p.getEyePosition();var to=from.add(direction.scale(16));if(!loadedLine(p.level(),from,to))return;var hit=p.level().clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));if(hit.getType()==HitResult.Type.MISS)return;for(var q:PlanSelection.select(p.level(),hit,program.plan()))apply(p,program,new BlockHitResult(Vec3.atCenterOf(q),hit.getDirection(),q,false),strength);return;}
        if(program.medium().equals("projectile")){var shot=new FocusProjectile(ThaumaturgyContent.PROJECTILE.get(),p.level());shot.setOwner(p);shot.setPos(p.getX(),p.getEyeY()-.1,p.getZ());var stack=new ItemStack(thaumcraft.content.SimpleContent.ITEMS.get("focus_1").get());stack.set(ThaumaturgyContent.PROGRAM.get(),program);shot.setItem(stack);shot.strength(strength);shot.shoot(direction.x,direction.y,direction.z,1.5F,0);p.level().addFreshEntity(shot);return;}
        if(!loadedLine(p.level(),p.getEyePosition(),p.getEyePosition().add(direction.scale(program.medium().equals("touch")?4:program.medium().equals("bolt")?32:16))))return;
        var hit=aim(p,direction,program.medium().equals("touch")?4:program.medium().equals("bolt")?32:16);
        if(program.medium().equals("cloud")||program.medium().equals("mine")){
            long active=0;for(var entity:p.level().getAllEntities())if(entity instanceof FocusFieldEntity field&&p.getUUID().equals(field.ownerId())&&!field.isRemoved()&&field.expires()>p.level().getGameTime())active++;if(active>=16)return;
            BlockPos support=null;Vec3 at=hit.getType()==HitResult.Type.MISS?p.getEyePosition().add(direction.scale(3)):hit.getLocation();
            if(hit instanceof BlockHitResult b&&hit.getType()!=HitResult.Type.MISS){support=b.getBlockPos();if(!p.level().hasChunkAt(support)||!p.level().mayInteract(p,support)||!p.mayUseItemAt(support,b.getDirection(),p.getMainHandItem()))return;at=at.add(new Vec3(b.getDirection().getStepX(),b.getDirection().getStepY(),b.getDirection().getStepZ()).scale(.05));}
            if(program.medium().equals("mine")&&support==null)return;if(!p.level().hasChunkAt(BlockPos.containing(at)))return;
            var field=new FocusFieldEntity(ThaumaturgyContent.FIELD.get(),p.level());field.configure(p,program,at,strength,program.medium().equals("mine")?support:null);p.level().addFreshEntity(field);return;
        }
        apply(p,program,hit,strength);var from=p.getEyePosition();var delta=hit.getLocation().subtract(from);int steps=Math.min(128,(int)(delta.length()*4));for(int i=1;i<=steps;i++){var v=from.add(delta.scale((double)i/Math.max(1,steps)));p.level().sendParticles(ParticleTypes.ENCHANT,v.x,v.y,v.z,1,0,0,0,0);}
    }
    public static void apply(ServerPlayer p,FocusProgram program,HitResult hit){
        apply(p,program,hit,1);
    }
    public static void apply(ServerPlayer p,FocusProgram program,HitResult hit,float strength){
        if(!program.valid()||!Float.isFinite(strength)||strength<=0||strength>1)return;
        var l=p.level();if(hit.getType()==HitResult.Type.MISS)return;LivingEntity target=hit instanceof EntityHitResult e&&e.getEntity() instanceof LivingEntity living?living:null;
        boolean protectedTarget=target!=null&&(friendly(p,target)||target instanceof net.minecraft.world.entity.player.Player other&&(!l.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.PVP)||!p.canHarmPlayer(other)||other.getAbilities().instabuild));
        for(var e:program.impactEffects()){
            float power=e.power()*strength;if(target!=null){if(protectedTarget&&!e.kind().equals("heal"))continue;switch(e.kind()){
                case "heal"->{if(target.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD))target.hurtServer(l,l.damageSources().indirectMagic(p,p),power*2);else target.heal(power*2);}
                case "fire"->{target.hurtServer(l,l.damageSources().indirectMagic(p,p),power*2);target.igniteForSeconds(e.duration()*2);}
                case "frost"->{target.hurtServer(l,l.damageSources().indirectMagic(p,p),power*2);target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,40*e.duration(),(int)power/2));}
                case "air"->{target.hurtServer(l,l.damageSources().indirectMagic(p,p),power);target.push(p.getLookAngle().x*power*.3,.2+power*.1,p.getLookAngle().z*power*.3);}
                case "earth","flux"->target.hurtServer(l,l.damageSources().indirectMagic(p,p),power*3);
                case "curse"->{target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,100*e.duration(),(int)power/2));target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,100*e.duration(),(int)power/2));}
            }}else if(hit instanceof BlockHitResult b){var q=b.getBlockPos();if(!l.hasChunkAt(q)||!l.mayInteract(p,q)||!p.mayUseItemAt(q,b.getDirection(),p.getMainHandItem()))continue;var state=l.getBlockState(q);switch(e.kind()){
                case "break"->{if(l.getBlockEntity(q)==null&&state.getDestroySpeed(l,q)>=0&&state.getDestroySpeed(l,q)<=power*4)p.gameMode.destroyBlock(q);}
                case "exchange"->{var replacement=p.getOffhandItem();if(!(replacement.getItem() instanceof BlockItem block)||l.getBlockEntity(q)!=null||state.getDestroySpeed(l,q)<0||state.getDestroySpeed(l,q)>power*4)continue;var intended=block.getBlock().defaultBlockState();if(intended.hasBlockEntity()||!intended.canSurvive(l,q)||!l.isUnobstructed(intended,q,net.minecraft.world.phys.shapes.CollisionContext.empty()))continue;if(p.gameMode.destroyBlock(q)&&l.getBlockState(q).isAir()){replacement.useOn(new net.minecraft.world.item.context.UseOnContext(p,net.minecraft.world.InteractionHand.OFF_HAND,new BlockHitResult(Vec3.atCenterOf(q),Direction.UP,q,false)));}}
                case "fire"->{var at=q.relative(b.getDirection());if(l.hasChunkAt(at)&&l.mayInteract(p,at)&&p.mayUseItemAt(at,b.getDirection(),p.getMainHandItem())&&l.isEmptyBlock(at)){var fire=BaseFireBlock.getState(l,at);if(fire.canSurvive(l,at)){var snapshot=net.minecraftforge.common.util.BlockSnapshot.create(l.dimension(),l,at);if(!net.minecraftforge.event.ForgeEventFactory.onBlockPlace(p,snapshot,b.getDirection()))l.setBlockAndUpdate(at,fire);}}}
            }}
        }
        var at=hit.getLocation();l.sendParticles(ParticleTypes.ENCHANT,at.x,at.y,at.z,15,.2,.2,.2,.1);
    }
}
