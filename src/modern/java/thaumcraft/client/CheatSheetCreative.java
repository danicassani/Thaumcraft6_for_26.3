package thaumcraft.client;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.research.CheatSheet;
/** Refresh the creative tab and native search caches after server config sync/reload. */
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class CheatSheetCreative {
 private static net.minecraft.client.multiplayer.ClientLevel lastLevel;private static boolean lastAllowed;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.getConnection()==null){lastLevel=null;return;}boolean allowed=CheatSheet.allowed();if(mc.level==lastLevel&&allowed==lastAllowed)return;lastLevel=mc.level;lastAllowed=allowed;
  var parameters=new CreativeModeTab.ItemDisplayParameters(mc.level.enabledFeatures(),mc.player.canUseGameMasterBlocks(),mc.level.registryAccess());
  CreativeModeTabs.tryRebuildTabContents(parameters.enabledFeatures(),parameters.hasPermissions(),parameters.holders());
  thaumcraft.content.BasicContent.TAB.get().buildContents(parameters);CreativeModeTabs.searchTab().buildContents(parameters);mc.getConnection().updateSearchTrees();
  if(mc.gui.screen() instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen screen)screen.resize(mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());
 }
}
