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
import thaumcraft.crafting.*;
import thaumcraft.research.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class ViewClientScene {
 static final BlockPos P=new BlockPos(62008,180,8);static ServerPlayer actor;static int ticks,phase;public static volatile boolean ready;static ArcaneWorkbenchEntity table;static final int[] COUNTS={52,32,3,2,18,63};
 @SubscribeEvent public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){if(System.getProperty("thaumcraft.bookProbe","").equals("view-client")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null)return;var l=actor.level();
  if(!ready&&++ticks>60){e.server().tickRateManager().setFrozen(false);l.getChunkAt(P);
   for(int x=-7;x<=7;x++)for(int z=-5;z<=12;z++){l.setBlockAndUpdate(P.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());for(int y=0;y<5;y++)l.setBlockAndUpdate(P.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   for(int x=-3;x<=3;x++)for(int y=0;y<4;y++)l.setBlockAndUpdate(P.offset(x,y,7),Blocks.OAK_PLANKS.defaultBlockState());l.setBlockAndUpdate(P.offset(0,1,7),Blocks.GLASS.defaultBlockState());
   actor.closeContainer();actor.setGameMode(GameType.SURVIVAL);actor.setHealth(actor.getMaxHealth());actor.getAbilities().invulnerable=true;actor.onUpdateAbilities();actor.setNoGravity(false);actor.getInventory().clearContent();actor.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ResearchContent.METER.get()));actor.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.EMERALD,3));KnowledgeStore.write(actor,KnowledgeState.EMPTY.flag("!gotthaumonomicon").stage("FIRSTSTEPS",2).stage("KNOWLEDGETYPES",2).craft("thaumcraft:arcane_workbench"));actor.teleportTo(l,P.getX()+.5,P.getY(),P.getZ()+.5,Set.of(),0,0,true);ready=true;phase=1;thaumcraft.Thaumcraft.LOGGER.info("VIEW62_SCENE_READY: idle Thaumometer and three physical emeralds in opposite hand");
  }
  if(!ready)return;if(phase!=2)ScanProbe.check(actor.getMainHandItem().is(ResearchContent.METER.get())&&actor.getOffhandItem().is(Items.EMERALD)&&actor.getOffhandItem().getCount()==3,"Viewing moved or consumed an equipped item");
  if(phase==1&&Files.exists(Path.of("../work/view62-offhand"))){var meter=actor.getMainHandItem();actor.setItemInHand(InteractionHand.MAIN_HAND,actor.getOffhandItem());actor.setItemInHand(InteractionHand.OFF_HAND,meter);phase=2;thaumcraft.Thaumcraft.LOGGER.info("VIEW62_OFFHAND_READY");}
  if(phase==2){ScanProbe.check(actor.getOffhandItem().is(ResearchContent.METER.get())&&actor.getMainHandItem().is(Items.EMERALD),"Reversed hand inventory changed");}
  if(phase<3&&Files.exists(Path.of("../work/view62-table"))){
   if(phase==2){var meter=actor.getOffhandItem();actor.setItemInHand(InteractionHand.OFF_HAND,actor.getMainHandItem());actor.setItemInHand(InteractionHand.MAIN_HAND,meter);}
   var at=P.east(2);l.setBlockAndUpdate(at,ArcaneContent.WORKBENCH.get().defaultBlockState());table=(ArcaneWorkbenchEntity)l.getBlockEntity(at);BookFlowProbe.resetAura(l);thaumcraft.aura.AuraData.get(l).put(thaumcraft.aura.AuraCell.key(at.getX()>>4,at.getZ()>>4),new thaumcraft.aura.AuraCell(200,200,0));BookFlowProbe.meter(table,1,true);for(int i=0;i<6;i++)table.inventory.getItem(9+i).setCount(COUNTS[i]);actor.teleportTo(l,at.getX()+.5,at.getY(),at.getZ()+2,Set.of(),180,0,true);actor.openMenu(table);phase=3;thaumcraft.Thaumcraft.LOGGER.info("VIEW62_TABLE_READY: original radial coordinates, real paid recipe, native output");
  }
  if(phase==3&&actor.containerMenu instanceof ArcaneMenu menu&&menu.getCarried().is(ResearchContent.METER.get())){ScanProbe.check(table.inventory.getItem(4).isEmpty()&&table.inventory.getItem(15).isEmpty(),"Native click failed to consume ingredients or duplicated output");for(int i=9;i<15;i++)ScanProbe.check(table.inventory.getItem(i).getCount()==COUNTS[i-9]-1,"Incorrect crystal payment");ScanProbe.check(ResearchService.state(actor).crafts().contains("thaumcraft:thaumometer"),"Craft proof missing");phase=4;thaumcraft.Thaumcraft.LOGGER.info("VIEW62_NATIVE_CRAFT_OK: real output click paid ingredients and six crystals");}
 }
}
