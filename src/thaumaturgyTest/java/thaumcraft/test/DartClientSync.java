package thaumcraft.test;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.*;
import thaumcraft.golems.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class DartClientSync {
 static boolean arms,projectile,busy;static final boolean[] gui=new boolean[5];
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.thaumaturgyProbe","").equals("dart-client"))return;var mc=net.minecraft.client.Minecraft.getInstance();if(mc.player==null||mc.level==null)return;for(var ent:mc.level.entitiesForRendering()){if(!arms&&ent instanceof WorkerGolem g&&g.arms()==4){arms=true;Thaumcraft.LOGGER.info("DART_ARMS_SYNC_OK");}if(!projectile&&ent instanceof GolemDart){projectile=true;Thaumcraft.LOGGER.info("DART_PROJECTILE_SYNC_OK");}}if(!(mc.player.containerMenu instanceof GolemBuilderMenu m))return;int a=m.design().arms();if(!gui[a]){gui[a]=true;Thaumcraft.LOGGER.info("DART_GUI_OK arms={}",a);}if(!busy&&m.value(5)>0){busy=true;Thaumcraft.LOGGER.info("DART_GUI_BUSY_OK");}}
}
