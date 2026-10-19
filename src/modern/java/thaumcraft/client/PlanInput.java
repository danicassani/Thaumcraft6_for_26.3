package thaumcraft.client;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.*;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.thaumaturgy.PlanControls;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class PlanInput {
    public static final KeyMapping RESIZE=new KeyMapping("key.thaumcraft.plan_resize",InputConstants.KEY_G,KeyMapping.Category.register(Identifier.parse("thaumcraft:caster")));
    private static final java.util.ArrayDeque<Boolean> MODIFIERS=new java.util.ArrayDeque<>();
    @SubscribeEvent public static void key(net.minecraftforge.client.event.InputEvent.Key e){if(e.getAction()==InputConstants.PRESS&&RESIZE.matches(e.getInfo())&&Minecraft.getInstance().gui.screen()==null&&MODIFIERS.size()<16)MODIFIERS.add((e.getModifiers()&InputConstants.MOD_CONTROL)!=0);}
    @SubscribeEvent public static void register(RegisterKeyMappingsEvent e){e.register(RESIZE);}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){var mc=Minecraft.getInstance();while(RESIZE.consumeClick())if(mc.player!=null&&mc.gui.screen()==null){boolean ctrl=MODIFIERS.isEmpty()?(InputConstants.isKeyDown(InputConstants.KEY_LCONTROL)||InputConstants.isKeyDown(InputConstants.KEY_RCONTROL)):MODIFIERS.removeFirst();PlanControls.request(ctrl);}MODIFIERS.clear();}
}
