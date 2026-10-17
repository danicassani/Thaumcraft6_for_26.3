package thaumcraft.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import thaumcraft.Thaumcraft;

/** Common code never references this physical-client subscriber. */
@Mod.EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientLifecycle {
    private ClientLifecycle() {}
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        Thaumcraft.LOGGER.info("Thaumcraft client infrastructure initialized");
    }
}
