package thaumcraft.client;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.content.SimpleContent;
import thaumcraft.golems.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class GolemHud {
 @SubscribeEvent public static void layers(AddGuiOverlayLayersEvent e){e.getLayeredDraw().add(SimpleContent.id("golem_status"),(graphics,time)->{var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.gui.hud.isHidden())return;if(!(mc.player.getMainHandItem().getItem() instanceof GolemBell)&&!(mc.player.getOffhandItem().getItem() instanceof GolemBell))return;Component first=null,second=null;
 if(mc.hitResult instanceof EntityHitResult hit&&hit.getEntity() instanceof WorkerGolem g){first=g.getDisplayName().copy().append(" • ").append(Component.translatable("golem.thaumcraft.channel",g.renderChannel()));second=Component.translatable("golem.thaumcraft.mode_"+g.mode()).append(" • ").append(g.progress().description()).append(" • "+Math.round(g.getHealth())+"/"+Math.round(g.getMaxHealth()));}
 if(mc.hitResult instanceof BlockHitResult hit&&mc.level.getBlockEntity(hit.getBlockPos()) instanceof SealEntity s){first=s.getDisplayName().copy().append(" • ").append(Component.translatable("golem.thaumcraft.channel",s.channel));second=Component.translatable("golem.thaumcraft.priority").append(" "+s.priority+" • ").append(Component.translatable("golem.thaumcraft."+(s.shared?"shared":"private")));}
 if(first!=null){int y=graphics.guiHeight()-64;graphics.fill(4,y,Math.min(graphics.guiWidth()-4,310),y+30,0xBB20152D);graphics.text(mc.font,first,8,y+4,0xFFE9D2FA,false);graphics.text(mc.font,second,8,y+17,0xFFB4DDF0,false);}});}
}
