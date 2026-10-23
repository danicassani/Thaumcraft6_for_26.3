package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.infusion.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class InfusionFinishClient {
 static int ticks;static boolean ready,shot,done,m16;static String screen="";
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post event){if(!InfusionFinishProbe.mode().equals("finish-client"))return;var mc=Minecraft.getInstance();if(mc.level==null&&mc.gui.screen()!=null){var name=mc.gui.screen().getClass().getSimpleName();if(!screen.equals(name)){screen=name;Thaumcraft.LOGGER.info("INFUSION55_UI_SCREEN: {}",name);}}if(mc.level==null||mc.player==null||mc.gui.screen()!=null)return;
  if(InfusionFinishProbe.scene){float yaw=180,pitch=M16FinishChecks.showcaseReady?16:(float)Math.toDegrees(Math.atan2(2.12,3.5));mc.player.setYRot(yaw);mc.player.setXRot(pitch);mc.player.yRotO=yaw;mc.player.xRotO=pitch;}
  if(!ready&&InfusionFinishProbe.scene&&++ticks>60){ready=true;Thaumcraft.LOGGER.info("INFUSION55_CLIENT_CLICK_READY: synchronized altar, real client gauntlet click required");}
  if(!shot&&InfusionFinishProbe.started&&mc.level.getBlockEntity(InfusionFinishProbe.M) instanceof InfusionMatrix m&&m.job()!=null&&m.job().remaining().size()>0&&m.job().total()-thaumcraft.alchemy.AspectAmounts.total(m.job().remaining())>=10){shot=true;Thaumcraft.LOGGER.info("INFUSION55_SCREENSHOT_READY: real rank-four ritual with synchronized paid progress, spinning matrix, native essence streams and offerings");}
  if(!m16&&M16FinishChecks.showcaseReady){for(var entity:mc.level.entitiesForRendering())if(entity instanceof thaumcraft.warp.ClericOrb&&entity.getUUID().equals(M16FinishChecks.showcaseOrb)){m16=true;Thaumcraft.LOGGER.info("M16_CLIENT_SCREENSHOT_READY: actual cleric orb synchronized with Warded and cultist models");break;}}
  if(!done&&InfusionFinishProbe.finished&&mc.level.getBlockEntity(InfusionFinishProbe.M.below(2)) instanceof InfusionPedestal p&&InfusionEnchantments.level(p.item(),InfusionEnchantments.Kind.SOUNDING)==4){done=true;Thaumcraft.LOGGER.info("INFUSION55_CLIENT_SYNC_OK: completed named tool and rank-four component synchronized to client");}
 }
}
