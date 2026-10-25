package thaumcraft.research;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
public final class ThaumonomiconItem extends Item {
 public ThaumonomiconItem(Properties p){super(p);}
 @Override public InteractionResult use(Level level,Player player,InteractionHand hand){
  if(player instanceof ServerPlayer real){
   if(CheatSheet.special(player.getItemInHand(hand))&&CheatSheet.allowed()&&!CheatSheet.grant(real,hand)){
    player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("research.thaumcraft.cheat_failed"));return InteractionResult.FAIL;
   }
   ResearchService.openBook(player);
  }
  return InteractionResult.SUCCESS;
 }
}
