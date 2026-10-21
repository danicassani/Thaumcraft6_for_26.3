package thaumcraft.thaumatorium;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.*;
import thaumcraft.*;
import thaumcraft.content.*;
public final class ThaumatoriumContent{
 private static final DeferredRegister<BlockEntityType<?>> TYPES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,Thaumcraft.MODID);
 private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,Thaumcraft.MODID);
 public static final RegistryObject<ThaumatoriumBlock> BASE=Registrar.BLOCKS.register("thaumatorium",()->new ThaumatoriumBlock(false,props("thaumatorium")));
 public static final RegistryObject<ThaumatoriumBlock> TOP=Registrar.BLOCKS.register("thaumatorium_top",()->new ThaumatoriumBlock(true,props("thaumatorium_top")));
 public static final RegistryObject<BrainBoxBlock> BRAIN=Registrar.BLOCKS.register("brain_box",()->new BrainBoxBlock(props("brain_box")));
 public static final RegistryObject<Item> ICON=Registrar.ITEMS.register("thaumatorium",()->new Item(item("thaumatorium").useBlockDescriptionPrefix()));
 public static final RegistryObject<Item> BRAIN_ITEM=Registrar.ITEMS.register("brain_box",()->new BlockItem(BRAIN.get(),item("brain_box").useBlockDescriptionPrefix()));
 public static final RegistryObject<BlockEntityType<Thaumatorium>> TYPE=TYPES.register("thaumatorium",()->new BlockEntityType<>(Thaumatorium::new,Set.of(BASE.get(),TOP.get())));
 public static final RegistryObject<MenuType<ThaumatoriumMenu>> MENU=MENUS.register("thaumatorium",()->new MenuType<>(ThaumatoriumMenu::new,FeatureFlags.VANILLA_SET));
 static{BasicContent.display(BRAIN_ITEM);}
 private static BlockBehaviour.Properties props(String n){return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id(n))).strength(2,10).sound(SoundType.METAL).noOcclusion();}
 private static Item.Properties item(String n){return new Item.Properties().setId(ResourceKey.create(Registries.ITEM,SimpleContent.id(n)));}
 public static void register(BusGroup b){TYPES.register(b);MENUS.register(b);}
}
