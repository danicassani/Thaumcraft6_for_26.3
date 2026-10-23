package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.warp.WarpMonster;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class CultClientSync{
 static boolean shown;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();if(shown||!CultProbe.ready||mc.level==null||mc.player==null||mc.gui.screen()!=null)return;
  boolean portal=false,minion=false;
  for(var entity:mc.level.entitiesForRendering())if(entity instanceof WarpMonster m){portal|=m.getUUID().equals(CultProbe.portalId)&&m.portalActive()&&m.portalAge()>50;minion|=m.kind==WarpMonster.Kind.KNIGHT||m.kind==WarpMonster.Kind.CLERIC;}
  if(portal&&minion){shown=true;Thaumcraft.LOGGER.info("CULT_SCREENSHOT_READY: synchronized active natural portal and wave");}
 }
}
