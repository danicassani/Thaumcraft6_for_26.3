package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class RiftEventsClientSync {
 static boolean shown,title;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!RiftEventsProbe.mode().equals("rift-events-client"))return;var mc=Minecraft.getInstance();if(RiftEventsProbe.ready&&mc.gui.screen() instanceof TitleScreen&&!title){title=true;Thaumcraft.LOGGER.info("RIFT_EVENTS_TITLE_READY: world saved and native title active");}if(!RiftEventsProbe.ready||shown||mc.level==null)return;int wisps=0,clouds=0;for(var en:mc.level.entitiesForRendering()){if(en instanceof thaumcraft.rifts.Wisp)wisps++;if(en instanceof thaumcraft.rifts.RiftCloud)clouds++;}if(wisps>=4&&clouds>0){shown=true;Thaumcraft.LOGGER.info("RIFT_EVENTS_SCREENSHOT_READY: native wisp and environmental cloud renderers synchronized");}}
}
