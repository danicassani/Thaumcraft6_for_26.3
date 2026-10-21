package thaumcraft.warp;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.*;
import thaumcraft.content.SimpleContent;
public final class WarpContent {
 private static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(Registries.MOB_EFFECT,"thaumcraft");
 private static RegistryObject<MobEffect> effect(String id,WarpEffect.Kind kind){return EFFECTS.register(id,()->new WarpEffect(kind));}
 public static final RegistryObject<MobEffect> HUNGER=effect("unnatural_hunger",WarpEffect.Kind.HUNGER),SUN=effect("sun_scorned",WarpEffect.Kind.SUN),THAUMARHIA=effect("thaumarhia",WarpEffect.Kind.THAUMARHIA),GAZE=effect("death_gaze",WarpEffect.Kind.GAZE),BLURRED=effect("blurred_vision",WarpEffect.Kind.BLURRED);
 public static void register(net.minecraftforge.eventbus.api.bus.BusGroup b){SimpleContent.item("sanity_soap",SanitySoap::new);EFFECTS.register(b);WarpEngine.register();WarpCreatures.register(b);}
}
