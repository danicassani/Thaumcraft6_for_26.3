package thaumcraft.essentia;

import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import thaumcraft.alchemy.AlchemyContent;
import thaumcraft.content.SimpleContent;

/** Original buffer pattern, with an explicit empty-phial check to preserve filled containers. */
public final class EmptyPhialRecipe extends CustomRecipe{
    @Override public boolean matches(CraftingInput in,Level l){if(in.width()!=3||in.height()!=3)return false;for(int i=0;i<9;i++){var s=in.getItem(i);boolean ok=switch(i){case 0,2,6,8->s.is(AlchemyContent.PHIAL.get())&&AlchemyContent.amount(s)==0&&AlchemyContent.aspect(s).isEmpty();case 1->s.is(EssentiaContent.TUBES.get("tube_valve").get().asItem());case 3,5->s.is(EssentiaContent.TUBES.get("tube").get().asItem());case 4->s.is(SimpleContent.ITEMS.get("plate_iron").get());case 7->s.is(EssentiaContent.TUBES.get("tube_restrict").get().asItem());default->false;};if(!ok)return false;}return true;}
    @Override public ItemStack assemble(CraftingInput in){return new ItemStack(EssentiaContent.TUBES.get("tube_buffer").get());}
    @Override public RecipeSerializer<EmptyPhialRecipe> getSerializer(){return EssentiaContent.BUFFER_RECIPE.get();}
}
