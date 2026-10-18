package thaumcraft.alchemy;

import java.util.*;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.*;
import thaumcraft.*;
import thaumcraft.content.*;

public final class AlchemyContent {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,Thaumcraft.MODID);
    private static final DeferredRegister<BlockEntityType<?>> TYPES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,Thaumcraft.MODID);
    private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,Thaumcraft.MODID);
    public static final RegistryObject<DataComponentType<String>> ESSENCE=COMPONENTS.register("essentia_aspect",()->DataComponentType.<String>builder().persistent(AspectAmounts.ASPECT).networkSynchronized(ByteBufCodecs.fromCodec(AspectAmounts.ASPECT)).build());
    public static final RegistryObject<DataComponentType<Integer>> AMOUNT=COMPONENTS.register("essentia_amount",()->DataComponentType.<Integer>builder().persistent(Codec.intRange(1,250)).networkSynchronized(ByteBufCodecs.fromCodec(Codec.intRange(1,250))).build());
    public static final RegistryObject<AlchemyBlock> CRUCIBLE=block("crucible",AlchemyBlock.Kind.CRUCIBLE),SMELTER=block("smelter_basic",AlchemyBlock.Kind.SMELTER),ALEMBIC=block("alembic",AlchemyBlock.Kind.ALEMBIC),JAR=block("jar_normal",AlchemyBlock.Kind.JAR),VOID_JAR=block("jar_void",AlchemyBlock.Kind.VOID_JAR);
    public static final RegistryObject<BlockEntityType<AlchemyMachine>> TYPE=TYPES.register("alchemy",()->new BlockEntityType<>(AlchemyMachine::new,Set.of(CRUCIBLE.get(),SMELTER.get(),ALEMBIC.get(),JAR.get(),VOID_JAR.get())));
    public static final RegistryObject<MenuType<AlchemyMenu>> MENU=MENUS.register("alchemy",()->new MenuType<>(AlchemyMenu::new,FeatureFlags.VANILLA_SET));
    public static final RegistryObject<RecipeType<AlchemyRecipe>> RECIPES=Registrar.RECIPE_TYPES.register("crucible",()->new RecipeType<>(){@Override public String toString(){return "thaumcraft:crucible";}});
    public static final RegistryObject<RecipeSerializer<AlchemyRecipe>> SERIALIZER=Registrar.RECIPE_SERIALIZERS.register("crucible",()->new RecipeSerializer<>(AlchemyRecipe.CODEC,AlchemyRecipe.STREAM));
    public static final RegistryObject<RecipeSerializer<VoidJarRecipe>> VOID_RECIPE=Registrar.RECIPE_SERIALIZERS.register("jar_void_upgrade",()->new RecipeSerializer<>(com.mojang.serialization.MapCodec.unit(new VoidJarRecipe()),net.minecraft.network.codec.StreamCodec.unit(new VoidJarRecipe())));
    public static final RegistryObject<RecipeSerializer<PhialScribingRecipe>> SCRIBING_RECIPE=Registrar.RECIPE_SERIALIZERS.register("phial_scribing",()->new RecipeSerializer<>(com.mojang.serialization.MapCodec.unit(new PhialScribingRecipe()),net.minecraft.network.codec.StreamCodec.unit(new PhialScribingRecipe())));
    public static final RegistryObject<Item> PHIAL=Registrar.ITEMS.register("phial",()->new PhialItem(properties("phial")));
    public static final RegistryObject<Item> ALUMENTUM=Registrar.ITEMS.register("alumentum",()->new AlumentumItem(properties("alumentum").component(DataComponents.COOKING_FUEL,new CookingFuel(new ResolvableInt.Constant(4800),new ResolvableFloat.Constant(1.25F)))));
    public static final RegistryObject<EntityType<AlumentumProjectile>> PROJECTILE=Registrar.ENTITIES.register("alumentum",()->EntityType.Builder.<AlumentumProjectile>of(AlumentumProjectile::new,MobCategory.MISC).sized(.25F,.25F).clientTrackingRange(4).updateInterval(1).build(ResourceKey.create(Registries.ENTITY_TYPE,SimpleContent.id("alumentum"))));
    static{BasicContent.display(PHIAL);BasicContent.display(ALUMENTUM);for(var a:thaumcraft.crystals.CrystalAspects.COLORS.keySet())BasicContent.displayStack(()->filled(PHIAL.get(),a,10));}
    private AlchemyContent(){}
    private static Item.Properties properties(String n){return new Item.Properties().setId(ResourceKey.create(Registries.ITEM,SimpleContent.id(n)));}
    private static RegistryObject<AlchemyBlock> block(String id,AlchemyBlock.Kind kind){var block=Registrar.BLOCKS.register(id,()->new AlchemyBlock(kind,BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id(id))).strength(2,10).sound(SoundType.METAL).noOcclusion()));var item=Registrar.ITEMS.register(id,()->new BlockItem(block.get(),properties(id).useBlockDescriptionPrefix()));BasicContent.display(item);return block;}
    public static ItemStack filled(Item item,String aspect,int amount){var s=new ItemStack(item);s.set(ESSENCE.get(),aspect);s.set(AMOUNT.get(),amount);s.set(DataComponents.ITEM_NAME,Component.translatable(item==PHIAL.get()?"alchemy.thaumcraft.phial":"alchemy.thaumcraft.jar",Component.translatable("tc.aspect."+aspect),amount));return s;}
    public static String aspect(ItemStack s){return s.getOrDefault(ESSENCE.get(),"");}
    public static int amount(ItemStack s){return s.getOrDefault(AMOUNT.get(),0);}
    public static ItemStack remainder(ItemStack s){if(s.is(PHIAL.get())&&amount(s)==10&&!aspect(s).isEmpty())return new ItemStack(PHIAL.get());var r=s.getCraftingRemainder();return r==null?ItemStack.EMPTY:r.create();}
    public static void register(BusGroup bus){COMPONENTS.register(bus);TYPES.register(bus);MENUS.register(bus);}
}
