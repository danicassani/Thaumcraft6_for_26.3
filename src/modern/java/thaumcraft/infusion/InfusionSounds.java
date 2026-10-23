package thaumcraft.infusion;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.*;
public final class InfusionSounds {
 private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,"thaumcraft");
 public static final RegistryObject<SoundEvent> START=sound("infusion_start"),FAIL=sound("infusion_fail"),COMPLETE=sound("infusion_complete");
 private static RegistryObject<SoundEvent> sound(String name){return SOUNDS.register(name,()->SoundEvent.createVariableRangeEvent(thaumcraft.content.SimpleContent.id(name)));}
 public static void register(net.minecraftforge.eventbus.api.bus.BusGroup bus){SOUNDS.register(bus);}
}
