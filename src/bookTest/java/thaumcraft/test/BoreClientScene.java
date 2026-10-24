package thaumcraft.test;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.bore.*;
import thaumcraft.research.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class BoreClientScene {
 static final BlockPos P=new BlockPos(64008,190,8);static ServerPlayer actor;static ArcaneBore bore;static int ticks;static boolean ready,drilled,menu;
 @SubscribeEvent public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){if(System.getProperty("thaumcraft.bookProbe","").equals("bore-client")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null)return;var l=actor.level();if(!ready&&++ticks>60){e.server().tickRateManager().setFrozen(false);for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)l.getChunk((P.getX()>>4)+x,z);l.getServer().clockManager().setTotalTicks(l.dimensionType().defaultClock().orElseThrow(),6000);for(var q:BlockPos.betweenClosed(P.offset(-8,-1,-6),P.offset(8,6,14)))l.setBlockAndUpdate(q,q.getY()==P.getY()-1?Blocks.SMOOTH_STONE.defaultBlockState():Blocks.AIR.defaultBlockState());l.getEntitiesOfClass(Entity.class,new AABB(P).inflate(20),en->!(en instanceof net.minecraft.world.entity.player.Player)).forEach(Entity::discard);
  actor.closeContainer();actor.setGameMode(GameType.SURVIVAL);actor.setHealth(actor.getMaxHealth());actor.getAbilities().invulnerable=true;actor.onUpdateAbilities();actor.getInventory().clearContent();actor.setYRot(0);actor.setXRot(0);KnowledgeStore.write(actor,KnowledgeState.EMPTY.flag("!gotthaumonomicon").stage("ARCANEBORE",3));BoreProbe.check(ResearchService.knows(actor,"ARCANEBORE"),"Scene did not finish bore research");actor.teleportTo(l,P.getX()+3.5,P.getY(),P.getZ()-3.5,Set.of(),0,0,true);var placer=thaumcraft.content.SimpleContent.ITEMS.get("arcane_bore").get();actor.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(placer));var hit=new BlockHitResult(Vec3.atCenterOf(P.below()).add(0,.501,0),Direction.UP,P.below(),false);BoreProbe.check(placer.useOn(new UseOnContext(actor,InteractionHand.MAIN_HAND,hit))==InteractionResult.SUCCESS,"Native bore placement failed");bore=l.getEntitiesOfClass(ArcaneBore.class,new AABB(P).inflate(2)).getFirst();BoreProbe.check(actor.getMainHandItem().isEmpty()&&bore.owned(actor),"Bore placement lost ownership or did not consume exactly one item");bore.tool.setItem(0,new ItemStack(Items.DIAMOND_PICKAXE));l.setBlockAndUpdate(P.west(),Blocks.CHEST.defaultBlockState());l.setBlockAndUpdate(P.below().west(),Blocks.REDSTONE_BLOCK.defaultBlockState());for(int x=-3;x<=3;x++)for(int y=0;y<5;y++)for(int z=4;z<=9;z++)l.setBlockAndUpdate(P.offset(x,y,z),(z==4&&y==1&&x==0?Blocks.DIAMOND_ORE:Blocks.STONE).defaultBlockState());thaumcraft.aura.AuraData.get(l).put(thaumcraft.aura.AuraCell.key(P.getX()>>4,P.getZ()>>4),new thaumcraft.aura.AuraCell(200,200,0));actor.teleportTo(l,P.getX()+4.5,P.getY()+1,P.getZ()-4.5,Set.of(),39,19,true);ready=true;thaumcraft.Thaumcraft.LOGGER.info("BORE64_CLIENT_SCENE_READY: native survival placement, original model, live drill beam and physical chest");}
  if(!ready)return;var chest=(ChestBlockEntity)l.getBlockEntity(P.west());if(!drilled&&BoreProbe.count(chest,Items.COBBLESTONE)>=3){drilled=true;thaumcraft.Thaumcraft.LOGGER.info("BORE64_CLIENT_DRILL_OK: real stone removed and at least three physical cobblestone delivered to chest");}
  if(!menu&&Files.exists(Path.of("../work/bore64-menu"))){actor.teleportTo(l,P.getX()+1.5,P.getY(),P.getZ()-2.5,Set.of(),0,0,true);BoreProbe.check(bore.interact(actor,InteractionHand.MAIN_HAND,Vec3.ZERO)==InteractionResult.SUCCESS,"Native bore interaction failed");menu=true;thaumcraft.Thaumcraft.LOGGER.info("BORE64_CLIENT_MENU_READY");}
 }
}
