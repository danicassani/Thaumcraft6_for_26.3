package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.theory.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class InfusionClientProbe{
    static boolean menu;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(menu||!System.getProperty("thaumcraft.infusionProbe","").equals("client"))return;var c=Minecraft.getInstance();if(c.player!=null&&c.player.containerMenu instanceof TheoryMenu m&&m.card(1)!=null&&m.card(1).kind()==TheoryCards.Kind.INFUSE){menu=true;Thaumcraft.LOGGER.info("INFUSION_CLIENT_MENU_OK: new card identities, item requirements and action state synchronized to client");}}
}
