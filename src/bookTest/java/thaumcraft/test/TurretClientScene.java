package thaumcraft.test;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.thaumaturgy.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class TurretClientScene {
 static ServerPlayer actor;public static boolean ready;static int ticks,sceneTicks;static CrossbowTurret basic,advanced;static net.minecraft.world.entity.monster.zombie.Zombie target;static boolean menu,damage;
 @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(System.getProperty("thaumcraft.bookProbe","").equals("turret-client")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null)return;var l=actor.level();var p=TurretProbe.P;if(!ready&&++ticks>60){e.server().tickRateManager().setFrozen(false);TurretProbe.clean(l);l.getServer().clockManager().setTotalTicks(l.dimensionType().defaultClock().orElseThrow(),6000);actor.closeContainer();actor.setGameMode(GameType.SURVIVAL);actor.getAbilities().invulnerable=true;actor.onUpdateAbilities();GrappleProbe.knowledge(actor);actor.getInventory().clearContent();actor.teleportTo(l,p.getX()+.5,p.getY(),p.getZ()-3,Set.of(),0,9,true);
  basic=TurretProbe.turret(l,actor,false);basic.setPos(Vec3.atBottomCenterOf(p.offset(-2,0,3)));basic.ammunition.setItem(0,new ItemStack(Items.ARROW,64));advanced=TurretProbe.turret(l,actor,true);advanced.setPos(Vec3.atBottomCenterOf(p.offset(2,0,3)));advanced.ammunition.setItem(0,TurretProbe.tipped().copyWithCount(64));
  target=new net.minecraft.world.entity.monster.zombie.Zombie(EntityTypes.ZOMBIE,l);target.setPos(Vec3.atBottomCenterOf(p.south(12)));target.setNoAi(true);target.setNoGravity(true);target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);target.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.NETHERITE_HELMET));l.addFreshEntity(target);ready=true;thaumcraft.Thaumcraft.LOGGER.info("TURRET67_CLIENT_READY: both original crossbows loaded in a real world");}
  if(!ready)return;if(++sceneTicks==100){for(var t:List.of(basic,advanced)){var q=t.blockPosition().below();l.setBlockAndUpdate(q,Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING,Direction.UP));var d=(net.minecraft.world.level.block.entity.DispenserBlockEntity)l.getBlockEntity(q);d.setItem(0,t.advanced()?TurretProbe.tipped().copyWithCount(64):new ItemStack(Items.ARROW,64));t.ammunition.clearContent();}thaumcraft.Thaumcraft.LOGGER.info("TURRET67_CLIENT_RELOAD_READY: real upward dispensers beneath both turrets");}target.setDeltaMovement(Vec3.ZERO);target.setPos(Vec3.atBottomCenterOf(p.south(12)));if(!damage&&target.getHealth()<1000&&target.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS)){damage=true;thaumcraft.Thaumcraft.LOGGER.info("TURRET67_CLIENT_IMPACT_OK: autonomous shots hit enemy and apply real potion effect");}if(!menu&&Files.exists(Path.of("../work/turret67-menu"))){actor.teleportTo(l,advanced.getX(),advanced.getY(),advanced.getZ()-2,Set.of(),0,10,true);menu=true;thaumcraft.Thaumcraft.LOGGER.info("TURRET67_CLIENT_MENU_READY: owner facing construct for native interaction");}
 }
}
