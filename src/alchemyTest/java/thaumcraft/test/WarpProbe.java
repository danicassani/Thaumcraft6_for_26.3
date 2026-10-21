package thaumcraft.test;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.content.SimpleContent;
import thaumcraft.persistence.PlayerWarpStore;
import thaumcraft.warp.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class WarpProbe {
 public static final BlockPos P=new BlockPos(3200,205,8);static int stage,ticks;static long due;public static volatile boolean ready;static ServerPlayer actor;static WarpMonster clientPortal;static boolean used,finished;
 static String mode(){return System.getProperty("thaumcraft.alchemyProbe","");}
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 static ItemStack soap(){return new ItemStack(SimpleContent.ITEMS.get("sanity_soap").get(),3);}
 static void state(int permanent,int normal,int temporary,int counter){check(PlayerWarpStore.write(actor,new PlayerWarpStore.State(1,permanent,normal,temporary,counter)).result().isPresent(),"warp write rejected");}
 static PlayerWarpStore.State state(){return PlayerWarpStore.read(actor).getOrThrow();}
 static void basics(ServerLevel l){
  actor.setPos(Vec3.atBottomCenterOf(P));state(30,10,20,0);check(thaumcraft.infusion.CuriosityContent.warp(actor,0,1,0)&&state().counter()==61,"positive warp did not rearm");
  var s=soap();check(!((SanitySoap)s.getItem()).releaseUsing(s,l,actor,5)&&s.getCount()==3,"early soap use spent item");check(((SanitySoap)s.getItem()).releaseUsing(s,l,actor,4)&&s.getCount()==2&&state().permanent()==30&&state().normal()==10&&state().temporary()==0,"soap release boundary/cost wrong");
  state(30,10,20,61);actor.addEffect(new MobEffectInstance(thaumcraft.taint.TaintContent.WARD.getHolder().orElseThrow(),1000));check(!WarpEngine.check(actor,0,5)&&state().temporary()==20,"ward did not suppress trigger/decay");l.setBlockAndUpdate(P,thaumcraft.taint.TaintContent.PURE.block.get().defaultBlockState());check(SanitySoap.clean(actor,s)&&state().normal()==7&&state().permanent()==30&&state().counter()==61,"bath and ward soap bonuses wrong");l.setBlockAndUpdate(P,Blocks.AIR.defaultBlockState());actor.removeAllEffects();
  var raw=new net.minecraft.nbt.CompoundTag();raw.putInt("version",99);actor.getPersistentData().put(PlayerWarpStore.ID,raw);int count=s.getCount();check(!SanitySoap.clean(actor,s)&&s.getCount()==count&&actor.getPersistentData().get(PlayerWarpStore.ID).equals(raw),"future warp overwritten/soap consumed");actor.getPersistentData().remove(PlayerWarpStore.ID);
  state(30,10,5,0);check(!WarpEngine.check(actor,0,5)&&state().temporary()==4,"zero counter event or missing decay");state(30,10,5,100);check(WarpEngine.check(actor,0,22)&&actor.hasEffect(WarpContent.HUNGER.getHolder().orElseThrow()),"native hunger event not applied");actor.removeAllEffects();
  for(var pair:List.of(new int[]{18,0},new int[]{38,0},new int[]{54,0})){WarpEngine.event(actor,pair[0],30);}check(actor.hasEffect(WarpContent.THAUMARHIA.getHolder().orElseThrow())&&actor.hasEffect(WarpContent.SUN.getHolder().orElseThrow())&&actor.hasEffect(WarpContent.GAZE.getHolder().orElseThrow()),"native effect table wrong");actor.removeAllEffects();
  var cow=EntityTypes.COW.create(l,EntitySpawnReason.TRIGGERED);cow.setNoAi(true);cow.setPos(Vec3.atBottomCenterOf(P.south(4)));l.addFreshEntity(cow);var shooter=new WarpMonster(WarpCreatures.GUARDIAN.get(),l,WarpMonster.Kind.GUARDIAN);shooter.setPos(Vec3.atBottomCenterOf(P.south(8)));var blast=new WarpOrb(WarpCreatures.ORB.get(),l);blast.setOwner(shooter);blast.setPos(cow.position());try{var method=WarpOrb.class.getDeclaredMethod("onHit",HitResult.class);method.setAccessible(true);method.invoke(blast,new BlockHitResult(cow.position(),Direction.UP,cow.blockPosition(),false));}catch(Exception ex){throw new AssertionError(ex);}check(cow.getHealth()<cow.getMaxHealth()&&cow.hasEffect(MobEffects.WEAKNESS)&&blast.isRemoved(),"orb block impact did not apply native area damage/weakness");cow.discard();
  var harmless=new MindSpider(WarpCreatures.SPIDER.get(),l);harmless.configure(actor,true);harmless.setNoAi(true);harmless.setPersistenceRequired();harmless.setPos(Vec3.atBottomCenterOf(P.east(3)));l.addFreshEntity(harmless);check(!harmless.doHurtTarget(l,actor)&&harmless.viewer().equals(actor.getUUID().toString()),"hallucination attacked/lost viewer");harmless.addTag("warp_reload_spider");
  var guardian=new WarpMonster(WarpCreatures.GUARDIAN.get(),l,WarpMonster.Kind.GUARDIAN);guardian.setNoAi(true);guardian.setPersistenceRequired();guardian.setPos(Vec3.atBottomCenterOf(P.west(3)));l.addFreshEntity(guardian);guardian.addTag("warp_reload_guardian");
  var portal=new WarpMonster(WarpCreatures.PORTAL.get(),l,WarpMonster.Kind.PORTAL);portal.setPos(Vec3.atBottomCenterOf(P.north(6)));portal.addTag("warp_reload_portal");l.addFreshEntity(portal);
  l.setBlockAndUpdate(P.east(10),Blocks.CHEST.defaultBlockState());((ChestBlockEntity)l.getBlockEntity(P.east(10))).setItem(0,s);((ChestBlockEntity)l.getBlockEntity(P.east(10))).setItem(1,soap());
  Thaumcraft.LOGGER.info("WARP_NATIVE_OK: rearming, decay, early release, exact soap consumption, bath/ward bonuses, permanent warp preservation, corrupt future data preservation, native harmful effects and harmless owner-only spider");
 }
 static void read(ServerLevel l){var spider=l.getEntitiesOfClass(MindSpider.class,new AABB(P).inflate(20)).stream().filter(x->x.entityTags().contains("warp_reload_spider")).findFirst().orElseThrow();check(spider.harmless()&&spider.viewer().equals(actor.getUUID().toString())&&!spider.doHurtTarget(l,actor),"spider state lost on reload");check(l.getEntitiesOfClass(WarpMonster.class,new AABB(P).inflate(20)).size()>=2,"guardian or portal lost");var chest=(ChestBlockEntity)l.getBlockEntity(P.east(10));check(chest.getItem(0).getCount()==1&&chest.getItem(1).getCount()==3,"soap inventory lost");Thaumcraft.LOGGER.info("WARP_RESTART_OK: native spider owner/harmless state, guardian, portal and soap stacks survived dedicated server restart");stage=9;l.getServer().halt(false);}
 static void gallery(ServerLevel l){l.getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);actor.removeAllEffects();actor.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);actor.addEffect(new MobEffectInstance(MobEffects.RESISTANCE,12000,10));actor.teleportTo(l,P.getX()+.5,P.getY()+3,P.getZ()+8.5,Set.of(),180,10,true);actor.getInventory().clearContent();actor.getInventory().setItem(0,soap());actor.getInventory().setSelectedSlot(0);
  var guardian=new WarpMonster(WarpCreatures.GUARDIAN.get(),l,WarpMonster.Kind.GUARDIAN);guardian.setNoAi(true);guardian.setPersistenceRequired();guardian.setPos(Vec3.atBottomCenterOf(P.west(3)));guardian.setYRot(0);l.addFreshEntity(guardian);
  var portal=new WarpMonster(WarpCreatures.PORTAL.get(),l,WarpMonster.Kind.PORTAL);portal.setPos(Vec3.atBottomCenterOf(P.east(3)));l.addFreshEntity(portal);clientPortal=portal;
  var spider=new MindSpider(WarpCreatures.SPIDER.get(),l);spider.configure(actor,true);spider.setNoAi(true);spider.setPos(Vec3.atBottomCenterOf(P.south(3)));l.addFreshEntity(spider);
  for(var type:List.of(WarpCreatures.KNIGHT,WarpCreatures.CLERIC)){var m=type.get().create(l,EntitySpawnReason.TRIGGERED);m.setNoAi(true);m.setPos(Vec3.atBottomCenterOf(P.offset(type==WarpCreatures.KNIGHT?-6:6,0,-2)));l.addFreshEntity(m);}
  state(30,10,20,60);stage=9;Thaumcraft.LOGGER.info("WARP_CLIENT_OK: native guardian, crimson portal, two cultists and owner-only mind spider placed; hold soap to test real use");
 }
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(!mode().startsWith("warp-"))return;var l=e.server().overworld();if(++ticks<80)return;if(stage==9){if(mode().equals("warp-client")){
   if(!ready&&clientPortal!=null&&clientPortal.getHealth()<100){check(l.getEntitiesOfClass(WarpMonster.class,new AABB(P).inflate(40),m->m.kind==WarpMonster.Kind.KNIGHT||m.kind==WarpMonster.Kind.CLERIC).size()>=3,"portal paid health without minion");actor.setGameMode(net.minecraft.world.level.GameType.CREATIVE);actor.getAbilities().flying=true;actor.onUpdateAbilities();l.getEntitiesOfClass(WarpMonster.class,new AABB(P).inflate(40)).forEach(m->m.setNoAi(true));ready=true;Thaumcraft.LOGGER.info("WARP_PORTAL_OK: actual survival player triggered native delayed minion spawn and portal health cost");}
   if(ready&&!used&&actor.isUsingItem()){used=true;Thaumcraft.LOGGER.info("WARP_SOAP_STARTED: real client held native use");}
   if(used&&!finished&&actor.getInventory().getItem(0).getCount()==2){check(state().permanent()==30&&state().normal()==9&&state().temporary()==0,"actual held soap use did not clean exactly once");finished=true;Thaumcraft.LOGGER.info("WARP_SOAP_CLIENT_OK: actual timed use consumed exactly one soap, cleared temporary, reduced normal and preserved permanent warp");}
  }return;}if(stage==0){if(e.server().isDedicatedServer())actor=PrimordialProbe.fake(l);else{if(e.server().getPlayerList().getPlayers().isEmpty())return;actor=e.server().getPlayerList().getPlayers().getFirst();}for(int x=199;x<=202;x++)for(int z=-1;z<=1;z++)l.setChunkForced(x,z,true);due=l.getGameTime()+50;stage=1;return;}if(stage==1&&l.getGameTime()>=due){if(mode().equals("warp-read")){read(l);return;}l.getEntitiesOfClass(Entity.class,new AABB(P).inflate(35),x->!(x instanceof net.minecraft.world.entity.player.Player)).forEach(Entity::discard);for(var at:BlockPos.betweenClosed(P.offset(-10,-1,-10),P.offset(10,8,10)))l.setBlockAndUpdate(at,at.getY()==P.getY()-1?Blocks.GLOWSTONE.defaultBlockState():Blocks.AIR.defaultBlockState());if(mode().equals("warp-client")){gallery(l);return;}basics(l);stage=9;e.server().halt(false);}}
}
