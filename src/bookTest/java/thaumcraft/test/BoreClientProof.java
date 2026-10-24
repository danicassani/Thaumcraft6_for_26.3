package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.client.*;
import thaumcraft.bore.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class BoreClientProof {
 static boolean rendered,menu;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.bookProbe","").equals("bore-client"))return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
  if(!rendered)for(var b:mc.level.entitiesForRendering())if(b instanceof ArcaneBore bore&&bore.digging()){BoreProbe.check(mc.getEntityRenderDispatcher().getRenderer(bore) instanceof BoreRenderer,"Missing original bore renderer");rendered=true;thaumcraft.Thaumcraft.LOGGER.info("BORE64_CLIENT_RENDER_OK: synchronized target and original native renderer");}
  if(!menu&&mc.gui.screen() instanceof BoreScreen screen){var m=screen.getMenu();BoreProbe.check(m.slots.get(0).getItem().is(net.minecraft.world.item.Items.DIAMOND_PICKAXE)&&m.value(1)>=2&&m.value(2)>=16,"Native bore menu lost tool or synchronized drilling values");menu=true;thaumcraft.Thaumcraft.LOGGER.info("BORE64_CLIENT_MENU_SYNC_OK");}
 }
}
