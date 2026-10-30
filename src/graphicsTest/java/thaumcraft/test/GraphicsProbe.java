package thaumcraft.test;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.alchemy.*;
import thaumcraft.mirrors.*;
import thaumcraft.config.ThaumcraftConfig;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class GraphicsProbe {
 public static final BlockPos P=new BlockPos(8,180,8),M=P.offset(8,1,0),N=M.east(3);
 static int ticks;static boolean done;
 static String mode(){return System.getProperty("thaumcraft.graphicsProbe","");}
 public static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 public static AlchemyMachine jar(ServerLevel l,BlockPos p,Direction face){l.getChunkAt(p);l.setBlockAndUpdate(p,AlchemyContent.JAR.get().defaultBlockState().setValue(AlchemyBlock.FACING,face));var m=(AlchemyMachine)l.getBlockEntity(p);var sample=AlchemyContent.filled(AlchemyContent.JAR.get().asItem(),"vitium",64);sample.set(thaumcraft.essentia.EssentiaContent.FILTER.get(),"vitium");m.placeContents(sample);return m;}
 public static MirrorEntity mirror(ServerLevel l,BlockPos p,Direction face){l.getChunkAt(p);l.getChunkAt(p.relative(face.getOpposite()));l.setBlockAndUpdate(p.relative(face.getOpposite()),Blocks.STONE_BRICKS.defaultBlockState());l.setBlockAndUpdate(p,MirrorContent.MIRROR.get().defaultBlockState().setValue(MirrorBlock.FACING,face));return (MirrorEntity)l.getBlockEntity(p);}
 @SubscribeEvent public static void start(ServerStartedEvent e){if(List.of("write","read").contains(mode()))e.getServer().tickRateManager().setFrozen(true);}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(done||!List.of("write","read").contains(mode())||++ticks<40)return;var l=e.server().overworld();l.setChunkForced(0,0,true);l.getChunkAt(P);l.getChunkAt(M);check(!ThaumcraftConfig.CLIENT.isLoaded(),"Dedicated server unexpectedly requires client settings");
  if(mode().equals("write")){for(int x=0;x<=14;x++)for(int y=0;y<=2;y++)for(int z=-1;z<=1;z++)l.setBlock(P.offset(x,y,z),Blocks.AIR.defaultBlockState(),net.minecraft.world.level.block.Block.UPDATE_SKIP_ALL_SIDEEFFECTS|2);for(var item:l.getEntitiesOfClass(ItemEntity.class,new AABB(P).inflate(20)))item.discard();int i=0;for(var face:Direction.Plane.HORIZONTAL){var jar=jar(l,P.east(i++),face);check(jar.total()==64&&jar.filter().equals("vitium"),"Jar state invalid");}var a=mirror(l,M,Direction.SOUTH);var b=mirror(l,N,Direction.SOUTH);check(a.bind(b.address())&&a.peer()==b,"Real mirror link failed");var handler=a.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER,Direction.UP).orElseThrow(()->new AssertionError("Input capability missing"));check(handler.insertItem(0,new ItemStack(Items.DIAMOND,3),false).isEmpty()&&b.queued()==3,"Native mirror transfer failed");check(b.ejectOne()&&b.queued()==2,"Native mirror output failed");check(l.getEntitiesOfClass(ItemEntity.class,new AABB(N).inflate(3)).stream().filter(x->x.getItem().is(Items.DIAMOND)).mapToInt(x->x.getItem().getCount()).sum()==1,"Mirror output amount wrong");thaumcraft.Thaumcraft.LOGGER.info("GRAPHICS77_WRITE_OK: dedicated server without client config; four jar faces, linked mirrors and native three-item transfer, one ejected, two persisted");}
  else{for(int i=0;i<4;i++){check(l.getBlockEntity(P.east(i)) instanceof AlchemyMachine,"Jar missing after restart");var jar=(AlchemyMachine)l.getBlockEntity(P.east(i));check(jar.total()==64&&jar.filter().equals("vitium"),"Visual update changed jar state");}var a=(MirrorEntity)l.getBlockEntity(M);var b=(MirrorEntity)l.getBlockEntity(N);check(a!=null&&a.peer()==b&&b.queued()==2,"Visual update changed mirror link or queue");check(b.ejectOne()&&b.queued()==1,"Restored mirror cannot transfer");thaumcraft.Thaumcraft.LOGGER.info("GRAPHICS77_RESTART_OK: four jar filters/amounts and mirror link/queue persist and operate without client config");}
  done=true;e.server().halt(false);
 }
}
