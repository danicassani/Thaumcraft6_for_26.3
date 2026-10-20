package thaumcraft.infernal;
import java.util.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.util.RandomSource;
import thaumcraft.research.ResearchCatalog;
public final class InfernalBonuses{
 private record Bonus(List<String> inputs,String output,float chance){}
 private static final List<Bonus> BONUSES=new ArrayList<>();
 static{for(var v:ResearchCatalog.load("infernal_bonuses.json").getAsJsonArray()){var o=v.getAsJsonObject();var list=new ArrayList<String>();for(var id:o.getAsJsonArray("inputs"))list.add(id.getAsString());BONUSES.add(new Bonus(List.copyOf(list),o.get("output").getAsString(),o.get("chance").getAsFloat()));}}
 public static List<ItemStack> roll(ItemStack input,int bellows,RandomSource random){var counts=new LinkedHashMap<String,Integer>();for(int n=0;n<InfernalRules.bonusRolls(bellows);n++)for(var bonus:BONUSES){boolean matches=bonus.inputs.stream().anyMatch(id->id.startsWith("#")?input.is(TagKey.create(Registries.ITEM,Identifier.parse(id.substring(1)))):BuiltInRegistries.ITEM.getKey(input.getItem()).toString().equals(id));if(matches&&random.nextFloat()<=bonus.chance){var item=BuiltInRegistries.ITEM.getValue(Identifier.parse(bonus.output));if(item!=null&&item!=Items.AIR)counts.merge(bonus.output,1,Integer::sum);}}var out=new ArrayList<ItemStack>();counts.forEach((id,count)->{var item=BuiltInRegistries.ITEM.getValue(Identifier.parse(id));int limit=Math.min(64,item.getDefaultMaxStackSize());for(int left=count;left>0;left-=limit)out.add(new ItemStack(item,Math.min(limit,left)));});return out;}
}
