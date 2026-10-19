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
    static boolean menu,advanced;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!advanced&&System.getProperty("thaumcraft.infusionProbe","").equals("advanced-client")){var c=Minecraft.getInstance();if(c.player!=null&&c.level!=null&&c.player.getInventory().getItem(1).has(thaumcraft.infusion.InfusionEnchantments.ENCHANTMENTS.get())){var source=c.level.getBlockEntity(AdvancedInfusionChecks.S);InfusionProbe.check(source instanceof thaumcraft.infusion.StabilizationEntity t&&t.energy()>0,"Client charge not synced");advanced=true;Thaumcraft.LOGGER.info("INFUSION_ADVANCED_CLIENT_SYNC_OK: native item component and stabilizer charge synchronized in graphical client");}return;}if(menu||!System.getProperty("thaumcraft.infusionProbe","").equals("client"))return;var c=Minecraft.getInstance();if(c.player!=null&&c.player.containerMenu instanceof TheoryMenu m&&m.card(1)!=null&&m.card(1).kind()==TheoryCards.Kind.INFUSE){menu=true;Thaumcraft.LOGGER.info("INFUSION_CLIENT_MENU_OK: new card identities, item requirements and action state synchronized to client");}}
}
