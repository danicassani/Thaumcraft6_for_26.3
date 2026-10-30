package thaumcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.gizmos.*;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.content.SimpleContent;
import thaumcraft.crystals.CrystalAspects;
import thaumcraft.research.*;

@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class ThaumometerSight {
    private static ScanInspection.Snapshot received;
    private static int receivedTick;
    private static Object world;
    private static ScanTarget target;
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){reset();}
    private static void reset(){ScanInspection.clear();received=null;target=null;world=null;}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();var p=mc.player;
        if(p==null||mc.level==null||!ScanTarget.holdingMeter(p)){reset();return;}
        if(world!=mc.level){reset();world=mc.level;}
        var update=ScanInspection.client();if(update!=received){received=update;receivedTick=p.tickCount;}
        if(received!=null&&(p.tickCount-receivedTick>12||!received.dimension().equals(mc.level.dimension().identifier()))){ScanInspection.clear();received=null;}
        target=ScanTarget.find(p);
        if(mc.gui.screen()!=null||mc.gui.hud.isHidden()||received==null||!received.matches(target)||!received.scannable())return;
        int alpha=(110+(int)(45*(1+Math.sin((p.tickCount)*.25))))/2;
        try(var ignored=mc.collectPerTickGizmos()){Gizmos.cuboid(target.bounds().inflate(.025),GizmoStyle.stroke((alpha<<24)|0xC888FF,1.5F));}
    }
    public static ScanInspection.Snapshot visible(){
        var mc=Minecraft.getInstance();return mc.player!=null&&mc.level!=null&&ScanTarget.holdingMeter(mc.player)&&mc.gui.screen()==null&&received!=null&&received.matches(target)&&received.dimension().equals(mc.level.dimension().identifier())?received:null;
    }
    public static void draw(GuiGraphicsExtractor g){
        var mc=Minecraft.getInstance();var snapshot=visible();if(snapshot==null||!snapshot.known()&&!snapshot.scannable())return;
        int count=snapshot.aspects().size(),cellWidth=27;
        for(int n:snapshot.aspects().values())cellWidth=Math.max(cellWidth,mc.font.width(Integer.toString(n))+14);
        int columns=Math.min(Math.min(6,Math.max(1,count)),Math.max(1,(mc.getWindow().getGuiScaledWidth()-22)/cellWidth));
        var title=Component.translatable(snapshot.nameKey());int width=Math.max(mc.font.width(title)+10,columns*cellWidth+10);
        if(!snapshot.known())width=Math.max(width,mc.font.width(Component.translatable("research.thaumcraft.unstudied"))+32);
        if(snapshot.known()&&count==0)width=Math.max(width,mc.font.width(Component.translatable("research.thaumcraft.studied"))+10);
        width=Math.min(width,mc.getWindow().getGuiScaledWidth()-12);
        int x=Math.min(mc.getWindow().getGuiScaledWidth()/2+18,mc.getWindow().getGuiScaledWidth()-width-6),y=mc.getWindow().getGuiScaledHeight()/2+12;
        int rows=Math.max(1,(count+columns-1)/columns),height=16+rows*24;
        y=Math.min(y,Math.max(6,mc.getWindow().getGuiScaledHeight()-height-28));
        g.fill(x,y,x+width,y+height,0x8820152D);g.text(mc.font,mc.font.plainSubstrByWidth(title.getString(),width-10),x+5,y+4,0xB3E9D2FA,false);
        if(!snapshot.known()){
            icon(g,"_unknown",x+5,y+17,0xB3FFFFFF);
            g.text(mc.font,Component.translatable("research.thaumcraft.unstudied"),x+25,y+21,0xB3E9D2FA,false);return;
        }
        int i=0;for(var e:new java.util.TreeMap<>(snapshot.aspects()).entrySet()){
            int xx=x+5+(i%columns)*cellWidth,yy=y+17+(i++/columns)*24;
            icon(g,e.getKey(),xx,yy,0xB3000000|CrystalAspects.COLORS.getOrDefault(e.getKey(),0xFFFFFF));
            g.text(mc.font,Integer.toString(e.getValue()),xx+12,yy+11,0xB3FFFFFF,true);
        }
        if(count==0)g.text(mc.font,Component.translatable("research.thaumcraft.studied"),x+5,y+21,0xB3E9D2FA,false);
    }
    private static void icon(GuiGraphicsExtractor g,String name,int x,int y,int color){g.blit(RenderPipelines.GUI_TEXTURED,SimpleContent.id("textures/aspects/"+name+".png"),x,y,0,0,16,16,32,32,32,32,color);}
}
