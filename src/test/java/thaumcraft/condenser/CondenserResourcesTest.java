package thaumcraft.condenser;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
class CondenserResourcesTest{
 @Test void everyStateResolvesItsModelsAndTextures()throws Exception{for(var block:java.util.List.of("condenser","condenser_lattice","condenser_lattice_dirty"))check(load("blockstates/"+block+".json"));}
 private static JsonElement load(String path)throws Exception{try(var stream=CondenserResourcesTest.class.getResourceAsStream("/assets/thaumcraft/"+path)){assertNotNull(stream,path);return JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8));}}
 private static void check(JsonElement node)throws Exception{if(node.isJsonArray()){for(var element:node.getAsJsonArray())check(element);}else if(node.isJsonObject()){for(var entry:node.getAsJsonObject().entrySet())if(entry.getKey().equals("model")){String id=entry.getValue().getAsString();assertTrue(id.startsWith("thaumcraft:block/"),id);var model=load("models/"+id.substring("thaumcraft:".length())+".json").getAsJsonObject();for(var texture:model.getAsJsonObject("textures").entrySet()){String value=texture.getValue().getAsString();if(value.startsWith("thaumcraft:"))assertNotNull(CondenserResourcesTest.class.getResource("/assets/thaumcraft/textures/"+value.substring("thaumcraft:".length())+".png"),value);}}else check(entry.getValue());}}
}
