package thaumcraft.test;
import java.util.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.*;
import thaumcraft.golems.*;
import static thaumcraft.test.ThaumaturgyProbe.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID)
public final class RankProbe {
 static final BlockPos Q=new BlockPos(1040,205,8);
 static final UUID OWNER=UUID.fromString("e7e69cd4-3c17-465a-9c62-b8fedb8a77d4");
 static final GolemDesign DESIGN=new GolemDesign(3,4,4,1,1);
 static ServerPlayer p;static int ticks;static boolean done;
 static WorkerGolem worker(GolemDesign design,BlockPos at){var g=new WorkerGolem(GolemContent.GOLEM.get(),p.level());g.configure(p,design,at,7);g.setMode(2);p.level().addFreshEntity(g);return g;}
 static void write(){var l=p.level();for(var g:l.getEntitiesOfClass(WorkerGolem.class,new AABB(Q).inflate(15)))g.discard();for(var at:BlockPos.betweenClosed(Q.offset(-5,-1,-4),Q.offset(8,-1,4)))l.setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());var g=worker(DESIGN,Q);g.setCustomName(Component.literal("Veteran darter"));g.setHealth(7);g.cargo.setItem(0,new ItemStack(Items.DIAMOND,9));g.restoreProgress(new GolemProgress(9,99991));check(g.getHealth()==7&&g.getMaxHealth()==25&&g.attackDamage()==5.25F,"Rank bonuses/healing invalid");var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,l.registryAccess());g.saveWithoutId(out);var copy=new WorkerGolem(GolemContent.GOLEM.get(),l);copy.load(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),out.buildResult()));check(copy.progress().equals(g.progress())&&copy.getHealth()==7&&copy.getMaxHealth()==25&&copy.channel()==7,"Rank snapshot changed attributes/state");var legacy=worker(GolemDesign.BASIC,Q.east(5));legacy.addRankXp(Integer.MAX_VALUE);check(legacy.progress().equals(GolemProgress.EMPTY),"Clockwork golem gained rank");var old=out.buildResult().copy();old.remove("golem_progress");var zero=new WorkerGolem(GolemContent.GOLEM.get(),l);zero.load(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),old));check(zero.progress().equals(GolemProgress.EMPTY)&&zero.design().equals(DESIGN)&&zero.getMaxHealth()==16&&zero.getHealth()==7,"Old golem migration changed design/health");Thaumcraft.LOGGER.info("RANK_WRITE_OK: rank nine threshold, actual attributes, no healing, cargo, channel, legacy migration and Clockwork rejection");}
 @SubscribeEvent public static void start(ServerStartedEvent e){String mode=System.getProperty("thaumcraft.thaumaturgyProbe","");if(!mode.equals("rank-write")&&!mode.equals("rank-read"))return;var l=e.getServer().overworld();for(int x=64;x<=66;x++)for(int z=-1;z<=1;z++)l.setChunkForced(x,z,true);l.setChunkForced(56,0,true);p=new ServerPlayer(e.getServer(),l,new GameProfile(OWNER,"RankProbe"),ClientInformation.createDefault()){@Override public void sendSystemMessage(Component c,boolean o){}@Override public void sendOverlayMessage(Component c){}@Override protected ItemCooldowns createItemCooldowns(){return new ItemCooldowns();}};knowledge(p);}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){String mode=System.getProperty("thaumcraft.thaumaturgyProbe","");if(!mode.equals("rank-write")&&!mode.equals("rank-read")||done||++ticks<60)return;var l=p.level();if(mode.equals("rank-write"))write();else{var g=l.getEntitiesOfClass(WorkerGolem.class,new AABB(Q).inflate(3)).stream().filter(w->w.getCustomName()!=null&&w.getCustomName().getString().equals("Veteran darter")).findFirst().orElseThrow();check(g.progress().equals(new GolemProgress(9,99991))&&g.getHealth()==7&&g.getMaxHealth()==25&&g.channel()==7&&g.cargo.getItem(0).getCount()==9,"Restart lost veteran progress/health/cargo");g.addRankXp(9);check(g.progress().equals(new GolemProgress(10,0))&&g.getHealth()==7&&g.getMaxHealth()==26&&g.attackDamage()==5.5F&&Math.abs(g.getAttributeValue(Attributes.MOVEMENT_SPEED)-DESIGN.speed()*1.25)<.000001,"Rank ten rollover/bonuses invalid");g.addRankXp(Integer.MAX_VALUE);check(g.progress().equals(new GolemProgress(10,0)),"Rank exceeded ten");Thaumcraft.LOGGER.info("RANK_RESTART_OK: persisted XP, rollover to ten, health unchanged, attack/speed bonuses and hard maximum");}
 check(l.getEntitiesOfClass(WorkerGolem.class,new AABB(DartProbe.Q).inflate(12)).stream().anyMatch(g->g.design().equals(DartProbe.DESIGN)&&g.progress().equals(GolemProgress.EMPTY)&&g.getHealth()==7&&g.cargo.getItem(0).getCount()==9),"Actual port.25 darter changed");Thaumcraft.LOGGER.info("RANK_PORT25_OK: actual old darter retains design, zero rank, health and cargo");done=true;e.server().halt(false);}
}
