package thaumcraft.thaumaturgy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.*;
import thaumcraft.*;
import thaumcraft.content.SimpleContent;
public final class TurretContent {
 private static RegistryObject<EntityType<CrossbowTurret>> turret(String id,float height){return Registrar.ENTITIES.register(id,()->EntityType.Builder.<CrossbowTurret>of(CrossbowTurret::new,MobCategory.MISC).sized(.95F,height).clientTrackingRange(10).updateInterval(2).build(ResourceKey.create(Registries.ENTITY_TYPE,SimpleContent.id(id))));}
 public static final RegistryObject<EntityType<CrossbowTurret>> BASIC=turret("turret_basic",1.25F),ADVANCED=turret("turret_advanced",1.5F);
 public static final RegistryObject<EntityType<TurretSpectralArrow>> SPECTRAL=Registrar.ENTITIES.register("turret_spectral_arrow",()->EntityType.Builder.<TurretSpectralArrow>of(TurretSpectralArrow::new,MobCategory.MISC).sized(.5F,.5F).clientTrackingRange(8).updateInterval(1).build(ResourceKey.create(Registries.ENTITY_TYPE,SimpleContent.id("turret_spectral_arrow"))));
 private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,Thaumcraft.MODID);
 public static final RegistryObject<MenuType<TurretMenu>> MENU=MENUS.register("turret",()->new MenuType<>(TurretMenu::new,FeatureFlags.VANILLA_SET));
 public static final RegistryObject<MenuType<TurretMenu>> ADVANCED_MENU=MENUS.register("turret_advanced",()->new MenuType<>((id,inv)->new TurretMenu(id,inv,true),FeatureFlags.VANILLA_SET));
 public static void register(net.minecraftforge.eventbus.api.bus.BusGroup bus){SimpleContent.item("turret_basic",p->new TurretItem(p,false));SimpleContent.item("turret_advanced",p->new TurretItem(p,true));MENUS.register(bus);net.minecraftforge.event.entity.EntityAttributeCreationEvent.BUS.addListener(e->{e.put(BASIC.get(),Mob.createMobAttributes().add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,30).add(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR,2).build());e.put(ADVANCED.get(),Mob.createMobAttributes().add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,40).add(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR,8).build());});}
 private TurretContent(){}
}
