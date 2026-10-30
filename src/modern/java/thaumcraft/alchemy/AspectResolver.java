package thaumcraft.alchemy;

import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import thaumcraft.crafting.ArcaneRecipe;
import thaumcraft.research.AspectCatalog;
import thaumcraft.infusion.*;

/** Recipe-manager-scoped inference. A datapack reload replaces both the graph and its cache. */
public final class AspectResolver {
    private record Rule(ItemStack result,List<Optional<Ingredient>> ingredients,int width,int height,CraftingRecipe crafting,AlchemyRecipe alchemy,int vis,InfusionRecipe infusion){
        Rule(ItemStack result,List<Optional<Ingredient>> ingredients,int width,int height,CraftingRecipe crafting,AlchemyRecipe alchemy,int vis){this(result,ingredients,width,height,crafting,alchemy,vis,null);}
    }
    private static final Map<RecipeManager,AspectResolver> MANAGERS=new WeakHashMap<>();
    private final Map<Item,List<Rule>> rules=new HashMap<>();
    private final Map<Item,Map<String,Integer>> cache=new HashMap<>();
    private AspectResolver(RecipeManager manager){
        for(var holder:manager.getRecipes()){
            var recipe=holder.value();CraftingRecipe craft=recipe instanceof ArcaneRecipe a?a.recipe():recipe instanceof CraftingRecipe c?c:null;Rule rule=null;
            if(craft instanceof ShapedRecipe shaped)rule=new Rule(shaped.assemble(CraftingInput.of(0,0,List.of())),shaped.getIngredients(),shaped.getWidth(),shaped.getHeight(),shaped,null,recipe instanceof ArcaneRecipe a?a.vis():0);
            else if(craft instanceof ShapelessRecipe shapeless){var list=shapeless.placementInfo().ingredients().stream().map(Optional::of).toList();rule=new Rule(shapeless.assemble(CraftingInput.of(0,0,List.of())),list,list.size(),1,shapeless,null,recipe instanceof ArcaneRecipe a?a.vis():0);}
            else if(recipe instanceof AbstractCookingRecipe cooking)rule=new Rule(cooking.assemble(new SingleRecipeInput(ItemStack.EMPTY)),List.of(Optional.of(cooking.input())),1,1,null,null,0);
            else if(recipe instanceof AlchemyRecipe alchemy)rule=new Rule(alchemy.result().create(),List.of(Optional.of(alchemy.catalyst())),1,1,null,alchemy,0);
            else if(recipe instanceof StonecutterRecipe stone)rule=new Rule(stone.assemble(new SingleRecipeInput(ItemStack.EMPTY)),List.of(Optional.of(stone.input())),1,1,null,null,0);
            else if(recipe instanceof SmithingTransformRecipe smith)rule=new Rule(smith.assemble(new SmithingRecipeInput(ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY)),List.of(smith.templateIngredient(),Optional.of(smith.baseIngredient()),smith.additionIngredient()),3,1,null,null,0);
            else if(recipe instanceof InfusionRecipe infusion&&infusion.enchantment().isEmpty()){
                var ingredients=new ArrayList<Optional<Ingredient>>();ingredients.add(Optional.of(infusion.input().item()));for(var component:infusion.components())ingredients.add(Optional.of(component.item()));
                rule=new Rule(infusion.result().create(),ingredients,ingredients.size(),1,null,null,0,infusion);
            }
            if(rule!=null&&!rule.result().isEmpty())rules.computeIfAbsent(rule.result().getItem(),k->new ArrayList<>()).add(rule);
        }
    }
    public static Map<String,Integer> item(ServerLevel level,ItemStack stack){
        if(stack.isEmpty())return Map.of();var equipment=thaumcraft.equipment.EquipmentContent.cultAspects(level,stack);if(equipment!=null)return equipment;var direct=AspectCatalog.item(stack);if(!direct.isEmpty())return AspectCatalog.augment(stack,direct);
        if(stack.is(AlchemyContent.PHIAL.get()))return Map.of();
        var manager=level.recipeAccess();var resolver=MANAGERS.computeIfAbsent(manager,AspectResolver::new);return resolver.resolve(stack,new HashSet<>(),new int[]{1024},0);
    }
    private Map<String,Integer> resolve(ItemStack stack,Set<Item> path,int[] budget,int depth){
        var direct=AspectCatalog.item(stack);if(!direct.isEmpty())return AspectCatalog.augment(stack,direct);
        Item item=stack.getItem();if(depth>=12||--budget[0]<0||path.contains(item))return Map.of();if(cache.containsKey(item))return cache.get(item);
        path.add(item);Map<String,Integer> best=Map.of();int minimum=Integer.MAX_VALUE;
        for(var rule:rules.getOrDefault(item,List.of())){
            var sum=new TreeMap<String,Integer>();var chosen=new ArrayList<ItemStack>();boolean valid=true;
            int slot=0;
            for(var ingredient:rule.ingredients()){
                int index=slot++;
                if(ingredient.isEmpty()){chosen.add(ItemStack.EMPTY);continue;}
                Map<String,Integer> cheapest=Map.of();ItemStack selected=ItemStack.EMPTY;int cost=Integer.MAX_VALUE;
                for(var candidate:ingredient.get().items().sorted(Comparator.comparing(h->BuiltInRegistries.ITEM.getKey(h.value()).toString())).limit(32).toList()){
                    var sample=new ItemStack(candidate);
                    if(rule.infusion()!=null){var required=index==0?rule.infusion().input():rule.infusion().components().get(index-1);if(!required.aspect().isEmpty()&&sample.is(thaumcraft.crystals.CrystalContent.ESSENCE.get()))sample=thaumcraft.crystals.CrystalContent.essence(required.aspect(),1);if(!required.test(sample))continue;}
                    var values=resolve(sample,path,budget,depth+1);int total=AspectAmounts.total(values);if(total>0&&total<cost){cost=total;cheapest=values;selected=sample;}
                }
                if(selected.isEmpty()){valid=false;break;}chosen.add(selected);cheapest.forEach((k,v)->sum.merge(k,v,Integer::sum));
            }
            if(!valid)continue;
            if(rule.crafting()!=null){var input=CraftingInput.of(rule.width(),rule.height(),chosen);for(var remainder:rule.crafting().getRemainingItems(input)){if(remainder.isEmpty())continue;resolve(remainder,path,budget,depth+1).forEach((k,v)->sum.computeIfPresent(k,(key,n)->Math.max(0,n-v)));}}
            Map<String,Integer> result;
            if(rule.alchemy()!=null){var m=new TreeMap<>(sum);rule.alchemy().aspects().forEach((k,v)->m.merge(k,(int)Math.sqrt(v)/rule.result().getCount(),Integer::sum));m.values().removeIf(v->v<=0);result=Map.copyOf(m);}
            else{result=AspectAmounts.crafting(sum,rule.result().getCount());if(rule.vis()>0){var m=new TreeMap<>(result);m.merge("praecantatio",(int)(Math.sqrt(1+rule.vis()/2D)/rule.result().getCount()),Integer::sum);m.values().removeIf(v->v<=0);result=Map.copyOf(m);}}
            if(rule.infusion()!=null){var m=new TreeMap<>(result);rule.infusion().aspects().forEach((a,n)->{int amount=(int)(Math.sqrt(n)/rule.result().getCount());if(amount>0)m.merge(a,amount,Integer::sum);});result=Map.copyOf(m);}
            int total=AspectAmounts.total(result);if(total>0&&total<minimum){best=result;minimum=total;}
        }
        if(best.isEmpty()){var equivalent=AspectCatalog.equivalent(stack);if(!equivalent.isEmpty())best=resolve(equivalent,path,budget,depth+1);}
        path.remove(item);best=AspectCatalog.augment(stack,best);if(!best.isEmpty())cache.put(item,best);return best;
    }
}
