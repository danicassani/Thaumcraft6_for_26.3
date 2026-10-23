package thaumcraft.warp;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.registries.RegistryObject;
import thaumcraft.Registrar;
import thaumcraft.content.SimpleContent;
public final class WarpCreatures {
 public static final RegistryObject<EntityType<MindSpider>> SPIDER=Registrar.ENTITIES.register("mind_spider",()->EntityType.Builder.<MindSpider>of(MindSpider::new,MobCategory.MONSTER).sized(.7F,.5F).clientTrackingRange(8).build(key("mind_spider")));
 private static RegistryObject<EntityType<WarpMonster>> monster(String id,WarpMonster.Kind k,float width,float height){return Registrar.ENTITIES.register(id,()->EntityType.Builder.<WarpMonster>of((t,l)->new WarpMonster(t,l,k),MobCategory.MONSTER).sized(width,height).clientTrackingRange(10).build(key(id)));}
 public static final RegistryObject<EntityType<WarpMonster>> GUARDIAN=monster("eldritch_guardian",WarpMonster.Kind.GUARDIAN,.8F,2.25F),KNIGHT=monster("cultist_knight",WarpMonster.Kind.KNIGHT,.6F,1.8F),CLERIC=monster("cultist_cleric",WarpMonster.Kind.CLERIC,.6F,1.8F),PORTAL=monster("cultist_portal_lesser",WarpMonster.Kind.PORTAL,1.5F,3);
 public static final RegistryObject<EntityType<WarpOrb>> ORB=Registrar.ENTITIES.register("eldritch_orb",()->EntityType.Builder.<WarpOrb>of(WarpOrb::new,MobCategory.MISC).sized(.3F,.3F).clientTrackingRange(8).updateInterval(1).build(key("eldritch_orb")));
 public static final RegistryObject<EntityType<ClericOrb>> CLERIC_ORB=Registrar.ENTITIES.register("golem_orb",()->EntityType.Builder.<ClericOrb>of(ClericOrb::new,MobCategory.MISC).sized(.3F,.3F).clientTrackingRange(8).updateInterval(1).build(key("golem_orb")));
 private static <T extends Entity> ResourceKey<EntityType<?>> key(String n){return ResourceKey.create(Registries.ENTITY_TYPE,SimpleContent.id(n));}
 public static void register(net.minecraftforge.eventbus.api.bus.BusGroup b){net.minecraftforge.event.entity.EntityAttributeCreationEvent.BUS.addListener(e->{e.put(SPIDER.get(),MindSpider.attributes());for(var t:java.util.List.of(GUARDIAN,KNIGHT,CLERIC,PORTAL))e.put(t.get(),WarpMonster.attributes(t==GUARDIAN?WarpMonster.Kind.GUARDIAN:t==KNIGHT?WarpMonster.Kind.KNIGHT:t==CLERIC?WarpMonster.Kind.CLERIC:WarpMonster.Kind.PORTAL));});}
 public static boolean place(ServerPlayer p,Mob mob){var l=p.level();if(thaumcraft.config.ThaumcraftConfig.wussMode.get()||l.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL)return false;for(int n=0;n<50;n++){var at=p.blockPosition().offset((7+l.getRandom().nextInt(18))*(l.getRandom().nextInt(3)-1),(7+l.getRandom().nextInt(18))*(l.getRandom().nextInt(3)-1),(7+l.getRandom().nextInt(18))*(l.getRandom().nextInt(3)-1));if(!l.isInWorldBounds(at.above(3))||!l.hasChunkAt(at)||!l.getWorldBorder().isWithinBounds(at)||!l.getBlockState(at.below()).isFaceSturdy(l,at.below(),Direction.UP))continue;mob.setPos(Vec3.atBottomCenterOf(at));var a=mob.getBoundingBox();if(!thaumcraft.thaumaturgy.SpellEngine.loadedLine(l,new Vec3(a.minX,a.minY,a.minZ),new Vec3(a.maxX,a.maxY,a.maxZ))||!l.getFluidState(at).isEmpty()||!l.getFluidState(at.above()).isEmpty()||!l.noCollision(mob))continue;if(l.addFreshEntity(mob)){mob.setTarget(p);return true;}}return false;}
 public static int spiders(ServerPlayer p,int count,boolean real){var l=p.level();int existing=l.getEntitiesOfClass(MindSpider.class,p.getBoundingBox().inflate(32)).size(),added=0;for(int i=0;i<Math.min(Math.min(50,count),Math.max(0,50-existing));i++){var spider=new MindSpider(SPIDER.get(),l);spider.configure(p,!real);if(place(p,spider))added++;}return added;}
 public static int mist(ServerPlayer p,int count){var l=p.level();int existing=l.getEntitiesOfClass(WarpMonster.class,p.getBoundingBox().inflate(48),m->m.kind==WarpMonster.Kind.GUARDIAN).size(),added=0;for(int i=0;i<Math.min(count,Math.max(0,8-existing));i++)if(place(p,new WarpMonster(GUARDIAN.get(),l,WarpMonster.Kind.GUARDIAN)))added++;return added;}
 public static boolean portal(ServerPlayer p){if(p.level().getEntitiesOfClass(WarpMonster.class,p.getBoundingBox().inflate(48),m->m.kind==WarpMonster.Kind.PORTAL).size()>=2)return false;return place(p,new WarpMonster(PORTAL.get(),p.level(),WarpMonster.Kind.PORTAL));}
}
