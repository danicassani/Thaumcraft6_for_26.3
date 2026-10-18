package thaumcraft.alchemy;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** Datapack recipe, separate from crafting and cooking; the machine pays aspects and water. */
public record AlchemyRecipe(Ingredient catalyst,Map<String,Integer> aspects,ItemStackTemplate result,String research) implements Recipe<SingleRecipeInput>{
    public static final MapCodec<AlchemyRecipe> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
        Ingredient.CODEC.fieldOf("catalyst").forGetter(AlchemyRecipe::catalyst),AspectAmounts.CODEC.validate(m->m.isEmpty()?DataResult.error(()->"Alchemy needs aspects"):DataResult.success(m)).fieldOf("aspects").forGetter(AlchemyRecipe::aspects),
        ItemStackTemplate.CODEC.fieldOf("result").forGetter(AlchemyRecipe::result),Codec.string(0,192).fieldOf("research").forGetter(AlchemyRecipe::research)
    ).apply(i,AlchemyRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf,AlchemyRecipe> STREAM=ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());
    public AlchemyRecipe{aspects=Map.copyOf(aspects);}
    @Override public boolean matches(SingleRecipeInput input,Level level){return catalyst.test(input.item());}
    @Override public ItemStack assemble(SingleRecipeInput input){return result.create();}
    @Override public RecipeSerializer<AlchemyRecipe> getSerializer(){return AlchemyContent.SERIALIZER.get();}
    @Override public RecipeType<AlchemyRecipe> getType(){return AlchemyContent.RECIPES.get();}
    @Override public PlacementInfo placementInfo(){return PlacementInfo.create(catalyst);}
    @Override public RecipeBookCategory recipeBookCategory(){return RecipeBookCategories.CRAFTING_MISC;}
    @Override public boolean showNotification(){return false;}
    @Override public String group(){return "thaumcraft:alchemy";}
}
