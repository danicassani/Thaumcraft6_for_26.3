package thaumcraft.crystals;

import com.mojang.serialization.Codec;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import thaumcraft.Registrar;
import thaumcraft.Thaumcraft;
import thaumcraft.content.BasicContent;
import thaumcraft.content.SimpleContent;

public final class CrystalContent {
    public static final String[] NATURAL = {"aer", "ignis", "aqua", "terra", "ordo", "perditio"};
    public static final Codec<String> ASPECT_CODEC = Codec.stringResolver(s -> s, s -> CrystalAspects.COLORS.containsKey(s) ? s : null);
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Thaumcraft.MODID);
    public static final RegistryObject<DataComponentType<String>> ASPECT = COMPONENTS.register("crystal_aspect", () -> DataComponentType.<String>builder()
        .persistent(ASPECT_CODEC).networkSynchronized(ByteBufCodecs.fromCodec(ASPECT_CODEC)).build());
    private static final DeferredRegister<com.mojang.serialization.MapCodec<? extends Feature>> FEATURES = DeferredRegister.create(Registries.FEATURE_TYPE, Thaumcraft.MODID);
    static { FEATURES.register("crystals", () -> CrystalFeature.CODEC); }
    public static final RegistryObject<Item> ESSENCE = Registrar.ITEMS.register("crystal_essence", () -> new Item(new Item.Properties()
        .setId(ResourceKey.create(Registries.ITEM, SimpleContent.id("crystal_essence")))
        .component(ASPECT.get(), "aer").component(DataComponents.ITEM_NAME, Component.translatable("item.thaumcraft.crystal_essence.aer"))));
    public static final Map<String, RegistryObject<CrystalBlock>> BLOCKS = new LinkedHashMap<>();
    static {
        for (String aspect : new String[]{"aer", "ignis", "aqua", "terra", "ordo", "perditio", "vitium"}) {
            String name = "crystal_" + aspect;
            var block = Registrar.BLOCKS.register(name, () -> new CrystalBlock(aspect, BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, SimpleContent.id(name))).strength(.25F).sound(net.minecraft.world.level.block.SoundType.AMETHYST)
                .noCollision().noOcclusion().randomTicks().lightLevel(state -> 1)));
            BLOCKS.put(aspect, block);
            var item = Registrar.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, SimpleContent.id(name))).useBlockDescriptionPrefix()));
            BasicContent.display(item);
        }
    }
    private CrystalContent() {}
    public static void register(BusGroup bus) {
        COMPONENTS.register(bus); FEATURES.register(bus);
        for (String aspect : CrystalAspects.COLORS.keySet()) BasicContent.displayStack(() -> essence(aspect, 1));
    }
    public static ItemStack essence(String aspect, int count) {
        if (!CrystalAspects.COLORS.containsKey(aspect)) throw new IllegalArgumentException("Unknown crystal aspect: " + aspect);
        var stack = new ItemStack(ESSENCE.get(), count);
        stack.set(ASPECT.get(), aspect);
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.thaumcraft.crystal_essence." + aspect));
        return stack;
    }
    public static String aspect(ItemStack stack) { return stack.getOrDefault(ASPECT.get(), "aer"); }
}
