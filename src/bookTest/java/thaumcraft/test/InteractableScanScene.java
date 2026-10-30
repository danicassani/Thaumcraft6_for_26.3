package thaumcraft.test;
import java.nio.file.*;
import java.util.Set;
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
/** Opt-in scene; all interactions come from the native client, never from observe(). */
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class InteractableScanScene {
 static ServerPlayer actor;static int ticks;static volatile String phase="";static boolean reported,opened;
 public static volatile boolean ready;
 static final BlockPos P=new BlockPos(58008,180,8),TARGET=P.offset(0,0,3);
 static String request(){try{return Files.readString(Path.of("../work/scan78-phase")).trim();}catch(Exception e){return "chest";}}
 @SubscribeEvent public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){if(System.getProperty("thaumcraft.bookProbe","").equals("interactable-client")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null)return;var l=actor.level();if(++ticks<60)return;
  if(!ready){e.server().tickRateManager().setFrozen(false);l.getChunkAt(P);for(int x=-4;x<=4;x++)for(int z=-2;z<=8;z++){l.setBlockAndUpdate(P.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());for(int y=0;y<4;y++)l.setBlockAndUpdate(P.offset(x,y,z),Blocks.AIR.defaultBlockState());}ready=true;}
  String next=request();if(!phase.equals(next)){
   phase=next;reported=false;actor.closeContainer();actor.stopUsingItem();actor.getInventory().clearContent();actor.setGameMode(GameType.SURVIVAL);actor.setHealth(actor.getMaxHealth());actor.getAbilities().invulnerable=true;actor.onUpdateAbilities();actor.setNoGravity(true);
   boolean off=phase.equals("offhand");actor.setItemInHand(off?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND,new ItemStack(ResearchContent.METER.get()));if(!off)actor.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.GLASS_PANE));
   KnowledgeStore.write(actor,KnowledgeState.EMPTY);l.setBlockAndUpdate(TARGET,Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(TARGET,(phase.equals("furnace")?Blocks.FURNACE:phase.equals("lever")?Blocks.LEVER:Blocks.CHEST).defaultBlockState());
   if(l.getBlockEntity(TARGET) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest)chest.setItem(0,new ItemStack(Items.DIAMOND,7));
   actor.teleportTo(l,P.getX()+.5,P.getY(),P.getZ()+.5,Set.of(),0,14,true);thaumcraft.Thaumcraft.LOGGER.info("SCAN78_CLIENT_READY: "+phase);
  }
  if(phase.equals("chest")&&!opened&&actor.containerMenu!=actor.inventoryMenu){opened=true;thaumcraft.Thaumcraft.LOGGER.info("SCAN78_CLIENT_NORMAL_OPEN_OK: standing use still opens chest");}
  String id="item:minecraft:"+(phase.equals("furnace")?"furnace":phase.equals("lever")?"lever":"chest");
  if(!reported&&ResearchService.state(actor).scans().contains(id)){
   ScanProbe.check(actor.containerMenu==actor.inventoryMenu,"Scanning opened a menu");ScanProbe.check(!ResearchService.state(actor).scans().contains("item:minecraft:glass_pane"),"Sneak scanned opposite hand");
   if(!phase.equals("offhand"))ScanProbe.check(actor.getOffhandItem().is(Items.GLASS_PANE)&&actor.getOffhandItem().getCount()==1,"Scanning consumed opposite item");
   if(phase.equals("lever"))ScanProbe.check(!l.getBlockState(TARGET).getValue(net.minecraft.world.level.block.LeverBlock.POWERED),"Scanning toggled lever");
   if(l.getBlockEntity(TARGET) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest)ScanProbe.check(chest.getItem(0).is(Items.DIAMOND)&&chest.getItem(0).getCount()==7,"Chest contents changed");
   reported=true;thaumcraft.Thaumcraft.LOGGER.info("SCAN78_CLIENT_OK: "+phase+"; menu closed, world target scanned, contents retained");
  }
 }
}
