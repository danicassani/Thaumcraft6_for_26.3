package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.theory.TheoryMenu;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class TheoryClientProbe {
    private static boolean menu,start,draw,play;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){
        if(!System.getProperty("thaumcraft.theoryProbe","").equals("client"))return;var c=Minecraft.getInstance();if(c.player==null||!(c.player.containerMenu instanceof TheoryMenu m))return;
        if(!menu&&m.value(10)==1){menu=true;Thaumcraft.LOGGER.info("THEORY_CLIENT_MENU_OK: native slots, aid availability and start readiness synchronized");}
        if(!start&&m.value(1)==2){start=true;Thaumcraft.LOGGER.info("THEORY_CLIENT_START_OK: native start packet created owned theory");}
        if(!draw&&m.card(0)!=null){draw=true;Thaumcraft.LOGGER.info("THEORY_CLIENT_DRAW_OK: native draw packet synchronized choices and paper cost");}
        if(!play&&m.value(7)>0){play=true;Thaumcraft.LOGGER.info("THEORY_CLIENT_PLAY_OK: native choose packet synchronized played count, inspiration and category percentages");}
    }
}
