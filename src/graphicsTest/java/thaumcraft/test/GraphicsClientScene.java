package thaumcraft.test;
import java.util.*;
import java.nio.file.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.mirrors.*;
import thaumcraft.alchemy.*;
import thaumcraft.rifts.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class GraphicsClientScene {
 public static final BlockPos P=new BlockPos(208,180,8),J=P.above(),M=P.offset(6,1,0),N=P.offset(9,1,0);
 public static volatile boolean ready,verified;public static volatile String view="advanced";public static volatile int riftId;static ServerPlayer actor;static int ticks;static FluxRiftEntity rift;
 public static Vec3 camera(String name){return name.equals("crooked")||name.equals("straight")?new Vec3(P.getX()+2.5,P.getY(),P.getZ()+1.9):new Vec3(P.getX()+6.5,P.getY()+2,P.getZ()+9);}
 static void position(String name){var at=camera(name);var target=name.equals("crooked")||name.equals("straight")?Vec3.atCenterOf(J.east(2)).add(0,-.14,.32):Vec3.atCenterOf(P.east(7)).add(0,1.7,0);actor.teleportTo(actor.level(),at.x,at.y,at.z,Set.of(),180,10,true);actor.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,target);view=name;thaumcraft.Thaumcraft.LOGGER.info("GRAPHICS77_VIEW_READY: {}",name);}
 @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(System.getProperty("thaumcraft.graphicsProbe","").equals("client")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null)return;var l=actor.level();
  if(!ready&&++ticks>80){e.server().tickRateManager().setFrozen(false);l.getServer().clockManager().setTotalTicks(l.dimensionType().defaultClock().orElseThrow(),6000);for(int x=-3;x<=15;x++)for(int z=-4;z<=12;z++){l.getChunkAt(P.offset(x,-1,z));l.setBlockAndUpdate(P.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());for(int y=0;y<6;y++)l.setBlock(P.offset(x,y,z),Blocks.AIR.defaultBlockState(),net.minecraft.world.level.block.Block.UPDATE_SKIP_ALL_SIDEEFFECTS|2);}for(int i=0;i<4;i++){l.setBlockAndUpdate(P.east(i),Blocks.STONE_BRICKS.defaultBlockState());GraphicsProbe.jar(l,J.east(i),Direction.SOUTH);}var a=GraphicsProbe.mirror(l,M,Direction.SOUTH);var b=GraphicsProbe.mirror(l,N,Direction.SOUTH);GraphicsProbe.check(a.bind(b.address()),"Scene mirror link failed");for(int i=0;i<10;i++){a.serverTick();b.serverTick();}
   for(var old:l.getEntitiesOfClass(FluxRiftEntity.class,new AABB(P).inflate(30)))old.discard();rift=new FluxRiftEntity(RiftContent.RIFT.get(),l);rift.setSeed(77);rift.setSize(45);rift.setStability(0);rift.setPos(Vec3.atCenterOf(P.offset(13,3,0)));l.addFreshEntity(rift);riftId=rift.getId();actor.closeContainer();actor.setGameMode(GameType.CREATIVE);actor.getInventory().clearContent();actor.getAbilities().flying=true;actor.onUpdateAbilities();actor.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,ItemStack.EMPTY);position("advanced");e.server().tickRateManager().setFrozen(true);ready=true;thaumcraft.Thaumcraft.LOGGER.info("GRAPHICS77_CLIENT_READY: four original jar labels, linked mirrors and isolated rift; only client graphics will change");}
  if(ready&&Files.exists(Path.of("../work/graphics77-view"))){try{var name=Files.readString(Path.of("../work/graphics77-view")).trim();Files.delete(Path.of("../work/graphics77-view"));GraphicsProbe.check(List.of("advanced","crooked","straight","simple","restored").contains(name),"Unknown scene view");position(name);}catch(Exception ex){throw new RuntimeException(ex);}}
  if(ready&&verified){for(int i=0;i<4;i++){var jar=(AlchemyMachine)l.getBlockEntity(J.east(i));GraphicsProbe.check(jar.total()==64&&jar.filter().equals("vitium"),"Graphics option changed jar contents");}var a=(MirrorEntity)l.getBlockEntity(M);GraphicsProbe.check(a.peer()==l.getBlockEntity(N)&&a.queued()==0,"Graphics option changed mirror data");GraphicsProbe.check(rift.size()==45&&rift.stability()==0,"Graphics option changed rift gameplay");verified=false;thaumcraft.Thaumcraft.LOGGER.info("GRAPHICS77_GAMEPLAY_UNCHANGED_OK: amounts, filters, mirror link and rift shape/stability remain intact");}
 }
 @SubscribeEvent public static void stop(net.minecraftforge.event.server.ServerStoppingEvent e){if(actor!=null)e.getServer().tickRateManager().setFrozen(false);}
}
