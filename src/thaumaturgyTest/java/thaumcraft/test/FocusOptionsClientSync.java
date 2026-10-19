package thaumcraft.test;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.*;
import thaumcraft.thaumaturgy.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class FocusOptionsClientSync {
    static boolean done,changed,plan;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.thaumaturgyProbe","").equals("options-client"))return;var mc=net.minecraft.client.Minecraft.getInstance();if(mc.player==null)return;var caster=mc.player.getMainHandItem();var a=caster.get(ThaumaturgyContent.PLAN_SELECTION.get());if(!plan&&a!=null&&a.equals(new PlanControls.Selection(1,0,0,1))){plan=true;Thaumcraft.LOGGER.info("FOCUS_OPTIONS_CLIENT_PLAN_SYNC_OK");}if(mc.gui.screen() instanceof thaumcraft.client.FocalScreen&&mc.player.containerMenu instanceof FocalMenu m){if(!done&&m.value(21)==5&&m.value(22)==1&&m.value(23)==2&&m.value(24)==1){ThaumaturgyProbe.check(FocusOptionsProbe.graph().equals(m.getSlot(0).getItem().get(ThaumaturgyContent.PROGRAM.get())),"Options focus did not synchronize");done=true;Thaumcraft.LOGGER.info("FOCUS_OPTIONS_MENU_SYNC_OK: speed, bounce, fortune, silk and full component");}if(done&&!changed&&m.value(21)==1&&m.value(22)==2&&m.value(23)==3&&m.value(24)==0){changed=true;Thaumcraft.LOGGER.info("FOCUS_OPTIONS_MENU_EDIT_OK: all four native GUI controls changed and synchronized");}}}
}
