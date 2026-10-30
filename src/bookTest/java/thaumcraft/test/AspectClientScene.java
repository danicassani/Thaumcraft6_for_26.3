package thaumcraft.test;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.research.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class AspectClientScene {
 static ServerPlayer actor;static int ticks;static ItemEntity drop;static boolean reported;
 public static volatile boolean ready;public static volatile String phase="";
 static final BlockPos P=new BlockPos(58008,180,8);
 static String request(){try{return Files.readString(Path.of("../work/aspect79-phase")).trim();}catch(Exception e){return "amber";}}
 static String id(String phase){return switch(phase){case "amber"->"thaumcraft:amber";case "amethyst"->"minecraft:amethyst_shard";case "copper"->"minecraft:raw_copper";default->"minecraft:allay_spawn_egg";};}
 @SubscribeEvent public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){if(System.getProperty("thaumcraft.bookProbe","").equals("aspect-client")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null||++ticks<60)return;var l=actor.level();
  if(!ready){e.server().tickRateManager().setFrozen(false);l.getChunkAt(P);for(int x=-4;x<=4;x++)for(int z=-2;z<=8;z++){l.setBlockAndUpdate(P.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());for(int y=0;y<4;y++)l.setBlockAndUpdate(P.offset(x,y,z),Blocks.AIR.defaultBlockState());}ready=true;}
  String next=request();if(!phase.equals(next)){
   phase=next;reported=false;if(drop!=null)drop.discard();actor.closeContainer();actor.stopUsingItem();actor.getInventory().clearContent();actor.setGameMode(GameType.SURVIVAL);actor.setHealth(actor.getMaxHealth());actor.getAbilities().invulnerable=true;actor.onUpdateAbilities();actor.setNoGravity(true);actor.setShiftKeyDown(false);
   actor.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(id(phase))),7));
   drop=actor.drop(actor.getMainHandItem().split(7),true,net.minecraft.util.Prediction.SERVER_ONLY);ScanProbe.check(drop!=null,"Native toss failed");drop.setPos(P.getX()+.5,P.getY()+.05,P.getZ()+3.5);drop.setNoGravity(true);drop.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);drop.setNeverPickUp();drop.setUnlimitedLifetime();
   actor.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ResearchContent.METER.get()));actor.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.GLASS_PANE));KnowledgeStore.write(actor,KnowledgeState.EMPTY);
   actor.teleportTo(l,P.getX()+.5,P.getY(),P.getZ()+.5,Set.of(),0,26,true);thaumcraft.Thaumcraft.LOGGER.info("ASPECT79_CLIENT_READY: "+phase);
  }
  ScanProbe.check(drop.isAlive()&&drop.getItem().getCount()==7&&actor.getOffhandItem().is(Items.GLASS_PANE)&&actor.getOffhandItem().getCount()==1,"Scan moved or consumed items");
  if(!reported&&ResearchService.state(actor).scans().contains("item:"+id(phase))){
   ScanProbe.check(actor.containerMenu==actor.inventoryMenu&&!ResearchService.state(actor).scans().contains("item:minecraft:glass_pane"),"Scan changed target or opened menu");
   if(phase.equals("uncatalogued"))ScanProbe.check(ResearchService.state(actor).points().values().stream().allMatch(n->n==0),"Unassigned item invented points");
   reported=true;thaumcraft.Thaumcraft.LOGGER.info("ASPECT79_CLIENT_OK: "+phase+"; real dropped stack retained and observation recorded");
  }
 }
}
