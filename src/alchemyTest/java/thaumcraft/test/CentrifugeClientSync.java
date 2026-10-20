package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.AlchemyMachine;
import thaumcraft.essentia.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class CentrifugeClientSync{
 static int waiting,phase=-1;static boolean rotated,shown,title;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!CentrifugeProbe.mode().equals("centrifuge-client"))return;var mc=Minecraft.getInstance();if(CentrifugeProbe.ready&&mc.gui.screen() instanceof TitleScreen&&!title){title=true;Thaumcraft.LOGGER.info("CENTRIFUGE_TITLE_READY: world saved and native title active");}if(!CentrifugeProbe.ready||shown||mc.level==null||mc.player==null)return;if(++waiting>2400)throw new AssertionError("Centrifuge visual synchronization timed out at "+mc.player.blockPosition());var at=CentrifugeProbe.P.offset(-2,1,0);var stoppedAt=CentrifugeProbe.P.offset(3,1,0);if(!(mc.level.getBlockEntity(at) instanceof CentrifugeEntity active)||!(mc.level.getBlockEntity(stoppedAt) instanceof CentrifugeEntity stopped))return;int next=active.getBlockState().getValue(CentrifugeBlock.PHASE);if(phase!=-1&&phase!=next)rotated=true;phase=next;if(rotated&&stopped.powered()&&stopped.snapshot().remaining()==23&&!stopped.getBlockState().getValue(CentrifugeBlock.ACTIVE)&&mc.level.getBlockEntity(at.east()) instanceof AlchemyMachine jar&&jar.total()>1&&active.getBlockState().getValue(CentrifugeBlock.ACTIVE)){shown=true;Thaumcraft.LOGGER.info("CENTRIFUGE_SCREENSHOT_READY: synchronized moving rotor, collected component in native jar and paused paid input=23 ticks");}}
}
