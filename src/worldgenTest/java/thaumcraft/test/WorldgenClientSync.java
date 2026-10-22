package thaumcraft.test;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.content.SimpleContent;

@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class WorldgenClientSync {
    static int ticks;static boolean shown;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();var p=WorldgenProbe.naturalSample;
        if (shown||!WorldgenProbe.ready||mc.level==null||mc.player==null||mc.gui.screen()!=null||p==null) return;
        var state=mc.level.getBlockState(p);
        if (!state.is(SimpleContent.BLOCKS.get("log_greatwood").get())&&!state.is(SimpleContent.BLOCKS.get("log_silverwood").get())) return;
        if (++ticks<100) return;
        shown=true;
        Thaumcraft.LOGGER.info("WORLDGEN_SCREENSHOT_READY: natural tree synchronized to client, biome {}",mc.level.getBiome(p).unwrapKey());
    }
}
