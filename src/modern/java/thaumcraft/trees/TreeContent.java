package thaumcraft.trees;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.*;
import thaumcraft.*;
import thaumcraft.content.*;
public final class TreeContent {
    private static final DeferredRegister<com.mojang.serialization.MapCodec<? extends Feature>> FEATURES=DeferredRegister.create(Registries.FEATURE_TYPE,Thaumcraft.MODID);
    public static final Map<String,RegistryObject<Block>> BLOCKS=new LinkedHashMap<>();
    static{
        FEATURES.register("special_tree",()->SpecialTreeFeature.CODEC);
        FEATURES.register("forest_decoration",()->thaumcraft.worldgen.ForestDecoration.CODEC);
        for(var wood:List.of("greatwood","silverwood")){
            var key=ResourceKey.<Feature>create(Registries.FEATURE,SimpleContent.id(wood));var grower=new TreeGrower("thaumcraft_"+wood,WeightedList.of(key),WeightedList.of(),WeightedList.of(),key);
            add("sapling_"+wood,()->new SaplingBlock(grower,props("sapling_"+wood).noCollision().noOcclusion().randomTicks().instabreak().sound(SoundType.GRASS)));
            add("leaves_"+wood,()->wood.equals("silverwood")?new SilverwoodLeaves(props("leaves_"+wood).strength(.2F).sound(SoundType.GRASS).randomTicks().noOcclusion()):new TintedParticleLeavesBlock(.01F,props("leaves_"+wood).strength(.2F).sound(SoundType.GRASS).randomTicks().noOcclusion()));
        }
    }
    private TreeContent(){}
    private static BlockBehaviour.Properties props(String n){return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id(n)));}
    private static void add(String n,java.util.function.Supplier<Block> supplier){var block=Registrar.BLOCKS.register(n,supplier);BLOCKS.put(n,block);var item=Registrar.ITEMS.register(n,()->new BlockItem(block.get(),new Item.Properties().setId(ResourceKey.create(Registries.ITEM,SimpleContent.id(n))).useBlockDescriptionPrefix()));BasicContent.display(item);}
    public static void register(BusGroup bus){FEATURES.register(bus);}
}
