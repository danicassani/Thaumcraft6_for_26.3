package thaumcraft.alchemy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
public final class PhialScribingRecipe extends CustomRecipe{
    @Override public boolean matches(CraftingInput input,Level level){int phial=0,feather=0,dye=0;for(var s:input.items()){if(s.isEmpty())continue;if(s.is(AlchemyContent.PHIAL.get())&&AlchemyContent.aspect(s).isEmpty()&&AlchemyContent.amount(s)==0)phial++;else if(s.is(Items.FEATHER))feather++;else if(s.is(BuiltInRegistries.ITEM.getValue(Identifier.parse("minecraft:black_dye"))))dye++;else return false;}return phial==1&&feather==1&&dye==1;}
    @Override public ItemStack assemble(CraftingInput input){return new ItemStack(thaumcraft.theory.TheoryContent.SCRIBE.get());}
    @Override public RecipeSerializer<PhialScribingRecipe> getSerializer(){return AlchemyContent.SCRIBING_RECIPE.get();}
}
