package thaumcraft.test;
import java.util.HashSet;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.client.ThaumometerSight;
import thaumcraft.research.ScanTarget;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class InteractableScanClientProof {
 static final HashSet<String> seen=new HashSet<>();static boolean menu;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post event){
  if(!System.getProperty("thaumcraft.bookProbe","").equals("interactable-client") )return;
  var mc=Minecraft.getInstance();
  if(seen.size()==4&&mc.level==null&&mc.gui.screen() instanceof net.minecraft.client.gui.screens.TitleScreen&&!menu){menu=true;thaumcraft.Thaumcraft.LOGGER.info("SCAN78_CLIENT_MENU_READY: world saved and returned to title screen");}
  if(!InteractableScanScene.ready)return;
  var s=ThaumometerSight.visible();String phase=InteractableScanScene.phase;
  String id="item:minecraft:"+(phase.equals("furnace")?"furnace":phase.equals("lever")?"lever":"chest");
  if(mc.player!=null&&mc.player.isShiftKeyDown()&&s!=null&&s.known()&&s.kind()==ScanTarget.BLOCK&&s.identity().equals(id)&&!s.aspects().isEmpty()&&seen.add(phase))thaumcraft.Thaumcraft.LOGGER.info("SCAN78_PREVIEW_OK: "+phase+"; known world block shown while crouching");
 }
}
