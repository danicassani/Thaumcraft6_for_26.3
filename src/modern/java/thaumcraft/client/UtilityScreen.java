package thaumcraft.client;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import thaumcraft.utility.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class UtilityScreen extends AbstractContainerScreen<UtilityMenu> {
 private Button mode;
 public UtilityScreen(UtilityMenu m,Inventory inv,Component title){super(m,inv,title,176,166);inventoryLabelY=72;}
 @SubscribeEvent public static void setup(FMLClientSetupEvent e){e.enqueueWork(()->MenuScreens.register(UtilityContent.MENU.get(),UtilityScreen::new));}
 protected void init(){super.init();mode=Button.builder(Component.translatable("utility.thaumcraft.mode"),b->{if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0);}).bounds(leftPos+108,topPos+22,60,20).build();addRenderableWidget(mode);}
 public void extractBackground(GuiGraphicsExtractor g,int x,int y,float partial){super.extractBackground(g,x,y,partial);mode.visible=menu.value(0)==0;mode.active=menu.value(1)==1;g.fill(leftPos,topPos,leftPos+176,topPos+166,0xffb9b1a2);for(var s:menu.slots)g.fill(leftPos+s.x-1,topPos+s.y-1,leftPos+s.x+17,topPos+s.y+17,0xff554a48);var text=menu.value(0)==0?Component.translatable(menu.value(3)==1?"utility.thaumcraft.mixing":"utility.thaumcraft.pouring",menu.value(2)):Component.translatable("utility.thaumcraft.sprayer",menu.value(2),menu.value(3),menu.value(4));g.text(font,text,leftPos+8,topPos+54,0xff302536,false);}
}
