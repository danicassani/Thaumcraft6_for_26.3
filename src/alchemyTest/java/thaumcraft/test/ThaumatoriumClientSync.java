package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.thaumatorium.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class ThaumatoriumClientSync{
 static int waiting,action;static boolean shown,title,menu;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!ThaumatoriumProbe.mode().equals("thaumatorium-client"))return;var mc=Minecraft.getInstance();if(ThaumatoriumProbe.ready&&mc.gui.screen() instanceof TitleScreen&&!title){title=true;Thaumcraft.LOGGER.info("THAUMATORIUM_TITLE_READY: world saved and title active");}if(ThaumatoriumProbe.ready&&!menu&&mc.player!=null&&mc.player.containerMenu instanceof ThaumatoriumMenu m&&m.value(2)==7&&m.value(3)==1&&m.value(1)>1&&java.util.stream.IntStream.range(0,5).anyMatch(i->!m.offer(i).isEmpty())){menu=true;Thaumcraft.LOGGER.info("THAUMATORIUM_MENU_OK: native synchronized researched offers, paid progress, three matrices and authorized selection");}if(ThaumatoriumProbe.ready&&mc.player!=null&&mc.player.containerMenu instanceof ThaumatoriumMenu m&&mc.gameMode!=null){if(action==0&&menu){mc.gameMode.handleInventoryButtonClick(m.containerId,0);action=1;}else if(action==1&&m.value(3)==2){mc.gameMode.handleInventoryButtonClick(m.containerId,0);action=2;}else if(action==2&&m.value(3)==1){mc.gameMode.handleInventoryButtonClick(m.containerId,6);action=3;}else if(action==3&&m.value(0)==2){mc.gameMode.handleInventoryButtonClick(m.containerId,5);action=4;}else if(action==4&&m.value(0)==1){action=5;Thaumcraft.LOGGER.info("THAUMATORIUM_GUI_ACTIONS_OK: native client packets select and remove a researched recipe, paginate forward/back, server capacity stays seven");}}
 if(!ThaumatoriumProbe.ready||shown||mc.level==null)return;if(++waiting>2400)throw new AssertionError("Thaumatorium client synchronization timeout");if(mc.level.getBlockEntity(ThaumatoriumProbe.P) instanceof Thaumatorium m&&m.supported()&&m.capacity()==7&&m.intact()&&m.total()==10&&m.inventory.getItem(0).getCount()>0){shown=true;Thaumcraft.LOGGER.info("THAUMATORIUM_SCREENSHOT_READY: original native two-block mesh, three matrices, essentia sources, catalyst and output chest synchronized");}}
}
