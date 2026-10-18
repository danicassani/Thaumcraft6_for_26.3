package thaumcraft.essentia;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import thaumcraft.alchemy.AlchemyContent;

/** Encode with ten units and return the bottle; a single encoded label can be erased. */
public final class LabelRecipe extends CustomRecipe{
    @Override public boolean matches(CraftingInput in,Level l){int blank=0,filled=0,phial=0;for(var s:in.items()){if(s.isEmpty())continue;if(s.is(EssentiaContent.LABEL.get())){if(AlchemyContent.aspect(s).isEmpty())blank++;else filled++;}else if(s.is(AlchemyContent.PHIAL.get())&&AlchemyContent.amount(s)==10&&!AlchemyContent.aspect(s).isEmpty())phial++;else return false;}return blank==1&&phial==1&&filled==0||blank==0&&phial==0&&filled==1;}
    @Override public ItemStack assemble(CraftingInput in){for(var s:in.items())if(s.is(AlchemyContent.PHIAL.get()))return EssentiaContent.label(AlchemyContent.aspect(s));return new ItemStack(EssentiaContent.LABEL.get());}
    @Override public NonNullList<ItemStack> getRemainingItems(CraftingInput in){var out=NonNullList.withSize(in.size(),ItemStack.EMPTY);for(int i=0;i<in.size();i++)if(in.getItem(i).is(AlchemyContent.PHIAL.get()))out.set(i,new ItemStack(AlchemyContent.PHIAL.get()));return out;}
    @Override public RecipeSerializer<LabelRecipe> getSerializer(){return EssentiaContent.LABEL_RECIPE.get();}
}
