package thaumcraft.alchemy;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
final class AspectSupplementTest {
 private static JsonObject read(String file)throws Exception{return JsonParser.parseString(Files.readString(Path.of(file))).getAsJsonObject();}
 private static JsonObject supplement()throws Exception{return read("src/modern/resources/assets/thaumcraft/knowledge/aspect-supplement.json");}
 @Test void definitionsUseRealAspectsAndSafeAmounts()throws Exception{
  var known=read("tools/crystal_aspects.json");var old=read("src/modern/resources/assets/thaumcraft/knowledge/aspects.json").getAsJsonObject("items");
  for(var entry:supplement().getAsJsonObject("items").entrySet()){
   int sum=0;for(var aspect:entry.getValue().getAsJsonObject().entrySet()){assertTrue(known.has(aspect.getKey()),entry.getKey());int n=aspect.getValue().getAsInt();assertTrue(n>0&&n<=500,entry.getKey());sum+=n;}assertTrue(sum>0&&sum<=AspectAmounts.LIMIT);
   if(old.has(entry.getKey()))assertEquals(old.get(entry.getKey()),entry.getValue(),"Do not shadow an original direct assignment");
  }
 }
 @Test void equivalentsCannotCycleOrReferToThemselves()throws Exception{
  var aliases=supplement().getAsJsonObject("aliases");for(var key:aliases.keySet()){
   var visited=new HashSet<String>();String next=key;while(aliases.has(next)){assertTrue(visited.add(next),"Alias cycle: "+key);next=aliases.get(next).getAsString();assertTrue(next.matches("[a-z0-9_]+:[a-z0-9_/]+"));}
  }
 }
 @Test void amberAndModernRootsHaveDistinctMaterialDefinitions()throws Exception{
  var definitions=supplement().getAsJsonObject("items");assertEquals(JsonParser.parseString("{\"vinculum\":10,\"vitreus\":10}"),definitions.get("thaumcraft:amber"));
  assertTrue(definitions.getAsJsonObject("minecraft:amethyst_shard").has("vitreus"));assertTrue(definitions.getAsJsonObject("minecraft:raw_copper").has("metallum"));assertTrue(definitions.getAsJsonObject("minecraft:sculk").has("mortuus"));
 }
}
