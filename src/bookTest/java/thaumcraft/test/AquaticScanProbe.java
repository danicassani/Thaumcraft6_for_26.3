package thaumcraft.test;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.research.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class AquaticScanProbe {
 @SubscribeEvent public static void start(ServerStartedEvent e){String mode=System.getProperty("thaumcraft.bookProbe","");if(!mode.equals("aquatic")&&!mode.equals("aquatic-baseline"))return;
 var server=e.getServer();var l=server.overworld();server.tickRateManager().setFrozen(true);var origin=new BlockPos(61008,180,8);for(int cx=-1;cx<=1;cx++)for(int cz=-1;cz<=1;cz++)l.getChunkAt(origin.offset(cx*16,0,cz*16));
 Player p=new Player(l,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"AquaticScan")){public GameType gameMode(){return GameType.SURVIVAL;}};p.setNoGravity(true);p.setPos(origin.getX()+.5,origin.getY(),origin.getZ()+.5);p.setYRot(0);p.setXRot(5);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ResearchContent.METER.get()));
 for(int z=0;z<10;z++)for(int y=0;y<4;y++)l.setBlockAndUpdate(origin.offset(0,y,z),Blocks.WATER.defaultBlockState());p.tick();p.tick();p.tick();p.setPos(origin.getX()+.5,origin.getY(),origin.getZ()+.5);ScanProbe.check(p.isInWater(),"Fixture not wet");var pos=origin.offset(0,1,3);
 var plants=List.of(Blocks.SEAGRASS.defaultBlockState(),Blocks.TALL_SEAGRASS.defaultBlockState(),Blocks.TALL_SEAGRASS.defaultBlockState().setValue(DoublePlantBlock.HALF,net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER),Blocks.KELP.defaultBlockState(),Blocks.KELP_PLANT.defaultBlockState());
 for(BlockState plant:plants){l.setBlockAndUpdate(pos.above(),Blocks.WATER.defaultBlockState());l.setBlockAndUpdate(pos.below(),Blocks.SAND.defaultBlockState());if(plant.is(Blocks.TALL_SEAGRASS)&&plant.getValue(DoublePlantBlock.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER){l.setBlockAndUpdate(pos.below(2),Blocks.SAND.defaultBlockState());l.setBlockAndUpdate(pos.below(),Blocks.TALL_SEAGRASS.defaultBlockState());}l.setBlockAndUpdate(pos,plant);var t=ScanTarget.find(p);thaumcraft.Thaumcraft.LOGGER.info("AQUATIC81_TARGET: "+plant+" -> "+(t==null?"none":t.identity()));if(mode.endsWith("baseline"))continue;
 var expected=plant.is(Blocks.SEAGRASS)||plant.is(Blocks.TALL_SEAGRASS)?Items.SEAGRASS:Items.KELP;
 ScanProbe.check(t!=null&&t.blockPos().equals(pos)&&t.stack().is(expected),"Wrong aquatic plant identity: "+plant);
 KnowledgeStore.write(p,KnowledgeState.EMPTY);var before=l.getBlockState(pos);ResearchContent.METER.get().use(l,p,InteractionHand.MAIN_HAND);ScanProbe.tick(p,19);ScanProbe.check(ResearchService.state(p).scans().isEmpty(),"Plant studied too early");ScanProbe.tick(p,1);
 ScanProbe.check(ResearchService.state(p).scans().contains("item:minecraft:"+(expected==Items.SEAGRASS?"seagrass":"kelp"))&&!ResearchService.state(p).scans().contains("item:minecraft:water_bucket"),"Timed plant scan failed");ScanProbe.check(l.getBlockState(pos).equals(before),"Scan changed plant");var k=ResearchService.state(p);ResearchContent.METER.get().use(l,p,InteractionHand.MAIN_HAND);ScanProbe.tick(p,20);ScanProbe.check(ResearchService.state(p).equals(k),"Duplicate plant scan rewarded");}
 l.setBlockAndUpdate(pos,Blocks.WATER.defaultBlockState());var phantom=EntityTypes.PHANTOM.create(l,EntitySpawnReason.COMMAND);phantom.setNoAi(true);phantom.setNoGravity(true);phantom.setPos(origin.getX()+.5,origin.getY()+1.4,origin.getZ()+4.5);ScanProbe.check(l.addFreshEntity(phantom),"Phantom spawn failed");p.setXRot(0);var t=ScanTarget.find(p);ScanProbe.check(t!=null&&t.entity()==phantom,"Phantom not targeted");thaumcraft.Thaumcraft.LOGGER.info("AQUATIC81_PHANTOM_ASPECTS: "+AspectCatalog.entity(phantom));
 if(!mode.endsWith("baseline")){KnowledgeStore.write(p,KnowledgeState.EMPTY);var preview=ScanInspection.describe(p,t);ScanProbe.check(preview.scannable()&&preview.aspects().isEmpty(),"Phantom not scannable or leaked aspects");ResearchContent.METER.get().use(l,p,InteractionHand.MAIN_HAND);ScanProbe.tick(p,20);ScanProbe.check(ResearchService.state(p).scans().contains("entity:minecraft:phantom"),"Timed phantom scan failed");ScanProbe.check(AspectCatalog.entity(phantom).equals(Map.of("exanimis",10,"volatus",10,"bestia",5)),"Phantom aspects wrong");var k=ResearchService.state(p);ScanProbe.check(!ScanningService.observe(p,phantom)&&ResearchService.state(p).equals(k),"Duplicate phantom reward");l.setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());ScanProbe.check(ScanTarget.find(p).entity()==null,"Phantom scanned through solid wall");}
 phantom.discard();thaumcraft.Thaumcraft.LOGGER.info(mode.endsWith("baseline")?"AQUATIC81_BASELINE_OK":"AQUATIC81_NATIVE_OK");server.halt(false);
 }
}
