package thaumcraft.client;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import thaumcraft.bore.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class BoreScreen extends AbstractContainerScreen<BoreMenu>{
 public BoreScreen(BoreMenu m,Inventory i,Component t){super(m,i,t,176,166);inventoryLabelY=72;}
 @SubscribeEvent public static void screens(FMLClientSetupEvent e){e.enqueueWork(()->net.minecraft.client.gui.screens.MenuScreens.register(BoreContent.MENU.get(),BoreScreen::new));}
 protected void extractLabels(GuiGraphicsExtractor g,int x,int y){}
 public void extractBackground(GuiGraphicsExtractor g,int x,int y,float p){super.extractBackground(g,x,y,p);g.blit(RenderPipelines.GUI_TEXTURED,Identifier.parse("thaumcraft:textures/gui/gui_arcanebore.png"),leftPos,topPos,0,0,imageWidth,imageHeight,256,256);g.blit(RenderPipelines.GUI_TEXTURED,Identifier.parse("thaumcraft:textures/gui/gui_arcanebore.png"),leftPos+68,topPos+59,192,48,39*menu.value(3)/100,6,256,256);g.pose().pushMatrix();g.pose().translate(leftPos+112,topPos+20);g.pose().scale(.5F,.5F);g.text(font,Component.translatable("bore.thaumcraft.size",1+menu.value(1)*2,menu.value(2)),0,0,0xffc8b5df,false);g.text(font,Component.translatable("bore.thaumcraft.vis",menu.value(0)/100F),0,12,0xffc8b5df,false);g.pose().popMatrix();}
}
