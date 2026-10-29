package thaumcraft.test;
import java.util.*;
import java.nio.file.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.thaumaturgy.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class ContainerFocusClient {
 public static final BlockPos P=new BlockPos(176,180,8),Q=P.offset(0,1,3);
 public static volatile int stage;public static volatile boolean cracks;static int ticks;static ServerPlayer actor;static ItemStack caster;static long due;
 static void equip(String effect){caster=ExchangeProbe.equip(actor,new FocusProgram("bolt",List.of(ContainerFocusProbe.effect(effect))));caster.set(ThaumaturgyContent.PICKED_BLOCK.get(),new ItemStack(Items.DIRT));}
 static int total(){int n=0;for(int i=0;i<actor.getInventory().getContainerSize();i++)n+=ContainerFocusProbe.inside(actor.getInventory().getItem(i));for(var entity:actor.level().getEntitiesOfClass(ItemEntity.class,new AABB(Q).inflate(20)))n+=ContainerFocusProbe.inside(entity.getItem());return n;}
 @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(System.getProperty("thaumcraft.containerFocusProbe","").equals("client")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null)return;var l=actor.level();long now=l.getGameTime();
  if(stage==0&&++ticks>80){e.server().tickRateManager().setFrozen(false);l.getServer().clockManager().setTotalTicks(l.dimensionType().defaultClock().orElseThrow(),6000);for(int x=-4;x<=4;x++)for(int z=-4;z<=5;z++){l.getChunkAt(P.offset(x,0,z));l.setBlockAndUpdate(P.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());for(int y=0;y<=3;y++)l.setBlock(P.offset(x,y,z),Blocks.AIR.defaultBlockState(),Block.UPDATE_SKIP_ALL_SIDEEFFECTS|Block.UPDATE_CLIENTS);}for(var drop:l.getEntitiesOfClass(ItemEntity.class,new AABB(P).inflate(20)))drop.discard();
   actor.closeContainer();actor.setGameMode(GameType.SURVIVAL);actor.getInventory().clearContent();actor.teleportTo(l,P.getX()+.5,P.getY(),P.getZ()+.5,Set.of(),0,0,true);actor.setHealth(actor.getMaxHealth());actor.getFoodData().setFoodLevel(20);ExchangeProbe.knowledge(actor);equip("exchange");actor.getInventory().setItem(10,new ItemStack(Items.DIRT,4));actor.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.STICK,17));l.setBlockAndUpdate(Q,Blocks.CHEST.defaultBlockState());((ChestBlockEntity)l.getBlockEntity(Q)).setItem(0,ContainerFocusProbe.treasure());l.setBlockAndUpdate(Q.west(2),Blocks.CHEST.defaultBlockState());thaumcraft.aura.AuraManager.addVis(l,P,1000);stage=1;thaumcraft.Thaumcraft.LOGGER.info("CONTAINER76_CLIENT_READY: physical right-click on filled chest required; exchange must cast instead of opening GUI");}
  if(stage==1&&l.getBlockState(Q).is(Blocks.DIRT)){ExchangeProbe.check(total()==13&&actor.getInventory().getItem(10).getCount()==3&&actor.containerMenu==actor.inventoryMenu&&actor.getOffhandItem().getCount()==17,"Native exchange lost content, consumed wrong material or opened GUI");stage=2;thaumcraft.Thaumcraft.LOGGER.info("CONTAINER76_CLIENT_EXCHANGE_OK: native right-click replaced full chest, exactly thirteen named diamonds preserved, no container GUI");}
  if(stage==2&&Files.exists(Path.of("..","work","containers76-break"))){equip("break");l.setBlockAndUpdate(Q,Blocks.CHEST.defaultBlockState());((ChestBlockEntity)l.getBlockEntity(Q)).setItem(0,ContainerFocusProbe.treasure().copyWithCount(7));stage=3;thaumcraft.Thaumcraft.LOGGER.info("CONTAINER76_CLIENT_BREAK_READY: physical right-click required on second full chest");}
  if(stage==3&&cracks){ExchangeProbe.check(l.getBlockEntity(Q) instanceof ChestBlockEntity c&&c.getItem(0).getCount()==7&&actor.containerMenu==actor.inventoryMenu,"Progress modified inventory or opened GUI");e.server().tickRateManager().setFrozen(true);stage=4;thaumcraft.Thaumcraft.LOGGER.info("CONTAINER76_CAPTURE_READY: native crack packets on full chest, content still seven; paused for prebuild image");}
  if(stage==4&&Files.exists(Path.of("..","work","containers76-resume"))){e.server().tickRateManager().setFrozen(false);stage=5;due=now+60;}
  if(stage==5&&l.isEmptyBlock(Q)){ExchangeProbe.check(total()==20&&actor.getMainHandItem()==caster&&actor.getOffhandItem().getCount()==17&&actor.containerMenu==actor.inventoryMenu,"Native gradual harvest lost or duplicated contents");stage=6;thaumcraft.Thaumcraft.LOGGER.info("CONTAINER76_CLIENT_BREAK_OK: second physical cast mined chest gradually; total thirteen plus seven named diamonds preserved");}
  if(stage==5)ExchangeProbe.check(now<due,"Gradual chest harvest did not finish");
 }
}
