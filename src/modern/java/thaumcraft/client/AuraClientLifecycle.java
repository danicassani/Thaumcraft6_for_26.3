package thaumcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.aura.AuraNetwork;

@Mod.EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class AuraClientLifecycle {
    private AuraClientLifecycle() {}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { AuraNetwork.clearClient(); }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post event) {
        var minecraft = Minecraft.getInstance(); var snapshot = AuraNetwork.clientSnapshot();
        if (snapshot != null && (minecraft.level == null || minecraft.player == null ||
            !minecraft.level.dimension().identifier().equals(snapshot.dimension()) || minecraft.player.chunkPosition().pack() != snapshot.chunk()))
            AuraNetwork.clearClient();
    }
}
