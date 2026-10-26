package thaumcraft.test;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.thaumaturgy.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class ExchangeClient {
 public static final BlockPos P=new BlockPos(112,180,8),Q=P.offset(0,1,3);
 public static volatile int stage;static int ticks;static ServerPlayer actor;static net.minecraft.world.item.ItemStack caster;
 @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(System.getProperty("thaumcraft.exchangeProbe","").equals("client")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null)return;var l=actor.level();
  if(stage==0&&++ticks>80){e.server().tickRateManager().setFrozen(false);l.getServer().clockManager().setTotalTicks(l.dimensionType().defaultClock().orElseThrow(),6000);for(int x=-4;x<=4;x++)for(int z=-4;z<=5;z++){l.getChunkAt(P.offset(x,0,z));l.setBlockAndUpdate(P.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());for(int y=0;y<=3;y++)l.setBlockAndUpdate(P.offset(x,y,z),Blocks.AIR.defaultBlockState());}for(int x=-1;x<=1;x++)l.setBlockAndUpdate(Q.offset(x,0,0),Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(Q,Blocks.STONE_BRICKS.defaultBlockState());
   actor.closeContainer();actor.setGameMode(GameType.SURVIVAL);actor.getInventory().clearContent();actor.teleportTo(l,P.getX()+.5,P.getY(),P.getZ()+.5,Set.of(),0,0,true);actor.setHealth(actor.getMaxHealth());actor.getFoodData().setFoodLevel(20);ExchangeProbe.knowledge(actor);caster=ExchangeProbe.equip(actor,new FocusProgram("bolt",List.of(new FocusProgram.Effect("exchange",1,1))));actor.getInventory().setItem(10,new net.minecraft.world.item.ItemStack(Items.STONE_BRICKS,3));actor.setItemInHand(InteractionHand.OFF_HAND,new net.minecraft.world.item.ItemStack(Items.STICK,17));thaumcraft.aura.AuraManager.addVis(l,P,1000);stage=1;thaumcraft.Thaumcraft.LOGGER.info("EXCHANGE75_CLIENT_READY: native sneak-right-click required to select visible stone bricks");
  }
  if(stage==1&&ExchangeSelection.selected(caster).is(Items.STONE_BRICKS)){l.setBlockAndUpdate(Q,Blocks.STONE.defaultBlockState());stage=2;thaumcraft.Thaumcraft.LOGGER.info("EXCHANGE75_CLIENT_PICK_OK: native sampling persisted; plain right-click required to exchange stone");}
  if(stage==2&&l.getBlockState(Q).is(Blocks.STONE_BRICKS)){ExchangeProbe.check(actor.getMainHandItem()==caster&&actor.getInventory().getItem(10).getCount()==2&&actor.getOffhandItem().is(Items.STICK)&&actor.getOffhandItem().getCount()==17,"Native cast changed wrong stack");int loot=actor.getInventory().getNonEquipmentItems().stream().filter(s->s.is(Items.COBBLESTONE)).mapToInt(s->s.getCount()).sum();ExchangeProbe.check(loot==1,"Native exchange did not pick up exactly one cobblestone");stage=3;thaumcraft.Thaumcraft.LOGGER.info("EXCHANGE75_CLIENT_CAST_OK: physical right-click, selected inventory material, pickup and both hands conserved");}
 }
}
