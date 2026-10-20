package thaumcraft.test;
import java.util.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.*;
import thaumcraft.golems.*;
import static thaumcraft.test.ThaumaturgyProbe.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID)
public final class AreaGeometry {
 static int ticks;
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e)throws Exception {if(!System.getProperty("thaumcraft.thaumaturgyProbe","").equals("area-geometry")||++ticks!=80)return;var l=e.server().overworld();var q=AreaClient.Q;for(int x=79;x<=81;x++)l.getChunk(x,0);var p=new ServerPlayer(e.server(),l,new GameProfile(UUID.randomUUID(),"Geometry"),ClientInformation.createDefault()){@Override public void sendSystemMessage(Component c,boolean b){}@Override protected ItemCooldowns createItemCooldowns(){return new ItemCooldowns();}};knowledge(p);p.setPos(Vec3.atCenterOf(q));for(var at:BlockPos.betweenClosed(q.offset(-5,0,-3),q.offset(10,5,3)))l.setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());for(var at:BlockPos.betweenClosed(q.offset(-5,-1,-3),q.offset(10,-1,3)))l.setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());var s=GolemProbe.seal(p,SealKind.BREAKER,q.east(5));l.setBlockAndUpdate(s.getBlockPos(),s.getBlockState().setValue(SealBlock.FACING,Direction.EAST));l.setBlockAndUpdate(s.support(),Blocks.STONE.defaultBlockState());AreaClient.area(p,s,new SealArea(3,1,1));l.setBlockAndUpdate(q.east(6),Blocks.STONE.defaultBlockState());var g=new WorkerGolem(GolemContent.GOLEM.get(),l);g.configure(p,new GolemDesign(1,1,3,0),q.east(2),0);g.setPos(1284.7458715227476,206,9.068427092237084);check(Math.abs(g.getBbWidth()-.56)<.000001&&Math.abs(g.getBbHeight()-1.08)<.000001&&Math.abs(g.getEyeHeight()-.918)<.000001,"Golem constructor retained unscaled dimensions");var target=q.east(6);var end=Vec3.atCenterOf(target).add(0,.6,0);var line=WorkerGolem.class.getDeclaredMethod("line",ServerLevel.class,Vec3.class);line.setAccessible(true);Thaumcraft.LOGGER.info("AREA_GEOMETRY_BEFORE scale={} width={} height={} eye={} dist={} line={} area={} target={}",g.getScale(),g.getBbWidth(),g.getBbHeight(),g.getEyePosition(),g.getEyePosition().distanceTo(Vec3.atCenterOf(target)),line.invoke(g,l,end),s.area(),l.getBlockState(target));g.refreshDimensions();Thaumcraft.LOGGER.info("AREA_GEOMETRY_AFTER scale={} width={} height={} eye={} dist={} line={}",g.getScale(),g.getBbWidth(),g.getBbHeight(),g.getEyePosition(),g.getEyePosition().distanceTo(Vec3.atCenterOf(target)),line.invoke(g,l,end));var job=WorkerGolem.class.getDeclaredField("job");job.setAccessible(true);job.set(g,s);var block=WorkerGolem.class.getDeclaredField("targetBlock");block.setAccessible(true);block.set(g,target);var perform=WorkerGolem.class.getDeclaredMethod("perform",ServerLevel.class,ServerPlayer.class,net.minecraft.world.entity.Entity.class);perform.setAccessible(true);perform.invoke(g,l,p,null);Thaumcraft.LOGGER.info("AREA_GEOMETRY_PERFORM air={} cargo={} active={} state={}",l.isEmptyBlock(target),g.cargo.getItem(0),s.active(),s.getBlockState());e.server().halt(false);}
}
