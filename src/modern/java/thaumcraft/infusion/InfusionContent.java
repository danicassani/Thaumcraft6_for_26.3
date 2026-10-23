package thaumcraft.infusion;
import java.util.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.*;
import thaumcraft.*;
import thaumcraft.content.*;
import com.mojang.serialization.Codec;
public final class InfusionContent{
    private static final DeferredRegister<BlockEntityType<?>> TYPES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,Thaumcraft.MODID);
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,Thaumcraft.MODID);
    public static final RegistryObject<DataComponentType<Integer>> BRAIN_XP=COMPONENTS.register("brain_xp",()->DataComponentType.<Integer>builder().persistent(Codec.intRange(0,2000)).networkSynchronized(ByteBufCodecs.fromCodec(Codec.intRange(0,2000))).build());
    public static final RegistryObject<InfusionBlock> ARCANE=block("pedestal_arcane",InfusionBlock.Kind.PEDESTAL),ANCIENT=block("pedestal_ancient",InfusionBlock.Kind.PEDESTAL),ELDRITCH=block("pedestal_eldritch",InfusionBlock.Kind.PEDESTAL),MATRIX=block("infusion_matrix",InfusionBlock.Kind.MATRIX),BRAIN_JAR=block("jar_brain",InfusionBlock.Kind.BRAIN),STABILIZER=block("stabilizer",InfusionBlock.Kind.STABILIZER),INLAY=block("inlay",InfusionBlock.Kind.INLAY);
    public static final RegistryObject<Block> GLIMMER=Registrar.BLOCKS.register("effect_glimmer",()->new thaumcraft.utility.GlimmerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id("effect_glimmer"))).noCollision().replaceable().strength(0).noLootTable().lightLevel(s->15).noOcclusion()));
    public static final RegistryObject<BlockEntityType<InfusionPedestal>> PEDESTAL_TYPE=TYPES.register("infusion_pedestal",()->new BlockEntityType<>(InfusionPedestal::new,Set.of(ARCANE.get(),ANCIENT.get(),ELDRITCH.get())));
    public static final RegistryObject<BlockEntityType<InfusionMatrix>> MATRIX_TYPE=TYPES.register("infusion_matrix",()->new BlockEntityType<>(InfusionMatrix::new,Set.of(MATRIX.get())));
    public static final RegistryObject<BlockEntityType<BrainJar>> BRAIN_TYPE=TYPES.register("jar_brain",()->new BlockEntityType<>(BrainJar::new,Set.of(BRAIN_JAR.get())));
    public static final RegistryObject<BlockEntityType<StabilizationEntity>> STABILIZER_TYPE=TYPES.register("stabilization",()->new BlockEntityType<>(StabilizationEntity::new,Set.of(STABILIZER.get(),INLAY.get())));
    public static final RegistryObject<RecipeType<InfusionRecipe>> RECIPES=Registrar.RECIPE_TYPES.register("infusion",()->new RecipeType<>(){@Override public String toString(){return "thaumcraft:infusion";}});
    public static final RegistryObject<RecipeSerializer<InfusionRecipe>> SERIALIZER=Registrar.RECIPE_SERIALIZERS.register("infusion",()->new RecipeSerializer<>(InfusionRecipe.CODEC,InfusionRecipe.STREAM));
    private static RegistryObject<InfusionBlock> block(String id,InfusionBlock.Kind kind){var b=Registrar.BLOCKS.register(id,()->new InfusionBlock(kind,BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id(id))).strength(2,10).lightLevel(s->kind==InfusionBlock.Kind.STABILIZER?4:kind==InfusionBlock.Kind.INLAY?1:0).noOcclusion().sound(SoundType.STONE)));var item=Registrar.ITEMS.register(id,()->new BlockItem(b.get(),new Item.Properties().setId(ResourceKey.create(Registries.ITEM,SimpleContent.id(id))).useBlockDescriptionPrefix()));BasicContent.display(item);return b;}
    public static void register(BusGroup bus){TYPES.register(bus);COMPONENTS.register(bus);InfusionSounds.register(bus);CuriosityContent.register(bus);InfusionEnchantments.register(bus);}
}
