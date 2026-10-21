package thaumcraft.client;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class WarpFeedbackClient {
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){int k=thaumcraft.warp.WarpFeedback.pending;if(k==0)return;thaumcraft.warp.WarpFeedback.pending=0;var mc=net.minecraft.client.Minecraft.getInstance();if(mc.player==null||thaumcraft.config.ThaumcraftConfig.nostress.get())return;if(k==1)mc.player.playSound(net.minecraft.sounds.SoundEvents.CREEPER_PRIMED,1,.5F);else mc.player.playSound(net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),4,.7F);}
}
