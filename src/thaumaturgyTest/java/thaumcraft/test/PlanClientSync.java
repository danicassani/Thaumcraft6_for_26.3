package thaumcraft.test;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.*;
import thaumcraft.thaumaturgy.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class PlanClientSync {
    static boolean done,preview;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.thaumaturgyProbe","").equals("plan-client"))return;var mc=net.minecraft.client.Minecraft.getInstance();if(!done&&mc.player!=null&&mc.gui.screen() instanceof thaumcraft.client.FocalScreen&&mc.player.containerMenu instanceof FocalMenu menu&&menu.value(3)==5&&menu.value(20)==1){ThaumaturgyProbe.check(menu.value(17)==1&&menu.value(18)==2&&menu.value(19)==3&&menu.value(20)==1&&PlanProbe.program().equals(menu.getSlot(0).getItem().get(ThaumaturgyContent.PROGRAM.get())),"Plan settings did not synchronize");done=true;Thaumcraft.LOGGER.info("PLAN_CLIENT_SYNC_OK: focus dimensions, surface mode and editor synchronized");}
        if(done&&!preview&&mc.player!=null&&mc.gui.screen()==null){var focus=mc.player.getMainHandItem().get(ThaumaturgyContent.FOCUS.get());var program=focus==null?null:focus.get(ThaumaturgyContent.PROGRAM.get());if(program!=null&&program.medium().equals("plan")&&program.plan().z()==0){var from=mc.player.getEyePosition();var hit=mc.level.clip(new net.minecraft.world.level.ClipContext(from,from.add(mc.player.getLookAngle().scale(16)),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,mc.player));int count=PlanSelection.select(mc.level,hit,program.plan()).size();if(count==9){preview=true;Thaumcraft.LOGGER.info("PLAN_CLIENT_PREVIEW_OK: matching client selection of nine blocks");}}}
    }
}
