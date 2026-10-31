package thaumcraft.client;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class AquaticClientProof {
 static boolean plant,phantom,menu;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.bookProbe","").equals("aquatic-client"))return;var mc=Minecraft.getInstance();if(plant&&phantom&&mc.level==null&&mc.gui.screen() instanceof net.minecraft.client.gui.screens.TitleScreen&&!menu){menu=true;thaumcraft.Thaumcraft.LOGGER.info("AQUATIC81_CLIENT_MENU_READY");}if(mc.player==null||mc.level==null)return;var view=ThaumometerSight.visible();if(view==null||!view.known()||view.aspects().isEmpty())return;
 if(!plant&&view.identity().equals("item:minecraft:seagrass")){if(!mc.player.isInWater())throw new AssertionError("Plant fixture not underwater");plant=true;thaumcraft.Thaumcraft.LOGGER.info("AQUATIC81_PREVIEW_OK: plant");}
 if(!phantom&&view.identity().equals("entity:minecraft:phantom")){phantom=true;thaumcraft.Thaumcraft.LOGGER.info("AQUATIC81_PREVIEW_OK: phantom");}
 }
}
