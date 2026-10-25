package thaumcraft.research;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.persistence.PlayerWarpStore;
/** The same book item, with an explicit persistent marker; names never confer authority. */
public final class CheatSheet {
 public static boolean allowed(){return ThaumcraftConfig.SERVER.isLoaded()&&ThaumcraftConfig.allowCheatSheet.get();}
 public static boolean special(ItemStack stack){return stack.is(ResearchContent.BOOK.get())&&stack.has(ResearchContent.CHEAT_SHEET.get());}
 public static ItemStack stack(){var item=new ItemStack(ResearchContent.BOOK.get());item.set(ResearchContent.CHEAT_SHEET.get(),net.minecraft.util.Unit.INSTANCE);item.set(DataComponents.ITEM_NAME,Component.translatable("item.thaumcraft.thaumonomicon_cheat"));item.set(DataComponents.RARITY,Rarity.EPIC);return item;}
 public static boolean grant(ServerPlayer player,InteractionHand hand){
  if(!player.level().getServer().isSameThread()||!allowed()||!special(player.getItemInHand(hand)))return false;
  var old=ResearchService.state(player);if(old==null)return false;var plan=CheatResearch.complete(old);
  PlayerWarpStore.State warp=null;
  if(!ThaumcraftConfig.wussMode.get()&&(plan.permanentWarp()>0||plan.normalWarp()>0)){
   var saved=PlayerWarpStore.read(player).result();if(saved.isEmpty())return false;
   warp=thaumcraft.warp.WarpRules.add(saved.get(),plan.permanentWarp(),plan.normalWarp(),0);
  }
  if(plan.knowledge().equals(old))return true;
  if(!KnowledgeStore.write(player,plan.knowledge()))return false;
  if(warp!=null&&PlayerWarpStore.write(player,warp).result().isEmpty()){KnowledgeStore.write(player,old);return false;}
  player.sendOverlayMessage(Component.translatable("research.thaumcraft.cheat_granted"));return true;
 }
 private CheatSheet(){}
}
