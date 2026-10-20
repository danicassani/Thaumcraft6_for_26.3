package thaumcraft.test;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.*;
import thaumcraft.golems.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class MobilityClientSync {
 static int seen;static final boolean[] gui=new boolean[4];static boolean busy;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.thaumaturgyProbe","").equals("mobility-client"))return;var mc=net.minecraft.client.Minecraft.getInstance();if(mc.player==null||mc.level==null)return;for(var entity:mc.level.entitiesForRendering())if(entity instanceof WorkerGolem g&&g.hasCustomName()&&g.getCustomName().getString().startsWith("Mobility ")){int bit=1<<g.legs();if((seen&bit)==0){seen|=bit;Thaumcraft.LOGGER.info("MOBILITY_RENDER_SYNC_OK legs={}",g.legs());}}
 if(!(mc.player.containerMenu instanceof GolemBuilderMenu m))return;int leg=m.design().legs();if(!gui[leg]){gui[leg]=true;Thaumcraft.LOGGER.info("MOBILITY_GUI_OK legs={}",leg);}if(!busy&&m.value(5)>0){busy=true;Thaumcraft.LOGGER.info("MOBILITY_GUI_BUSY_OK");}
 }
}
