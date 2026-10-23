package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.pech.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class PechClientSync {
    static int ticks;static boolean world,menu;static String screen="";
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post event){var mc=Minecraft.getInstance();if(!PechProbe.mode().equals("pech-client"))return;if(mc.level==null&&mc.gui.screen()!=null){var name=mc.gui.screen().getClass().getSimpleName();if(!name.equals(screen)){screen=name;Thaumcraft.LOGGER.info("PECH_UI_SCREEN: {}",name);}}
        if(mc.level==null||mc.player==null)return;
        if(!world&&PechProbe.readyWorld&&mc.gui.screen()==null&&++ticks>100){var ids=new java.util.HashSet<java.util.UUID>();for(var entity:mc.level.entitiesForRendering())if(entity instanceof Pech p&&PechProbe.viewIds.contains(p.getUUID()))ids.add(p.getUUID());if(ids.size()==3){world=true;Thaumcraft.LOGGER.info("PECH_WORLD_SCREENSHOT_READY: all three variant entities synchronized in native forest");}}
        if(!menu&&PechProbe.tradeDone&&mc.gui.screen() instanceof thaumcraft.client.PechScreen s&&s.getMenu().slots.get(0).getItem().getCount()==7){menu=true;Thaumcraft.LOGGER.info("PECH_MENU_SCREENSHOT_READY: actual client trade, seven remaining offerings, synchronized rewards and native inventory screen");}
    }
}
