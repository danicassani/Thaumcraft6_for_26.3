package thaumcraft.theory;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
/** Refills the existing tool, preserving its other components and refusing fresh tools. */
public final class ScribingRefillRecipe extends CustomRecipe {
    @Override public boolean matches(CraftingInput input,Level level){int tool=0,dye=0;for(var s:input.items()){if(s.isEmpty())continue;if(s.is(TheoryContent.SCRIBE.get())&&s.getDamageValue()>0)tool++;else if(s.is(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse("minecraft:black_dye"))))dye++;else return false;}return tool==1&&dye==1;}
    @Override public ItemStack assemble(CraftingInput input){for(var s:input.items())if(s.is(TheoryContent.SCRIBE.get())){var out=s.copyWithCount(1);out.setDamageValue(0);return out;}return ItemStack.EMPTY;}
    @Override public RecipeSerializer<ScribingRefillRecipe> getSerializer(){return TheoryContent.REFILL.get();}
}
