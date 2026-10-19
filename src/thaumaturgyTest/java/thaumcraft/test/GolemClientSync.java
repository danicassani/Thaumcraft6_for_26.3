package thaumcraft.test;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.*;
import thaumcraft.golems.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class GolemClientSync {
    static boolean builder,edit,seal,channel;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.thaumaturgyProbe","").equals("golem-client"))return;var mc=net.minecraft.client.Minecraft.getInstance();if(mc.player==null)return;if(mc.player.containerMenu instanceof GolemBuilderMenu m){if(!builder){ThaumaturgyProbe.check(m.slots.size()==37&&m.value(4)==0,"Builder menu synchronization failed");builder=true;Thaumcraft.LOGGER.info("GOLEM_BUILDER_GUI_OK");}if(builder&&!edit&&m.value(0)==1){edit=true;Thaumcraft.LOGGER.info("GOLEM_BUILDER_EDIT_OK: native material click reached server and returned");}}
        if(mc.player.containerMenu instanceof SealMenu m){if(!seal){ThaumaturgyProbe.check(m.value(0)==7&&m.value(1)==2&&m.value(2)==5&&m.getSlot(0).getItem().is(net.minecraft.world.item.Items.DIAMOND),"Seal menu data/filter not synchronized");seal=true;Thaumcraft.LOGGER.info("GOLEM_SEAL_GUI_OK");}if(!channel&&m.value(0)==8){channel=true;Thaumcraft.LOGGER.info("GOLEM_SEAL_EDIT_OK: native channel click round trip");}}
    }
}
