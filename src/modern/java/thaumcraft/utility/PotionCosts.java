package thaumcraft.utility;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.item.crafting.BrewingRecipe;
public final class PotionCosts {
 private PotionCosts(){}
 public static Map<String,Integer> recipe(ServerLevel l,ItemStack stack){var contents=stack.getOrDefault(DataComponents.POTION_CONTENTS,PotionContents.EMPTY);var result=new TreeMap<String,Integer>();var reagents=new LinkedHashMap<String,ItemStack>();if(contents.customEffects().isEmpty()&&contents.potion().isPresent())collect(l,contents.potion().get(),reagents,new HashSet<>(),0);if(reagents.isEmpty())return Map.of("praecantatio",5,"alkimia",5);for(var item:reagents.values()){thaumcraft.research.AspectCatalog.item(item).forEach((a,n)->result.merge(a,n,Integer::sum));result.merge("alkimia",3,Integer::sum);}result.replaceAll((a,n)->Math.clamp(n-(int)(n*.66),1,10000));if(result.size()>10){var keep=result.entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey())).limit(10).toList();result.clear();for(var e:keep)result.put(e.getKey(),e.getValue());}return Map.copyOf(result);}
 private static void collect(ServerLevel l,Holder<Potion> target,Map<String,ItemStack> result,Set<Holder<Potion>> seen,int depth){if(depth>=16||!seen.add(target))return;for(var h:l.recipeAccess().getRecipes().stream().sorted(Comparator.comparing(h->h.id().toString())).toList())if(h.value() instanceof BrewingRecipe r){var out=r.getOutput().create().getOrDefault(DataComponents.POTION_CONTENTS,PotionContents.EMPTY);if(out.potion().isEmpty()||!out.potion().get().equals(target))continue;var input=r.getInput().potions().flatMap(p->p.potions()).flatMap(s->s.stream().findFirst());if(input.isEmpty()||input.get().equals(target))continue;var reagent=r.getReagent().ingredient().items().findFirst();if(reagent.isEmpty())continue;var item=new ItemStack(reagent.get().value());result.putIfAbsent(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.getItem()).toString(),item);collect(l,input.get(),result,seen,depth+1);return;}}
}
