package thaumcraft.test;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.*;
import thaumcraft.thaumaturgy.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class DeepDevicesClientSync {
    static boolean sync,grandchild,removed,added,bag;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.thaumaturgyProbe","").equals("deep-client"))return;var mc=net.minecraft.client.Minecraft.getInstance();if(mc.player==null)return;
        if(mc.gui.screen() instanceof thaumcraft.client.FocalScreen&&mc.player.containerMenu instanceof FocalMenu m){if(!sync){ThaumaturgyProbe.check(DeepDevicesProbe.graph().equals(m.getSlot(0).getItem().get(ThaumaturgyContent.PROGRAM.get())),"Deep graph component did not reach client");sync=true;Thaumcraft.LOGGER.info("DEEP_MENU_SYNC_OK");}if(!grandchild&&m.value(25)==2&&m.value(3)==0){grandchild=true;Thaumcraft.LOGGER.info("DEEP_GUI_GRANDCHILD_OK");}if(grandchild&&!removed&&m.value(25)==0&&m.value(8)==1){removed=true;Thaumcraft.LOGGER.info("DEEP_GUI_REMOVE_OK");}if(removed&&!added&&m.value(25)==1&&m.value(8)==2){added=true;Thaumcraft.LOGGER.info("DEEP_GUI_ADD_OK");}}
        if(!bag&&mc.gui.screen() instanceof thaumcraft.client.PouchScreen&&mc.player.containerMenu instanceof PouchMenu m){ThaumaturgyProbe.check(m.slots.size()==54,"Bag slot count changed");bag=true;Thaumcraft.LOGGER.info("DEEP_POUCH_SCREEN_OK: 18 focus slots and player inventory synchronized");}
    }
}
