package thaumcraft.content;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import thaumcraft.Registrar;
import thaumcraft.Thaumcraft;

/** First content slice. Legacy metadata materials receive separate stable item IDs. */
public final class BasicContent {
    private static final List<java.util.function.Supplier<ItemStack>> DISPLAY_STACKS = new ArrayList<>();
    private static final List<RegistryObject<? extends Item>> DISPLAY_ITEMS = new ArrayList<>();
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Thaumcraft.MODID);

    public static final RegistryObject<Item> AMBER = material("amber");
    public static final RegistryObject<Item> QUICKSILVER = material("quicksilver");
    public static final RegistryObject<Item> INGOT_THAUMIUM = material("ingot_thaumium");
    public static final RegistryObject<Item> INGOT_VOID = material("ingot_void");
    public static final RegistryObject<Item> INGOT_BRASS = material("ingot_brass");
    public static final RegistryObject<Item> NUGGET_THAUMIUM = material("nugget_thaumium");
    public static final RegistryObject<Item> NUGGET_VOID = material("nugget_void");
    public static final RegistryObject<Item> NUGGET_BRASS = material("nugget_brass");
    public static final RegistryObject<Item> NUGGET_QUICKSILVER = material("nugget_quicksilver");

    public static final RegistryObject<Block> ORE_AMBER = block("ore_amber", 1.5F, false, true);
    public static final RegistryObject<Block> ORE_CINNABAR = block("ore_cinnabar", 2.0F, false, false);
    public static final RegistryObject<Block> ORE_QUARTZ = block("ore_quartz", 3.0F, false, true);
    public static final RegistryObject<Block> METAL_THAUMIUM = block("metal_thaumium", 4.0F, true, false);
    public static final RegistryObject<Block> METAL_VOID = block("metal_void", 4.0F, true, false);
    public static final RegistryObject<Block> METAL_BRASS = block("metal_brass", 4.0F, true, false);
    public static final RegistryObject<Block> AMBER_BLOCK = block("amber_block", 0.5F, false, false);
    public static final RegistryObject<Block> AMBER_BRICK = block("amber_brick", 0.5F, false, false);

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("thaumcraft", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.thaumcraft"))
            .icon(() -> new ItemStack(AMBER.get()))
            .displayItems((parameters, output) -> { DISPLAY_ITEMS.forEach(item -> output.accept(item.get())); DISPLAY_STACKS.forEach(stack -> output.accept(stack.get())); })
            .build());

    private BasicContent() {}

    public static void register(BusGroup bus) {
        SimpleContent.register(bus);
        LuminousContent.register();
        AuraContent.register();
        thaumcraft.crystals.CrystalContent.register(bus);
        thaumcraft.devices.DeviceContent.register(bus);
        thaumcraft.crafting.ArcaneContent.register(bus);
        thaumcraft.rifts.RiftContent.register();
        thaumcraft.research.ResearchContent.register(bus);
        thaumcraft.theory.TheoryContent.register(bus);
        thaumcraft.alchemy.AlchemyContent.register(bus);
        thaumcraft.essentia.EssentiaContent.register(bus);
        thaumcraft.infernal.InfernalContent.register(bus);
        thaumcraft.thaumatorium.ThaumatoriumContent.register(bus);
        thaumcraft.condenser.CondenserContent.register(bus);
        thaumcraft.infusion.InfusionContent.register(bus);
        thaumcraft.trees.TreeContent.register(bus);
        thaumcraft.thaumaturgy.ThaumaturgyContent.register(bus);
        thaumcraft.equipment.EquipmentContent.register(bus);
        thaumcraft.golems.GolemContent.register(bus);
        TABS.register(bus);
        OreCountPlacement.register(bus);
    }

    public static void displayStack(java.util.function.Supplier<ItemStack> stack) { DISPLAY_STACKS.add(stack); }

    public static void display(RegistryObject<? extends Item> item) { DISPLAY_ITEMS.add(item); }

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(Thaumcraft.MODID, name);
    }

    private static RegistryObject<Item> material(String name) {
        var item = Registrar.ITEMS.register(name, () -> new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id(name)))));
        DISPLAY_ITEMS.add(item);
        return item;
    }

    private static RegistryObject<Block> block(String name, float hardness, boolean metal, boolean experience) {
        var block = Registrar.BLOCKS.register(name, () -> {
            var properties = BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id(name)))
                .strength(hardness, metal ? 10.0F : 5.0F).requiresCorrectToolForDrops()
                .sound(metal ? SoundType.METAL : SoundType.STONE);
            if (name.startsWith("amber_")) {
                // Amber remains harvestable by hand, like the legacy translucent block.
                return new TransparentBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id(name)))
                    .strength(0.5F).sound(SoundType.STONE).noOcclusion());
            }
            return experience ? new DropExperienceBlock(UniformInt.of(1, 4), properties) : new Block(properties);
        });
        var item = Registrar.ITEMS.register(name, () -> new BlockItem(block.get(),
            new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id(name))).useBlockDescriptionPrefix()));
        DISPLAY_ITEMS.add(item);
        return block;
    }
}
