package thaumcraft.client;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.content.SimpleContent;
import thaumcraft.thaumaturgy.ExchangeSelection;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class ExchangeHud {
 @SubscribeEvent public static void layers(AddGuiOverlayLayersEvent e){e.getLayeredDraw().add(SimpleContent.id("exchange_sample"),(g,time)->{
  var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.gui.hud.isHidden())return;var caster=mc.player.getMainHandItem();if(!ExchangeSelection.supports(caster))return;var sample=ExchangeSelection.selected(caster);if(sample.isEmpty())return;
  int width=Math.min(mc.font.width(sample.getHoverName()),Math.max(0,mc.getWindow().getGuiScaledWidth()-42));
  g.fill(5,27,30+width,48,0xAA20152D);g.item(sample,7,29);g.text(mc.font,mc.font.plainSubstrByWidth(sample.getHoverName().getString(),width),27,34,0xB3E9D2FA,false);
 });}
}
