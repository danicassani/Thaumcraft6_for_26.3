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
public final class ProgressionClientScene {
 static ServerPlayer actor;public static boolean ready;static int ticks;static boolean expanded;
 @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(System.getProperty("thaumcraft.bookProbe","").equals("progression-client")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null)return;if(!ready&&++ticks>60){actor.closeContainer();actor.getInventory().clearContent();actor.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ResearchContent.BOOK.get()));var state=KnowledgeState.EMPTY.stage("UNLOCKGOLEMANCY",ResearchCatalog.BY_KEY.get("UNLOCKGOLEMANCY").stages().size()+1).stage("SEALCOLLECT",ResearchCatalog.BY_KEY.get("SEALCOLLECT").stages().size()+1);KnowledgeStore.write(actor,state);ResearchService.openBook(actor);e.server().tickRateManager().setFrozen(true);ready=true;thaumcraft.Thaumcraft.LOGGER.info("PROGRESSION68_CLIENT_READY: controlled completed seal, biothaumic mind still undiscovered");}if(ready&&!expanded&&Files.exists(Path.of("../work/progression68-addendum"))){var s=ResearchService.state(actor);KnowledgeStore.write(actor,s.stage("MINDBIOTHAUMIC",ResearchCatalog.BY_KEY.get("MINDBIOTHAUMIC").stages().size()+1));actor.containerMenu.broadcastChanges();expanded=true;thaumcraft.Thaumcraft.LOGGER.info("PROGRESSION68_CLIENT_EXPANDED: server completed biothaumic mind for addendum visibility test");}}
}
