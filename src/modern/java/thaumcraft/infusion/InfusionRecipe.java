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
public record InfusionRecipe(InfusionIngredient input,List<InfusionIngredient> components,Map<String,Integer> aspects,int instability,String research,ItemStackTemplate result,String enchantment) implements Recipe<InfusionInput>{
    public InfusionRecipe(InfusionIngredient input,List<InfusionIngredient> components,Map<String,Integer> aspects,int instability,String research,ItemStackTemplate result){this(input,components,aspects,instability,research,result,"");}
    public static final MapCodec<InfusionRecipe> CODEC=RecordCodecBuilder.mapCodec(i->i.group(InfusionIngredient.CODEC.fieldOf("input").forGetter(InfusionRecipe::input),InfusionIngredient.CODEC.listOf(1,32).fieldOf("components").forGetter(InfusionRecipe::components),AspectAmounts.CODEC.fieldOf("aspects").forGetter(InfusionRecipe::aspects),Codec.intRange(0,100).fieldOf("instability").forGetter(InfusionRecipe::instability),Codec.string(0,192).fieldOf("research").forGetter(InfusionRecipe::research),ItemStackTemplate.CODEC.fieldOf("result").forGetter(InfusionRecipe::result),Codec.STRING.validate(s->s.isEmpty()||s.equals("runic")||thaumcraft.equipment.FortressMasks.upgrade(s)||InfusionEnchantments.Kind.of(s)!=null?DataResult.success(s):DataResult.error(()->"Unknown infusion enchantment")).optionalFieldOf("infusion_enchantment","").forGetter(InfusionRecipe::enchantment)).apply(i,InfusionRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf,InfusionRecipe> STREAM=ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());
    public InfusionRecipe{components=List.copyOf(components);aspects=Map.copyOf(aspects);}
    public int[] assignment(InfusionInput in){if(enchantment.equals("runic"))return runicAssignment(in);if(thaumcraft.equipment.FortressMasks.upgrade(enchantment)&&!thaumcraft.equipment.FortressMasks.accepts(in.primary(),enchantment))return null;if(!enchantment.isEmpty()&&!thaumcraft.equipment.FortressMasks.upgrade(enchantment)&&!InfusionEnchantments.canApply(in.primary(),InfusionEnchantments.Kind.of(enchantment)))return null;if(!input.test(in.primary())||components.size()!=in.components().size())return null;int n=components.size();boolean[][] matrix=new boolean[n][n];for(int r=0;r<n;r++)for(int s=0;s<n;s++)matrix[r][s]=components.get(r).test(in.components().get(s));return InfusionMatching.assign(matrix);}
    @Override public boolean matches(InfusionInput in,Level l){return assignment(in)!=null;}
    @Override public ItemStack assemble(InfusionInput in){if(enchantment.equals("runic"))return thaumcraft.equipment.EquipmentContent.augment(in.primary());if(thaumcraft.equipment.FortressMasks.upgrade(enchantment))return thaumcraft.equipment.FortressMasks.apply(in.primary(),enchantment);if(!enchantment.isEmpty())return InfusionEnchantments.apply(in.primary(),InfusionEnchantments.Kind.of(enchantment));var out=result.create();if(in.primary().isDamageableItem()&&out.isDamageableItem()&&in.primary().getDamageValue()>0)out.setDamageValue(Math.min(out.getMaxDamage()-1,(int)((long)out.getMaxDamage()*in.primary().getDamageValue()/in.primary().getMaxDamage())));return out;}
    public Map<String,Integer> aspects(InfusionInput in){if(enchantment.equals("runic")){int n=20+20*(1<<thaumcraft.equipment.EquipmentContent.runic(in.primary()));return Map.of("praemunio",n,"vitreus",n/2,"potentia",n/2);}if(enchantment.isEmpty()||thaumcraft.equipment.FortressMasks.upgrade(enchantment))return aspects;float multiplier=InfusionEnchantments.cost(in.primary(),InfusionEnchantments.Kind.of(enchantment));var out=new TreeMap<String,Integer>();aspects.forEach((a,n)->out.put(a,(int)(n*multiplier)));return Map.copyOf(out);}
    public int instability(InfusionInput input){return enchantment.equals("runic")?5+thaumcraft.equipment.EquipmentContent.runic(input.primary())/2:instability;}
    private int[] runicAssignment(InfusionInput in){if(!thaumcraft.equipment.EquipmentContent.canAugment(in.primary()))return null;int amber=thaumcraft.equipment.EquipmentContent.runic(in.primary())+1;if(in.components().size()!=amber+1)return null;int salis=0,found=0;for(var s:in.components()){if(s.is(thaumcraft.crafting.ArcaneContent.SALIS.get()))salis++;else if(s.is(thaumcraft.content.BasicContent.AMBER.get()))found++;else return null;}if(salis!=1||found!=amber)return null;int[] out=new int[amber+1];for(int i=0;i<out.length;i++)out[i]=i;return out;}
    @Override public RecipeSerializer<InfusionRecipe> getSerializer(){return InfusionContent.SERIALIZER.get();}
    @Override public RecipeType<InfusionRecipe> getType(){return InfusionContent.RECIPES.get();}
    @Override public PlacementInfo placementInfo(){return PlacementInfo.create(input.item());}
    @Override public RecipeBookCategory recipeBookCategory(){return RecipeBookCategories.CRAFTING_MISC;}
    @Override public boolean showNotification(){return false;}
    @Override public String group(){return "thaumcraft:infusion";}
}
