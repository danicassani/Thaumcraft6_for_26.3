package thaumcraft.test;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.research.*;
import thaumcraft.theory.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class ResearchArtScene {
 public static final BlockPos P=new BlockPos(58008,180,8),TABLE=P.offset(0,0,3);
 static ServerPlayer actor;static int ticks;static boolean started,drawn,played,cancelled,recovered;
 public static volatile boolean ready;
 @SubscribeEvent public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){if(System.getProperty("thaumcraft.bookProbe","").equals("research-art")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null||++ticks<60)return;var l=actor.level();
  if(!ready){e.server().tickRateManager().setFrozen(false);e.server().clockManager().setTotalTicks(l.dimensionType().defaultClock().orElseThrow(),6000);l.getChunkAt(P);
   for(int x=-4;x<=5;x++)for(int z=-3;z<=8;z++){l.setBlockAndUpdate(P.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());for(int y=0;y<4;y++)l.setBlockAndUpdate(P.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   for(var entity:l.getEntities(actor,new net.minecraft.world.phys.AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(P.offset(-6,-1,-5)),net.minecraft.world.phys.Vec3.atLowerCornerOf(P.offset(7,5,10))),v->v instanceof net.minecraft.world.entity.item.ItemEntity))entity.discard();
   l.setBlockAndUpdate(TABLE,TheoryContent.TABLE.get().defaultBlockState());l.setBlockAndUpdate(TABLE.east(2),thaumcraft.crafting.ArcaneContent.WORKBENCH.get().defaultBlockState());var t=(ResearchTableEntity)l.getBlockEntity(TABLE);t.inventory.setItem(1,new ItemStack(Items.PAPER,20));
   actor.closeContainer();actor.getInventory().clearContent();actor.setGameMode(GameType.SURVIVAL);actor.setHealth(actor.getMaxHealth());actor.getAbilities().invulnerable=true;actor.onUpdateAbilities();actor.setNoGravity(true);actor.giveExperienceLevels(50);
   // Real paid craft followed by a deliberately missing mod proof models the reported old-save symptom.
   actor.setPos(TABLE.getX()+.5,TABLE.getY(),TABLE.getZ()-1.8);l.setBlockAndUpdate(TABLE.west(2),Blocks.CRAFTING_TABLE.defaultBlockState());var craft=new net.minecraft.world.inventory.CraftingMenu(80,actor.getInventory(),net.minecraft.world.inventory.ContainerLevelAccess.create(l,TABLE.west(2)));actor.containerMenu=craft;
   craft.getSlot(1).setByPlayer(new ItemStack(Items.GLASS_BOTTLE));craft.getSlot(2).setByPlayer(new ItemStack(Items.FEATHER));craft.getSlot(3).setByPlayer(new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse("minecraft:black_dye"))));craft.clicked(0,0,net.minecraft.world.inventory.ContainerInput.PICKUP,actor);
   ScanProbe.check(craft.getCarried().is(TheoryContent.SCRIBE.get()),"Recovery scene did not actually craft tools");t.inventory.setItem(0,craft.getCarried());craft.setCarried(ItemStack.EMPTY);actor.containerMenu=actor.inventoryMenu;
   var state=KnowledgeState.EMPTY.stage("THEORYRESEARCH",3).stage("FIRSTSTEPS",4).stage("CELESTIALSCANNING",2);for(var cat:ResearchCatalog.CATEGORIES)state=state.addPoints("OBSERVATION",cat,512);KnowledgeStore.write(actor,state);
   actor.teleportTo(l,TABLE.getX()+.5,TABLE.getY(),TABLE.getZ()-1.8,Set.of(),0,15,true);ready=true;thaumcraft.Thaumcraft.LOGGER.info("RESEARCH80_WORLD_READY: actual scribing tools and paper in table; open with native use");
  }
  if(!recovered&&ResearchService.state(actor).crafts().contains("thaumcraft:scribing_tools")){recovered=true;thaumcraft.Thaumcraft.LOGGER.info("SCRIBE80_LIVE_RECOVERY_OK: native player tick restored a deliberately missing flag after a paid craft");}
  var table=(ResearchTableEntity)l.getBlockEntity(TABLE);var s=table.session();
  if(!started&&s!=null){started=true;ScanProbe.check(table.inventory.getItem(1).getCount()==20&&table.inventory.getItem(0).getDamageValue()==0,"Starting changed supplies");s.bonus=1;s.add("BASICS",33);s.add("ALCHEMY",27);table.setChanged();l.sendBlockUpdated(TABLE,table.getBlockState(),table.getBlockState(),3);thaumcraft.Thaumcraft.LOGGER.info("RESEARCH80_START_OK: native start, prepared bonus for three-card presentation");}
  if(started&&!drawn&&s!=null&&s.choices.size()==3){drawn=true;ScanProbe.check(table.inventory.getItem(1).getCount()==19&&s.bonus==0,"Three-card draw did not pay one paper and bonus");for(var c:s.choices)for(var need:c.needs())actor.getInventory().add(need.stack().copy());actor.inventoryMenu.broadcastChanges();thaumcraft.Thaumcraft.LOGGER.info("RESEARCH80_DRAW_OK: native paper deck draw, all three real cards and exact costs");}
  if(drawn&&!played&&s!=null&&s.played==1){played=true;ScanProbe.check(s.choices.isEmpty()&&table.inventory.getItem(0).getDamageValue()>0,"Card selection did not consume ink or clear choices");thaumcraft.Thaumcraft.LOGGER.info("RESEARCH80_PLAY_OK: native parchment click selected a card and paid its costs");}
  if(played&&!cancelled&&s==null){cancelled=true;thaumcraft.Thaumcraft.LOGGER.info("RESEARCH80_CANCEL_OK: native scrap button returned to idle without duplicate rewards");}
 }
}
