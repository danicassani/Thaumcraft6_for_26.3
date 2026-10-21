package thaumcraft.client;
import net.minecraft.client.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.equipment.AccessoryMenuAccess;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class AccessoryInput {
 private static final KeyMapping OPEN=new KeyMapping("key.thaumcraft.accessories",com.mojang.blaze3d.platform.InputConstants.KEY_B,KeyMapping.Category.INVENTORY);
 @SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){e.register(OPEN);}
 @SubscribeEvent public static void setup(FMLClientSetupEvent e){e.enqueueWork(()->net.minecraft.client.gui.screens.MenuScreens.register(AccessoryMenuAccess.MENU.get(),AccessoryScreen::new));}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){var mc=Minecraft.getInstance();while(OPEN.consumeClick())if(mc.player!=null&&mc.gui.screen()==null)AccessoryMenuAccess.request();}
}
