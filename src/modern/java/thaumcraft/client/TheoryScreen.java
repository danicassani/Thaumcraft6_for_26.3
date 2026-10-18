package thaumcraft.client;

import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.research.ResearchCatalog;
import thaumcraft.theory.*;

@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class TheoryScreen extends AbstractContainerScreen<TheoryMenu> {
    private Button start,draw,bonus,finish,cancel;
    private final List<Button> cards=new ArrayList<>(),aids=new ArrayList<>();
    public TheoryScreen(TheoryMenu m,Inventory inv,Component title){super(m,inv,title,360,240);}
    @SubscribeEvent public static void register(FMLClientSetupEvent e){e.enqueueWork(()->net.minecraft.client.gui.screens.MenuScreens.register(TheoryContent.MENU.get(),TheoryScreen::new));}
    private Button button(String key,int x,int y,int w,int action){return addRenderableWidget(Button.builder(Component.translatable("theory.thaumcraft."+key),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,menu.packet(action))).bounds(leftPos+x,topPos+y,w,16).build());}
    @Override protected void init(){
        super.init();cards.clear();aids.clear();start=button("start",8,122,70,1);draw=button("draw",80,122,70,2);bonus=button("bonus",152,122,70,3);finish=button("finish",224,122,64,7);cancel=button("cancel",290,122,62,8);
        for(int i=0;i<3;i++)cards.add(button("choose",8+i*116,101,112,4+i));
        for(int i=0;i<TheoryCards.AID_KEYS.size();i++)aids.add(button("aid."+i,8,147+i*14,86,10+i));
    }
    @Override public void containerTick(){super.containerTick();boolean owned=menu.value(2)==1;start.active=menu.value(10)==1;draw.active=menu.value(12)==1;bonus.active=draw.active&&menu.value(5)>0;finish.active=owned&&menu.value(1)==3;cancel.active=owned&&(menu.value(1)==2||menu.value(1)==3);
        for(int i=0;i<3;i++){cards.get(i).visible=menu.card(i)!=null;cards.get(i).active=owned&&menu.value(21+i*8+6)==1;}
        for(int i=0;i<aids.size();i++){var b=aids.get(i);b.visible=(menu.value(8)&(1<<i))!=0;b.active=menu.value(1)==1;b.setMessage(Component.literal((menu.value(9)&(1<<i))!=0?"✓ ":"+ ").append(Component.translatable("theory.thaumcraft.aid."+i)));}
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){}
    private Component category(int i){return Component.translatable("research.thaumcraft.category."+ResearchCatalog.CATEGORIES.get(i));}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float partial){
        super.extractBackground(g,mouseX,mouseY,partial);g.fill(leftPos,topPos,leftPos+360,topPos+240,0xFF34273E);
        g.text(font,title,leftPos+8,topPos+5,0xFFFFE9BA,false);g.text(font,Component.translatable("theory.thaumcraft.status",menu.value(3),menu.value(4),menu.value(5)),leftPos+8,topPos+16,0xFFFFE9BA,false);
        g.fill(leftPos+9,topPos+23,leftPos+27,topPos+41,0xFF786779);g.fill(leftPos+331,topPos+23,leftPos+349,topPos+41,0xFF786779);
        for(int i=0;i<3;i++){
            int x=leftPos+8+i*116;g.fill(x,topPos+44,x+112,topPos+99,0xFFF1E4C9);var card=menu.card(i);if(card==null)continue;
            String key=card.kind().name().toLowerCase(Locale.ROOT);g.text(font,Component.translatable("theory.thaumcraft.card."+key),x+3,topPos+47,0xFF302638,false);
            g.text(font,Component.translatable("theory.thaumcraft.card_cost",card.cost()),x+3,topPos+58,0xFF715367,false);
            var lines=font.split(Component.translatable("theory.thaumcraft.effect."+key,category(card.category()),card.amount()),106);for(int j=0;j<Math.min(3,lines.size());j++)g.text(font,lines.get(j),x+3,topPos+69+j*9,0xFF302638,false);
        }
        if(menu.value(1)==1)g.text(font,Component.translatable(menu.value(11)==1?"theory.thaumcraft.supplies":"theory.thaumcraft.locked"),leftPos+32,topPos+32,0xFFFFE9BA,false);
        if(menu.value(1)==4||menu.value(2)==0)g.text(font,Component.translatable(menu.value(1)==4?"theory.thaumcraft.invalid":"theory.thaumcraft.owner"),leftPos+32,topPos+32,0xFFFF8888,false);
        for(var slot:menu.slots)g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xFF17141F);
        for(int i=0;i<7;i++)g.text(font,Component.literal(category(i).getString()+": "+menu.value(14+i)+"%"),leftPos+267,topPos+146+i*12,0xFFFFE9BA,false);
    }
    @Override public void extractContents(GuiGraphicsExtractor g,int mouseX,int mouseY,float partial){
        super.extractContents(g,mouseX,mouseY,partial);
        for(int i=0;i<3;i++){var card=menu.card(i);if(card==null)continue;int x=leftPos+8+i*116;
            if(mouseX>=x&&mouseX<x+112&&mouseY>=topPos+44&&mouseY<topPos+99){
                var lines=new ArrayList<Component>();String key=card.kind().name().toLowerCase(Locale.ROOT);lines.add(Component.translatable("theory.thaumcraft.effect."+key,category(card.category()),card.amount()));
                for(var need:card.needs())lines.add(Component.translatable(need.consume()?"theory.thaumcraft.consume":"theory.thaumcraft.keep",need.stack().getHoverName()));
                g.setTooltipForNextFrame(font,lines,Optional.empty(),mouseX,mouseY);
            }
        }
    }
}
