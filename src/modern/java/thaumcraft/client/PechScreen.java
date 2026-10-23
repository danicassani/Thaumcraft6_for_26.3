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
import thaumcraft.pech.*;
public final class PechScreen extends AbstractContainerScreen<PechMenu> {
    private Button roll;
    public PechScreen(PechMenu m,Inventory inv,Component title){super(m,inv,title,175,232);inventoryLabelY=72;}
    protected void init(){super.init();roll=Button.builder(Component.translatable("pech.thaumcraft.roll"),b->{if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0);}).bounds(leftPos+59,topPos+24,42,24).build();addRenderableWidget(roll);}
    public void extractBackground(GuiGraphicsExtractor g,int x,int y,float partial){super.extractBackground(g,x,y,partial);roll.active=menu.ready();g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,net.minecraft.resources.Identifier.parse("thaumcraft:textures/gui/gui_pech.png"),leftPos,topPos,0,0,175,232,256,256);g.text(font,Component.translatable("pech.thaumcraft.offering"),leftPos+8,topPos+54,0xff30271e,false);}
    @Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
    public static final class Registration{@SubscribeEvent public static void setup(FMLClientSetupEvent e){e.enqueueWork(()->MenuScreens.register(PechContent.MENU.get(),PechScreen::new));}}
}
