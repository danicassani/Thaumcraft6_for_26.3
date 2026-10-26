package thaumcraft.test;
import java.util.*;
import java.nio.file.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.thaumaturgy.*;
import thaumcraft.research.*;
import thaumcraft.aura.AuraManager;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class BreakClient {
 public static final BlockPos P=new BlockPos(80,180,8), Q=P.offset(0,1,3);
 public static volatile int stage;public static volatile boolean cracks;
 static ServerPlayer player;static ItemStack caster;static int ticks;static float before;static long due;
 @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(System.getProperty("thaumcraft.breakProbe","").equals("client")&&e.getEntity() instanceof ServerPlayer p)player=p;}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(player==null)return;var l=player.level();long now=l.getGameTime();
  if(stage==0&&++ticks>80){e.server().tickRateManager().setFrozen(false);l.getServer().clockManager().setTotalTicks(l.dimensionType().defaultClock().orElseThrow(),6000);for(int x=-4;x<=4;x++)for(int z=-4;z<=5;z++){l.getChunkAt(P.offset(x,0,z));l.setBlockAndUpdate(P.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());for(int y=0;y<=3;y++)l.setBlockAndUpdate(P.offset(x,y,z),Blocks.AIR.defaultBlockState());}l.setBlockAndUpdate(Q,Blocks.IRON_BLOCK.defaultBlockState());for(var it:l.getEntitiesOfClass(ItemEntity.class,new AABB(P).inflate(15)))it.discard();
   player.closeContainer();player.setGameMode(GameType.SURVIVAL);player.getInventory().clearContent();player.teleportTo(l,P.getX()+.5,P.getY(),P.getZ()+.5,Set.of(),0,0,true);player.setHealth(player.getMaxHealth());player.getFoodData().setFoodLevel(20);
   KnowledgeStore.write(player,CheatResearch.complete(KnowledgeState.EMPTY).knowledge());caster=new ItemStack(thaumcraft.content.SimpleContent.ITEMS.get("caster_basic").get());var focus=new ItemStack(thaumcraft.content.SimpleContent.ITEMS.get("focus_3").get());focus.set(ThaumaturgyContent.PROGRAM.get(),new FocusProgram("bolt",List.of(new FocusProgram.Effect("break",1,1,8,0,true))));caster.set(ThaumaturgyContent.FOCUS.get(),focus);player.setItemInHand(InteractionHand.MAIN_HAND,caster);AuraManager.addVis(l,P,1000);before=AuraManager.get(l,Q).vis();stage=1;thaumcraft.Thaumcraft.LOGGER.info("BREAK74_CLIENT_READY: survival, real programmed gauntlet, native right-click required");
  }
  if(stage==1&&cracks){BreakProbe.check(l.getBlockState(Q).is(Blocks.IRON_BLOCK),"Rotura completed before progress");e.server().tickRateManager().setFrozen(true);stage=2;thaumcraft.Thaumcraft.LOGGER.info("BREAK74_CAPTURE_READY: actual crack packet received, world paused for prebuild screenshot");}
  if(stage==2&&Files.exists(Path.of("..","work","break74-resume"))){e.server().tickRateManager().setFrozen(false);due=now+60;stage=3;}
  if(stage==3&&l.isEmptyBlock(Q)){BreakProbe.check(player.getMainHandItem()==caster,"Native cast modified gauntlet");BreakProbe.check(l.getEntitiesOfClass(ItemEntity.class,new AABB(Q).inflate(4)).stream().filter(it->it.getItem().is(Items.IRON_BLOCK)).mapToInt(it->it.getItem().getCount()).sum()==1,"Native cast loot missing/duplicated");BreakProbe.check(AuraManager.get(l,Q).vis()<before,"Native cast did not pay aura");stage=4;thaumcraft.Thaumcraft.LOGGER.info("BREAK74_CLIENT_HARVEST_OK: physical right-click, incremental cracks, survival silk loot and gauntlet retained");}
  if(stage==3)BreakProbe.check(now<due,"Client work did not finish after resume");
 }
}
