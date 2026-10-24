package thaumcraft.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.crafting.*;
import thaumcraft.crystals.CrystalAspects;

@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class ArcaneScreen extends AbstractContainerScreen<ArcaneMenu> {
    private static final Identifier TEXTURE=Identifier.parse("thaumcraft:textures/gui/arcaneworkbench.png");
    public ArcaneScreen(ArcaneMenu menu,Inventory inventory,Component title){super(menu,inventory,title,190,234);}
    @SubscribeEvent public static void screens(FMLClientSetupEvent event){event.enqueueWork(()->net.minecraft.client.gui.screens.MenuScreens.register(ArcaneContent.MENU.get(),ArcaneScreen::new));}
    @Override protected void extractLabels(GuiGraphicsExtractor graphics,int mouseX,int mouseY){}
    @Override public void extractBackground(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partial){
        super.extractBackground(graphics,mouseX,mouseY,partial);
        graphics.blit(RenderPipelines.GUI_TEXTURED,TEXTURE,leftPos,topPos,0,0,imageWidth,imageHeight,256,256);
        for(int i=0;i<6;i++)if(menu.crystalCost(i)>0){
            int color=0x55000000|CrystalAspects.COLORS.get(ArcaneRecipe.PRIMALS.get(i));
            graphics.pose().pushMatrix();graphics.pose().translate(leftPos+ArcaneMenu.crystalX(i)+8,topPos+ArcaneMenu.crystalY(i)+8);
            graphics.pose().rotate((float)Math.toRadians(i*60+(minecraft.player.tickCount+partial)%360));
            graphics.blit(RenderPipelines.GUI_TEXTURED,TEXTURE,-16,-16,192,0,32,32,64,64,256,256,color);
            graphics.pose().popMatrix();
        }
        smallCentered(graphics,Component.translatable("menu.thaumcraft.vis_available",menu.available()),168,46,menu.reason()==4?0xFFFF8888:0xFFAE8FCE);
        if(menu.reason()!=1)smallCentered(graphics,Component.translatable("menu.thaumcraft.vis_cost",menu.cost()),168,38,0xFFBDE5F2);
        if(menu.reason()>1){
            graphics.pose().pushMatrix();graphics.pose().translate(leftPos+148,topPos+101);graphics.pose().scale(.5F,.5F);
            int y=0;for(var line:font.split(Component.translatable("menu.thaumcraft.reason."+menu.reason()),80)){graphics.text(font,line,0,y,0xFFFFC580,false);y+=9;}
            graphics.pose().popMatrix();
        }
    }
    private void smallCentered(GuiGraphicsExtractor graphics,Component text,int x,int y,int color){
        graphics.pose().pushMatrix();graphics.pose().translate(leftPos+x,topPos+y);graphics.pose().scale(.5F,.5F);
        graphics.text(font,text,-font.width(text)/2,0,color,false);graphics.pose().popMatrix();
    }
}
