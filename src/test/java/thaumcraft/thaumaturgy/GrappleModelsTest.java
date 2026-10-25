package thaumcraft.thaumaturgy;
import com.google.gson.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class GrappleModelsTest {
 @Test void originalMeshesAndTransformsArePreservedAndNativeConditionUsesItsOwnComponent() throws Exception {
  for(String name:new String[]{"ice1","ice2","ice3"})assertArrayEquals(Files.readAllBytes(Path.of("src/main/resources/assets/thaumcraft/sounds/"+name+".ogg")),Files.readAllBytes(Path.of("src/modern/resources/assets/thaumcraft/sounds/"+name+".ogg")));
  for(String name:new String[]{"grapple_gun_1","grapple_gun_2"}){var original=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/thaumcraft/models/item/"+name+".json"))).getAsJsonObject();original.remove("__createdwith");var modern=JsonParser.parseString(Files.readString(Path.of("src/modern/resources/assets/thaumcraft/models/item/"+name+".json")));assertEquals(original,modern);}
  var item=JsonParser.parseString(Files.readString(Path.of("src/modern/resources/assets/thaumcraft/items/grapple_gun.json"))).getAsJsonObject().getAsJsonObject("model");assertEquals("minecraft:has_component",item.get("property").getAsString());assertEquals("thaumcraft:grapple_link",item.get("component").getAsString());assertEquals("thaumcraft:item/grapple_gun_2",item.getAsJsonObject("on_true").get("model").getAsString());assertEquals("thaumcraft:item/grapple_gun_1",item.getAsJsonObject("on_false").get("model").getAsString());
 }
}
