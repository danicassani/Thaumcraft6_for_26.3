package thaumcraft.client;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import thaumcraft.mirrors.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class HandMirrorScreen extends AbstractContainerScreen<HandMirrorMenu> {
 public HandMirrorScreen(HandMirrorMenu menu,Inventory inv,Component title){super(menu,inv,title,176,166);inventoryLabelY=72;}
 @SubscribeEvent public static void setup(FMLClientSetupEvent e){e.enqueueWork(()->MenuScreens.register(MirrorContent.MENU.get(),HandMirrorScreen::new));}
 public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float partial){super.extractBackground(g,mouseX,mouseY,partial);g.fill(leftPos,topPos,leftPos+176,topPos+166,0xffa59bb5);g.fill(leftPos+79,topPos+27,leftPos+97,topPos+45,0xff332c45);for(var slot:menu.slots)g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xff51475e);g.text(font,Component.translatable("mirror.thaumcraft.deposit"),leftPos+8,topPos+54,0xff302536,false);}
}
