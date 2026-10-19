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
    private final Button[] buttons=new Button[14];private boolean advanced;
    public FocalScreen(FocalMenu menu,Inventory inv,Component title){super(menu,inv,title,232,228);inventoryLabelY=134;}
    @SubscribeEvent public static void screens(FMLClientSetupEvent e){e.enqueueWork(()->net.minecraft.client.gui.screens.MenuScreens.register(ThaumaturgyContent.MENU.get(),FocalScreen::new));}
    @SubscribeEvent public static void renderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(ThaumaturgyContent.PROJECTILE.get(),net.minecraft.client.renderer.entity.ThrownItemRenderer::new);e.registerEntityRenderer(ThaumaturgyContent.FIELD.get(),net.minecraft.client.renderer.entity.NoopRenderer::new);}
    protected void init(){super.init();String[] names={"craft","medium","effect","power","duration","add","remove","modifier","branch","forks","cone","radius","seconds","target"};for(int i=0;i<14;i++){final int action=i;int order=i<=6?i-1:i-7;int x=i==0||i==13?8:48+order%2*90;int y=i==0?88:i==13?44:22+order/2*22;buttons[i]=addRenderableWidget(Button.builder(Component.translatable("focal.thaumcraft."+names[i]),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,action)).bounds(leftPos+x,topPos+y,i==0||i==13?34:86,i==13?18:20).build());}addRenderableWidget(Button.builder(Component.translatable("focal.thaumcraft.page"),b->{advanced=!advanced;containerTick();}).bounds(leftPos+180,topPos+2,44,14).build());containerTick();}
    private net.minecraft.network.chat.MutableComponent text(String key){return Component.translatable("focal.thaumcraft."+key);}
    public void containerTick(){super.containerTick();for(int i=0;i<buttons.length;i++){var b=buttons[i];if(b==null)continue;b.visible=i==0||(advanced?i>=7:i<=6);b.active=!menu.working();}buttons[0].active&=menu.value(9)>0&&menu.value(7)<=menu.value(9);buttons[1].active&=!(menu.value(10)==1&&menu.value(11)==2);buttons[8].active&=menu.value(11)>=2;buttons[9].active&=menu.value(11)==1;buttons[10].active&=menu.value(11)==1;buttons[11].active&=menu.value(3)==3;buttons[12].active&=menu.value(3)==3;buttons[13].active&=menu.value(3)==4;
        buttons[1].setMessage(Component.translatable("focus.thaumcraft."+FocusProgram.MEDIA.get(Math.clamp(menu.value(3),0,FocusProgram.MEDIA.size()-1))));buttons[2].setMessage(Component.translatable("focus.thaumcraft."+FocusProgram.EFFECTS.get(Math.clamp(menu.value(4),0,FocusProgram.EFFECTS.size()-1))));buttons[3].setMessage(text("power").append(": "+menu.value(5)));buttons[4].setMessage(text("duration").append(": "+menu.value(6)));buttons[7].setMessage(Component.translatable("focus.thaumcraft."+FocusProgram.MODIFIERS.get(Math.clamp(menu.value(11),0,3))));buttons[8].setMessage(text("branch").append(": "+(menu.value(10)+1)));buttons[9].setMessage(text("forks").append(": "+menu.value(12)));buttons[10].setMessage(text("cone").append(": "+menu.value(13)));buttons[11].setMessage(text("radius").append(": "+menu.value(14)));buttons[12].setMessage(text("seconds").append(": "+menu.value(15)));buttons[13].setMessage(text(menu.value(16)==1?"friend":"enemy"));
    }
    public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){super.extractBackground(g,mx,my,partial);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFFB9AFCC);g.fill(leftPos+4,topPos+18,leftPos+228,topPos+132,0xFF302840);for(var slot:menu.slots)g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xFF17141F);g.text(font,Component.translatable("focal.thaumcraft.complexity",menu.value(7),menu.value(9),menu.value(8)),leftPos+48,topPos+90,menu.value(7)>menu.value(9)?0xFFFF8888:0xFFFFFFFF,false);if(menu.working())g.text(font,Component.translatable("focal.thaumcraft.progress",menu.value(2)-menu.value(1),menu.value(2)),leftPos+48,topPos+101,0xFFFFFFFF,false);else g.text(font,Component.translatable("focal.thaumcraft.path",menu.value(10)+1),leftPos+48,topPos+101,0xFFDDCCEE,false);}
}
