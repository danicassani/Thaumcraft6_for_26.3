package thaumcraft.test;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.research.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class AquaticClientScene {
 public static final BlockPos P=new BlockPos(62008,180,8);static ServerPlayer actor;static int ticks;public static volatile String phase="";static boolean reported;
 @SubscribeEvent public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){if(System.getProperty("thaumcraft.bookProbe","").equals("aquatic-client")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null||++ticks<60)return;var l=actor.level();String next="plant";try{next=Files.readString(Path.of("../work/aquatic81-phase")).trim();}catch(Exception ignored){}
 if(!phase.equals(next)){phase=next;reported=false;e.server().tickRateManager().setFrozen(false);e.server().getCommands().performPrefixedCommand(e.server().createCommandSourceStack(),"time set 18000");l.getChunkAt(P);l.getChunkAt(P.south(16));for(int x=-4;x<=4;x++)for(int z=-2;z<=10;z++){l.setBlockAndUpdate(P.offset(x,-1,z),Blocks.DIRT.defaultBlockState());for(int y=0;y<5;y++)l.setBlockAndUpdate(P.offset(x,y,z),phase.equals("plant")?Blocks.WATER.defaultBlockState():Blocks.AIR.defaultBlockState());}
 if(phase.equals("plant")){l.setBlockAndUpdate(P.south(4),Blocks.TALL_SEAGRASS.defaultBlockState());l.setBlockAndUpdate(P.south(4).above(),Blocks.TALL_SEAGRASS.defaultBlockState().setValue(DoublePlantBlock.HALF,net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));}
 else{var phantom=EntityTypes.PHANTOM.create(l,EntitySpawnReason.COMMAND);phantom.setNoAi(true);phantom.setNoGravity(true);phantom.setPos(P.getX()+.5,P.getY()+1.3,P.getZ()+4.5);ScanProbe.check(l.addFreshEntity(phantom),"Client phantom spawn failed");}
 actor.closeContainer();actor.stopUsingItem();actor.getInventory().clearContent();KnowledgeStore.write(actor,KnowledgeState.EMPTY);actor.setGameMode(GameType.SURVIVAL);actor.getAbilities().invulnerable=true;actor.getAbilities().flying=false;actor.onUpdateAbilities();actor.setNoGravity(true);actor.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION,12000,0,false,false));actor.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ResearchContent.METER.get()));actor.teleportTo(l,P.getX()+.5,P.getY(),P.getZ()+.5,Set.of(),0,0,true);thaumcraft.Thaumcraft.LOGGER.info("AQUATIC81_CLIENT_READY: "+phase);}
 String identity=phase.equals("plant")?"item:minecraft:seagrass":"entity:minecraft:phantom";if(!reported&&ResearchService.state(actor).scans().contains(identity)){reported=true;thaumcraft.Thaumcraft.LOGGER.info("AQUATIC81_CLIENT_SCAN_OK: "+phase);}
 }
}
