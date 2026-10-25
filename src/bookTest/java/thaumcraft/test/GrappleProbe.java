package thaumcraft.test;
import java.util.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.minecraft.util.ProblemReporter;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.thaumaturgy.*;
import thaumcraft.research.*;
import thaumcraft.api.items.RechargeHelper;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class GrappleProbe {
 static final BlockPos P=new BlockPos(64008,160,8);
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 static ItemStack gun(){var s=new ItemStack(thaumcraft.content.SimpleContent.ITEMS.get("grapple_gun").get());s.set(thaumcraft.devices.DeviceContent.CHARGE.get(),100);return s;}
 static void knowledge(ServerPlayer p){var s=KnowledgeState.EMPTY;for(var e:ResearchCatalog.ENTRIES)s=s.stage(e.key(),e.stages().size()+1);check(KnowledgeStore.write(p,s),"Cannot prepare grapple research");}
 static void cooldown(ServerPlayer p){for(int i=0;i<12;i++)p.getCooldowns().tick();}
 static GrappleProjectile fire(ServerPlayer p,InteractionHand hand){cooldown(p);var gun=p.getItemInHand(hand);check(((GrappleGun)gun.getItem()).use(p.level(),p,hand)==InteractionResult.SUCCESS,"Native launcher refused shot");var link=gun.get(ThaumaturgyContent.GRAPPLE_LINK.get());check(link!=null&&p.level().getEntity(link) instanceof GrappleProjectile,"Shot lacks authoritative gun link");var g=(GrappleProjectile)p.level().getEntity(link);check(g.hand()==hand&&g.getOwner()==p,"Shot lost hand or owner");return g;}
 static GrappleProjectile attach(ServerPlayer p,InteractionHand hand){var g=fire(p,hand);for(int i=0;i<30&&!g.anchored()&&!g.isRemoved();i++)g.tick();check(g.anchored()&&!g.isRemoved()&&g.isNoGravity()&&g.getDeltaMovement().lengthSqr()<.001,"Native projectile failed to anchor cleanly");return g;}
 static void wall(ServerLevel l){for(int x=-2;x<=2;x++)for(int y=0;y<=8;y++)l.setBlockAndUpdate(P.offset(x,y,8),Blocks.STONE.defaultBlockState());}
 static void run(ServerPlayer p){var l=p.level();for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)l.getChunk((P.getX()>>4)+x,z);for(var q:BlockPos.betweenClosed(P.offset(-3,-1,-3),P.offset(3,9,10)))l.setBlockAndUpdate(q,Blocks.AIR.defaultBlockState());wall(l);p.setPos(P.getX()+.5,P.getY(),P.getZ()+.5);p.setYRot(0);p.setXRot(-15);p.setNoGravity(true);knowledge(p);
  for(var arm:HumanoidArm.values())for(var hand:InteractionHand.values()){p.getInventory().clearContent();p.setMainArm(arm);var gun=gun();p.setItemInHand(hand,gun);var g=attach(p,hand);check(RechargeHelper.getCharge(gun)==99,"Shot did not cost exactly one charge");p.fallDistance=12;p.setDeltaMovement(Vec3.ZERO);for(int i=0;i<20;i++)g.tick();check(p.getDeltaMovement().length()<=1.201&&p.getDeltaMovement().dot(g.position().subtract(p.getEyePosition()))>0&&p.fallDistance==0,"Anchored pull or velocity cap failed");
   var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,l.registryAccess());g.saveWithoutId(out);var copy=new GrappleProjectile(ThaumaturgyContent.GRAPPLE.get(),l);copy.load(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),out.buildResult()));check(copy.anchored()&&copy.hand()==hand&&copy.flightAge()==g.flightAge()&&copy.anchorAge(0)==g.anchorAge(0),"Anchor snapshot lost render/hand metadata");check(((GrappleGun)gun.getItem()).use(l,p,hand)==InteractionResult.SUCCESS&&g.isRemoved()&&!gun.has(ThaumaturgyContent.GRAPPLE_LINK.get())&&RechargeHelper.getCharge(gun)==99,"Native release kept cable state or charged twice");}
  thaumcraft.Thaumcraft.LOGGER.info("GRAPPLE66_HANDS_PULL_SAVE_OK: both hands and dominant arms, exact charge, clean anchor, capped pull, snapshot and release");
  p.getInventory().clearContent();p.setMainArm(HumanoidArm.RIGHT);var gun=gun();p.setItemInHand(InteractionHand.MAIN_HAND,gun);var g=attach(p,InteractionHand.MAIN_HAND);p.setShiftKeyDown(true);g.tick();p.setShiftKeyDown(false);check(g.isRemoved()&&!gun.has(ThaumaturgyContent.GRAPPLE_LINK.get()),"Sneak release left active model");
  g=attach(p,InteractionHand.MAIN_HAND);p.getInventory().setItem(1,gun);p.setItemInHand(InteractionHand.MAIN_HAND,gun());g.tick();check(g.isRemoved()&&!gun.has(ThaumaturgyContent.GRAPPLE_LINK.get()),"Another gun inherited the tether");
  p.setItemInHand(InteractionHand.MAIN_HAND,gun);g=attach(p,InteractionHand.MAIN_HAND);for(int x=-2;x<=2;x++)for(int y=0;y<=8;y++)l.setBlockAndUpdate(P.offset(x,y,3),Blocks.STONE.defaultBlockState());g.tick();check(g.isRemoved()&&!gun.has(ThaumaturgyContent.GRAPPLE_LINK.get()),"Cable crossed an obstruction");for(int x=-2;x<=2;x++)for(int y=0;y<=8;y++)l.setBlockAndUpdate(P.offset(x,y,3),Blocks.AIR.defaultBlockState());
  g=attach(p,InteractionHand.MAIN_HAND);var anchor=BlockPos.containing(g.position().add(0,0,.1));l.setBlockAndUpdate(anchor,Blocks.AIR.defaultBlockState());g.tick();check(g.isRemoved(),"Changed support did not release tether");wall(l);
  g=fire(p,InteractionHand.MAIN_HAND);var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,l.registryAccess());g.saveWithoutId(out);var saved=out.buildResult();saved.putLong("expires",l.getGameTime()-1);g.load(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),saved));g.setOwner(p);g.tick();check(g.isRemoved()&&!gun.has(ThaumaturgyContent.GRAPPLE_LINK.get()),"Expired shot left active model");
  gun.set(ThaumaturgyContent.GRAPPLE_LINK.get(),UUID.randomUUID());((GrappleGun)gun.getItem()).inventoryTick(gun,l,p,EquipmentSlot.MAINHAND);check(!gun.has(ThaumaturgyContent.GRAPPLE_LINK.get()),"Stale saved link did not reset item model");
  var empty=new ItemStack(gun.getItem());p.setItemInHand(InteractionHand.MAIN_HAND,empty);cooldown(p);check(((GrappleGun)empty.getItem()).use(l,p,InteractionHand.MAIN_HAND)==InteractionResult.FAIL&&!empty.has(ThaumaturgyContent.GRAPPLE_LINK.get()),"Empty launcher created cable");
  thaumcraft.Thaumcraft.LOGGER.info("GRAPPLE66_RELEASE_RULES_OK: sneak, replacement gun, obstruction, support, expiry, stale model and zero charge");
 }
 @SubscribeEvent public static void start(ServerStartedEvent e){if(!System.getProperty("thaumcraft.bookProbe","").equals("grapple"))return;var l=e.getServer().overworld();e.getServer().tickRateManager().setFrozen(true);var p=new ServerPlayer(e.getServer(),l,new GameProfile(UUID.randomUUID(),"GrappleProof"),ClientInformation.createDefault()){@Override public void sendSystemMessage(net.minecraft.network.chat.Component c,boolean overlay){}@Override protected ItemCooldowns createItemCooldowns(){return new ItemCooldowns();}};new net.minecraft.server.network.ServerGamePacketListenerImpl(e.getServer(),new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false)){@Override public void send(net.minecraft.network.protocol.Packet<?> packet){}@Override public void send(net.minecraft.network.protocol.Packet<?> packet,io.netty.channel.ChannelFutureListener listener){}};p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);run(p);thaumcraft.Thaumcraft.LOGGER.info("GRAPPLE66_NATIVE_OK");e.getServer().halt(false);}
}
