package thaumcraft.test;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.*;
import thaumcraft.thaumaturgy.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class BatRiftClientSync {
    static boolean done,branch,bat;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.thaumaturgyProbe","").equals("bat-rift-client"))return;var mc=net.minecraft.client.Minecraft.getInstance();if(mc.player==null)return;if(!bat&&!mc.level.getEntitiesOfClass(SpellBat.class,mc.player.getBoundingBox().inflate(32)).isEmpty()){bat=true;Thaumcraft.LOGGER.info("BAT_RIFT_CLIENT_TRACKING_OK: native spellbat entity synchronized");}
        if(mc.gui.screen() instanceof thaumcraft.client.FocalScreen&&mc.player.containerMenu instanceof FocalMenu menu){if(!done&&menu.value(3)==6&&menu.value(16)==1){ThaumaturgyProbe.check(BatRiftProbe.graph().equals(menu.getSlot(0).getItem().get(ThaumaturgyContent.PROGRAM.get())),"Combined graph did not synchronize");done=true;Thaumcraft.LOGGER.info("BAT_RIFT_CLIENT_SYNC_OK: friendly spellbat and combined component synchronized");}if(done&&!branch&&menu.value(10)==1&&menu.value(4)==9&&menu.value(6)==10){ThaumaturgyProbe.check(menu.value(5)==8,"Rift depth did not synchronize");branch=true;Thaumcraft.LOGGER.info("BAT_RIFT_CLIENT_RIFT_CONTROLS_OK: native depth/duration fields synchronized");}}
    }
}
