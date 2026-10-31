package thaumcraft.client;

import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.research.ResearchCatalog;
import thaumcraft.theory.*;

/** Original TC6 board, paper cards and split inventory, backed by the server-owned menu. */
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class TheoryScreen extends AbstractContainerScreen<TheoryMenu> {
    private static final Identifier BOARD=texture("gui/gui_research_table"),BASE=texture("gui/gui_base"),PAPER=texture("gui/paper"),GILDED=texture("gui/papergilded");
    private ArtButton start,draw,finish,cancel;
    private int previousScale=-1,fittedScale=-1;
    private final List<ArtButton> cards=new ArrayList<>(),aids=new ArrayList<>();
    public TheoryScreen(TheoryMenu m,Inventory inv,Component title){super(m,inv,title,255,255);}
    private static Identifier texture(String path){return Identifier.parse("thaumcraft:textures/"+path+".png");}
    @SubscribeEvent public static void register(FMLClientSetupEvent e){e.enqueueWork(()->net.minecraft.client.gui.screens.MenuScreens.register(TheoryContent.MENU.get(),TheoryScreen::new));}
    private void send(int action){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,menu.packet(action));}
    private ArtButton control(String key,int x,int y,int w,int h,int action){return addRenderableWidget(new ArtButton(leftPos+x,topPos+y,w,h,Component.translatable("theory.thaumcraft."+key),"control",action,b->send(action)));}
    @Override protected void init(){
        var window=minecraft.getWindow();if(previousScale<0||window.getGuiScale()!=fittedScale)previousScale=window.getGuiScale();
        int fit=Math.max(1,Math.min(previousScale,Math.min(window.getWidth()/365,window.getHeight()/265)));
        if(window.getGuiScale()!=fit)window.setGuiScale(fit);fittedScale=fit;width=window.getGuiScaledWidth();height=window.getGuiScaledHeight();
        super.init();cards.clear();aids.clear();start=control("start",96,20,64,12,1);finish=control("finish",92,84,72,13,7);cancel=control("cancel",92,168,72,12,8);
        draw=addRenderableWidget(new ArtButton(leftPos+16,topPos+40,100,126,Component.translatable("theory.thaumcraft.draw"),"deck",0,b->send(menu.value(5)>0?3:2)));
        for(int i=0;i<3;i++){final int index=i;cards.add(addRenderableWidget(new ArtButton(leftPos,topPos+37,100,128,Component.empty(),"card",i,b->send(4+index))));}
        for(int i=0;i<TheoryCards.AID_KEYS.size();i++){final int index=i;aids.add(addRenderableWidget(new ArtButton(leftPos,topPos+85,18,18,Component.translatable("theory.thaumcraft.aid."+i),"aid",i,b->send(10+index))));}
        containerTick();
    }
    @Override public void removed(){super.removed();if(previousScale>0)minecraft.getWindow().setGuiScale(previousScale);}
    private int count(){int n=0;for(int i=0;i<3;i++)if(menu.card(i)!=null)n++;return n;}
    @Override public void containerTick(){
        super.containerTick();if(start==null)return;int state=menu.value(1),n=count();boolean owned=menu.value(2)==1;
        start.visible=state==1;start.active=menu.value(10)==1;start.setTooltip(Tooltip.create(Component.translatable(menu.value(11)==1?"theory.thaumcraft.supplies":"theory.thaumcraft.locked")));
        finish.visible=state==3&&n==0;finish.active=owned&&finish.visible;cancel.visible=state==2||state==3;cancel.active=owned;
        draw.visible=(state==2&&n==0);draw.active=menu.value(12)==1;draw.setMessage(Component.translatable(menu.value(5)>0?"theory.thaumcraft.bonus":"theory.thaumcraft.draw"));draw.setTooltip(Tooltip.create(draw.getMessage()));
        for(int i=0;i<3;i++){var button=cards.get(i);var card=menu.card(i);button.visible=card!=null;button.active=owned&&menu.value(21+i*8+6)==1;
            button.setX(leftPos+(255-((n-1)*110+128))/2+i*110+14);if(card!=null){button.setMessage(Component.translatable("theory.thaumcraft.card."+key(card)));button.setTooltip(Tooltip.create(description(card)));}}
        int available=Integer.bitCount(menu.value(8)),index=0;
        for(int i=0;i<aids.size();i++){var b=aids.get(i);b.visible=state==1&&(menu.value(8)&(1<<i))!=0;b.active=b.visible;if(b.visible){int row=index/6,columns=Math.min(6,available-row*6);b.setX(leftPos+128-columns*10+(index%6)*20);b.setY(topPos+85+row*28);index++;}b.setTooltip(Tooltip.create(b.getMessage()));}
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){}
    private Component category(int i){return Component.translatable("research.thaumcraft.category."+ResearchCatalog.CATEGORIES.get(i));}
    private String key(TheoryCards.Card c){return c.kind().name().toLowerCase(Locale.ROOT);}
    private Component effect(TheoryCards.Card c){return Component.translatable("theory.thaumcraft.effect."+key(c),c.kind()==TheoryCards.Kind.INFUSE?Component.literal(Integer.toString(c.amount())):category(c.category()),c.amount());}
    private Component description(TheoryCards.Card c){var text=effect(c).copy().append("\n").append(Component.translatable("theory.thaumcraft.card_cost",c.cost()));for(var need:c.needs())text.append("\n").append(Component.translatable(need.consume()?"theory.thaumcraft.consume":"theory.thaumcraft.keep",need.stack().getHoverName()));return text;}
    private void bulb(GuiGraphicsExtractor g,int x,int y,boolean lit){g.blit(RenderPipelines.GUI_TEXTURED,BASE,x,y,lit?32:48,96,8,8,16,16,256,256);}
    private void fitted(GuiGraphicsExtractor g,Component text,int x,int y,int w,int h,float maximum,int color,boolean centered){
        float scale=maximum;var lines=font.split(text,Math.max(1,(int)(w/scale)));while(lines.size()*9*scale>h&&scale>.30F){scale-=.025F;lines=font.split(text,Math.max(1,(int)(w/scale)));}
        g.pose().pushMatrix();g.pose().translate(x,y);g.pose().scale(scale,scale);int yy=0;for(var line:lines){g.text(font,line,centered?Math.round((w/scale-font.width(line))/2):0,yy,color,false);yy+=9;}g.pose().popMatrix();
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float partial){
        super.extractBackground(g,mouseX,mouseY,partial);g.blit(RenderPipelines.GUI_TEXTURED,BOARD,leftPos,topPos,0,0,255,255,256,256);
        int maximum=menu.value(4),current=menu.value(3)-(menu.value(1)==1?Integer.bitCount(menu.value(9)):0);for(int i=0;i<maximum;i++)bulb(g,leftPos+128-maximum*5+i*10,topPos+(menu.value(1)==1?55:16),i<current);
        if(menu.value(1)!=1){var order=new ArrayList<Integer>();for(int i=0;i<7;i++)if(menu.value(14+i)>0)order.add(i);order.sort(Comparator.<Integer>comparingInt(i->menu.value(14+i)).reversed().thenComparing(i->ResearchCatalog.CATEGORIES.get(i)));
            for(int row=0;row<order.size();row++){int i=order.get(row),y=topPos+16+row*20;int sideX=Math.min(leftPos+254,width-86);var icon=texture(i==0?"items/thaumonomicon_cheat":"research/cat_"+ResearchCatalog.CATEGORIES.get(i).toLowerCase(Locale.ROOT));int size=32;g.blit(RenderPipelines.GUI_TEXTURED,icon,sideX,y,0,0,16,16,size,size,size,size);fitted(g,Component.literal(menu.value(14+i)+"%"+(row>menu.value(6)?" (-"+menu.value(14+i)/3+")":"")),sideX+21,y+4,64,12,.85F,(menu.value(13)&(1<<i))!=0?0xff888079:row>menu.value(6)?0xffe5ded4:0xff49e0dc,false);if(mouseX>=sideX&&mouseX<sideX+54&&mouseY>=y&&mouseY<y+18)g.setTooltipForNextFrame(font,category(i),mouseX,mouseY);}}
        if(menu.value(1)==4||menu.value(2)==0)fitted(g,Component.translatable(menu.value(1)==4?"theory.thaumcraft.invalid":"theory.thaumcraft.owner"),leftPos+45,topPos+65,165,42,.8F,0xffefb0a0,true);
    }
    private ItemStack aid(int i){String id=TheoryCards.AID_KEYS.get(i);if(id.equals("minecraft:nether_portal"))return new ItemStack(Items.OBSIDIAN);if(id.equals("minecraft:end_portal"))return new ItemStack(Items.ENDER_EYE);return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(id)));}
    private final class ArtButton extends Button {
        final String art;final int ref;
        ArtButton(int x,int y,int w,int h,Component message,String art,int ref,OnPress press){super(x,y,w,h,message,press,DEFAULT_NARRATION);this.art=art;this.ref=ref;}
        @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float partial){int x=getX(),y=getY();
            if(art.equals("control")){g.blit(RenderPipelines.GUI_TEXTURED,BASE,x,y,37,66,width,height,51,13,256,256,active?(ref==8?0xffaf5f50:0xff82ae72):0xff77716a);fitted(g,getMessage(),x+2,y+3,width-4,height-3,.65F,active?0xffffffff:0xffc5bbb0,true);return;}
            if(art.equals("aid")){if((menu.value(9)&(1<<ref))!=0)g.blit(RenderPipelines.GUI_TEXTURED,BASE,x,y,0,96,18,18,16,16,256,256);g.item(aid(ref),x+1,y+1);if(isHovered)g.fill(x,y+17,x+18,y+18,0xffffd98b);return;}
            if(art.equals("deck")){g.blit(RenderPipelines.GUI_TEXTURED,PAPER,x-14,y,0,0,128,128,256,256,256,256);fitted(g,getMessage(),x+5,y+48,88,25,.75F,active?0xff674a30:0xffa99c86,true);if(menu.value(5)>0){bulb(g,x+43,y+28,true);fitted(g,Component.literal("+"+menu.value(5)),x+27,y+14,40,12,.65F,0xff966822,true);}return;}
            var card=menu.card(ref);if(card==null)return;int paperX=x-14;g.blit(RenderPipelines.GUI_TEXTURED,card.aid()?GILDED:PAPER,paperX,y,0,0,128,128,256,256,256,256);
            int cost=card.cost();if(cost<0)fitted(g,Component.literal("+"),x+32,y+11,10,10,.7F,0xffaa7c25,true);for(int i=0;i<Math.abs(cost);i++)bulb(g,x+50-Math.abs(cost)*5+i*10,y+11,true);
            int color=active?0xff30241b:0xff817364;fitted(g,getMessage(),x+5,y+25,88,17,.65F,color,true);fitted(g,effect(card),x+5,y+45,88,61,.65F,color,false);
            var needs=card.needs();for(int i=0;i<needs.size();i++){g.pose().pushMatrix();g.pose().translate(x+50-needs.size()*7+i*14,y+108);g.pose().scale(.75F,.75F);g.item(needs.get(i).stack(),0,0);g.itemDecorations(font,needs.get(i).stack(),0,0);g.pose().popMatrix();}
            if(isHovered&&active)g.fill(x+7,y+122,x+94,y+123,0xffb78947);
        }
    }
}
