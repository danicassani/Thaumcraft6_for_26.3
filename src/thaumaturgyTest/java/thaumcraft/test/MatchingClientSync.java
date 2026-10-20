package thaumcraft.test;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.*;
import thaumcraft.golems.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class MatchingClientSync {
 static String last="",panel="";static boolean title,ready;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!MatchingClient.mode())return;var mc=net.minecraft.client.Minecraft.getInstance();if(mc.player!=null&&mc.player.isDeadOrDying()&&!MatchingClient.tested){mc.player.respawn();return;}if(MatchingClient.tested&&mc.gui.screen() instanceof net.minecraft.client.gui.screens.TitleScreen&&!title){title=true;Thaumcraft.LOGGER.info("MATCHING_TITLE_READY: world saved and title screen active");}if(mc.player==null||!(mc.player.containerMenu instanceof SealMenu m))return;String state=m.kind()+":"+m.value(30)+":"+m.value(31)+":"+m.value(32);if(!state.equals(last)){last=state;Thaumcraft.LOGGER.info("MATCHING_MENU_SYNC {}",state);}if(mc.gui.screen() instanceof thaumcraft.client.GolemScreens.Seal gui){var controls=gui.children().stream().filter(w->w instanceof net.minecraft.client.gui.components.Button b&&b.visible).map(w->(net.minecraft.client.gui.components.Button)w).toList();if(controls.stream().anyMatch(b->b.getMessage().getString().startsWith("Materials"))&&!panel.equals(state)){panel=state;for(int i=0;i<3;i++){var text=net.minecraft.network.chat.Component.translatable("golem.thaumcraft.match_"+new String[]{"damage","tags","mod"}[i]).append(" "+(m.value(30+i)==1?"✓":"×")).getString();ThaumaturgyProbe.check(controls.stream().anyMatch(b->b.getMessage().getString().equals(text)),"Missing matching control "+text);}Thaumcraft.LOGGER.info("MATCHING_PANEL_STATE {}",state);}if(!ready&&MatchingClient.tested&&state.equals("STOCK:1:1:0")&&controls.stream().anyMatch(b->b.getMessage().getString().startsWith("Materials"))){ready=true;Thaumcraft.LOGGER.info("MATCHING_SCREENSHOT_READY: real synchronized material controls, completed stock transaction, first and ninth samples visible");}}}
}
