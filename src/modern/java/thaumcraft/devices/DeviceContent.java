package thaumcraft.devices;

import java.util.Set;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import thaumcraft.Registrar;
import thaumcraft.Thaumcraft;
import thaumcraft.content.BasicContent;
import thaumcraft.content.SimpleContent;

public final class DeviceContent {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Thaumcraft.MODID);
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Thaumcraft.MODID);
    public static final RegistryObject<DataComponentType<Integer>> CHARGE = COMPONENTS.register("vis_charge", () -> DataComponentType.<Integer>builder()
        .persistent(Codec.intRange(0, 32766)).networkSynchronized(ByteBufCodecs.fromCodec(Codec.intRange(0, 32766))).build());
    public static final RegistryObject<AuraDeviceBlock> GENERATOR = block("vis_generator", AuraDeviceBlock.Kind.GENERATOR);
    public static final RegistryObject<AuraDeviceBlock> DIOPTRA = block("dioptra", AuraDeviceBlock.Kind.DIOPTRA);
    public static final RegistryObject<AuraDeviceBlock> PEDESTAL = block("recharge_pedestal", AuraDeviceBlock.Kind.PEDESTAL);
    public static final RegistryObject<AuraDeviceBlock> LEVITATOR = block("levitator", AuraDeviceBlock.Kind.LEVITATOR);
    public static final RegistryObject<AuraDeviceBlock> SIPHON = block("void_siphon", AuraDeviceBlock.Kind.SIPHON);
    public static final RegistryObject<BlockEntityType<LevitatorEntity>> LEVITATOR_TYPE = TYPES.register("levitator", () -> new BlockEntityType<>(LevitatorEntity::new, Set.of(LEVITATOR.get())));
    public static final RegistryObject<BlockEntityType<VisGeneratorEntity>> GENERATOR_TYPE = TYPES.register("vis_generator", () -> new BlockEntityType<>(VisGeneratorEntity::new, Set.of(GENERATOR.get())));
    public static final RegistryObject<BlockEntityType<DioptraEntity>> DIOPTRA_TYPE = TYPES.register("dioptra", () -> new BlockEntityType<>(DioptraEntity::new, Set.of(DIOPTRA.get())));
    public static final RegistryObject<BlockEntityType<RechargePedestalEntity>> PEDESTAL_TYPE = TYPES.register("recharge_pedestal", () -> new BlockEntityType<>(RechargePedestalEntity::new, Set.of(PEDESTAL.get())));
    public static final RegistryObject<BlockEntityType<VoidSiphonEntity>> SIPHON_TYPE = TYPES.register("void_siphon", () -> new BlockEntityType<>(VoidSiphonEntity::new, Set.of(SIPHON.get())));
    public static final RegistryObject<GrassBlock> GRASS = Registrar.BLOCKS.register("grass_ambient", () -> new AmbientGrassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
        .setId(ResourceKey.create(Registries.BLOCK, SimpleContent.id("grass_ambient")))));
    static { item("grass_ambient", GRASS); }
    private DeviceContent() {}
    public static void register(BusGroup bus) { TYPES.register(bus); COMPONENTS.register(bus); }
    private static RegistryObject<AuraDeviceBlock> block(String name, AuraDeviceBlock.Kind kind) {
        var block = Registrar.BLOCKS.register(name, () -> new AuraDeviceBlock(kind, BlockBehaviour.Properties.of()
            .setId(ResourceKey.create(Registries.BLOCK, SimpleContent.id(name))).strength(2).noOcclusion()
            .sound(kind == AuraDeviceBlock.Kind.GENERATOR ? SoundType.WOOD : SoundType.STONE)));
        item(name, block); return block;
    }
    private static void item(String name, RegistryObject<? extends Block> block) {
        var item = Registrar.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, SimpleContent.id(name))).useBlockDescriptionPrefix()));
        BasicContent.display(item);
    }
}
