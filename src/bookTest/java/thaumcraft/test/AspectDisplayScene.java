package thaumcraft.test;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.research.*;
import thaumcraft.alchemy.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class AspectDisplayScene {
 public static final BlockPos P=new BlockPos(64008,180,8),C=P.south(3);static ServerPlayer actor;static int ticks;public static volatile String phase="";
 @SubscribeEvent public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){if(System.getProperty("thaumcraft.bookProbe","").equals("aspect-display")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null||++ticks<60)return;String next="book";try{next=Files.readString(Path.of("../work/aspects82-phase")).trim();}catch(Exception ignored){}if(phase.equals(next))return;phase=next;var l=actor.level();e.server().tickRateManager().setFrozen(false);l.getChunkAt(P);l.getChunkAt(P.south(16));l.getServer().clockManager().setTotalTicks(l.dimensionType().defaultClock().orElseThrow(),6000);
 if(phase.equals("book")){for(var q:BlockPos.betweenClosed(P.offset(-4,-1,-3),P.offset(4,4,8)))l.setBlockAndUpdate(q,q.getY()==P.getY()-1?Blocks.SMOOTH_STONE.defaultBlockState():Blocks.AIR.defaultBlockState());var state=KnowledgeState.EMPTY;for(var entry:ResearchCatalog.ENTRIES)state=state.stage(entry.key(),entry.stages().size()+1);KnowledgeStore.write(actor,state);actor.setGameMode(GameType.SURVIVAL);actor.getInventory().clearContent();actor.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ResearchContent.BOOK.get()));actor.getInventory().setItem(1,new ItemStack(Items.COAL,64));actor.getInventory().setItem(2,new ItemStack(Items.DIAMOND,7));actor.getInventory().setItem(3,thaumcraft.crystals.CrystalContent.essence("aer",9));actor.getInventory().setItem(4,thaumcraft.crystals.CrystalContent.essence("ignis",12));ScanningService.observe(actor,actor.getInventory().getItem(1));ScanningService.observe(actor,actor.getInventory().getItem(3));actor.teleportTo(l,P.getX()+.5,P.getY(),P.getZ()+.5,Set.of(),0,20,true);actor.setNoGravity(true);actor.getAbilities().invulnerable=true;actor.onUpdateAbilities();ResearchService.openBook(actor);var menu=(ResearchMenu)actor.containerMenu;menu.clickMenuButton(actor,1000+ResearchCatalog.ENTRIES.indexOf(ResearchCatalog.BY_KEY.get("UNLOCKALCHEMY")));menu.broadcastChanges();}
 else if(phase.equals("inventory")){actor.closeContainer();actor.inventoryMenu.broadcastChanges();}
 else{actor.closeContainer();actor.setItemInHand(InteractionHand.MAIN_HAND,phase.equals("crucible")?new ItemStack(ResearchContent.METER.get()):ItemStack.EMPTY);actor.setItemSlot(EquipmentSlot.HEAD,phase.equals("goggles")||phase.equals("empty")||phase.equals("away")?new ItemStack(thaumcraft.content.SimpleContent.ITEMS.get("goggles").get()):ItemStack.EMPTY);actor.teleportTo(l,P.getX()+.5,P.getY(),P.getZ()+.5,Set.of(),phase.equals("away")?90:0,20,true);
 l.setBlockAndUpdate(C,AlchemyContent.CRUCIBLE.get().defaultBlockState());var m=(AlchemyMachine)l.getBlockEntity(C);if(phase.equals("empty"))m.dump();else{var tag=m.saveWithoutMetadata(l.registryAccess());tag.put("alchemy",AlchemyState.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE,new AlchemyState(1,phase.equals("goggles")?Map.of("aqua",7,"ignis",4,"lux",3):Map.of("aqua",10,"ignis",10,"potentia",10),1000,0,0,0,0,-250)).getOrThrow());m.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,l.registryAccess(),tag));m.setChanged();l.sendBlockUpdated(C,m.getBlockState(),m.getBlockState(),3);}}
 thaumcraft.Thaumcraft.LOGGER.info("ASPECTS82_SCENE_READY: "+phase);
 }
}
