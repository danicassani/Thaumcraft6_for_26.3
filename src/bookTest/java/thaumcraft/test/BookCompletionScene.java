package thaumcraft.test;
import java.nio.file.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.research.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class BookCompletionScene {
 static ServerPlayer actor;public static boolean ready,expanded;static int ticks;
 @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(System.getProperty("thaumcraft.bookProbe","").equals("book-complete-client")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null)return;if(!ready&&++ticks>60){actor.closeContainer();actor.getInventory().clearContent();actor.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ResearchContent.BOOK.get()));KnowledgeStore.write(actor,KnowledgeState.EMPTY);ResearchService.openBook(actor);e.server().tickRateManager().setFrozen(true);ready=true;thaumcraft.Thaumcraft.LOGGER.info("BOOK70_CLIENT_READY: empty knowledge opens original map with Basics only");}if(ready&&!expanded&&Files.exists(Path.of("../work/book70-expand"))){var state=ResearchService.state(actor);for(var entry:ResearchCatalog.ENTRIES)state=state.stage(entry.key(),entry.stages().size()+1);KnowledgeStore.write(actor,state);actor.containerMenu.broadcastChanges();expanded=true;thaumcraft.Thaumcraft.LOGGER.info("BOOK70_CLIENT_EXPANDED: controlled late-game state for all atlas assets and chapter layout");}}
}
