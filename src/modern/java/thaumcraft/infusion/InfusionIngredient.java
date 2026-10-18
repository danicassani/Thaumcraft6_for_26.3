package thaumcraft.infusion;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import thaumcraft.crystals.CrystalContent;
public record InfusionIngredient(Ingredient item,String aspect,boolean emptyEssentia){
    public static final Codec<InfusionIngredient> CODEC=RecordCodecBuilder.create(i->i.group(Ingredient.CODEC.fieldOf("item").forGetter(InfusionIngredient::item),thaumcraft.essentia.TubeState.FILTER.optionalFieldOf("crystal_aspect","").forGetter(InfusionIngredient::aspect),Codec.BOOL.optionalFieldOf("empty_essentia",false).forGetter(InfusionIngredient::emptyEssentia)).apply(i,InfusionIngredient::new));
    public boolean test(ItemStack stack){return !stack.isEmpty()&&item.test(stack)&&(!emptyEssentia||thaumcraft.alchemy.AlchemyContent.amount(stack)==0&&thaumcraft.alchemy.AlchemyContent.aspect(stack).isEmpty()&&!stack.has(thaumcraft.essentia.EssentiaContent.FILTER.get()))&&(aspect.isEmpty()||stack.is(CrystalContent.ESSENCE.get())&&CrystalContent.aspect(stack).equals(aspect));}
}
