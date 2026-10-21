package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class WarpClientSync {
 static boolean shown,title,tuned;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!WarpProbe.mode().equals("warp-client"))return;var mc=Minecraft.getInstance();if(!tuned){tuned=true;mc.options.renderDistance().set(8);mc.options.simulationDistance().set(8);}if(WarpProbe.ready&&mc.gui.screen() instanceof TitleScreen&&!title){title=true;Thaumcraft.LOGGER.info("WARP_TITLE_READY: native world saved and title active");}if(!WarpProbe.ready||shown||mc.level==null)return;int mobs=0;for(var en:mc.level.entitiesForRendering())if(en instanceof thaumcraft.warp.WarpMonster||en instanceof thaumcraft.warp.MindSpider)mobs++;if(mobs>=5){shown=true;Thaumcraft.LOGGER.info("WARP_SCREENSHOT_READY: native warp entities synchronized; private spider lifetime is verified in the dedicated fixture");}}
}
