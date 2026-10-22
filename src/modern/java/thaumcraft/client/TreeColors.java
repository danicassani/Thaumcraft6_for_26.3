package thaumcraft.client;

import java.util.List;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.trees.TreeContent;

/** Original greatwood biome tint; silverwood keeps its own pale texture colors. */
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class TreeColors {
    @SubscribeEvent public static void colors(RegisterColorHandlersEvent.Block event) {
        event.register(List.of(BlockTintSources.foliage()),TreeContent.BLOCKS.get("leaves_greatwood").get());
        event.register(List.of(BlockTintSources.constant(-1)),TreeContent.BLOCKS.get("leaves_silverwood").get());
    }
}
