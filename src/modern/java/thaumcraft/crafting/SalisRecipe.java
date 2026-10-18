package thaumcraft.crafting;

import java.util.HashSet;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import thaumcraft.crystals.CrystalContent;

/** Original three distinct aspect crystals, flint, redstone and bowl. */
public final class SalisRecipe extends CustomRecipe {
    @Override public boolean matches(CraftingInput input,Level level) {
        int flint=0,redstone=0,bowl=0;var aspects=new HashSet<String>();int crystals=0;
        for(var stack:input.items()) {
            if(stack.isEmpty())continue;
            if(stack.is(Items.FLINT))flint++;
            else if(stack.is(Items.REDSTONE))redstone++;
            else if(stack.is(Items.BOWL))bowl++;
            else if(stack.is(CrystalContent.ESSENCE.get())) { aspects.add(CrystalContent.aspect(stack));crystals++; }
            else return false;
        }
        return flint==1 && redstone==1 && bowl==1 && crystals==3 && aspects.size()==3;
    }
    @Override public ItemStack assemble(CraftingInput input) { return new ItemStack(ArcaneContent.SALIS.get()); }
    @Override public RecipeSerializer<SalisRecipe> getSerializer() { return ArcaneContent.SALIS_RECIPE.get(); }
    @Override public net.minecraft.core.NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        var remaining=CraftingRecipe.defaultCraftingReminder(input);
        for(int i=0;i<input.size();i++)if(input.getItem(i).is(Items.FLINT)||input.getItem(i).is(Items.BOWL))remaining.set(i,input.getItem(i).copyWithCount(1));
        return remaining;
    }
}
