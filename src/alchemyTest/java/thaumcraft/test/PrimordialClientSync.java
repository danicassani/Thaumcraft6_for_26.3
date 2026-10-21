package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class PrimordialClientSync {
 static boolean shown,title;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!PrimordialProbe.mode().equals("primordial-client"))return;var mc=Minecraft.getInstance();if(PrimordialProbe.ready&&mc.gui.screen() instanceof TitleScreen&&!title){title=true;Thaumcraft.LOGGER.info("PRIMORDIAL_TITLE_READY: native world saved and title active");}if(!PrimordialProbe.ready||shown||mc.level==null)return;int pearls=0;for(int i=0;i<3;i++)if(mc.level.getBlockEntity(PrimordialProbe.P.offset(-4+i*4,0,0)) instanceof thaumcraft.infusion.InfusionPedestal p&&p.item().getItem() instanceof thaumcraft.rifts.PrimordialPearl)pearls++;boolean rift=false;for(var en:mc.level.entitiesForRendering())if(en instanceof thaumcraft.rifts.FluxRiftEntity)rift=true;if(pearls==3&&rift){shown=true;Thaumcraft.LOGGER.info("PRIMORDIAL_SCREENSHOT_READY: three original pearl models and native rift synchronized");}}
}
