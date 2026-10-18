package thaumcraft.infusion;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import thaumcraft.alchemy.AspectAmounts;
public record InfusionRecipe(InfusionIngredient input,List<InfusionIngredient> components,Map<String,Integer> aspects,int instability,String research,ItemStackTemplate result) implements Recipe<InfusionInput>{
    public static final MapCodec<InfusionRecipe> CODEC=RecordCodecBuilder.mapCodec(i->i.group(InfusionIngredient.CODEC.fieldOf("input").forGetter(InfusionRecipe::input),InfusionIngredient.CODEC.listOf(1,32).fieldOf("components").forGetter(InfusionRecipe::components),AspectAmounts.CODEC.fieldOf("aspects").forGetter(InfusionRecipe::aspects),Codec.intRange(0,100).fieldOf("instability").forGetter(InfusionRecipe::instability),Codec.string(0,192).fieldOf("research").forGetter(InfusionRecipe::research),ItemStackTemplate.CODEC.fieldOf("result").forGetter(InfusionRecipe::result)).apply(i,InfusionRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf,InfusionRecipe> STREAM=ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());
    public InfusionRecipe{components=List.copyOf(components);aspects=Map.copyOf(aspects);}
    public int[] assignment(InfusionInput in){if(!input.test(in.primary())||components.size()!=in.components().size())return null;int n=components.size();boolean[][] matrix=new boolean[n][n];for(int r=0;r<n;r++)for(int s=0;s<n;s++)matrix[r][s]=components.get(r).test(in.components().get(s));return InfusionMatching.assign(matrix);}
    @Override public boolean matches(InfusionInput in,Level l){return assignment(in)!=null;}
    @Override public ItemStack assemble(InfusionInput in){var out=result.create();if(in.primary().isDamageableItem()&&out.isDamageableItem()&&in.primary().getDamageValue()>0)out.setDamageValue(Math.min(out.getMaxDamage()-1,(int)((long)out.getMaxDamage()*in.primary().getDamageValue()/in.primary().getMaxDamage())));return out;}
    @Override public RecipeSerializer<InfusionRecipe> getSerializer(){return InfusionContent.SERIALIZER.get();}
    @Override public RecipeType<InfusionRecipe> getType(){return InfusionContent.RECIPES.get();}
    @Override public PlacementInfo placementInfo(){return PlacementInfo.create(input.item());}
    @Override public RecipeBookCategory recipeBookCategory(){return RecipeBookCategories.CRAFTING_MISC;}
    @Override public boolean showNotification(){return false;}
    @Override public String group(){return "thaumcraft:infusion";}
}
