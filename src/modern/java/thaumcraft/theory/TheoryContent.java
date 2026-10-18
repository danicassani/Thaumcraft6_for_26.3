package thaumcraft.theory;

import java.util.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.*;
import thaumcraft.*;
import thaumcraft.content.*;

public final class TheoryContent {
    private static final DeferredRegister<BlockEntityType<?>> TYPES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,Thaumcraft.MODID);
    private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,Thaumcraft.MODID);
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,Thaumcraft.MODID);
    public static final List<String> NOTES=List.of("sun","stars_1","stars_2","stars_3","stars_4","moon_1","moon_2","moon_3","moon_4","moon_5","moon_6","moon_7","moon_8");
    public static final RegistryObject<DataComponentType<Integer>> NOTE=COMPONENTS.register("celestial_note",()->DataComponentType.<Integer>builder().persistent(com.mojang.serialization.Codec.intRange(0,12)).networkSynchronized(ByteBufCodecs.fromCodec(com.mojang.serialization.Codec.intRange(0,12))).build());
    public static final RegistryObject<Item> SCRIBE=Registrar.ITEMS.register("scribing_tools",()->new ScribingToolsItem(itemProperties("scribing_tools").durability(100)));
    public static final RegistryObject<Item> CELESTIAL=Registrar.ITEMS.register("celestial_notes",()->new Item(itemProperties("celestial_notes").component(NOTE.get(),0)));
    public static final RegistryObject<ResearchTableBlock> WOOD=block("table_wood",false);
    public static final RegistryObject<ResearchTableBlock> TABLE=block("research_table",true);
    public static final RegistryObject<BlockEntityType<ResearchTableEntity>> TYPE=TYPES.register("research_table",()->new BlockEntityType<>(ResearchTableEntity::new,Set.of(TABLE.get())));
    public static final RegistryObject<MenuType<TheoryMenu>> MENU=MENUS.register("research_table",()->new MenuType<>(TheoryMenu::new,FeatureFlags.VANILLA_SET));
    public static final RegistryObject<RecipeSerializer<ScribingRefillRecipe>> REFILL=Registrar.RECIPE_SERIALIZERS.register("scribing_refill",()->new RecipeSerializer<>(com.mojang.serialization.MapCodec.unit(new ScribingRefillRecipe()),net.minecraft.network.codec.StreamCodec.unit(new ScribingRefillRecipe())));
    static{BasicContent.display(SCRIBE);BasicContent.displayStack(()->note(0));for(int n=1;n<13;n++){final int v=n;BasicContent.displayStack(()->note(v));}}
    private TheoryContent(){}
    public static Item.Properties itemProperties(String id){return new Item.Properties().setId(ResourceKey.create(Registries.ITEM,SimpleContent.id(id)));}
    private static RegistryObject<ResearchTableBlock> block(String id,boolean research){
        var b=Registrar.BLOCKS.register(id,()->new ResearchTableBlock(research,BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id(id))).strength(2).sound(SoundType.WOOD).noOcclusion()));
        var item=Registrar.ITEMS.register(id,()->new BlockItem(b.get(),itemProperties(id).useBlockDescriptionPrefix()));BasicContent.display(item);return b;
    }
    public static ItemStack note(int variant){if(variant<0||variant>=13)throw new IllegalArgumentException("Unknown celestial note");var s=new ItemStack(CELESTIAL.get());s.set(NOTE.get(),variant);s.set(net.minecraft.core.component.DataComponents.ITEM_NAME,net.minecraft.network.chat.Component.translatable("item.thaumcraft.celestial_notes."+NOTES.get(variant)));return s;}
    public static int variant(ItemStack stack){return stack.getOrDefault(NOTE.get(),0);}
    public static boolean ink(ItemStack stack,int amount){return stack.is(SCRIBE.get())&&stack.getMaxDamage()-stack.getDamageValue()>=amount;}
    public static void register(BusGroup bus){TYPES.register(bus);MENUS.register(bus);COMPONENTS.register(bus);}
}
