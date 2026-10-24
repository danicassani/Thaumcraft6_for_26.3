package thaumcraft.test;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.thaumaturgy.*;
import thaumcraft.aura.*;
import thaumcraft.crystals.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class CanvasClientScene {
 static ServerPlayer actor;static FocalEntity table;static int ticks;static boolean ready,edited,properties,sibling,paid,finished;static FocusProgram paidProgram;public static volatile boolean open;
 @SubscribeEvent public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){if(System.getProperty("thaumcraft.bookProbe","").equals("canvas-client")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null)return;var l=actor.level();var p=CanvasProbe.P;if(!ready&&++ticks>60){e.server().tickRateManager().setFrozen(false);l.getChunkAt(p);l.getServer().clockManager().setTotalTicks(l.dimensionType().defaultClock().orElseThrow(),6000);for(var q:BlockPos.betweenClosed(p.offset(-4,-1,-4),p.offset(4,4,5)))l.setBlockAndUpdate(q,q.getY()==p.getY()-1?Blocks.SMOOTH_STONE.defaultBlockState():Blocks.AIR.defaultBlockState());l.getEntitiesOfClass(net.minecraft.world.entity.Entity.class,new net.minecraft.world.phys.AABB(p).inflate(10),en->!(en instanceof net.minecraft.world.entity.player.Player)).forEach(net.minecraft.world.entity.Entity::discard);actor.closeContainer();actor.setGameMode(GameType.SURVIVAL);actor.getAbilities().invulnerable=true;actor.onUpdateAbilities();actor.setHealth(actor.getMaxHealth());actor.getInventory().clearContent();actor.experienceLevel=40;CanvasProbe.knowledge(actor);actor.teleportTo(l,p.getX()+.5,p.getY(),p.getZ()-1.5,Set.of(),0,0,true);l.setBlockAndUpdate(p,ThaumaturgyContent.TABLE.get().defaultBlockState());table=(FocalEntity)l.getBlockEntity(p);table.draft=CanvasProbe.graph();var focus=CanvasProbe.item("focus_3");focus.set(DataComponents.CUSTOM_NAME,Component.literal("Canvas focus"));table.inventory.setItem(0,focus);int i=1;for(String aspect:List.of("motus","ignis","aer","aversio","potentia","perditio","gelum","victus","terra"))table.inventory.setItem(i++,CrystalContent.essence(aspect,32));AuraManager.addVis(l,p,2000);actor.openMenu(table);ready=true;open=true;thaumcraft.Thaumcraft.LOGGER.info("CANVAS65_CLIENT_READY: native graph, real focus and crystal inventory");}
  if(!ready)return;var graph=table.draft;if(!edited&&graph.effects().getFirst().kind().equals("frost")){CanvasProbe.check(graph.effects().get(1).kind().equals("air")&&graph.branches().getFirst().effects().getFirst().kind().equals("fire"),"Native selection changed wrong effect or sibling");edited=true;thaumcraft.Thaumcraft.LOGGER.info("CANVAS65_CLIENT_EDIT_OK: native graph click and palette replaced first effect, leaving second and sibling intact");}
  if(!properties&&edited&&graph.effects().getFirst().power()==2){properties=true;thaumcraft.Thaumcraft.LOGGER.info("CANVAS65_CLIENT_PROPERTY_OK: native click changed selected non-last effect power");}
  if(!sibling&&properties&&graph.branches().getFirst().effects().getFirst().kind().equals("heal")){CanvasProbe.check(graph.effects().getFirst().kind().equals("frost")&&graph.effects().getFirst().power()==2,"Sibling edit overwrote first branch");sibling=true;thaumcraft.Thaumcraft.LOGGER.info("CANVAS65_CLIENT_SIBLING_OK: direct selection of second branch preserved first branch");}
  if(!paid&&sibling&&table.working()){paid=true;paidProgram=graph;int crystals=0;for(int i=1;i<10;i++)crystals+=table.inventory.getItem(i).getCount();CanvasProbe.check(actor.experienceLevel==40-graph.levels()&&crystals==288-graph.crystals().values().stream().mapToInt(Integer::intValue).sum(),"Native craft did not pay exact XP and crystals");CanvasProbe.check(!actor.containerMenu.clickMenuButton(actor,88),"Busy editor accepted mutation");thaumcraft.Thaumcraft.LOGGER.info("CANVAS65_CLIENT_PAYMENT_OK: actual craft click paid all graph costs and locked edits");}
  if(paid&&!finished&&!table.working()){CanvasProbe.check(table.inventory.getItem(0).get(ThaumaturgyContent.PROGRAM.get()).equals(paidProgram),"Completed physical focus lost graph");finished=true;thaumcraft.Thaumcraft.LOGGER.info("CANVAS65_CLIENT_CRAFT_OK: physical named focus received full edited graph");}
 }
}
