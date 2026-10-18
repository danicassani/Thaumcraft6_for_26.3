package thaumcraft.infusion;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
public record InfusionInput(ItemStack primary,List<ItemStack> components) implements RecipeInput{
    public InfusionInput{components=List.copyOf(components);}
    @Override public ItemStack getItem(int i){return i==0?primary:components.get(i-1);}
    @Override public int size(){return 1+components.size();}
}
