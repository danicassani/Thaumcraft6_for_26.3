package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.*;
import thaumcraft.alchemy.*;
import thaumcraft.condenser.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class CondenserClientSync{
 static int waiting;static boolean shown,title,menu;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!CondenserProbe.mode().equals("condenser-client"))return;var mc=Minecraft.getInstance();if(CondenserProbe.ready&&mc.gui.screen() instanceof TitleScreen&&!title){title=true;Thaumcraft.LOGGER.info("CONDENSER_TITLE_READY: native world saved and title active");}if(CondenserProbe.ready&&!menu&&mc.player!=null&&mc.player.containerMenu instanceof AlchemyMenu m&&m.value(8+AlchemyMenu.ASPECTS.indexOf("vitium"))>=1){menu=true;Thaumcraft.LOGGER.info("CONDENSER_JAR_OK: native jar menu contains physically condensed Vitium");}if(!CondenserProbe.ready||shown||mc.level==null)return;if(++waiting>2400)throw new AssertionError("Condenser visual synchronization timeout");if(mc.level.getBlockEntity(CondenserProbe.P) instanceof Condenser active&&active.supported()&&active.snapshot().converted()>=1&&active.getBlockState().getValue(CondenserBlock.ACTIVE)&&active.network().cells().size()==7&&mc.level.getBlockEntity(CondenserProbe.P.east(6)) instanceof Condenser stopped&&stopped.powered()&&stopped.snapshot().fuel()==50&&stopped.snapshot().progress()==100&&mc.level.getBlockState(CondenserProbe.P.east(6).above()).is(CondenserContent.DIRTY.get())&&mc.level.getBlockEntity(CondenserProbe.P.below()) instanceof AlchemyMachine jar&&jar.aspects().getOrDefault("vitium",0)>=1){shown=true;Thaumcraft.LOGGER.info("CONDENSER_SCREENSHOT_READY: active original condenser and seven connected lattices, actual Vitium jar, clogged root and redstone-paused second machine synchronized");}}
}
