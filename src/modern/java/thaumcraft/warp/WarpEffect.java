package thaumcraft.warp;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import thaumcraft.taint.*;
public final class WarpEffect extends MobEffect {
 public enum Kind{HUNGER,SUN,THAUMARHIA,GAZE,BLURRED}
 public final Kind kind;
 public WarpEffect(Kind k){super(MobEffectCategory.HARMFUL,0x664477);kind=k;}
 public boolean shouldApplyEffectTickThisTick(int ticks,int amp){return kind==Kind.HUNGER||kind==Kind.THAUMARHIA&&ticks%20==0||kind==Kind.SUN&&ticks%40==0||kind==Kind.GAZE&&ticks%20==0;}
 public boolean applyEffectTick(ServerLevel l,LivingEntity e,int amp){if(thaumcraft.config.ThaumcraftConfig.wussMode.get())return true;
  if(kind==Kind.HUNGER&&e instanceof Player p)p.causeFoodExhaustion(.025F*(amp+1));
  if(kind==Kind.SUN&&l.hasChunkAt(e.blockPosition())){float light=e.getLightLevelDependentMagicValue();if(light>.5&&l.getRandom().nextFloat()*30<(light-.4)*2&&l.canSeeSky(e.blockPosition()))e.igniteForSeconds(4);else if(light<.25&&l.getRandom().nextFloat()>light*2)e.heal(1);}
  if(kind==Kind.THAUMARHIA&&l.getRandom().nextInt(15)==0&&l.hasChunkAt(e.blockPosition())&&l.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.MOB_GRIEFING)&&l.getBlockState(e.blockPosition()).isAir())TaintWorld.place(l,e.blockPosition(),e.blockPosition(),TaintContent.GOO.get().defaultBlockState(),e instanceof Player p?p:null);
  if(kind==Kind.GAZE&&e instanceof net.minecraft.server.level.ServerPlayer p)WarpEngine.gaze(p,amp);
  return true;
 }
}
