package thaumcraft.bore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.*;
import thaumcraft.*;
import thaumcraft.content.SimpleContent;
public final class BoreContent {
 public static final RegistryObject<EntityType<ArcaneBore>> TYPE=Registrar.ENTITIES.register("arcane_bore",()->EntityType.Builder.<ArcaneBore>of(ArcaneBore::new,MobCategory.MISC).sized(.9F,.9F).clientTrackingRange(10).updateInterval(2).build(ResourceKey.create(Registries.ENTITY_TYPE,SimpleContent.id("arcane_bore"))));
 private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,Thaumcraft.MODID);
 public static final RegistryObject<MenuType<BoreMenu>> MENU=MENUS.register("arcane_bore",()->new MenuType<>(BoreMenu::new,FeatureFlags.VANILLA_SET));
 public static void register(net.minecraftforge.eventbus.api.bus.BusGroup bus){SimpleContent.item("arcane_bore",BoreItem::new);MENUS.register(bus);net.minecraftforge.event.entity.EntityAttributeCreationEvent.BUS.addListener(e->e.put(TYPE.get(),Mob.createMobAttributes().add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,50).build()));}
 private BoreContent(){}
}
