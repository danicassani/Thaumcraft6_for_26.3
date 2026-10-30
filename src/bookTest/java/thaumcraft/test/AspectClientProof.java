package thaumcraft.test;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.client.ThaumometerSight;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class AspectClientProof {
 static final Set<String> seen=new HashSet<>();static boolean menu;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post event){
  if(!System.getProperty("thaumcraft.bookProbe","").equals("aspect-client"))return;var mc=Minecraft.getInstance();
  if(seen.size()==4&&mc.level==null&&mc.gui.screen() instanceof net.minecraft.client.gui.screens.TitleScreen&&!menu){menu=true;thaumcraft.Thaumcraft.LOGGER.info("ASPECT79_CLIENT_MENU_READY");}
  var s=ThaumometerSight.visible();String phase=AspectClientScene.phase;
  if(s!=null&&s.known()&&s.identity().equals("item:"+AspectClientScene.id(phase))&&!seen.contains(phase)){
   Map<String,Integer> expected=switch(phase){case "amber"->Map.of("vinculum",10,"vitreus",10);case "amethyst"->Map.of("vitreus",10,"sensus",5);case "copper"->Map.of("metallum",10,"permutatio",5);default->Map.of();};
   ScanProbe.check(!s.scannable()&&s.aspects().equals(expected),"Incorrect known aspect preview: "+phase);seen.add(phase);thaumcraft.Thaumcraft.LOGGER.info("ASPECT79_PREVIEW_OK: "+phase+" "+expected);
  }
 }
}
