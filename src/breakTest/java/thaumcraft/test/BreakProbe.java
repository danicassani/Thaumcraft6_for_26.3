package thaumcraft.test;

import java.util.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.aura.*;
import thaumcraft.thaumaturgy.*;

@Mod.EventBusSubscriber(modid="thaumcraft")
public final class BreakProbe {
 public static final BlockPos P=new BlockPos(8,180,8), Q=P.above();
 static ServerPlayer player,other;static int ticks,stage;static long due;static ItemStack held;static BlockPos denied;static float balance;static int events;static boolean done;
 public static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 static String mode(){return System.getProperty("thaumcraft.breakProbe","");}
 static ServerPlayer player(ServerLevel l,String name){var p=new ServerPlayer(l.getServer(),l,new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(java.nio.charset.StandardCharsets.UTF_8)),name),ClientInformation.createDefault());new net.minecraft.server.network.ServerGamePacketListenerImpl(l.getServer(),new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false)){@Override public void send(net.minecraft.network.protocol.Packet<?> packet){}@Override public void send(net.minecraft.network.protocol.Packet<?> packet,io.netty.channel.ChannelFutureListener listener){}};p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);p.setPos(P.getX()+.5,P.getY(),P.getZ()+.5);return p;}
 static FocusProgram.Effect effect(int power,int fortune,boolean silk){return new FocusProgram.Effect("break",power,1,8,fortune,silk);}
 static void hit(ServerPlayer p,BlockPos q,FocusProgram.Effect e){SpellEngine.apply(p,new FocusProgram("bolt",List.of(e)),new BlockHitResult(Vec3.atCenterOf(q),Direction.UP,q,false));}
 static float vis(ServerLevel l){return AuraManager.get(l,Q).vis();}
 static void refill(ServerLevel l){AuraManager.addVis(l,Q,1000);}
 static int drops(ServerLevel l,Item item){return l.getEntitiesOfClass(ItemEntity.class,new AABB(P).inflate(20)).stream().filter(e->e.getItem().is(item)).mapToInt(e->e.getItem().getCount()).sum();}
 static void clear(ServerLevel l){for(var e:l.getEntitiesOfClass(ItemEntity.class,new AABB(P).inflate(20)))e.discard();for(int x=-6;x<=6;x++)for(int y=0;y<=3;y++)for(int z=-6;z<=6;z++)l.setBlockAndUpdate(P.offset(x,y,z),Blocks.AIR.defaultBlockState());}
 @SubscribeEvent public static void protection(net.minecraftforge.event.level.BlockEvent.BreakEvent e){if(!mode().isEmpty()&&e.getPos().equals(denied)){events++;e.setResult(net.minecraftforge.common.util.Result.DENY);}}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(done||!List.of("write","read").contains(mode())||++ticks<40)return;var l=e.server().overworld();long now=l.getGameTime();
  if(player==null){l.setChunkForced(0,0,true);l.getChunkAt(P);player=player(l,"Break74");other=player(l,"Break74Other");held=new ItemStack(Items.STICK,17);player.setItemInHand(InteractionHand.MAIN_HAND,held);refill(l);if(mode().equals("read")){check(l.getBlockState(Q).is(Blocks.OBSIDIAN),"Pending work harvested during shutdown");due=now+100;stage=90;}else clear(l);}
  if(stage==90&&now>=due){check(l.getBlockState(Q).is(Blocks.OBSIDIAN),"Transient work resumed after restart");hit(player,Q,effect(5,0,false));due=now+18;stage=91;return;}
  if(stage==91&&now>=due){check(l.isEmptyBlock(Q),"New cast after restart failed");done=true;thaumcraft.Thaumcraft.LOGGER.info("BREAK74_RESTART_OK: no replay of pending work, new cast succeeds");e.server().halt(false);return;}
  if(stage==0){if(now%20!=2)return;l.setBlockAndUpdate(Q,Blocks.STONE.defaultBlockState());balance=vis(l);hit(player,Q,effect(1,0,false));check(l.getBlockState(Q).is(Blocks.STONE),"Rotura is still instantaneous");check(!FocusBreaking.start(player,Q,Direction.UP,effect(1,0,false),1,0),"Duplicate work accepted");due=now+14;stage=1;return;}
  if(stage==1&&now>=due){check(l.isEmptyBlock(Q)&&drops(l,Items.COBBLESTONE)==1,"Gradual stone harvest failed or duplicated");check(Math.abs(vis(l)-(balance-.25F))<.001,"Local vis not charged exactly once");check(player.getMainHandItem()==held&&held.getCount()==17,"Real held stack changed");l.setBlockAndUpdate(Q,Blocks.OBSIDIAN.defaultBlockState());hit(player,Q,effect(1,0,false));check(l.getBlockState(Q).is(Blocks.OBSIDIAN),"Hard block destroyed instantly");due=now+80;stage=2;return;}
  if(stage==2&&now>=due){check(l.isEmptyBlock(Q)&&drops(l,Items.OBSIDIAN)==1,"Hard block rejected by old hardness cutoff");thaumcraft.Thaumcraft.LOGGER.info("BREAK74_WORK_OK: stone progresses over ticks, original local cost once, duplicate prevention, obsidian mineable at power one");clear(l);stage=3;return;}
  if(stage==3){if(now%20!=2)return;refill(l);l.setBlockAndUpdate(Q,Blocks.DIAMOND_ORE.defaultBlockState());balance=vis(l);hit(player,Q,effect(5,4,true));due=now+5;stage=4;return;}
  if(stage==4&&now>=due){check(l.isEmptyBlock(Q)&&drops(l,Items.DIAMOND_ORE)==1&&drops(l,Items.DIAMOND)==0,"Silk native loot failed");check(Math.abs(vis(l)-(balance-.9F))<.001,"Silk/fortune local cost wrong");clear(l);stage=5;return;}
  if(stage==5){if(now%20!=2)return;denied=Q;l.setBlockAndUpdate(Q,Blocks.STONE.defaultBlockState());balance=vis(l);hit(player,Q,effect(5,0,false));due=now+4;stage=6;return;}
  if(stage==6&&now>=due){check(events==1&&l.getBlockState(Q).is(Blocks.STONE)&&drops(l,Items.COBBLESTONE)==0,"Forge denial bypassed");check(Math.abs(vis(l)-balance)<.001&&player.getMainHandItem()==held,"Denied harvest consumed aura or held item");denied=null;
   for(int i=0;i<32;i++){var q=P.offset(i%8-4,1,i/8-2);l.setBlockAndUpdate(q,Blocks.DIAMOND_ORE.defaultBlockState());hit(player,q,effect(5,4,false));}due=now+6;stage=7;return;}
  if(stage==7&&now>=due){check(drops(l,Items.DIAMOND)>32,"Fortune IV native loot missing");thaumcraft.Thaumcraft.LOGGER.info("BREAK74_LOOT_OK: silk precedence, fortune IV, real held stack preserved, Forge denial refunds aura and yields no loot");clear(l);refill(l);
   l.setBlockAndUpdate(Q,Blocks.STONE.defaultBlockState());hit(player,Q,effect(1,0,false));l.setBlockAndUpdate(Q,Blocks.GOLD_BLOCK.defaultBlockState());
   var dead=Q.east();l.setBlockAndUpdate(dead,Blocks.STONE.defaultBlockState());hit(other,dead,effect(1,0,false));other.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
   check(!FocusBreaking.start(player,Q.above(2),Direction.UP,effect(1,0,false),1,0),"Air accepted");
   var bedrock=Q.west();l.setBlockAndUpdate(bedrock,Blocks.BEDROCK.defaultBlockState());hit(player,bedrock,effect(5,0,false));var chest=Q.south();l.setBlockAndUpdate(chest,Blocks.CHEST.defaultBlockState());hit(player,chest,effect(5,0,false));
   check(!FocusBreaking.start(player,new BlockPos(160008,180,8),Direction.UP,effect(1,0,false),1,0),"Unloaded chunk accepted");check(l.getChunkSource().getChunkNow(10000,0)==null,"Spell loaded distant terrain");due=now+18;stage=8;return;}
  if(stage==8&&now>=due){check(l.getBlockState(Q).is(Blocks.GOLD_BLOCK)&&l.getBlockState(Q.east()).is(Blocks.STONE)&&l.getBlockState(Q.west()).is(Blocks.BEDROCK)&&l.getBlockState(Q.south()).is(Blocks.CHEST),"Changed/protected/spectator block destroyed");clear(l);refill(l);l.setBlockAndUpdate(Q,Blocks.STONE.defaultBlockState());hit(player,Q,effect(1,0,false));AuraManager.drainVis(l,Q,32766,false);due=now+18;stage=9;return;}
  if(stage==9&&now>=due){check(l.getBlockState(Q).is(Blocks.STONE),"Work continued after aura exhausted");refill(l);due=now+18;stage=10;return;}
  if(stage==10&&now>=due){check(l.getBlockState(Q).is(Blocks.STONE),"Cancelled work resumed when aura returned");thaumcraft.Thaumcraft.LOGGER.info("BREAK74_CANCEL_OK: changed state, spectator, bedrock, block entities, unloaded terrain and depleted aura cancel without delayed destruction");
   clear(l);refill(l);for(int x=-1;x<=1;x++)l.setBlockAndUpdate(Q.offset(x,0,0),Blocks.STONE.defaultBlockState());
   var program=new FocusProgram("plan",List.of(effect(1,0,false)),"none",2,10,1,5,false,List.of(),new FocusProgram.PlanSettings(1,0,0,false));
   var state=thaumcraft.research.KnowledgeState.EMPTY;for(var r:thaumcraft.research.ResearchCatalog.ENTRIES)state=state.stage(r.key(),r.stages().size()+1);thaumcraft.research.KnowledgeStore.write(player,state);
   var caster=new ItemStack(thaumcraft.content.SimpleContent.ITEMS.get("caster_basic").get());var focus=new ItemStack(thaumcraft.content.SimpleContent.ITEMS.get("focus_3").get());focus.set(ThaumaturgyContent.PROGRAM.get(),program);caster.set(ThaumaturgyContent.FOCUS.get(),focus);player.setItemInHand(InteractionHand.MAIN_HAND,caster);player.setPos(P.getX()+.5,P.getY(),P.getZ()-3.5);player.setYRot(0);player.setXRot(0);
   check(player.gameMode.useItem(player,l,caster,InteractionHand.MAIN_HAND).consumesAction(),"Native Plan cast failed");check(l.getBlockState(Q).is(Blocks.STONE),"Plan still instant");due=now+14;stage=11;return;
  }
  if(stage==11&&now>=due){check(l.isEmptyBlock(Q)&&l.getBlockState(Q.west()).is(Blocks.STONE)&&l.getBlockState(Q.east()).is(Blocks.STONE),"Plan targets not staggered");due=now+10;stage=12;return;}
  if(stage==12&&now>=due){check(l.isEmptyBlock(Q.west())&&l.isEmptyBlock(Q.east())&&drops(l,Items.COBBLESTONE)==3,"Plan did not finish exactly three native harvests");player.setItemInHand(InteractionHand.MAIN_HAND,held);thaumcraft.Thaumcraft.LOGGER.info("BREAK74_PLAN_OK: native caster schedules three targets with original stagger, native loot once each");
   clear(l);l.setBlockAndUpdate(Q,Blocks.OBSIDIAN.defaultBlockState());hit(player,Q,effect(1,0,false));check(l.getBlockState(Q).is(Blocks.OBSIDIAN),"Restart fixture not pending");done=true;thaumcraft.Thaumcraft.LOGGER.info("BREAK74_WRITE_OK: pending work is transient; saving block for cold restart");e.server().halt(false);
  }
 }
}
