package thaumcraft.test;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.*;
import thaumcraft.golems.*;
import static thaumcraft.test.ThaumaturgyProbe.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID)
public final class MatchingClient {
 static final BlockPos Q=new BlockPos(1472,205,8);static int ticks,stage=-1;static long due;static SealEntity s,dest;static ChestBlockEntity in,out;static WorkerGolem g,other;static ItemEntity good,bad;static boolean tested;
 static boolean mode(){return System.getProperty("thaumcraft.thaumaturgyProbe","").equals("matching-client");}
 static ItemEntity drop(ServerLevel l,BlockPos at,ItemStack stack){var e=new ItemEntity(l,at.getX()+.5,at.getY()+.1,at.getZ()+.5,stack);e.setNoGravity(true);e.setDeltaMovement(Vec3.ZERO);l.addFreshEntity(e);return e;}
 static void open(ServerPlayer p,SealEntity seal){p.teleportTo(p.level(),seal.getBlockPos().getX()+.5,Q.getY(),seal.getBlockPos().getZ()+3.5,Set.of(),180,0,true);check(!p.isDeadOrDying(),"Test player died before seal menu");p.openMenu(seal);}
 @SubscribeEvent public static void joined(net.minecraftforge.event.entity.EntityJoinLevelEvent e){if(mode()&&e.getEntity() instanceof WorkerGolem w&&w.level() instanceof ServerLevel)AreaClient.spawned=w;}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(!mode()||e.server().isDedicatedServer()||e.server().getPlayerList().getPlayers().isEmpty()||++ticks<80)return;var p=e.server().getPlayerList().getPlayers().getFirst();var l=p.level();long now=l.getGameTime();p.resetFallDistance();
 if(stage==-1){p.setGameMode(GameType.CREATIVE);for(int x=91;x<=93;x++)for(int z=-1;z<=1;z++)l.setChunkForced(x,z,true);p.teleportTo(l,Q.getX()+.5,Q.getY(),Q.getZ()+5.5,Set.of(),180,0,true);stage=0;due=now+60;return;}
 if(stage==0&&now>=due){knowledge(p);MatchingRules.verify();for(var w:l.getEntitiesOfClass(LivingEntity.class,new AABB(Q).inflate(25)))if(!(w instanceof net.minecraft.world.entity.player.Player))w.discard();for(var w:l.getEntitiesOfClass(ItemEntity.class,new AABB(Q).inflate(25)))w.discard();for(var at:BlockPos.betweenClosed(Q.offset(-10,0,-7),Q.offset(16,6,7)))l.setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());for(var at:BlockPos.betweenClosed(Q.offset(-10,-1,-7),Q.offset(16,-1,7)))l.setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());p.setGameMode(GameType.SURVIVAL);p.getInventory().clearContent();p.teleportTo(l,Q.getX()+.5,Q.getY(),Q.getZ()+5.5,Set.of(),180,0,true);p.resetFallDistance();p.setDeltaMovement(Vec3.ZERO);p.setHealth(p.getMaxHealth());p.setAirSupply(300);p.getFoodData().setFoodLevel(20);s=GolemProbe.seal(p,SealKind.PICKUP_ADVANCED,Q);s.sample(8,MatchingRules.tool(1,"Inspector"));dest=LogisticsClient.chestSeal(p,SealKind.FILL,Q.west(7));out=(ChestBlockEntity)l.getBlockEntity(dest.support());good=drop(l,Q.east(),MatchingRules.tool(20,"Inspector"));bad=drop(l,Q.east(2),MatchingRules.tool(20,"Other"));g=AreaClient.spawn(p,new GolemDesign(3,1,0,3),Q.west(3));stage=1;due=now+80;return;}
 if(stage==1&&now>=due){check(good.isAlive()&&bad.isAlive()&&out.isEmpty(),"Exact filter collected differently damaged tool");open(p,s);stage=2;Thaumcraft.LOGGER.info("MATCHING_DAMAGE_READY: two differently named damaged tools rejected by exact filter; ready for native Damage toggle");return;}
 if(stage==2&&!s.matchDamage){stage=3;due=now+500;return;}
 if(stage==3){check(now<due,"Damage-filter pickup timeout "+g.position());if(GolemClient.count(out,Items.IRON_PICKAXE)!=1)return;check(!good.isAlive()&&bad.isAlive()&&out.getItem(0).getDamageValue()==20&&out.getItem(0).getHoverName().getString().equals("Inspector"),"Damage filter lost wear/name or accepted wrong component");p.closeContainer();GolemClient.remove(s);GolemClient.remove(dest);g.discard();bad.discard();s=LogisticsClient.chestSeal(p,SealKind.PROVIDER,Q.west(6));in=(ChestBlockEntity)l.getBlockEntity(s.support());var birch=new ItemStack(Items.BIRCH_PLANKS,20);birch.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,Component.literal("Physical birch"));in.setItem(0,birch);dest=LogisticsClient.chestSeal(p,SealKind.STOCK,Q.east(8));dest.sample(0,new ItemStack(Items.OAK_PLANKS));dest.sample(8,new ItemStack(Items.BIRCH_PLANKS));dest.amounts[0]=dest.amounts[8]=11;out=(ChestBlockEntity)l.getBlockEntity(dest.support());out.setItem(0,new ItemStack(Items.OAK_PLANKS,3));g=AreaClient.spawn(p,GolemDesign.BASIC,Q.west(2));other=AreaClient.spawn(p,GolemDesign.BASIC,Q);stage=4;due=now+80;Thaumcraft.LOGGER.info("MATCHING_DAMAGE_OK: GUI toggle collected exactly one worn named pickaxe, wrong name untouched, original damage preserved");return;}
 if(stage==4&&now>=due){check(GolemClient.count(in,Items.BIRCH_PLANKS)==20&&GolemClient.count(out,Items.BIRCH_PLANKS)==0,"Exact stock accepted named birch");open(p,dest);stage=5;Thaumcraft.LOGGER.info("MATCHING_TAGS_READY: exact stock rejects named birch; three oak planks already present, target eleven, two workers active");return;}
 if(stage==5&&dest.matchTags){stage=6;due=now+600;return;}
 if(stage==6){check(now<due,"Material stock timeout "+g.position()+" "+other.position());if(GolemClient.count(out,Items.BIRCH_PLANKS)!=8)return;check(GolemClient.count(out,Items.OAK_PLANKS)==3&&GolemClient.count(in,Items.BIRCH_PLANKS)==12&&g.cargo.isEmpty()&&other.cargo.isEmpty(),"Overlapping material samples overstocked or lost cargo");check(out.getItem(1).getHoverName().getString().equals("Physical birch"),"Stock replaced actual components");stage=7;due=now+100;return;}
 if(stage==7&&now>=due){check(GolemClient.count(in,Items.BIRCH_PLANKS)==12&&GolemClient.count(out,Items.BIRCH_PLANKS)==8,"Completed material stock keeps extracting");g.setMode(2);other.setMode(2);tested=true;stage=8;Thaumcraft.LOGGER.info("MATCHING_CLIENT_OK: actual native Damage/Materials clicks, exact component exclusion, two workers, overlapping samples, eight physical birch plus three oak, no overstock; ready for screenshot");}
 }
}
