package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import thaumcraft.Thaumcraft;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class HungryChestClientSync {
 static boolean shown,title;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!HungryChestProbe.mode().equals("hungry-client")||!HungryChestProbe.ready)return;var mc=Minecraft.getInstance();if(!shown&&mc.player!=null&&mc.player.containerMenu instanceof ChestMenu menu&&menu.getRowCount()==3&&menu.getSlot(0).getItem().is(net.minecraft.world.item.Items.DIAMOND)&&menu.getSlot(0).getItem().getCount()==12){shown=true;Thaumcraft.LOGGER.info("HUNGRY_SCREENSHOT_READY: real native menu opened, twelve absorbed diamonds synchronized in 27-slot chest");}if(shown&&!title&&mc.gui.screen() instanceof net.minecraft.client.gui.screens.TitleScreen){title=true;Thaumcraft.LOGGER.info("HUNGRY_TITLE_READY: native client saved and closed integrated server");}}
}
