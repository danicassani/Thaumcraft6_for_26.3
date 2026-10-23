package thaumcraft.test;

import java.util.*;
import java.nio.file.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.crystals.CrystalContent;
import thaumcraft.rifts.*;
import thaumcraft.worldgen.MagicalForest;

@Mod.EventBusSubscriber(modid="thaumcraft")
public final class WispSpawnProbe {
 static final BlockPos ROOM=new BlockPos(18008,190,8);
 static int stage,ticks,attempts;static ServerPlayer actor;static BlockPos forest,netherPos;static Wisp saved;
 static final List<Wisp> spawned=new ArrayList<>(),crowd=new ArrayList<>();static final List<ItemEntity> drops=new ArrayList<>();
 public static volatile boolean ready;public static volatile UUID viewId;
 static String mode(){return System.getProperty("thaumcraft.worldgenProbe","");}
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(!mode().startsWith("wisp"))return;if(e.getEntity() instanceof Wisp w)spawned.add(w);if(e.getEntity() instanceof ItemEntity i)drops.add(i);}
 @SubscribeEvent public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event){
  if(mode().equals("wisp-client")&&event.getEntity() instanceof ServerPlayer p){actor=p;p.setGameMode(GameType.CREATIVE);p.setHealth(p.getMaxHealth());p.clearFire();p.removeAllEffects();Thaumcraft.LOGGER.info("WISP_INSPECTOR_READY: player restored and protected at login");}
 }
 static void force(ServerLevel l,BlockPos p){for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)l.setChunkForced((p.getX()>>4)+x,(p.getZ()>>4)+z,true);l.getChunk(p);}
 static void clock(ServerLevel l,long time){l.getServer().clockManager().setTotalTicks(l.dimensionType().defaultClock().orElseThrow(),time);}
 static ServerPlayer fake(ServerLevel l,String name){var p=new ServerPlayer(l.getServer(),l,new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(java.nio.charset.StandardCharsets.UTF_8)),name),ClientInformation.createDefault());new net.minecraft.server.network.ServerGamePacketListenerImpl(l.getServer(),new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));p.setPos(0,200,0);l.addNewPlayer(p);p.getAbilities().invulnerable=true;p.setNoGravity(true);return p;}
 static boolean placement(ServerLevel l,BlockPos p){return SpawnPlacements.checkSpawnRules(RiftContent.WISP.get(),l,EntitySpawnReason.NATURAL,p,RandomSource.create(52));}
 static void room(ServerLevel l){force(l,ROOM);for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)for(int y=-1;y<=5;y++){boolean shell=Math.abs(x)==5||Math.abs(z)==5||y==-1||y==5;l.setBlock(ROOM.offset(x,y,z),(shell?Blocks.STONE:Blocks.AIR).defaultBlockState(),2);}clock(l,6000);}
 static void gates(ServerLevel l){
  check(l.getBrightness(LightLayer.SKY,ROOM)==0&&l.getMaxLocalRawBrightness(ROOM)==0,"Dark room not ready");check(placement(l,ROOM),"Dark airborne placement rejected");
  check(!placement(l,ROOM.above(7)),"Daylight accepted");
  var w=new Wisp(RiftContent.WISP.get(),l);w.setPos(Vec3.atBottomCenterOf(ROOM));check(w.getMaxSpawnClusterSize()==2,"Wrong pack limit");
  ThaumcraftConfig.allowSpawnWisp.set(false);check(!placement(l,ROOM)&&!w.checkSpawnRules(l,EntitySpawnReason.NATURAL),"Spawn option ignored");ThaumcraftConfig.allowSpawnWisp.set(true);
  ThaumcraftConfig.wussMode.set(true);check(!placement(l,ROOM)&&!w.checkSpawnRules(l,EntitySpawnReason.NATURAL),"Wuss ignored");ThaumcraftConfig.wussMode.set(false);
  l.getServer().setDifficulty(net.minecraft.world.Difficulty.PEACEFUL,true);check(!placement(l,ROOM),"Peaceful accepted");l.getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);
  l.setBlock(ROOM,Blocks.WATER.defaultBlockState(),2);check(!placement(l,ROOM),"Water accepted");l.setBlock(ROOM,Blocks.AIR.defaultBlockState(),2);
  var aspects=new HashSet<String>();for(int n=0;n<120;n++){var sample=new Wisp(RiftContent.WISP.get(),l);sample.finalizeSpawn(l,l.getCurrentDifficultyAt(ROOM),EntitySpawnReason.NATURAL,null);aspects.add(sample.aspect());}
  check(aspects.size()>5,"Natural wisps all use default aspect");w.aspect("vitium");w.finalizeSpawn(l,l.getCurrentDifficultyAt(ROOM),EntitySpawnReason.NATURAL,null);check(w.aspect().equals("vitium"),"Explicit aspect overwritten");
  for(int n=0;n<8;n++){var m=new Wisp(RiftContent.WISP.get(),l);m.setPos(Vec3.atBottomCenterOf(ROOM.offset(n%3,0,n/3)));m.setNoAi(true);m.setPersistenceRequired();l.addFreshEntity(m);crowd.add(m);}
  Thaumcraft.LOGGER.info("WISP_GATES_OK: dark air, daylight, fluids, spawn option, wuss, peaceful, pack limit two, varied natural aspects, explicit aspect retained");
 }
 static void table(ServerLevel l,BlockPos p,int weight,int min,int max){var g=l.getChunkSource().getGenerator();var list=g.getMobsAt(l,l.structureManager(),MobCategory.MONSTER,p).unwrap();var entries=list.stream().filter(e->e.value().type()==RiftContent.WISP.get()).toList();check(entries.size()==1,"Missing or duplicate native wisp entry at "+p);var e=entries.getFirst();check(e.weight()==weight&&e.value().count().minInclusive()==min&&e.value().count().maxInclusive()==max,"Wrong spawn weight/pack");}
 static void natural(ServerLevel l,BlockPos p){for(int n=0;n<8;n++)NaturalSpawner.spawnCategoryForPosition(MobCategory.MONSTER,l,p);}
 static Wisp find(ServerLevel l,BlockPos p){return spawned.stream().filter(w->!w.isRemoved()&&w.level()==l&&w.getSpawnReason()==EntitySpawnReason.NATURAL&&w.distanceToSqr(Vec3.atCenterOf(p))<1600).findFirst().orElse(null);}
 static void pin(Wisp w){w.setNoAi(true);w.setPersistenceRequired();w.setDeltaMovement(Vec3.ZERO);}
 static void save(Wisp w){try{Files.writeString(Path.of("wisp52-proof.txt"),w.getUUID()+","+w.getX()+","+w.getY()+","+w.getZ()+","+w.aspect());}catch(Exception e){throw new RuntimeException(e);}}
 static void read(ServerLevel l){try{var a=Files.readString(Path.of("wisp52-proof.txt")).split(",");var id=UUID.fromString(a[0]);var p=BlockPos.containing(Double.parseDouble(a[1]),Double.parseDouble(a[2]),Double.parseDouble(a[3]));force(l,p);l.waitForEntities(new ChunkPos(p.getX()>>4,p.getZ()>>4),0);var w=(Wisp)l.getEntity(id);check(w!=null&&w.aspect().equals(a[4])&&w.isNoGravity()&&w.getSpawnReason()==EntitySpawnReason.NATURAL,"Natural wisp lost UUID/aspect/reason on restart");Thaumcraft.LOGGER.info("WISP_RESTART_OK: UUID, natural spawn reason, aspect and flight retained with spawning disabled");}catch(Exception e){throw new RuntimeException(e);}}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post event){
  if(!mode().startsWith("wisp")||ready)return;var l=event.server().overworld();if(++ticks<80)return;
  if(mode().equals("wisp-read")){ThaumcraftConfig.allowSpawnWisp.set(false);read(l);ThaumcraftConfig.allowSpawnWisp.set(true);ready=true;event.server().halt(false);return;}
  if(stage==0){if(mode().equals("wisp-client")&&(actor==null||actor.isRemoved()))return;event.server().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);room(l);stage=1;ticks=0;return;}
  if(stage==1){gates(l);stage=2;ticks=0;return;}
  if(stage==2){var w=new Wisp(RiftContent.WISP.get(),l);w.setPos(Vec3.atBottomCenterOf(ROOM));check(!w.checkSpawnRules(l,EntitySpawnReason.NATURAL),"Local cap eight ignored");crowd.forEach(Entity::discard);Thaumcraft.LOGGER.info("WISP_CAP_OK: eight live wisps block another natural spawn");
   var at=WorldgenProbe.locate(l);force(l,at);forest=new BlockPos(at.getX(),l.getHeight(Heightmap.Types.WORLD_SURFACE,at.getX(),at.getZ())+2,at.getZ());check(l.getBiome(forest).is(MagicalForest.KEY),"Not magical forest");table(l,forest,20,1,2);
   if(mode().equals("wisp-client")){actor.setGameMode(GameType.SURVIVAL);actor.getAbilities().invulnerable=true;actor.onUpdateAbilities();actor.teleportTo(l,forest.getX()+40,forest.getY(),forest.getZ(),Set.of(),90,0,true);actor.setNoGravity(true);}else{actor=fake(l,"WispForestProbe");actor.setPos(Vec3.atCenterOf(forest.east(40)));}
   clock(l,18000);stage=3;ticks=0;return;
  }
  if(stage==3){natural(l,forest);var w=find(l,forest);if(w==null){check(++attempts<100,"No forest native wisp");return;}pin(w);saved=w;save(w);Thaumcraft.LOGGER.info("WISP_FOREST_OK: native weighted spawner, NATURAL reason, position {}, aspect {}",w.blockPosition(),w.aspect());
   if(mode().equals("wisp-client")){var view=w.position().add(-5,2,5);var delta=w.position().subtract(view.add(0,1.62,0));float yaw=(float)Math.toDegrees(Math.atan2(-delta.x,delta.z)),pitch=(float)-Math.toDegrees(Math.atan2(delta.y,Math.sqrt(delta.x*delta.x+delta.z*delta.z)));actor.teleportTo(l,view.x,view.y,view.z,Set.of(),yaw,pitch,true);actor.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION,12000,0,false,false));viewId=w.getUUID();ready=true;Thaumcraft.LOGGER.info("WISP_CLIENT_READY: actual natural wisp in magical forest, night vision for inspection only");return;}
   var nether=event.server().getLevel(Level.NETHER);netherPos=new BlockPos(264,80,264);force(nether,netherPos);var p=netherPos;netherPos=null;
   for(int y=40;y<=104&&netherPos==null;y+=4)for(int dx=-8;dx<=8&&netherPos==null;dx+=4)for(int dz=-8;dz<=8&&netherPos==null;dz+=4){var q=new BlockPos(p.getX()+dx,y,p.getZ()+dz);if(nether.getBlockState(q).isAir()&&nether.getBlockState(q.above()).isAir()&&nether.getMaxLocalRawBrightness(q)==0)netherPos=q;}
   check(netherPos!=null,"No dark Nether air");table(nether,netherPos,5,1,1);var np=fake(nether,"WispNetherProbe");np.setPos(Vec3.atCenterOf(netherPos.east(40)));stage=4;ticks=0;attempts=0;return;
  }
  if(stage==4){var nether=event.server().getLevel(Level.NETHER);natural(nether,netherPos);var w=find(nether,netherPos);if(w==null){check(++attempts<200,"No Nether native wisp");return;}pin(w);Thaumcraft.LOGGER.info("WISP_NETHER_OK: native weighted spawner, NATURAL reason, position {}, aspect {}",w.blockPosition(),w.aspect());
   String aspect=w.aspect();drops.clear();w.hurtServer(nether,w.damageSources().fellOutOfWorld(),1000);check(drops.stream().anyMatch(i->i.getItem().is(CrystalContent.ESSENCE.get())&&CrystalContent.aspect(i.getItem()).equals(aspect)),"Natural wisp crystal loot wrong");Thaumcraft.LOGGER.info("WISP_LOOT_OK: actual natural wisp drops its aspect crystal");ready=true;event.server().halt(false);
  }
 }
}
