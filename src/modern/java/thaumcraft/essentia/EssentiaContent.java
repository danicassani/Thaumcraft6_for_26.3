package thaumcraft.essentia;

import com.mojang.serialization.Codec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.*;
import thaumcraft.Registrar;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.*;
import thaumcraft.content.*;

public final class EssentiaContent{
    private static final DeferredRegister<BlockEntityType<?>> TYPES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,Thaumcraft.MODID);
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,Thaumcraft.MODID);
    public static final RegistryObject<DataComponentType<String>> FILTER=COMPONENTS.register("essentia_filter",()->DataComponentType.<String>builder().persistent(AspectAmounts.ASPECT).networkSynchronized(ByteBufCodecs.fromCodec(AspectAmounts.ASPECT)).build());
    public static final Map<String,RegistryObject<TubeBlock>> TUBES=new LinkedHashMap<>();
    static{for(var k:TubeBlock.Kind.values()){String id=k==TubeBlock.Kind.NORMAL?"tube":"tube_"+k.name().toLowerCase(Locale.ROOT);var block=Registrar.BLOCKS.register(id,()->new TubeBlock(k,BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id(id))).strength(.5F,5).sound(SoundType.METAL).noOcclusion()));TUBES.put(id,block);var item=Registrar.ITEMS.register(id,()->new BlockItem(block.get(),properties(id).useBlockDescriptionPrefix()));BasicContent.display(item);}}
    public static final RegistryObject<BlockEntityType<TubeEntity>> TYPE=TYPES.register("essentia_tube",()->new BlockEntityType<>(TubeEntity::new,new HashSet<>(TUBES.values().stream().map(RegistryObject::get).toList())));
    public static final RegistryObject<Item> LABEL=Registrar.ITEMS.register("label",()->new Item(properties("label")){
        @Override public InteractionResult useOn(UseOnContext c){if(c.getPlayer()!=null&&c.getLevel().getBlockEntity(c.getClickedPos()) instanceof AlchemyMachine m)return m.label(c.getPlayer(),c.getHand(),c.getClickedFace());return InteractionResult.PASS;}
    });
    public static final RegistryObject<Item> RESONATOR=Registrar.ITEMS.register("resonator",()->new Item(properties("resonator").stacksTo(1)){
        @Override public InteractionResult useOn(UseOnContext c){var player=c.getPlayer();if(player==null||!(c.getLevel().getBlockEntity(c.getClickedPos()) instanceof EssentiaTransport t))return InteractionResult.PASS;if(!c.getLevel().isClientSide())inspect(player,t,c.getClickedFace());return InteractionResult.SUCCESS;}
    });
    public static final RegistryObject<RecipeSerializer<LabelRecipe>> LABEL_RECIPE=Registrar.RECIPE_SERIALIZERS.register("essentia_label",()->new RecipeSerializer<>(com.mojang.serialization.MapCodec.unit(new LabelRecipe()),net.minecraft.network.codec.StreamCodec.unit(new LabelRecipe())));
    public static final RegistryObject<RecipeSerializer<EmptyPhialRecipe>> BUFFER_RECIPE=Registrar.RECIPE_SERIALIZERS.register("essentia_buffer",()->new RecipeSerializer<>(com.mojang.serialization.MapCodec.unit(new EmptyPhialRecipe()),net.minecraft.network.codec.StreamCodec.unit(new EmptyPhialRecipe())));
    static{BasicContent.display(LABEL);BasicContent.display(RESONATOR);}
    private EssentiaContent(){}
    private static Item.Properties properties(String id){return new Item.Properties().setId(ResourceKey.create(Registries.ITEM,SimpleContent.id(id)));}
    public static ItemStack label(String aspect){var s=new ItemStack(LABEL.get());s.set(AlchemyContent.ESSENCE.get(),aspect);s.set(net.minecraft.core.component.DataComponents.ITEM_NAME,Component.translatable("essentia.thaumcraft.label",Component.translatable("tc.aspect."+aspect)));return s;}
    public static void inspect(Player player,EssentiaTransport t,Direction face){
        var amounts=t.contents(face);String text=amounts.isEmpty()?"0":amounts.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e->Component.translatable("tc.aspect."+e.getKey()).getString()+" "+e.getValue()).collect(java.util.stream.Collectors.joining(", "));
        String type=t.suctionType(face);player.sendSystemMessage(Component.translatable("essentia.thaumcraft.reading",text,t.suction(face),type.isEmpty()?Component.translatable("essentia.thaumcraft.any"):Component.translatable("tc.aspect."+type)));
        if(t instanceof TubeEntity tube){if(!tube.supported())player.sendSystemMessage(Component.translatable("alchemy.thaumcraft.invalid"));else if(tube.vent()>0)player.sendSystemMessage(Component.translatable("essentia.thaumcraft.conflict"));if(!tube.filter().isEmpty())player.sendSystemMessage(Component.translatable("essentia.thaumcraft.filter",Component.translatable("tc.aspect."+tube.filter())));if(tube.kind()==TubeBlock.Kind.BUFFER)player.sendSystemMessage(Component.translatable("essentia.thaumcraft.choke",tube.snapshot().chokes().get(face.ordinal())));}
        if(t instanceof AlchemyMachine m&&!m.filter().isEmpty())player.sendSystemMessage(Component.translatable("essentia.thaumcraft.filter",Component.translatable("tc.aspect."+m.filter())));
    }
    public static void register(BusGroup bus){TYPES.register(bus);COMPONENTS.register(bus);}
}
