package thaumcraft.taint;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
public final class TaintBottle extends ThrowableItemProjectile{
 public TaintBottle(EntityType<? extends TaintBottle> t,Level l){super(t,l);}
 protected Item getDefaultItem(){return TaintContent.BOTTLE.get();}
 protected void onHit(HitResult result){super.onHit(result);if(!(level() instanceof ServerLevel l))return;var owner=getOwner() instanceof Player p?p:null;
  if(!thaumcraft.config.ThaumcraftConfig.wussMode.get()){
   for(var living:l.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(5)))if(!(living instanceof TaintedMob)&&!(living instanceof ThaumicSlime)&&!living.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)&&!(living instanceof Player p&&(p.isCreative()||p.isSpectator()||owner!=null&&(owner.isAlliedTo(p)||!owner.canHarmPlayer(p)))))living.addEffect(new MobEffectInstance(TaintContent.DISEASE.getHolder().orElseThrow(),100));
   if(l.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.MOB_GRIEFING))for(int n=0;n<10;n++){var p=blockPosition().offset(random.nextInt(9)-4,random.nextInt(9)-4,random.nextInt(9)-4);if(!l.hasChunkAt(p)||!l.hasChunkAt(p.below()))continue;if(!l.getBlockState(p).canBeReplaced())p=p.above();if(l.hasChunkAt(p)&&l.hasChunkAt(p.below())&&l.getBlockState(p).canBeReplaced()&&l.getFluidState(p).isEmpty()&&l.getBlockState(p.below()).isFaceSturdy(l,p.below(),Direction.UP)&&random.nextBoolean())TaintWorld.place(l,p,blockPosition(),TaintContent.GOO.get().defaultBlockState(),owner);}
  }
  l.levelEvent(2002,blockPosition(),0x882288);discard();
 }
}
