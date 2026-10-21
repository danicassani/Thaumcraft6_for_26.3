package thaumcraft.taint;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
public final class TaintEffect extends MobEffect{
 public enum Kind{DISEASE,EXHAUST,INFECTIOUS,WARD}
 public final Kind kind;
 public TaintEffect(Kind k){super(k==Kind.WARD?MobEffectCategory.BENEFICIAL:MobEffectCategory.HARMFUL,k==Kind.WARD?0xeeeeff:0x9933aa);kind=k;}
 public boolean shouldApplyEffectTickThisTick(int ticks,int amp){return kind==Kind.DISEASE?ticks%TaintRules.diseaseInterval(amp)==0:kind==Kind.INFECTIOUS&&ticks%40==0;}
 public boolean applyEffectTick(ServerLevel l,LivingEntity e,int amp){if(kind==Kind.DISEASE){if(e instanceof TaintedMob||e instanceof ThaumicSlime)e.heal(1);else if(!e.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD))e.hurtServer(l,TaintContent.damage(l,"taint"),1);}else if(kind==Kind.INFECTIOUS)for(var other:l.getEntitiesOfClass(LivingEntity.class,e.getBoundingBox().inflate(4)))if(other!=e&&!(other instanceof Player p&&(p.isCreative()||p.isSpectator()||e instanceof Player source&&(source.isAlliedTo(p)||!source.canHarmPlayer(p))))&&!other.hasEffect(TaintContent.INFECTIOUS.getHolder().orElseThrow()))other.addEffect(new MobEffectInstance((amp>0?TaintContent.INFECTIOUS:TaintContent.EXHAUST).getHolder().orElseThrow(),6000,Math.max(0,amp-1)));return true;}
}
