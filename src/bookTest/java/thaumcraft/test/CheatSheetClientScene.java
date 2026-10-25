package thaumcraft.test;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.research.*;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.persistence.PlayerWarpStore;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class CheatSheetClientScene {
 public static volatile int stage;public static volatile boolean disabled,enabled,done;static ServerPlayer actor;static int ticks;static boolean oldAllow,oldWuss;
 @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(System.getProperty("thaumcraft.bookProbe","").equals("cheat-client")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null)return;var l=actor.level();if(stage==0&&++ticks>80){oldAllow=ThaumcraftConfig.allowCheatSheet.get();oldWuss=ThaumcraftConfig.wussMode.get();e.server().tickRateManager().setFrozen(false);l.getServer().clockManager().setTotalTicks(l.dimensionType().defaultClock().orElseThrow(),6000);var at=CheatSheetProbe.P.east(100);for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++){l.getChunkAt(at.offset(x,-1,z));l.setBlockAndUpdate(at.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());}actor.closeContainer();actor.setGameMode(GameType.CREATIVE);actor.getAbilities().flying=false;actor.onUpdateAbilities();actor.teleportTo(l,at.getX()+.5,at.getY(),at.getZ()+.5,Set.of(),180,0,true);actor.getInventory().clearContent();KnowledgeStore.write(actor,KnowledgeState.EMPTY);PlayerWarpStore.write(actor,PlayerWarpStore.State.EMPTY);ThaumcraftConfig.allowCheatSheet.set(false);ThaumcraftConfig.wussMode.set(false);actor.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(ResearchContent.BOOK.get()));stage=1;thaumcraft.Thaumcraft.LOGGER.info("CHEAT73_CLIENT_DISABLED_READY");}
 if(stage==1&&disabled){ThaumcraftConfig.allowCheatSheet.set(true);actor.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,CheatSheet.stack());stage=2;thaumcraft.Thaumcraft.LOGGER.info("CHEAT73_CLIENT_ENABLED_READY: special book held; waiting for native use");}
 if(stage==2&&actor.containerMenu instanceof ResearchMenu&&ResearchService.state(actor)!=null&&ResearchCatalog.ENTRIES.stream().allMatch(r->ResearchService.knows(actor,r.key()))){var plan=CheatResearch.complete(KnowledgeState.EMPTY);var warp=PlayerWarpStore.read(actor).getOrThrow();CheatSheetProbe.check(warp.permanent()==plan.permanentWarp()&&warp.normal()==plan.normalWarp(),"Client research warp not paid once");stage=3;thaumcraft.Thaumcraft.LOGGER.info("CHEAT73_CLIENT_GRANTED_OK: native use completed all 148 entries, original warp once and native book menu");}
 }
 @SubscribeEvent public static void stop(net.minecraftforge.event.server.ServerStoppingEvent e){if(actor!=null&&stage>0){ThaumcraftConfig.allowCheatSheet.set(oldAllow);ThaumcraftConfig.wussMode.set(oldWuss);}}
}
