package thaumcraft.test;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.crafting.*;
import thaumcraft.research.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class BookClientScene {
 static ServerPlayer actor;static int ticks,stage;public static volatile boolean ready;static ArcaneWorkbenchEntity table;
 static String mode(){return System.getProperty("thaumcraft.bookProbe","");}
 @SubscribeEvent public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){if(mode().equals("client")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(!mode().equals("client")||actor==null)return;var l=e.server().overworld();ticks++;
  if(stage==0&&ticks>60){e.server().tickRateManager().setFrozen(false);var p=BookFlowProbe.P;l.getChunkAt(p);l.setBlockAndUpdate(p,ArcaneContent.WORKBENCH.get().defaultBlockState());table=(ArcaneWorkbenchEntity)l.getBlockEntity(p);BookFlowProbe.resetAura(l);BookFlowProbe.meter(table,1,true);actor.getInventory().clearContent();actor.getInventory().add(new ItemStack(ResearchContent.BOOK.get()));KnowledgeStore.write(actor,KnowledgeState.EMPTY.flag("!gotthaumonomicon").stage("FIRSTSTEPS",2).stage("KNOWLEDGETYPES",2).craft("thaumcraft:arcane_workbench"));actor.setGameMode(GameType.SURVIVAL);actor.teleportTo(l,p.getX()+.5,p.getY()+1,p.getZ()+2,Set.of(),180,20,true);actor.setNoGravity(true);ResearchService.openBook(actor);ready=true;stage=1;thaumcraft.Thaumcraft.LOGGER.info("BOOK56_BOOK_OPEN: first steps stage two, only Basics discovered, exact thaumometer diagram");}
  if(stage==1&&Files.exists(Path.of("../work/book56-show-table"))){actor.closeContainer();actor.openMenu(table);stage=2;thaumcraft.Thaumcraft.LOGGER.info("BOOK56_TABLE_OPEN: native single output slot, four gold ingots, glass pane, six crystals and 20 vis");}
  if(stage==2&&actor.containerMenu instanceof ArcaneMenu menu&&menu.getCarried().is(ResearchContent.METER.get())){BookFlowProbe.check(table.inventory.getItem(15).isEmpty()&&table.inventory.getItem(4).isEmpty(),"Client pickup duplicated output or ingredients");for(int i=9;i<15;i++)BookFlowProbe.check(table.inventory.getItem(i).isEmpty(),"Client crystal not consumed");BookFlowProbe.check(ResearchService.state(actor).crafts().contains("thaumcraft:thaumometer"),"Client craft did not record First Steps proof");stage=3;thaumcraft.Thaumcraft.LOGGER.info("BOOK56_CLIENT_CRAFT_OK: actual native output pickup, crystals and ingredients paid, First Steps craft recorded");}
  if(stage>=2&&stage<5&&Files.exists(Path.of("../work/book56-show-bosses"))){actor.closeContainer();actor.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,ItemStack.EMPTY);EldritchProbe.actor=actor;EldritchProbe.stage=99;EldritchProbe.force(l);EldritchProbe.scene(l);System.setProperty("thaumcraft.eldritchProbe","client");actor.teleportTo(l,EldritchProbe.P.getX()+10,EldritchProbe.P.getY()+2,EldritchProbe.P.getZ()+12,Set.of(),180,12,true);stage=5;thaumcraft.Thaumcraft.LOGGER.info("ELDRITCH56_CLIENT_BOSSES_READY: actual final construct scale, praetor, Warden and greater portal");}
 }
}
