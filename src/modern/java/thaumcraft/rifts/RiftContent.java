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
    private RiftContent() {}
    public static void register() {}
}
