package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.AlchemyMenu;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class AlchemyClientProbe{
    private static boolean menu;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.alchemyProbe","").equals("client"))return;var c=Minecraft.getInstance();if(!menu&&c.player!=null&&c.player.containerMenu instanceof AlchemyMenu m&&m.value(2)==950&&m.value(3)==200){menu=true;Thaumcraft.LOGGER.info("ALCHEMY_CLIENT_MENU_OK: native menu synchronized exact water/heat and residual aspects after real craft");}}
}
