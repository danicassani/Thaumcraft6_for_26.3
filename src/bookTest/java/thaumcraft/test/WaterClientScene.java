package thaumcraft.test;
import java.nio.file.*;
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
import thaumcraft.alchemy.*;
import thaumcraft.research.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class WaterClientScene {
 public static final BlockPos P=new BlockPos(60008,180,8),C=P.offset(0,0,3),T=P.east(20);
 static ServerPlayer actor;static int ticks;static boolean reported;public static volatile boolean ready;public static volatile String phase="";
 static String request(){try{return Files.readString(Path.of("../work/water80-phase")).trim();}catch(Exception e){return "crucibles";}}
 @SubscribeEvent public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){if(System.getProperty("thaumcraft.bookProbe","").equals("water-client")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 static void crucible(net.minecraft.server.level.ServerLevel l,BlockPos pos,int water,Map<String,Integer> aspects){l.setBlockAndUpdate(pos,AlchemyContent.CRUCIBLE.get().defaultBlockState());var m=(AlchemyMachine)l.getBlockEntity(pos);var tag=m.saveWithoutMetadata(l.registryAccess());tag.put("alchemy",AlchemyState.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE,new AlchemyState(1,aspects,water,0,0,0,0,-250)).getOrThrow());m.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,l.registryAccess(),tag));m.setChanged();l.sendBlockUpdated(pos,m.getBlockState(),m.getBlockState(),3);}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null||++ticks<60)return;var l=actor.level();
  if(!ready){e.server().tickRateManager().setFrozen(false);e.server().getCommands().performPrefixedCommand(e.server().createCommandSourceStack(),"time set 6000");l.getChunkAt(P);l.getChunkAt(T);
   for(int x=-5;x<=25;x++)for(int z=-2;z<=12;z++){l.setBlockAndUpdate(P.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());for(int y=0;y<5;y++)l.setBlockAndUpdate(P.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   crucible(l,C,1000,Map.of());crucible(l,C.east(2),1000,Map.of("vitium",200));crucible(l,C.east(4),0,Map.of());l.setBlockAndUpdate(C.west(2),Blocks.WATER_CAULDRON.defaultBlockState().setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL,3));
   for(int x=-3;x<=3;x++)for(int z=1;z<=9;z++)for(int y=0;y<4;y++)l.setBlockAndUpdate(T.offset(x,y,z),(x==-3||x==3||z==1||z==9)?Blocks.GLASS.defaultBlockState():Blocks.WATER.defaultBlockState());
   l.setBlockAndUpdate(T.offset(0,1,7),Blocks.DIAMOND_BLOCK.defaultBlockState());ready=true;
  }
  String next=request();if(!phase.equals(next)){
   phase=next;actor.closeContainer();actor.stopUsingItem();actor.getInventory().clearContent();actor.setGameMode(phase.equals("crucibles")?GameType.CREATIVE:GameType.SURVIVAL);actor.setHealth(actor.getMaxHealth());actor.getAbilities().invulnerable=true;actor.getAbilities().flying=phase.equals("crucibles");actor.onUpdateAbilities();actor.setNoGravity(true);
   if(phase.equals("crucibles")){actor.teleportTo(l,P.getX()+1.5,P.getY()+2.5,P.getZ()+10,Set.of(),180,27,true);e.server().tickRateManager().setFrozen(true);}
   else {e.server().tickRateManager().setFrozen(false);actor.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ResearchContent.METER.get()));actor.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.GLASS_PANE));KnowledgeStore.write(actor,KnowledgeState.EMPTY);actor.teleportTo(l,T.getX()+.5,T.getY(),T.getZ()+2.5,Set.of(),0,0,true);}
   thaumcraft.Thaumcraft.LOGGER.info("WATER80_CLIENT_READY: "+phase);
  }
  if(phase.equals("underwater")&&!reported&&ResearchService.state(actor).scans().contains("item:minecraft:diamond_block")){
   ScanProbe.check(actor.isInWater()&&!ResearchService.state(actor).scans().contains("item:minecraft:water_bucket")&&actor.getOffhandItem().is(Items.GLASS_PANE)&&actor.getOffhandItem().getCount()==1,"Underwater scan targeted water or consumed opposite item");reported=true;thaumcraft.Thaumcraft.LOGGER.info("WATER80_CLIENT_SCAN_OK: actual underwater use studied diamond behind water");
  }
 }
}
