package thaumcraft.test;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.*;
import thaumcraft.golems.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class TaskClientSync {
 static boolean opened,left,sneak,empty;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.thaumaturgyProbe","").equals("tasks-client"))return;var mc=net.minecraft.client.Minecraft.getInstance();if(mc.player==null||!(mc.player.containerMenu instanceof SealMenu m)||m.kind()!=SealKind.USE)return;
  if(!opened){ThaumaturgyProbe.check(m.getSlot(0).getItem().is(net.minecraft.world.item.Items.DIRT)&&m.value(25)==1,"Use GUI lacks sample/options");opened=true;Thaumcraft.LOGGER.info("TASK_GUI_OK");}
  if(!left&&m.value(21)==1){left=true;Thaumcraft.LOGGER.info("TASK_GUI_LEFT_OK");}if(!sneak&&m.value(24)==1){sneak=true;Thaumcraft.LOGGER.info("TASK_GUI_SNEAK_OK");}if(!empty&&m.value(23)==1){empty=true;Thaumcraft.LOGGER.info("TASK_GUI_EMPTY_OK");}
 }
}
