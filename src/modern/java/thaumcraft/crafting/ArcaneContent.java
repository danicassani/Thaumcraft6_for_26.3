package thaumcraft.crafting;

import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
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

public final class ArcaneContent {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Thaumcraft.MODID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Thaumcraft.MODID);
    public static final RegistryObject<WorkbenchBlock> WORKBENCH = Registrar.BLOCKS.register("arcane_workbench", () -> new WorkbenchBlock(properties("arcane_workbench")));
    public static final RegistryObject<WorkbenchChargerBlock> CHARGER = Registrar.BLOCKS.register("arcane_workbench_charger", () -> new WorkbenchChargerBlock(properties("arcane_workbench_charger")));
    public static final RegistryObject<BlockEntityType<ArcaneWorkbenchEntity>> WORKBENCH_TYPE = TYPES.register("arcane_workbench", () -> new BlockEntityType<>(ArcaneWorkbenchEntity::new, Set.of(WORKBENCH.get())));
    public static final RegistryObject<MenuType<ArcaneMenu>> MENU = MENUS.register("arcane_workbench", () -> new MenuType<>(ArcaneMenu::new, FeatureFlags.VANILLA_SET));
    public static final RegistryObject<RecipeType<ArcaneRecipe>> RECIPES = Registrar.RECIPE_TYPES.register("arcane", () -> new RecipeType<>() { @Override public String toString() { return "thaumcraft:arcane"; } });
    public static final RegistryObject<RecipeSerializer<ArcaneRecipe>> SERIALIZER = Registrar.RECIPE_SERIALIZERS.register("arcane", () -> new RecipeSerializer<>(ArcaneRecipe.CODEC, ArcaneRecipe.STREAM));
    public static final RegistryObject<Item> SALIS = Registrar.ITEMS.register("salis_mundus", () -> new SalisMundusItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, SimpleContent.id("salis_mundus")))));
    public static final RegistryObject<RecipeSerializer<SalisRecipe>> SALIS_RECIPE = Registrar.RECIPE_SERIALIZERS.register("salis_mundus", () -> new RecipeSerializer<>(com.mojang.serialization.MapCodec.unit(new SalisRecipe()), net.minecraft.network.codec.StreamCodec.unit(new SalisRecipe())));
    static { blockItem("arcane_workbench", WORKBENCH); blockItem("arcane_workbench_charger", CHARGER); BasicContent.display(SALIS); }
    private ArcaneContent() {}
    private static BlockBehaviour.Properties properties(String id) { return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, SimpleContent.id(id))).strength(1.25F,10).sound(SoundType.WOOD).noOcclusion(); }
    private static void blockItem(String id, RegistryObject<? extends Block> block) { var item = Registrar.ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, SimpleContent.id(id))).useBlockDescriptionPrefix())); BasicContent.display(item); }
    public static void register(BusGroup bus) { TYPES.register(bus); MENUS.register(bus); }
}
