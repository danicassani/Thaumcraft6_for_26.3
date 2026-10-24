package thaumcraft.test;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.research.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class SightClientScene {
 public static final BlockPos P=new BlockPos(61008,180,8);
 static ServerPlayer actor;public static volatile boolean ready;public static volatile int phase;static int ticks;static ItemEntity drop;static Entity cow;static boolean ground,block,mob,other;
 @SubscribeEvent public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){if(System.getProperty("thaumcraft.bookProbe","").equals("sight-client")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 static void camera(float pitch){actor.teleportTo(actor.level(),P.getX()+.5,P.getY(),P.getZ()+.5,Set.of(),0,pitch,true);}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null)return;var l=actor.level();
  if(!ready&&++ticks>60){e.server().tickRateManager().setFrozen(false);l.getChunkAt(P);
   for(int x=-5;x<=5;x++)for(int z=-3;z<=10;z++){l.setBlockAndUpdate(P.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());for(int y=0;y<5;y++)l.setBlockAndUpdate(P.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   actor.closeContainer();actor.getInventory().clearContent();actor.setGameMode(GameType.SURVIVAL);actor.setHealth(actor.getMaxHealth());actor.getAbilities().invulnerable=true;actor.onUpdateAbilities();actor.setNoGravity(true);camera(0);
   actor.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND,7));drop=actor.drop(actor.getMainHandItem().split(7),true,net.minecraft.util.Prediction.SERVER_ONLY);ScanProbe.check(drop!=null,"Native toss returned null");drop.setPos(P.getX()+.5,P.getY()+.05,P.getZ()+3);drop.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);drop.setNoGravity(true);drop.setNeverPickUp();drop.setUnlimitedLifetime();
   actor.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ResearchContent.METER.get()));actor.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.EMERALD,3));KnowledgeStore.write(actor,KnowledgeState.EMPTY);camera((float)-Math.toDegrees(Math.atan2(drop.getY()+.125-actor.getEyeY(),2.5)));
   var target=ScanTarget.find(actor);ScanProbe.check(target!=null&&target.entity()==drop,"Client scene did not target its ground item");phase=1;ready=true;thaumcraft.Thaumcraft.LOGGER.info("SIGHT61_SCENE_READY: native player-tossed stack of seven diamonds resting over a solid floor");
  }
  if(!ready)return;
  ScanProbe.check(actor.isAlive()&&drop!=null&&drop.isAlive()&&drop.getItem().getCount()==7,"Test actor or dropped stack changed");
  var knowledge=ResearchService.state(actor);
  if(!ground&&knowledge.scans().contains("item:minecraft:diamond")){ground=true;thaumcraft.Thaumcraft.LOGGER.info("SIGHT61_CLIENT_GROUND_OK: native use scanned the item, retained all seven units and recorded item identity");}
  if(phase==1&&Files.exists(Path.of("../work/sight61-block"))){drop.setPos(P.getX()+4,P.getY()+.05,P.getZ()+3);l.setBlockAndUpdate(P.offset(0,1,3),Blocks.GLASS.defaultBlockState());camera(0);phase=2;}
  if(!block&&knowledge.scans().contains("item:minecraft:glass")){block=true;thaumcraft.Thaumcraft.LOGGER.info("SIGHT61_CLIENT_BLOCK_OK");}
  if(phase==2&&Files.exists(Path.of("../work/sight61-cow"))){l.setBlockAndUpdate(P.offset(0,1,3),Blocks.AIR.defaultBlockState());var c=EntityTypes.COW.create(l,EntitySpawnReason.COMMAND);c.setPos(P.getX()+.5,P.getY(),P.getZ()+3);c.setNoAi(true);ScanProbe.check(l.addFreshEntity(c),"Cow not spawned");cow=c;camera(10);phase=3;}
  if(!mob&&knowledge.scans().contains("entity:minecraft:cow")){mob=true;thaumcraft.Thaumcraft.LOGGER.info("SIGHT61_CLIENT_MOB_OK");}
  if(!other&&knowledge.scans().contains("item:minecraft:emerald")){other=true;ScanProbe.check(actor.getOffhandItem().getCount()==3,"Other-hand stack changed");thaumcraft.Thaumcraft.LOGGER.info("SIGHT61_CLIENT_OTHER_OK");}
 }
}
