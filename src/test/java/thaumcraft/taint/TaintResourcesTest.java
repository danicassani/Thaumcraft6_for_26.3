package thaumcraft.taint;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
class TaintResourcesTest{
 @Test void allCorruptionModelsAndTexturesExist()throws Exception{for(var n:java.util.List.of("taint_fibre","taint_soil","taint_rock","taint_crust","taint_log","taint_feature","taint_geyser","stone_porous","flux_goo","liquid_death","purifying_fluid"))walk(load("blockstates/"+n+".json"));}
 static JsonElement load(String path)throws Exception{try(var s=TaintResourcesTest.class.getResourceAsStream("/assets/thaumcraft/"+path)){assertNotNull(s,path);return JsonParser.parseReader(new InputStreamReader(s,StandardCharsets.UTF_8));}}
 static void walk(JsonElement e)throws Exception{if(e.isJsonArray()){for(var v:e.getAsJsonArray())walk(v);}else if(e.isJsonObject())for(var pair:e.getAsJsonObject().entrySet()){if(pair.getKey().equals("model")||pair.getKey().equals("parent")){String id=pair.getValue().getAsString();if(id.startsWith("thaumcraft:")){assertTrue(id.startsWith("thaumcraft:block/"),id);walk(load("models/"+id.substring(11)+".json"));}}else if(pair.getKey().equals("textures")){for(var t:pair.getValue().getAsJsonObject().entrySet()){String id=t.getValue().getAsString();if(id.startsWith("thaumcraft:"))assertNotNull(TaintResourcesTest.class.getResource("/assets/thaumcraft/textures/"+id.substring(11)+".png"),id);}}else walk(pair.getValue());}}
 @Test void noFluidHasCollectibleGooOrFreeCraftingRecipe()throws Exception{assertNull(TaintResourcesTest.class.getResource("/assets/thaumcraft/items/flux_goo.json"));var recipe=JsonParser.parseReader(new InputStreamReader(TaintResourcesTest.class.getResourceAsStream("/data/thaumcraft/recipe/bottle_taint.json"))).getAsJsonObject();assertEquals("vitium",recipe.get("phial_aspect").getAsString());assertEquals(30,recipe.getAsJsonObject("aspects").get("vitium").getAsInt());}
}
