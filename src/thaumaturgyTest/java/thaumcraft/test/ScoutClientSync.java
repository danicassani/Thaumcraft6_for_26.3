package thaumcraft.test;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.*;
import thaumcraft.golems.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class ScoutClientSync {
 static final boolean[] gui=new boolean[5],heads=new boolean[5];static boolean busy;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.thaumaturgyProbe","").equals("scout-client"))return;var mc=net.minecraft.client.Minecraft.getInstance();if(mc.player==null||mc.level==null)return;for(var entity:mc.level.entitiesForRendering())if(entity instanceof WorkerGolem g&&g.hasCustomName()&&g.getCustomName().getString().startsWith("Scout ")&&!heads[g.head()]){heads[g.head()]=true;Thaumcraft.LOGGER.info("SCOUT_RENDER_SYNC_OK head={}",g.head());}if(!(mc.player.containerMenu instanceof GolemBuilderMenu m))return;int h=m.design().head();if(!gui[h]){gui[h]=true;Thaumcraft.LOGGER.info("SCOUT_GUI_OK head={}",h);}if(!busy&&m.value(5)>0){busy=true;Thaumcraft.LOGGER.info("SCOUT_GUI_BUSY_OK");}
 }
}
