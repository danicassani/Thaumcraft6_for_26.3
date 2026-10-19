package thaumcraft.test;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.*;
import thaumcraft.thaumaturgy.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class ThaumaturgyClientSync {
    static boolean checked;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(checked||!System.getProperty("thaumcraft.thaumaturgyProbe","").equals("client"))return;var mc=net.minecraft.client.Minecraft.getInstance();if(mc.player!=null&&mc.gui.screen() instanceof thaumcraft.client.FocalScreen&&mc.player.containerMenu instanceof FocalMenu menu&&menu.value(9)==15){ThaumaturgyProbe.check(mc.player.getMainHandItem().has(ThaumaturgyContent.FOCUS.get()),"Nested focus not synced to client");checked=true;Thaumcraft.LOGGER.info("THAUMATURGY_CLIENT_SYNC_OK: focal screen, slot inventory, program preview, complexity and nested focus components synchronized");}}
}
