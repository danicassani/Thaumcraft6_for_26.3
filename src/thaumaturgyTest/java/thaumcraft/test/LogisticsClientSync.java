package thaumcraft.test;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.*;
import thaumcraft.golems.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class LogisticsClientSync {
    static boolean opened,selected,amount,exact;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.thaumaturgyProbe","").equals("logistics-client"))return;var mc=net.minecraft.client.Minecraft.getInstance();if(mc.player==null||!(mc.player.containerMenu instanceof SealMenu m))return;
        if(!opened){ThaumaturgyProbe.check(m.kind()==SealKind.STOCK&&m.slots.size()==45&&m.getSlot(8).getItem().getHoverName().getString().equals("Requested parcel")&&m.value(14)==23,"Nine samples/quantities did not reach client");opened=true;Thaumcraft.LOGGER.info("LOGISTICS_GUI_OK: nine locked samples and all stock quantities synchronized");}
        if(!selected&&m.value(15)==8){selected=true;Thaumcraft.LOGGER.info("LOGISTICS_GUI_SELECT_OK");}if(!amount&&selected&&m.value(20)==24){amount=true;Thaumcraft.LOGGER.info("LOGISTICS_GUI_AMOUNT_OK");}if(!exact&&amount&&m.value(16)==0){exact=true;Thaumcraft.LOGGER.info("LOGISTICS_GUI_EXACT_OK");}
    }
}
