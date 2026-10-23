package thaumcraft.pech;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.registries.*;
import thaumcraft.Registrar;
import thaumcraft.content.SimpleContent;

public final class PechContent {
    public static final RegistryObject<EntityType<Pech>> PECH=Registrar.ENTITIES.register("pech",()->EntityType.Builder.<Pech>of(Pech::new,MobCategory.MONSTER).sized(.6F,1.8F).eyeHeight(1.188F).clientTrackingRange(8).build(key("pech")));
    public static final RegistryObject<EntityType<PechBolt>> BOLT=Registrar.ENTITIES.register("pech_bolt",()->EntityType.Builder.<PechBolt>of(PechBolt::new,MobCategory.MISC).sized(.25F,.25F).clientTrackingRange(8).updateInterval(1).build(key("pech_bolt")));
    private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,"thaumcraft");
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,"thaumcraft");
    public static final RegistryObject<MenuType<PechMenu>> MENU=MENUS.register("pech",()->new MenuType<>(PechMenu::new,FeatureFlags.VANILLA_SET));
    public static final RegistryObject<SoundEvent> IDLE=sound("pech_idle"),HIT=sound("pech_hit"),DEATH=sound("pech_death"),TRADE=sound("pech_trade"),DICE=sound("pech_dice"),CHARGE=sound("pech_charge");
    private static RegistryObject<SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(SimpleContent.id(id)));}
    private static ResourceKey<EntityType<?>> key(String id){return ResourceKey.create(Registries.ENTITY_TYPE,SimpleContent.id(id));}
    private PechContent(){}
    public static void register(net.minecraftforge.eventbus.api.bus.BusGroup bus){
        SimpleContent.item("pech_spawn_egg",p->new net.minecraft.world.item.SpawnEggItem(p.spawnEgg(PECH.get())));
        MENUS.register(bus);SOUNDS.register(bus);
        net.minecraftforge.event.entity.EntityAttributeCreationEvent.BUS.addListener(e->e.put(PECH.get(),Pech.attributes()));
        SpawnPlacementRegisterEvent.BUS.addListener(e->e.register(PECH.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,Pech::placement,SpawnPlacementRegisterEvent.Operation.REPLACE));
    }
}
