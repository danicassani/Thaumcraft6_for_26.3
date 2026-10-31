package thaumcraft.test;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.research.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class WaterProbe {
 @SubscribeEvent public static void start(ServerStartedEvent e){if(!System.getProperty("thaumcraft.bookProbe","").equals("water"))return;
  var server=e.getServer();var l=server.overworld();server.tickRateManager().setFrozen(true);var p0=new BlockPos(59008,180,8);l.getChunkAt(p0);l.getChunkAt(p0.south(16));
  Player p=new Player(l,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"WaterScan")){public GameType gameMode(){return GameType.SURVIVAL;}};p.setNoGravity(true);p.setPos(p0.getX()+.5,p0.getY(),p0.getZ()+.5);p.setYRot(0);p.setXRot(0);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ResearchContent.METER.get()));
  for(int z=0;z<10;z++)for(int y=0;y<3;y++)l.setBlockAndUpdate(p0.offset(0,y,z),Blocks.AIR.defaultBlockState());
  var water=p0.offset(0,1,3);var diamond=p0.offset(0,1,6);l.setBlockAndUpdate(water,Blocks.WATER.defaultBlockState());l.setBlockAndUpdate(diamond,Blocks.DIAMOND_BLOCK.defaultBlockState());
  var t=ScanTarget.find(p);ScanProbe.check(!p.isInWater()&&t!=null&&t.blockPos().equals(water)&&t.stack().is(Items.WATER_BUCKET),"Dry player must still target water");
  l.setBlockAndUpdate(p0,Blocks.WATER.defaultBlockState());l.setBlockAndUpdate(p0.above(),Blocks.WATER.defaultBlockState());p.tick();p.setPos(p0.getX()+.5,p0.getY(),p0.getZ()+.5);ScanProbe.check(p.isInWater(),"Fixture player not touching native water");
  t=ScanTarget.find(p);ScanProbe.check(t!=null&&t.blockPos().equals(diamond),"Submerged ray stopped at water");
  l.setBlockAndUpdate(water,Blocks.WATER.defaultBlockState().setValue(net.minecraft.world.level.block.LiquidBlock.LEVEL,3));t=ScanTarget.find(p);ScanProbe.check(t!=null&&t.blockPos().equals(diamond),"Flowing water blocked scan");
  l.setBlockAndUpdate(p0.above(),Blocks.AIR.defaultBlockState());p.tick();p.tick();p.tick();p.setPos(p0.getX()+.5,p0.getY(),p0.getZ()+.5);ScanProbe.check(p.isInWater()&&!p.isUnderWater(),"Fixture feet-only contact failed");t=ScanTarget.find(p);ScanProbe.check(t!=null&&t.blockPos().equals(diamond),"Feet touching water must bypass water too");
  l.setBlockAndUpdate(water,Blocks.STONE.defaultBlockState());t=ScanTarget.find(p);ScanProbe.check(t!=null&&t.blockPos().equals(water)&&t.stack().is(Items.STONE),"Water bypass saw through solid stone");
  l.setBlockAndUpdate(water,Blocks.OAK_FENCE.defaultBlockState().setValue(net.minecraft.world.level.block.FenceBlock.WATERLOGGED,true));t=ScanTarget.find(p);ScanProbe.check(t!=null&&t.blockPos().equals(water)&&t.stack().is(Items.OAK_FENCE),"Waterlogged block outline was ignored");
  l.setBlockAndUpdate(water,Blocks.LAVA.defaultBlockState());t=ScanTarget.find(p);ScanProbe.check(t!=null&&t.blockPos().equals(water)&&t.stack().is(Items.LAVA_BUCKET),"Water bypass incorrectly skipped lava");
  l.setBlockAndUpdate(water,Blocks.WATER.defaultBlockState());var item=new ItemEntity(l,p0.getX()+.5,p0.getY()+1.5,p0.getZ()+4.5,new ItemStack(Items.AMETHYST_SHARD,7));item.setNoGravity(true);item.setNeverPickUp();ScanProbe.check(l.addFreshEntity(item),"Underwater item did not spawn");t=ScanTarget.find(p);ScanProbe.check(t!=null&&t.entity()==item,"Water occluded a physical item");
  KnowledgeStore.write(p,KnowledgeState.EMPTY);ResearchContent.METER.get().use(l,p,InteractionHand.MAIN_HAND);ScanProbe.tick(p,20);ScanProbe.check(ResearchService.state(p).scans().contains("item:minecraft:amethyst_shard")&&item.getItem().getCount()==7,"Timed underwater item scan failed or consumed items");item.discard();
  p.setPos(160000,180,160000);ScanProbe.check(ScanTarget.find(p)==null&&l.getChunkSource().getChunkNow(10000,10000)==null,"Water scan forced distant chunks");
  thaumcraft.Thaumcraft.LOGGER.info("WATER80_NATIVE_OK: dry water, immersed and feet-only contact, flowing water, solid and waterlogged blocks, lava, twenty-tick physical item scan, count and unloaded range");server.halt(false);
 }
}
