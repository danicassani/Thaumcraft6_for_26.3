package thaumcraft.test;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.research.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class ScanClientScene {
 static ServerPlayer actor;static int ticks;static boolean ready,done,otherDone;
 @SubscribeEvent public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){if(System.getProperty("thaumcraft.bookProbe","").equals("scan-client")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null)return;var level=actor.level();
  if(!ready&&++ticks>60){var p=new BlockPos(57008,180,8);e.server().tickRateManager().setFrozen(false);level.getChunkAt(p);
   for(int x=-5;x<=5;x++)for(int z=-3;z<=9;z++){level.setBlockAndUpdate(p.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());for(int y=0;y<=4;y++)level.setBlockAndUpdate(p.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   level.setBlockAndUpdate(p.offset(0,1,3),Blocks.DIAMOND_BLOCK.defaultBlockState());actor.closeContainer();actor.getInventory().clearContent();actor.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ResearchContent.METER.get()));actor.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.GLASS_PANE));KnowledgeStore.write(actor,KnowledgeState.EMPTY);actor.setGameMode(GameType.SURVIVAL);actor.teleportTo(level,p.getX()+.5,p.getY(),p.getZ()+.5,Set.of(),0,2,true);actor.setNoGravity(true);ready=true;thaumcraft.Thaumcraft.LOGGER.info("SCAN57_CLIENT_READY: actual Thaumometer, other-hand glass pane, diamond target; hold use for one second");}
  if(ready){ScanProbe.check(actor.getOffhandItem().is(Items.GLASS_PANE)&&actor.getOffhandItem().getCount()==1,"Held use placed or consumed the opposite-hand pane");}
  if(ready&&!done&&ResearchService.state(actor).scans().stream().anyMatch(s->s.contains("diamond_block"))){done=true;thaumcraft.Thaumcraft.LOGGER.info("SCAN57_CLIENT_WORLD_OK: real client use completed server scan");}
  if(ready&&!otherDone&&actor.isShiftKeyDown()&&ResearchService.state(actor).scans().stream().anyMatch(s->s.contains("glass_pane"))){otherDone=true;thaumcraft.Thaumcraft.LOGGER.info("SCAN57_CLIENT_OTHER_OK: real client Shift use scanned pane without moving either item");}
 }
}
