package thaumcraft.aura;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;

@Mod.EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class AuraClientProbe {
    private static int samples;
    private static AuraNetwork.Snapshot previous;
    private AuraClientProbe() {}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post event) {
        var client = Minecraft.getInstance(); var state = AuraNetwork.clientSnapshot();
        if (state == null || client.level == null || client.player == null || samples >= 3) return;
        if (!state.dimension().equals(client.level.dimension().identifier()) || state.chunk() != client.player.chunkPosition().pack()) throw new AssertionError("Stale client aura snapshot");
        if (state == previous) return;
        previous = state;
        if (++samples == 3) Thaumcraft.LOGGER.info("AURA_CLIENT_SYNC_OK: dimension={}, chunk={}, base={}, vis={}, flux={}", state.dimension(), state.chunk(), state.cell().base(), state.cell().vis(), state.cell().flux());
    }
}
