package thaumcraft.client;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.thaumatorium.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class ThaumatoriumScreen extends AbstractContainerScreen<ThaumatoriumMenu>{
 private final Button[] buttons=new Button[5];public ThaumatoriumScreen(ThaumatoriumMenu m,Inventory i,Component t){super(m,i,t,256,238);inventoryLabelX=48;inventoryLabelY=144;}
 @SubscribeEvent public static void setup(FMLClientSetupEvent e){e.enqueueWork(()->net.minecraft.client.gui.screens.MenuScreens.register(ThaumatoriumContent.MENU.get(),ThaumatoriumScreen::new));}
 protected void init(){super.init();for(int j=0;j<5;j++){final int b=j;buttons[j]=addRenderableWidget(Button.builder(Component.literal("+"),v->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,b)).bounds(leftPos+32,topPos+28+j*22,18,20).build());}for(int j=0;j<2;j++){final int b=5+j;addRenderableWidget(Button.builder(Component.literal(j==0?"<":">"),v->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,b)).bounds(leftPos+202+j*24,topPos+108,22,18).build());}containerTick();}
 public void containerTick(){super.containerTick();for(int j=0;j<5;j++)if(buttons[j]!=null){buttons[j].active=!menu.offer(j).isEmpty()&&menu.value(8)==1;buttons[j].setMessage(Component.literal(menu.value(9+j)==1?"−":"+"));var tip=Component.empty();for(var e:menu.cost(j).entrySet())tip.append(Component.translatable("tc.aspect."+e.getKey())).append(" × "+e.getValue()+"\n");buttons[j].setTooltip(Tooltip.create(tip));}}
 public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float t){super.extractBackground(g,mx,my,t);g.fill(leftPos,topPos,leftPos+256,topPos+238,0xFFBFB5A0);for(var s:menu.slots){g.fill(leftPos+s.x-1,topPos+s.y-1,leftPos+s.x+17,topPos+s.y+17,0xFF38302A);g.fill(leftPos+s.x,topPos+s.y,leftPos+s.x+16,topPos+s.y+16,0xFF817665);}for(int j=0;j<5;j++)if(!menu.offer(j).isEmpty())g.text(font,Component.literal(font.plainSubstrByWidth(menu.offer(j).getHoverName().getString(),142)),leftPos+54,topPos+34+j*22,0xFF222222,false);g.text(font,Component.literal(menu.value(0)+" / "+menu.value(1)+"    "+menu.value(3)+" / "+menu.value(2)),leftPos+60,topPos+134,0xFF222222,false);g.text(font,Component.translatable("thaumatorium.thaumcraft.catalyst"),leftPos+204,topPos+44,0xFF222222,false);g.text(font,Component.translatable("thaumatorium.thaumcraft."+(menu.value(8)==0?"invalid":menu.value(5)==1?"paused":menu.value(4)==0?"cold":"heated")),leftPos+202,topPos+64,0xFF222222,false);g.text(font,Component.literal(menu.value(6)+" / "+menu.value(7)),leftPos+206,topPos+82,0xFF222222,false);}
}
