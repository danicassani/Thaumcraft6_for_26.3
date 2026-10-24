package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.client.FocalScreen;
import thaumcraft.thaumaturgy.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class CanvasClientProof {
 static boolean seen,locked,changed;static int selection=-99;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.bookProbe","").equals("canvas-client")||!CanvasClientScene.open)return;var mc=Minecraft.getInstance();if(!(mc.gui.screen() instanceof FocalScreen f))return;var m=f.getMenu();if(selection!=m.value(29)){selection=m.value(29);thaumcraft.Thaumcraft.LOGGER.info("CANVAS65_SELECTION_SYNC index={} kind={}",selection,m.value(28));}if(!seen&&m.program().equals(CanvasProbe.graph())&&f.nodeCount()==8){CanvasProbe.check(m.slots.get(0).x==8&&m.slots.get(0).y==112&&m.value(30)==0,"Canvas inventory or craft readiness out of sync");f.center();var point=f.nodeCenter(2);double scale=mc.getWindow().getGuiScale();thaumcraft.Thaumcraft.LOGGER.info("CANVAS65_CLIENT_SYNC_OK: eight original icons, projected selection and real inventory; first-effect native click at {},{}",Math.round(point[0]*scale),Math.round(point[1]*scale));seen=true;}
  if(seen&&!changed&&m.program().effects().getFirst().kind().equals("frost")){changed=true;thaumcraft.Thaumcraft.LOGGER.info("CANVAS65_CLIENT_GRAPH_SYNC_OK: authoritative graph edit returned through native slot component");}
  if(seen&&!locked&&m.working()){locked=true;CanvasProbe.check(m.value(30)==6,"Busy reason not synchronized");thaumcraft.Thaumcraft.LOGGER.info("CANVAS65_CLIENT_LOCK_SYNC_OK");}
 }
}
