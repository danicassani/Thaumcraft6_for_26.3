package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class BreakClientProof {
 static boolean ready,cleared;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.breakProbe","").equals("client"))return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
  var progress=mc.level.destructionProgress().get(BreakClient.Q.asLong());
  if(!ready&&progress!=null&&progress.stream().anyMatch(p->p.getProgress()>=4&&p.getProgress()<=8)){BreakClient.cracks=true;ready=true;thaumcraft.Thaumcraft.LOGGER.info("BREAK74_CLIENT_CRACKS_OK: native destruction-progress packets render gradual Rotura");}
  if(!cleared&&BreakClient.stage==4&&mc.level.getBlockState(BreakClient.Q).isAir()&&(progress==null||progress.isEmpty())){cleared=true;thaumcraft.Thaumcraft.LOGGER.info("BREAK74_CLIENT_CLEAR_OK: block removal and crack cleanup synchronized");}
 }
}
