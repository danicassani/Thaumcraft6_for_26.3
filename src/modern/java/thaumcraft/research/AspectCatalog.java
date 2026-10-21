package thaumcraft.research;

import com.google.gson.*;
import java.util.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import thaumcraft.crystals.CrystalContent;

public final class AspectCatalog {
    private static final JsonObject DATA=ResearchCatalog.load("aspects.json").getAsJsonObject();
    private AspectCatalog(){}
    private static Map<String,Integer> values(JsonObject object){var result=new LinkedHashMap<String,Integer>();object.entrySet().forEach(e->result.put(e.getKey(),e.getValue().getAsInt()));return Map.copyOf(result);}
    public static Map<String,Integer> item(ItemStack stack){
        if(stack.isEmpty())return Map.of();if(stack.is(thaumcraft.infusion.CuriosityContent.CURIO.get())){var type=thaumcraft.infusion.CuriosityContent.variant(stack);return switch(type){case "knowledge"->Map.of("cognitio",30);case "rites"->Map.of("cognitio",15,"alienis",5,"spiritus",5,"praecantatio",5);default->Map.of("cognitio",15,switch(type){case "arcane"->"praecantatio";case "preserved"->"bestia";case "ancient"->"mortuus";case "eldritch"->"alienis";default->"vitium";},15);};}if(stack.is(thaumcraft.alchemy.AlchemyContent.PHIAL.get())&&thaumcraft.alchemy.AlchemyContent.amount(stack)==10&&!thaumcraft.alchemy.AlchemyContent.aspect(stack).isEmpty())return Map.of(thaumcraft.alchemy.AlchemyContent.aspect(stack),10);if(stack.is(CrystalContent.ESSENCE.get()))return Map.of(CrystalContent.aspect(stack),1);
        var definitions=DATA.getAsJsonObject("items");String id=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if(definitions.has(id))return values(definitions.getAsJsonObject(id));
        Map<String,Integer> result=Map.of();
        for(var e:definitions.entrySet())if(e.getKey().startsWith("#")&&stack.is(TagKey.create(Registries.ITEM,Identifier.parse(e.getKey().substring(1)))))result=values(e.getValue().getAsJsonObject());
        return result;
    }
    public static Map<String,Integer> entity(Entity entity){if(entity instanceof thaumcraft.rifts.Wisp w){var aspects=new LinkedHashMap<String,Integer>();aspects.put("auram",5);aspects.put("volatus",5);aspects.merge(w.aspect(),5,Integer::sum);return Map.copyOf(aspects);}var id=BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();var e=DATA.getAsJsonObject("entities").getAsJsonObject(id);return e==null?Map.of():values(e);}
    public static int observation(String category,Map<String,Integer> aspects){var formula=DATA.getAsJsonObject("formulae").getAsJsonObject(category);double total=0;for(var e:formula.entrySet())total+=aspects.getOrDefault(e.getKey(),0)*e.getValue().getAsInt()/10D;return (int)Math.ceil(Math.sqrt(total));}
    public static List<String> triggers(boolean entity,String id){var o=DATA.getAsJsonObject("triggers").getAsJsonObject(entity?"entities":"items");return o.has(id)?ResearchCatalog.strings(o,id):List.of();}
    public static List<String> components(String aspect){var o=DATA.getAsJsonObject("components");return o.has(aspect)?ResearchCatalog.strings(o,aspect):List.of();}
    public static String combine(String first,String second){for(var aspect:DATA.getAsJsonObject("components").keySet()){var c=components(aspect);if(c.size()==2&&((c.get(0).equals(first)&&c.get(1).equals(second))||(c.get(1).equals(first)&&c.get(0).equals(second))))return aspect;}return null;}
}
