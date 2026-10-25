package thaumcraft.client;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import thaumcraft.thaumaturgy.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class TurretScreen extends AbstractContainerScreen<TurretMenu>{
 private final boolean advanced;private final Button[] toggles=new Button[3];
 public TurretScreen(TurretMenu m,Inventory i,Component t){super(m,i,t,176,166);advanced=m.getType()==TurretContent.ADVANCED_MENU.get();}
 @SubscribeEvent public static void screens(FMLClientSetupEvent e){e.enqueueWork(()->{net.minecraft.client.gui.screens.MenuScreens.register(TurretContent.MENU.get(),TurretScreen::new);net.minecraft.client.gui.screens.MenuScreens.register(TurretContent.ADVANCED_MENU.get(),TurretScreen::new);});}
 protected void init(){super.init();if(advanced)for(int i=0;i<3;i++){int bit=i;toggles[i]=addRenderableWidget(Button.builder(label(i),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,bit)).bounds(leftPos+88,topPos+10+18*i,83,17).build());}}
 private Component label(int bit){return Component.literal((menu.value(1)&(1<<bit))!=0?"✓ ":"○ ").append(Component.translatable("turret.thaumcraft."+new String[]{"animals","monsters","players"}[bit]));}
 protected void containerTick(){super.containerTick();if(advanced)for(int i=0;i<3;i++)toggles[i].setMessage(label(i));}
 protected void extractLabels(GuiGraphicsExtractor g,int x,int y){}
 public void extractBackground(GuiGraphicsExtractor g,int x,int y,float p){super.extractBackground(g,x,y,p);var texture=Identifier.parse("thaumcraft:textures/gui/gui_turret_"+(advanced?"advanced":"basic")+".png");g.blit(RenderPipelines.GUI_TEXTURED,texture,leftPos,topPos,0,0,imageWidth,imageHeight,256,256);g.blit(RenderPipelines.GUI_TEXTURED,texture,leftPos+(advanced?30:68),topPos+59,192,48,39*menu.value(2)/100,6,256,256);}
}
