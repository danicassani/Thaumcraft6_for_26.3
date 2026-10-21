package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.taint.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class TaintClientSync{
 static boolean shown,title,goo;static int waiting;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!TaintProbe.mode().equals("taint-client"))return;var mc=Minecraft.getInstance();if(TaintProbe.ready&&mc.gui.screen() instanceof TitleScreen&&!title){title=true;Thaumcraft.LOGGER.info("TAINT_TITLE_READY: native world saved and title active");}if(TaintProbe.ready&&!goo&&mc.level!=null&&mc.player!=null&&mc.player.getInventory().getSelectedSlot()==8&&mc.level.getBlockState(TaintProbe.P.south(7).east(6)).is(TaintContent.GOO.get())){goo=true;Thaumcraft.LOGGER.info("TAINT_GOO_READY: native finite goo basin synchronized");}if(!TaintProbe.ready||shown||mc.level==null)return;if(++waiting>1800)throw new AssertionError("Taint visual synchronization timeout");if(mc.level.getBlockState(TaintProbe.P.below()).is(TaintContent.BLOCKS.get("taint_soil").get())&&mc.level.getBlockState(TaintProbe.P.south(7).west(6)).is(TaintContent.DEATH.block.get())&&mc.level.getBlockState(TaintProbe.P.south(7)).is(TaintContent.PURE.block.get())&&mc.level.entitiesForRendering().iterator().hasNext()){shown=true;Thaumcraft.LOGGER.info("TAINT_SCREENSHOT_READY: native corrupted terrain, entities and physical death/purifier basins synchronized");}}
}
