package thaumcraft.taint;
import java.util.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
/** Milk now clears effects through data components. Preserve TC6's uncurable exhaustion. */
public final class TaintMilk{
 private record Snapshot(List<MobEffectInstance> effects){}
 private static final Map<LivingEntity,Snapshot> DRINKING=new WeakHashMap<>();
 private static void remember(LivingEntityUseItemEvent e){if(e.getEntity().level().isClientSide())return;var entity=e.getEntity();if(!e.getItem().is(Items.MILK_BUCKET)){DRINKING.remove(entity);return;}DRINKING.put(entity,new Snapshot(entity.getActiveEffects().stream().filter(effect->(effect.getEffect().value() instanceof TaintEffect t&&(t.kind==TaintEffect.Kind.EXHAUST||t.kind==TaintEffect.Kind.INFECTIOUS))||(effect.getEffect().value() instanceof thaumcraft.warp.WarpEffect w&&w.kind!=thaumcraft.warp.WarpEffect.Kind.BLURRED)).toList()));}
 public static void register(){LivingEntityUseItemEvent.Start.BUS.addListener(TaintMilk::remember);LivingEntityUseItemEvent.Tick.BUS.addListener(TaintMilk::remember);LivingEntityUseItemEvent.Stop.BUS.addListener(e->{if(!e.getEntity().level().isClientSide())DRINKING.remove(e.getEntity());});LivingEntityUseItemEvent.Finish.BUS.addListener(e->{if(e.getEntity().level().isClientSide())return;var saved=DRINKING.remove(e.getEntity());if(saved==null||!e.getItem().is(Items.MILK_BUCKET)||!e.getEntity().isAlive())return;for(var effect:saved.effects())if(effect.isInfiniteDuration()||effect.getDuration()>0)e.getEntity().addEffect(effect);});}
}
