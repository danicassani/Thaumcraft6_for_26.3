package thaumcraft.test;
import java.util.*;
import java.nio.file.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.rifts.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class RiftEventsProbe {
 public static final BlockPos P=new BlockPos(2880,205,8);public static volatile boolean ready;static int stage,ticks;static long due;static ServerPlayer actor;static Cow victim,blocked;static RiftCloud cloud;static boolean refreshed;
 static String mode(){return System.getProperty("thaumcraft.alchemyProbe","");}
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 static void put(ServerLevel l,BlockPos p,net.minecraft.world.level.block.state.BlockState s){l.setBlockAndUpdate(p,s);}
 static Cow cow(ServerLevel l,Vec3 p){var c=EntityTypes.COW.create(l,EntitySpawnReason.TRIGGERED);c.setNoAi(true);c.setNoGravity(true);c.setPersistenceRequired();c.setPos(p);l.addFreshEntity(c);return c;}
 static Wisp wisp(ServerLevel l,Vec3 p,String aspect){var w=new Wisp(RiftContent.WISP.get(),l);w.aspect(aspect);w.setNoAi(true);w.setPersistenceRequired();w.setPos(p);l.addFreshEntity(w);return w;}
 static void mechanics(ServerLevel l){
  var at=Vec3.atBottomCenterOf(P);var far=new Vec3(10000000,205,10000000);check(Wisp.spawn(l,far,true)==null&&!l.hasChunkAt(BlockPos.containing(far)),"Wisp forced unloaded chunk");check(Wisp.spawn(l,new Vec3(Double.NaN,0,0),false)==null,"Nonfinite spawn accepted");
  boolean allow=thaumcraft.config.ThaumcraftConfig.allowSpawnWisp.get();thaumcraft.config.ThaumcraftConfig.allowSpawnWisp.set(false);try{check(Wisp.spawn(l,at,true)==null,"Wisp spawn option ignored");}finally{thaumcraft.config.ThaumcraftConfig.allowSpawnWisp.set(allow);}
  boolean wuss=thaumcraft.config.ThaumcraftConfig.wussMode.get();thaumcraft.config.ThaumcraftConfig.wussMode.set(true);try{check(Wisp.spawn(l,at,true)==null,"Wuss spawn ignored");var c=new RiftCloud(RiftContent.CLOUD.get(),l);c.configure(l,at,1,5);c.tick();check(c.isRemoved(),"Wuss cloud persisted");}finally{thaumcraft.config.ThaumcraftConfig.wussMode.set(wuss);}
  var w=wisp(l,at.add(-5,2,0),"vitium");check(w.getMaxHealth()==22,"Original wisp health");check(thaumcraft.research.AspectCatalog.entity(w).equals(Map.of("auram",5,"volatus",5,"vitium",5)),"Dynamic scan aspects lost");w.aspect("auram");check(thaumcraft.research.AspectCatalog.entity(w).get("auram")==10,"Wisp aspect merge");w.aspect("vitium");
  var target=cow(l,at.add(-3,2,0));boolean hit=false;for(int n=0;n<200&&!hit;n++){target.setHealth(10);target.damageCooldownTime=0;hit=w.zap(l,target);}check(hit&&target.getHealth()==6,"Stationary zap damage");target.setDeltaMovement(new Vec3(.2,0,0));hit=false;for(int n=0;n<200&&!hit;n++){target.setHealth(10);target.damageCooldownTime=0;hit=w.zap(l,target);}check(hit&&target.getHealth()==7,"Moving zap damage");target.setDeltaMovement(Vec3.ZERO);
  for(int y=0;y<5;y++)put(l,P.offset(-4,y,0),Blocks.STONE_BRICKS.defaultBlockState());check(!w.visible(l,target)&&!w.zap(l,target),"Wisp attacked through wall");for(int y=0;y<5;y++)put(l,P.offset(-4,y,0),Blocks.AIR.defaultBlockState());target.discard();
  check(w.hurtServer(l,l.damageSources().generic(),100),"Wisp death failed");check(l.getEntitiesOfClass(ItemEntity.class,w.getBoundingBox().inflate(2)).stream().anyMatch(e->e.getItem().is(thaumcraft.crystals.CrystalContent.ESSENCE.get())&&thaumcraft.crystals.CrystalContent.aspect(e.getItem()).equals("vitium")),"Wisp crystal drop");w.discard();
  // A dark sealed cell makes the native spawn/light test independent of time/weather.
  for(var q:BlockPos.betweenClosed(P.offset(-8,-1,-8),P.offset(8,6,8)))if(Math.abs(q.getX()-P.getX())==8||Math.abs(q.getZ()-P.getZ())==8||q.getY()==P.getY()+6)put(l,q,Blocks.STONE.defaultBlockState());
  var r=new FluxRiftEntity(RiftContent.RIFT.get(),l);r.setPos(at);r.setStability(-20);boolean spawned=false;for(int n=0;n<256&&!spawned;n++)spawned=r.corruptionEvent(l,0);check(spawned&&r.stability()==-15,"Successful wisp event stability");
  l.getEntitiesOfClass(Wisp.class,new AABB(P).inflate(16)).forEach(Entity::discard);for(int n=0;n<8;n++)wisp(l,at.add(n*.1,0,0),"aer");for(int n=0;n<32;n++)check(Wisp.spawn(l,at,true)==null,"Wisp population cap ignored");float stability=r.stability();check(!r.corruptionEvent(l,0)&&r.stability()==stability,"Failed event paid stability");l.getEntitiesOfClass(Wisp.class,new AABB(P).inflate(16)).forEach(Entity::discard);
  for(var q:BlockPos.betweenClosed(P.offset(-8,0,-8),P.offset(8,6,8)))put(l,q,Blocks.AIR.defaultBlockState());
  victim=cow(l,at.add(1,0,0));blocked=cow(l,at.add(2,0,1));for(int y=0;y<3;y++)put(l,P.offset(1,y,1),Blocks.STONE_BRICKS.defaultBlockState());cloud=new RiftCloud(RiftContent.CLOUD.get(),l);cloud.configure(l,at.add(0,.7,0),3,5);l.addFreshEntity(cloud);due=l.getGameTime()+6;stage=2;
  Thaumcraft.LOGGER.info("RIFT_EVENTS_MECHANICS_OK: native wisps, weighted event stability, config, cap, unloaded/finite positions, original health, scan aspects, crystal drops, moving/stationary damage and line of sight");
 }
 static void finish(ServerLevel l){
  victim.discard();blocked.discard();for(int y=0;y<3;y++)put(l,P.offset(1,y,1),Blocks.AIR.defaultBlockState());
  var w=wisp(l,Vec3.atBottomCenterOf(P).add(-4,2,0),"vitium");var c=new RiftCloud(RiftContent.CLOUD.get(),l);c.configure(l,Vec3.atCenterOf(P.east(5)),2,30);l.addFreshEntity(c);
  try{var props=new Properties();props.setProperty("wisp",w.getUUID().toString());props.setProperty("cloud",c.getUUID().toString());props.setProperty("expires",Long.toString(c.expires()));try(var out=Files.newOutputStream(Path.of("../../work/rift-events-state.properties"))){props.store(out,"Native restart identity/deadline");}}catch(Exception ex){throw new RuntimeException(ex);}
  stage=5;Thaumcraft.LOGGER.info("RIFT_EVENTS_WRITE_OK: persistent wisp identity/aspect and absolute cloud deadline saved");l.getServer().halt(false);
 }
 static void restart(ServerLevel l){try{var props=new Properties();try(var in=Files.newInputStream(Path.of("../../work/rift-events-state.properties"))){props.load(in);}check(l.getEntity(UUID.fromString(props.getProperty("wisp"))) instanceof Wisp w&&w.aspect().equals("vitium")&&w.getMaxHealth()==22,"Wisp restart identity/aspect");check(l.getEntity(UUID.fromString(props.getProperty("cloud"))) instanceof RiftCloud c&&c.radius()==2&&c.expires()==Long.parseLong(props.getProperty("expires")),"Cloud reset lifetime on reload");check(thaumcraft.taint.TaintWorld.seed(l,TaintProbe.P)!=null,"Previous taint save lost");check(l.getBlockEntity(CondenserProbe.P) instanceof thaumcraft.condenser.Condenser,"Previous condenser lost");}catch(Exception ex){throw new RuntimeException(ex);}stage=5;Thaumcraft.LOGGER.info("RIFT_EVENTS_RESTART_OK: exact entity UUIDs/aspect/radius/deadline and prior taint/condenser world preserved");l.getServer().halt(false);}
 static void gallery(ServerLevel l){var at=Vec3.atBottomCenterOf(P);for(int i=0;i<4;i++)wisp(l,at.add(-5+i*3,2,0),new String[]{"aer","ignis","vitium","aqua"}[i]);victim=cow(l,at.add(3,0,5));victim.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);victim.setHealth(1000);var attacker=wisp(l,at.add(-2,1,5),"vitium");check(attacker.canBreatheUnderwater()&&!attacker.causeFallDamage(100,1,l.damageSources().fall()),"Flying wisp drowned/fell");attacker.setNoAi(false);attacker.setTarget(victim);actor.setGameMode(net.minecraft.world.level.GameType.CREATIVE);actor.teleportTo(l,P.getX()+.5,P.getY()+5,P.getZ()+15.5,Set.of(),180,15,true);actor.getAbilities().flying=true;actor.onUpdateAbilities();actor.getInventory().clearContent();actor.getInventory().setSelectedSlot(0);l.getServer().getCommands().performPrefixedCommand(l.getServer().createCommandSourceStack(),"time set day");ready=true;stage=5;Thaumcraft.LOGGER.info("RIFT_EVENTS_CLIENT_OK: original multicolor wisps, active charged attacker and flux cloud target in native world");}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(!mode().startsWith("rift-events-"))return;var l=e.server().overworld();if(++ticks<80)return;
  if(stage==5){if(ready&&actor.getInventory().getSelectedSlot()!=8)refreshed=false;if(ready&&!refreshed&&actor.getInventory().getSelectedSlot()==8){l.getEntitiesOfClass(RiftCloud.class,new AABB(P).inflate(30)).forEach(Entity::discard);var eventRift=new FluxRiftEntity(RiftContent.RIFT.get(),l);eventRift.setPos(actor.position());eventRift.setSize(100);eventRift.setStability(-20);for(int n=0;n<16;n++)check(eventRift.corruptionEvent(l,80)&&eventRift.stability()==-20,"Actual cloud event failed or paid unintended stability");check(!eventRift.corruptionEvent(l,80),"Cloud population cap ignored");l.getEntitiesOfClass(RiftCloud.class,new AABB(P).inflate(30)).forEach(Entity::discard);cloud=new RiftCloud(RiftContent.CLOUD.get(),l);cloud.configure(l,Vec3.atCenterOf(P.south(5).east(3)),2,30);l.addFreshEntity(cloud);refreshed=true;Thaumcraft.LOGGER.info("RIFT_EVENTS_CLOUD_READY: actual damaging 30-second cloud, native lifetime and shared cooldown active");}return;}
  if(stage==0){if(!e.server().isDedicatedServer()){if(e.server().getPlayerList().getPlayers().isEmpty())return;actor=e.server().getPlayerList().getPlayers().getFirst();}for(int x=179;x<=181;x++)for(int z=-1;z<=1;z++)l.setChunkForced(x,z,true);due=l.getGameTime()+50;stage=1;return;}
  if(stage==1&&l.getGameTime()>=due){if(mode().equals("rift-events-read")){restart(l);return;}l.getEntitiesOfClass(Entity.class,new AABB(P).inflate(24),en->!(en instanceof net.minecraft.world.entity.player.Player)).forEach(Entity::discard);for(var q:BlockPos.betweenClosed(P.offset(-9,-1,-9),P.offset(9,7,9)))put(l,q,q.getY()==P.getY()-1?Blocks.STONE_BRICKS.defaultBlockState():Blocks.AIR.defaultBlockState());if(mode().equals("rift-events-client")){gallery(l);return;}l.getServer().getCommands().performPrefixedCommand(l.getServer().createCommandSourceStack(),"time set midnight");due=l.getGameTime()+40;stage=6;return;}
  if(stage==6&&l.getGameTime()>=due){mechanics(l);return;}
  if(stage==2&&l.getGameTime()>=due){check(victim.getHealth()==8&&blocked.getHealth()==10,"Cloud damage/blocked target: "+victim.getHealth()+"/"+blocked.getHealth());var duplicate=new RiftCloud(RiftContent.CLOUD.get(),l);duplicate.configure(l,cloud.position(),3,5);l.addFreshEntity(duplicate);due=l.getGameTime()+10;stage=3;return;}
  if(stage==3&&l.getGameTime()>=due){check(victim.getHealth()==8,"Overlapping clouds bypassed shared cooldown");due=cloud.expires()+1;stage=4;return;}
  if(stage==4&&l.getGameTime()>=due){check(cloud.isRemoved(),"Cloud did not expire at original deadline");check(blocked.getHealth()==10,"Cloud pierced block LOS");Thaumcraft.LOGGER.info("RIFT_EVENTS_CLOUD_OK: two damage, spherical radius, blocked LOS, overlapping shared cooldown and native absolute expiry");finish(l);}
 }
}
