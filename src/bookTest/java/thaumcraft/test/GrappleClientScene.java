package thaumcraft.test;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.thaumaturgy.*;
import thaumcraft.api.items.RechargeHelper;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class GrappleClientScene {
 static ServerPlayer actor;static GrappleProjectile hook;static int ticks,phase,settle;static double startDistance,startY,maxY;public static volatile boolean ready;static boolean pulled;
 @SubscribeEvent public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){if(System.getProperty("thaumcraft.bookProbe","").equals("grapple-client")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 static void pose(){var p=GrappleProbe.P;actor.teleportTo(actor.level(),p.getX()+.5,p.getY(),p.getZ()+.5,Set.of(),0,-18,true);actor.setDeltaMovement(Vec3.ZERO);}
 static void prepare(boolean offhand){var l=actor.level();if(hook!=null&&!hook.isRemoved())hook.discard();hook=null;settle=0;actor.getInventory().clearContent();actor.setMainArm(HumanoidArm.RIGHT);actor.setItemInHand(offhand?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND,GrappleProbe.gun());actor.setNoGravity(true);pose();phase=offhand?4:1;l.getServer().tickRateManager().setFrozen(false);thaumcraft.Thaumcraft.LOGGER.info(offhand?"GRAPPLE66_OFFHAND_READY: right-dominant avatar, launcher in other hand":"GRAPPLE66_CLIENT_READY: native launcher, regular world and an elevated anchor");}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null)return;var l=actor.level();var p=GrappleProbe.P;if(!ready&&++ticks>60){e.server().tickRateManager().setFrozen(false);l.getChunkAt(p);l.getServer().clockManager().setTotalTicks(l.dimensionType().defaultClock().orElseThrow(),6000);for(var q:BlockPos.betweenClosed(p.offset(-7,-1,-5),p.offset(7,10,20)))l.setBlockAndUpdate(q,q.getY()==p.getY()-1?Blocks.SMOOTH_STONE.defaultBlockState():Blocks.AIR.defaultBlockState());l.getEntitiesOfClass(Entity.class,new AABB(p).inflate(24),en->!(en instanceof net.minecraft.world.entity.player.Player)).forEach(Entity::discard);for(int x=-5;x<=5;x++)for(int y=0;y<=8;y++)l.setBlockAndUpdate(p.offset(x,y,12),(Math.abs(x)<=1&&y>=4&&y<=6?Blocks.GOLD_BLOCK:Blocks.STONE_BRICKS).defaultBlockState());actor.closeContainer();actor.setGameMode(GameType.SURVIVAL);actor.getAbilities().invulnerable=true;actor.onUpdateAbilities();actor.setHealth(actor.getMaxHealth());GrappleProbe.knowledge(actor);ready=true;prepare(false);}
  if(!ready)return;if((phase==1||phase==4)&&hook==null){var hand=phase==4?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND;var gun=actor.getItemInHand(hand);var link=gun.get(ThaumaturgyContent.GRAPPLE_LINK.get());if(link!=null&&l.getEntity(link) instanceof GrappleProjectile shot){hook=shot;GrappleProbe.check(RechargeHelper.getCharge(gun)==99&&shot.hand()==hand,"Native input lost hand or exact charge");thaumcraft.Thaumcraft.LOGGER.info("GRAPPLE66_CLIENT_SHOT_OK hand={}",hand);}}
  if((phase==1||phase==4)&&hook!=null){actor.setDeltaMovement(Vec3.ZERO);if(hook.anchored()&&++settle>=10){pose();e.server().tickRateManager().setFrozen(true);phase=phase==4?5:2;thaumcraft.Thaumcraft.LOGGER.info(phase==5?"GRAPPLE66_OFFHAND_ANCHORED: synchronized original head and gun/cable state":"GRAPPLE66_CLIENT_ANCHORED: stable actual anchor after native firing animation");}}
  if(phase==2&&Files.exists(Path.of("../work/grapple66-offhand")))prepare(true);
  if((phase==2||phase==5)&&Files.exists(Path.of("../work/grapple66-pull"))){var hand=hook.hand();hook.discard();pose();actor.setNoGravity(false);var gun=actor.getItemInHand(hand);GrappleProbe.check(((GrappleGun)gun.getItem()).use(l,actor,hand)==InteractionResult.SUCCESS,"Fresh physical-pull shot failed");hook=(GrappleProjectile)l.getEntity(gun.get(ThaumaturgyContent.GRAPPLE_LINK.get()));startDistance=-1;startY=maxY=actor.getY();e.server().tickRateManager().setFrozen(false);phase=6;thaumcraft.Thaumcraft.LOGGER.info("GRAPPLE66_PULL_STARTED: real player under normal gravity");}
  if(phase==6){if(startDistance<0&&hook.anchored())startDistance=actor.distanceTo(hook);maxY=Math.max(maxY,actor.getY());if(!pulled&&startDistance>0&&actor.distanceTo(hook)<startDistance-2&&maxY>startY+.5){pulled=true;thaumcraft.Thaumcraft.LOGGER.info("GRAPPLE66_CLIENT_PULL_OK: physical player moved toward anchor and rose {} blocks under normal gravity",maxY-startY);}if(hook.isRemoved()){GrappleProbe.check(pulled,"Real pull did not move and lift player");for(var hand:InteractionHand.values())GrappleProbe.check(!actor.getItemInHand(hand).has(ThaumaturgyContent.GRAPPLE_LINK.get()),"Released physical launcher kept unloaded model");phase=7;thaumcraft.Thaumcraft.LOGGER.info("GRAPPLE66_CLIENT_RELEASE_OK: tether expired or arrived, launcher restored its original loaded model");}}
 }
}
