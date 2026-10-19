package thaumcraft.research;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import thaumcraft.crystals.CrystalContent;

/** Bundled original research definitions, shared by the server and the book interface. */
public final class ResearchCatalog {
    public static final List<String> CATEGORIES=List.of("BASICS","AUROMANCY","ALCHEMY","ARTIFICE","INFUSION","GOLEMANCY","ELDRITCH");
    public record ItemRequirement(String id,int count,String aspect,String legacy){
        public boolean matches(ItemStack stack){if(legacy.contains("enchanted_placeholder")&&(legacy.contains("id:16s,lvl:1s")||legacy.contains("id:33s,lvl:1s")||legacy.contains("id:35s,lvl:1s")||legacy.contains("id:0s,lvl:1s")))return !stack.isEmpty()&&java.util.stream.Stream.of(stack.getEnchantments(),stack.getOrDefault(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS,net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY)).anyMatch(en->en.entrySet().stream().anyMatch(e->e.getKey().is(legacy.contains("id:0s")?net.minecraft.world.item.enchantment.Enchantments.PROTECTION:legacy.contains("id:33s")?net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH:legacy.contains("id:35s")?net.minecraft.world.item.enchantment.Enchantments.FORTUNE:net.minecraft.world.item.enchantment.Enchantments.SHARPNESS)&&e.getIntValue()>=1));return !stack.isEmpty()&&!id.startsWith("unavailable:")
            &&BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(id)
            &&(aspect.isEmpty()||(stack.is(CrystalContent.ESSENCE.get())&&CrystalContent.aspect(stack).equals(aspect))||(stack.is(thaumcraft.alchemy.AlchemyContent.PHIAL.get())&&thaumcraft.alchemy.AlchemyContent.amount(stack)==10&&thaumcraft.alchemy.AlchemyContent.aspect(stack).equals(aspect))||(stack.is(thaumcraft.infusion.CuriosityContent.CURIO.get())&&thaumcraft.infusion.CuriosityContent.variant(stack).equals(aspect)));}
        public String proof(){return id+(aspect.isEmpty()?"":"/"+aspect);}
    }
    public record Cost(String kind,String category,int amount){public int progression(){return kind.equals("THEORY")?32:16;}}
    public record Stage(String text,List<ItemRequirement> items,List<ItemRequirement> crafts,List<String> research,List<Cost> costs,List<String> recipes,int warp){
        public boolean free(){return items.isEmpty()&&crafts.isEmpty()&&research.isEmpty()&&costs.isEmpty();}
    }
    public record Entry(String key,String name,String category,List<String> parents,List<String> siblings,List<Stage> stages,boolean hidden){}
    public static final List<Entry> ENTRIES;
    public static final Map<String,Entry> BY_KEY;
    static {
        var entries=new ArrayList<Entry>();
        for(var value:load("research.json").getAsJsonArray()){
            var e=value.getAsJsonObject();var stages=new ArrayList<Stage>();
            for(var v:e.getAsJsonArray("stages")){
                var s=v.getAsJsonObject();var costs=new ArrayList<Cost>();
                for(var raw:strings(s,"required_knowledge")){var parts=raw.split(";");costs.add(new Cost(parts[0],parts[1],Integer.parseInt(parts[2])));}
                stages.add(new Stage(s.get("text").getAsString(),items(s,"required_item"),items(s,"required_craft"),strings(s,"required_research"),List.copyOf(costs),strings(s,"recipes"),s.has("warp")?s.get("warp").getAsInt():0));
            }
            entries.add(new Entry(e.get("key").getAsString(),e.get("name").getAsString(),e.get("category").getAsString(),strings(e,"parents"),strings(e,"siblings"),List.copyOf(stages),strings(e,"meta").contains("HIDDEN")));
        }
        ENTRIES=List.copyOf(entries);var byKey=new LinkedHashMap<String,Entry>();for(var entry:entries)if(byKey.put(entry.key,entry)!=null)throw new IllegalStateException("Duplicate research key");BY_KEY=Map.copyOf(byKey);
    }
    private ResearchCatalog(){}
    public static JsonElement load(String name){
        try(var stream=ResearchCatalog.class.getResourceAsStream("/assets/thaumcraft/knowledge/"+name)){
            if(stream==null)throw new IllegalStateException("Missing knowledge resource: "+name);
            return JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8));
        }catch(IOException ex){throw new IllegalStateException(ex);}
    }
    static List<String> strings(JsonObject object,String key){if(!object.has(key))return List.of();var result=new ArrayList<String>();for(var v:object.getAsJsonArray(key))result.add(v.getAsString());return List.copyOf(result);}
    private static List<ItemRequirement> items(JsonObject object,String key){var result=new ArrayList<ItemRequirement>();for(var v:object.getAsJsonArray(key)){var o=v.getAsJsonObject();result.add(new ItemRequirement(o.get("id").getAsString(),o.get("count").getAsInt(),o.has("aspect")?o.get("aspect").getAsString():"",o.get("legacy").getAsString()));}return List.copyOf(result);}
    public static boolean knows(KnowledgeState state,String query){
        if(query==null||query.isEmpty())return true;
        if(query.startsWith("~"))query=query.substring(1);
        if(query.contains("||")){for(var part:query.split("\\|\\|"))if(knows(state,part))return true;return false;}
        if(query.contains("&&")){for(var part:query.split("&&"))if(!knows(state,part))return false;return true;}
        var parts=query.split("@",-1);var entry=BY_KEY.get(parts[0]);
        if(entry==null)return parts.length==1&&state.flags().contains(parts[0]);
        if(parts.length==1&&state.flags().contains(entry.key())&&entry.stages().size()==1&&entry.stages().getFirst().free())return true;
        int stage=state.stages().getOrDefault(parts[0],0);
        if(parts.length==2){try{return stage>=Integer.parseInt(parts[1])&&stage>0;}catch(NumberFormatException ex){return false;}}
        return parts.length==1&&stage>entry.stages.size();
    }
    public static String proof(ItemStack stack){var key=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();if(stack.is(thaumcraft.infusion.CuriosityContent.CURIO.get()))return key+"/"+thaumcraft.infusion.CuriosityContent.variant(stack);if(stack.is(thaumcraft.alchemy.AlchemyContent.PHIAL.get()))return key+(thaumcraft.alchemy.AlchemyContent.aspect(stack).isEmpty()?"":"/"+thaumcraft.alchemy.AlchemyContent.aspect(stack));if(stack.is(thaumcraft.theory.TheoryContent.CELESTIAL.get()))return key+"/"+thaumcraft.theory.TheoryContent.variant(stack);return key+(stack.is(CrystalContent.ESSENCE.get())?"/"+CrystalContent.aspect(stack):"");}
}
