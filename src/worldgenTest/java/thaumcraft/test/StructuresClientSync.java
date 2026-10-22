package thaumcraft.test;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.treasure.TreasureBlock;

@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class StructuresClientSync {
    static boolean shown;static int ticks;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();var p=StructuresProbe.naturalOrigin;
        if (shown||!StructuresProbe.ready||p==null||mc.level==null||mc.player==null||mc.gui.screen()!=null) return;
        if (!(mc.level.getBlockState(p.offset(9,1,7)).getBlock() instanceof TreasureBlock)||!(mc.level.getBlockState(p.offset(9,1,11)).getBlock() instanceof TreasureBlock)) return;
        if (++ticks<80) return;shown=true;
        Thaumcraft.LOGGER.info("STRUCTURES_SCREENSHOT_READY: natural mound chamber and two synchronized original treasure models");
    }
}
