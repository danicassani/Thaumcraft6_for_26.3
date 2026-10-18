package thaumcraft.alchemy;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
public final class VoidJarRecipe extends CustomRecipe{
    @Override public boolean matches(CraftingInput input,Level level){int n=0;for(var s:input.items()){if(s.isEmpty())continue;if(!s.is(AlchemyContent.JAR.get().asItem()))return false;String a=AlchemyContent.aspect(s);int amount=AlchemyContent.amount(s);String f=s.getOrDefault(thaumcraft.essentia.EssentiaContent.FILTER.get(),"");if(a.isEmpty()!=(amount==0)||amount>250||!f.isEmpty()&&!a.isEmpty()&&!f.equals(a))return false;n++;}return n==1;}
    @Override public ItemStack assemble(CraftingInput input){for(var s:input.items())if(!s.isEmpty()){var r=AlchemyContent.amount(s)>0?AlchemyContent.filled(AlchemyContent.VOID_JAR.get().asItem(),AlchemyContent.aspect(s),AlchemyContent.amount(s)):new ItemStack(AlchemyContent.VOID_JAR.get());var filter=s.get(thaumcraft.essentia.EssentiaContent.FILTER.get());if(filter!=null)r.set(thaumcraft.essentia.EssentiaContent.FILTER.get(),filter);var name=s.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME);if(name!=null)r.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,name);return r;}return ItemStack.EMPTY;}
    @Override public RecipeSerializer<VoidJarRecipe> getSerializer(){return AlchemyContent.VOID_RECIPE.get();}
}
