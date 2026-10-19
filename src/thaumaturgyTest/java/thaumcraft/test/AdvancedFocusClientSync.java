package thaumcraft.test;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.*;
import thaumcraft.thaumaturgy.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class AdvancedFocusClientSync {
    static boolean done;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(done||!System.getProperty("thaumcraft.thaumaturgyProbe","").equals("advanced-client"))return;var mc=net.minecraft.client.Minecraft.getInstance();if(mc.player!=null&&mc.gui.screen() instanceof thaumcraft.client.FocalScreen&&mc.player.containerMenu instanceof FocalMenu menu&&menu.value(9)==50&&menu.value(11)==3){var program=menu.getSlot(0).getItem().get(ThaumaturgyContent.PROGRAM.get());ThaumaturgyProbe.check(program!=null&&program.equals(AdvancedFocusProbe.graph()),"Branched focus did not synchronize");done=true;Thaumcraft.LOGGER.info("FOCUS_ADVANCED_CLIENT_SYNC_OK: branched item component, editor controls and global complexity synchronized");}}
}
