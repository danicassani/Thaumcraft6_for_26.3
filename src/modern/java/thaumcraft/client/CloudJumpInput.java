package thaumcraft.client;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.Thaumcraft;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class CloudJumpInput {
    private static boolean pressed;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){var mc=Minecraft.getInstance();boolean down=mc.player!=null&&mc.gui.screen()==null&&mc.options.keyJump.isDown();if(down&&!pressed&&mc.player!=null&&!mc.player.onGround())thaumcraft.equipment.CloudJump.request();pressed=down;}
}
