package thaumcraft.test;

import java.util.*;
import java.nio.file.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.warp.*;
import thaumcraft.worldgen.CultistPortalFeature;

@Mod.EventBusSubscriber(modid="thaumcraft")
public final class CultProbe {
 static int ticks,phase,candidates,scan=45000,featureIndex=-1;static WarpMonster portal;static ServerPlayer actor;static float health;
 static final List<WarpMonster> seen=new ArrayList<>();static boolean scanning;
 public static volatile boolean ready;public static volatile UUID portalId;
 static String mode(){return System.getProperty("thaumcraft.worldgenProbe","");}
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(mode().startsWith("cult")&&e.getEntity() instanceof WarpMonster m)seen.add(m);}
 @SuppressWarnings("unchecked")
 static List<net.minecraft.world.level.biome.FeatureSorter.StepFeatureData> nativeFeatures(net.minecraft.world.level.chunk.ChunkGenerator g){try{var field=net.minecraft.world.level.chunk.ChunkGenerator.class.getDeclaredField("featuresPerStep");field.setAccessible(true);return (List<net.minecraft.world.level.biome.FeatureSorter.StepFeatureData>)((java.util.function.Supplier<?>)field.get(g)).get();}catch(Exception e){throw new RuntimeException(e);}}
 static void save(){try{Files.writeString(Path.of("cult51-proof.txt"),portal.getUUID()+","+portal.getX()+","+portal.getY()+","+portal.getZ()+","+portal.getHealth());}catch(Exception e){throw new RuntimeException(e);}}
 static void checks(ServerLevel l){
  var pos=new BlockPos(14008,190,8);l.getChunk(pos);
  for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){l.setBlock(pos.offset(x,-1,z),Blocks.SANDSTONE.defaultBlockState(),2);for(int y=0;y<5;y++)l.setBlock(pos.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);}
  var f=new CultistPortalFeature();var g=l.getChunkSource().getGenerator();
  check(!f.place(l,g,RandomSource.create(51),pos),"Unsupported ground accepted");
  for(var block:List.of(Blocks.GRASS_BLOCK,Blocks.PODZOL,Blocks.MYCELIUM)){
   l.setBlock(pos.below(),block.defaultBlockState(),2);check(f.place(l,g,RandomSource.create(51),pos),"Natural ground rejected: "+block);seen.getLast().discard();
  }
  l.setBlock(pos.below(),Blocks.SAND.defaultBlockState(),2);
  thaumcraft.config.ThaumcraftConfig.generateStructure.set(false);check(!f.place(l,g,RandomSource.create(51),pos),"Disabled generation accepted");thaumcraft.config.ThaumcraftConfig.generateStructure.set(true);
  thaumcraft.config.ThaumcraftConfig.wussMode.set(true);check(!f.place(l,g,RandomSource.create(51),pos),"Wuss accepted");thaumcraft.config.ThaumcraftConfig.wussMode.set(false);
  l.setBlock(pos.above(),Blocks.STONE.defaultBlockState(),2);check(!f.place(l,g,RandomSource.create(51),pos),"Obstructed portal accepted");l.setBlock(pos.above(),Blocks.AIR.defaultBlockState(),2);
  check(f.place(l,g,RandomSource.create(51),pos),"Sand placement failed");var m=seen.getLast();check(m.kind==WarpMonster.Kind.PORTAL&&!m.portalActive()&&m.isPersistenceRequired()&&m.fireImmune()&&!m.isPushable(),"Portal initial state wrong");check(!m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON,100)),"Portal accepted potion");m.hurtServer(l,m.damageSources().fellOutOfWorld(),1000);check(m.isDeadOrDying()&&l.getBlockState(pos.below()).is(Blocks.SAND),"Portal closure damaged terrain");m.discard();
  Thaumcraft.LOGGER.info("CULT_GATES_OK: sand terrain, obstruction, config, wuss, dormant persistent fireproof immovable portal");
 }
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post event){
  if(!mode().startsWith("cult")||ready)return;var l=event.server().overworld();
  if(++ticks<100)return;
  if(mode().equals("cult-read")){
   if(phase==0){thaumcraft.config.ThaumcraftConfig.generateStructure.set(false);try{var a=Files.readString(Path.of("cult51-proof.txt")).split(",");portalId=UUID.fromString(a[0]);l.getChunk((int)Math.floor(Double.parseDouble(a[1]))>>4,(int)Math.floor(Double.parseDouble(a[3]))>>4);health=Float.parseFloat(a[4]);}catch(Exception e){throw new RuntimeException(e);}phase=1;ticks=0;return;}
   portal=(WarpMonster)l.getEntity(portalId);check(portal!=null&&portal.portalActive()&&portal.getHealth()==health&&portal.isPersistenceRequired(),"Cold restart lost portal state");Thaumcraft.LOGGER.info("CULT_RESTART_OK: entity UUID, activation, health and persistence survived second JVM");thaumcraft.config.ThaumcraftConfig.generateStructure.set(true);ready=true;event.server().halt(false);return;
  }
  if(phase==0){if(mode().equals("cult-client")&&l.players().isEmpty())return;l.getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);checks(l);phase=1;ticks=100;return;}
  if(phase==1){
   if(ticks%20!=0)return;
   // Native decoration with the shipped 1/500 placement. Only load fresh chunks until an encounter is found.
   for(int n=0;n<1;n++){
    var g=l.getChunkSource().getGenerator();int step=net.minecraft.world.level.levelgen.GenerationStep.Decoration.SURFACE_STRUCTURES.ordinal();
    if(featureIndex<0){var features=nativeFeatures(g).get(step);var target=l.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.PLACED_FEATURE).getValue(thaumcraft.content.SimpleContent.id("cultist_portal"));check(features.features().contains(target),"Portal feature absent from native decoration order");featureIndex=features.indexMapping().applyAsInt(target);Thaumcraft.LOGGER.info("CULT_FEATURE_INDEX: {}",featureIndex);}
    var random=new net.minecraft.world.level.levelgen.WorldgenRandom(new net.minecraft.world.level.levelgen.XoroshiroRandomSource(0));int cx,cz;
    do{cx=-400+scan%256;cz=-400+scan/256;scan++;check(scan<65536,"No encounter candidate");long seed=random.setDecorationSeed(l.getSeed(),cx*16,cz*16);random.setFeatureSeed(seed,featureIndex,step);}while(random.nextFloat()>=1F/500);
    candidates++;Thaumcraft.LOGGER.info("CULT_SCAN: native seeded rarity candidate {},{} ({} considered)",cx,cz,scan);l.setChunkForced(cx,cz,true);l.getChunk(cx,cz);l.waitForEntities(new ChunkPos(cx,cz),0);
    int px=cx*16+random.nextInt(16),pz=cz*16+random.nextInt(16);var candidate=new BlockPos(px,l.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,px,pz),pz);Thaumcraft.LOGGER.info("CULT_CANDIDATE: index {} position {} ground {} biome {} entities {}",featureIndex,candidate,l.getBlockState(candidate.below()),l.getBiome(candidate).unwrapKey(),seen.size());
    for(var m:seen)if(!m.isRemoved()&&m.kind==WarpMonster.Kind.PORTAL&&(m.getBlockX()>>4)==cx&&(m.getBlockZ()>>4)==cz){portal=m;break;}
    if(portal!=null)break;else l.setChunkForced(cx,cz,false);
   }
   if(portal==null)return;
   for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)l.setChunkForced((portal.getBlockX()>>4)+dx,(portal.getBlockZ()>>4)+dz,true);
   portalId=portal.getUUID();check(!portal.portalActive(),"Natural portal unexpectedly active without players");
   Thaumcraft.LOGGER.info("CULT_NATURAL_OK: shipped native 1/500 feature at {} after {} candidate chunks ({} seed positions), initially dormant",portal.blockPosition(),candidates,scan);
   if(mode().equals("cult-client")){actor=l.players().getFirst();actor.setGameMode(GameType.SURVIVAL);actor.getAbilities().invulnerable=true;actor.onUpdateAbilities();}
   else{actor=new ServerPlayer(event.server(),l,new GameProfile(UUID.fromString("516438bf-8419-4458-835b-f9c4805caa51"),"CultProbe"),ClientInformation.createDefault());new net.minecraft.server.network.ServerGamePacketListenerImpl(event.server(),new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND),actor,net.minecraft.server.network.CommonListenerCookie.createInitial(actor.getGameProfile(),false));actor.setGameMode(GameType.SURVIVAL);l.addNewPlayer(actor);}
   if(mode().equals("cult-client")){
    net.minecraft.world.phys.Vec3 view=null;
    for(int dy:new int[]{1,3,5})for(int[] offset:new int[][]{{-6,8},{6,8},{8,-6},{-8,-6},{0,10},{10,0},{0,-10},{-10,0}}){
     var q=portal.position().add(offset[0],dy,offset[1]);
     if(view==null&&l.noCollision(new net.minecraft.world.phys.AABB(q.x-.3,q.y,q.z-.3,q.x+.3,q.y+1.8,q.z+.3))&&l.clip(new ClipContext(portal.getEyePosition(),q.add(0,1.62,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,portal)).getType()==net.minecraft.world.phys.HitResult.Type.MISS)view=q;
    }
    check(view!=null,"No unobstructed inspection camera");var delta=portal.position().add(0,1.5,0).subtract(view.add(0,1.62,0));
    float yaw=(float)Math.toDegrees(Math.atan2(-delta.x,delta.z)),pitch=(float)-Math.toDegrees(Math.atan2(delta.y,Math.sqrt(delta.x*delta.x+delta.z*delta.z)));
    actor.teleportTo(l,view.x,view.y,view.z,Set.of(),yaw,pitch,true);
   }else actor.setPos(portal.position().add(-6,1,8));actor.setNoGravity(true);
   event.server().clockManager().setTotalTicks(l.dimensionType().defaultClock().orElseThrow(),6000);
   health=portal.getHealth();phase=2;ticks=0;return;
  }
  if(phase==2){check(portal.portalActive(),"Proximity failed to activate");if(portal.getHealth()==health){check(ticks<240,"No real wave");return;}
   check(portal.getHealth()>=91&&portal.getHealth()<=95,"Wrong wave health cost");check(seen.stream().anyMatch(m->!m.isRemoved()&&(m.kind==WarpMonster.Kind.KNIGHT||m.kind==WarpMonster.Kind.CLERIC)),"No minion");
   Thaumcraft.LOGGER.info("CULT_WAVE_OK: proximity activation, delayed wave, knight/cleric, 5..9 health cost");
   if(mode().equals("cult-client")){ready=true;Thaumcraft.LOGGER.info("CULT_CLIENT_READY: natural active portal and first native wave");return;}
   actor.getAbilities().invulnerable=false;float hp=actor.getHealth();actor.setPos(portal.position());portal.playerTouch(actor);check(actor.getHealth()<hp,"Contact did not hurt");actor.setPos(portal.position().add(-6,1,8));actor.getAbilities().invulnerable=true;
   for(int i=0;i<4;i++){var m=new WarpMonster(WarpCreatures.KNIGHT.get(),l,WarpMonster.Kind.KNIGHT);m.setPos(portal.position().add(4+i,0,0));m.setNoAi(true);l.addFreshEntity(m);}health=portal.getHealth();phase=3;ticks=0;return;
  }
  if(phase==3){check(portal.getHealth()==health,"Population cap charged health");Thaumcraft.LOGGER.info("CULT_CAP_CONTACT_OK: native magic contact damage, normal population cap blocks further waves without spending health");save();ready=true;event.server().halt(false);}
 }
}
