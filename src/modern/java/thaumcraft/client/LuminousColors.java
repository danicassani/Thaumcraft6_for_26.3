package thaumcraft.client;

import java.util.List;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.content.LuminousContent;

@Mod.EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class LuminousColors {
    private LuminousColors() {}
    @SubscribeEvent public static void colors(RegisterColorHandlersEvent.Block event) {
        LuminousContent.BLOCKS.forEach((name, block) -> event.register(List.of(BlockTintSources.constant(0xFF000000 | LuminousContent.COLORS.get(name))), block.get()));
    }
}
