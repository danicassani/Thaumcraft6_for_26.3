package thaumcraft.client;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.thaumaturgy.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class FocalScreen extends AbstractContainerScreen<FocalMenu> {
    private final Button[] buttons=new Button[7];
    public FocalScreen(FocalMenu menu,Inventory inv,Component title){super(menu,inv,title,232,228);inventoryLabelY=134;}
    @SubscribeEvent public static void screens(FMLClientSetupEvent e){e.enqueueWork(()->net.minecraft.client.gui.screens.MenuScreens.register(ThaumaturgyContent.MENU.get(),FocalScreen::new));}
    @SubscribeEvent public static void renderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(ThaumaturgyContent.PROJECTILE.get(),net.minecraft.client.renderer.entity.ThrownItemRenderer::new);}
    protected void init(){super.init();String[] names={"craft","medium","effect","power","duration","add","remove"};for(int i=0;i<7;i++){final int action=i;int x=i==0?8:48+(i-1)%2*90;int y=i==0?88:22+(i-1)/2*22;buttons[i]=addRenderableWidget(Button.builder(Component.translatable("focal.thaumcraft."+names[i]),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,action)).bounds(leftPos+x,topPos+y,i==0?34:86,20).build());}}
    public void containerTick(){super.containerTick();for(var b:buttons)b.active=!menu.working();buttons[0].active&=menu.value(9)>0&&menu.value(7)<=menu.value(9);buttons[1].setMessage(Component.translatable("focus.thaumcraft."+FocusProgram.MEDIA.get(Math.clamp(menu.value(3),0,2))));buttons[2].setMessage(Component.translatable("focus.thaumcraft."+FocusProgram.EFFECTS.get(Math.clamp(menu.value(4),0,8))));buttons[3].setMessage(Component.translatable("focal.thaumcraft.power").append(": "+menu.value(5)));buttons[4].setMessage(Component.translatable("focal.thaumcraft.duration").append(": "+menu.value(6)));}
    public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){super.extractBackground(g,mx,my,partial);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFFB9AFCC);g.fill(leftPos+4,topPos+18,leftPos+228,topPos+132,0xFF302840);for(var slot:menu.slots)g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xFF17141F);g.text(font,Component.translatable("focal.thaumcraft.complexity",menu.value(7),menu.value(9),menu.value(8)),leftPos+48,topPos+90,menu.value(7)>menu.value(9)?0xFFFF8888:0xFFFFFFFF,false);if(menu.working())g.text(font,Component.translatable("focal.thaumcraft.progress",menu.value(2)-menu.value(1),menu.value(2)),leftPos+48,topPos+101,0xFFFFFFFF,false);}
}
