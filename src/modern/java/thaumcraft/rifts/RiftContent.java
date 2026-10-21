package thaumcraft.rifts;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraftforge.registries.RegistryObject;
import thaumcraft.Registrar;
import thaumcraft.content.SimpleContent;

public final class RiftContent {
    public static final RegistryObject<EntityType<FluxRiftEntity>> RIFT=Registrar.ENTITIES.register("flux_rift",()->EntityType.Builder.<FluxRiftEntity>of(FluxRiftEntity::new,MobCategory.MISC)
        .sized(2,2).fireImmune().clientTrackingRange(10).updateInterval(1).build(ResourceKey.create(Registries.ENTITY_TYPE,SimpleContent.id("flux_rift"))));
    public static final RegistryObject<EntityType<Wisp>> WISP=Registrar.ENTITIES.register("wisp",()->EntityType.Builder.<Wisp>of(Wisp::new,MobCategory.MONSTER).sized(.9F,.9F).clientTrackingRange(8).updateInterval(2).build(ResourceKey.create(Registries.ENTITY_TYPE,SimpleContent.id("wisp"))));
    public static final RegistryObject<EntityType<RiftCloud>> CLOUD=Registrar.ENTITIES.register("rift_cloud",()->EntityType.Builder.<RiftCloud>of(RiftCloud::new,MobCategory.MISC).sized(.25F,.25F).fireImmune().clientTrackingRange(8).updateInterval(5).build(ResourceKey.create(Registries.ENTITY_TYPE,SimpleContent.id("rift_cloud"))));
    private static final net.minecraftforge.registries.DeferredRegister<net.minecraft.sounds.SoundEvent> SOUNDS=net.minecraftforge.registries.DeferredRegister.create(Registries.SOUND_EVENT,"thaumcraft");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> LIVE=sound("wisplive"),DEAD=sound("wispdead"),ZAP=sound("zap");
    private static RegistryObject<net.minecraft.sounds.SoundEvent> sound(String id){return SOUNDS.register(id,()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(SimpleContent.id(id)));}
    private RiftContent() {}
    public static void register(net.minecraftforge.eventbus.api.bus.BusGroup bus) {SOUNDS.register(bus);net.minecraftforge.event.entity.EntityAttributeCreationEvent.BUS.addListener(e->e.put(WISP.get(),Wisp.attributes()));}
}
