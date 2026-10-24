package thaumcraft.eldritch;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.*;
import thaumcraft.content.SimpleContent;
public final class EldritchSounds {
 private static final DeferredRegister<SoundEvent> EVENTS=DeferredRegister.create(Registries.SOUND_EVENT,"thaumcraft");
 public static final RegistryObject<SoundEvent> IDLE=sound("egidle"),ATTACK=sound("egattack"),DEATH=sound("egdeath"),SCREECH=sound("egscreech"),CRAB=sound("crabtalk"),CLAW=sound("crabclaw"),CRAB_DEATH=sound("crabdeath"),MONOLITH=sound("monolith");
 private static RegistryObject<SoundEvent> sound(String n){return EVENTS.register(n,()->SoundEvent.createVariableRangeEvent(SimpleContent.id(n)));}
 public static void register(net.minecraftforge.eventbus.api.bus.BusGroup b){EVENTS.register(b);}
}
