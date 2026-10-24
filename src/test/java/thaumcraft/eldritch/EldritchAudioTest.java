package thaumcraft.eldritch;
import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class EldritchAudioTest {
 @Test void everyEldritchSampleIsPackagedAndMatchesOriginal() throws Exception {
  var modern=Path.of("src/modern/resources/assets/thaumcraft");var old=Path.of("src/main/resources/assets/thaumcraft");var sounds=JsonParser.parseString(Files.readString(modern.resolve("sounds.json"))).getAsJsonObject();var names=new HashSet<String>();
  for(var key:List.of("egidle","egattack","egdeath","egscreech","crabtalk","crabclaw","crabdeath","monolith"))for(var entry:sounds.getAsJsonObject(key).getAsJsonArray("sounds")){String name=entry.isJsonPrimitive()?entry.getAsString():entry.getAsJsonObject().get("name").getAsString();name=name.replace("thaumcraft:","");names.add(name);assertArrayEquals(Files.readAllBytes(old.resolve("sounds/"+name+".ogg")),Files.readAllBytes(modern.resolve("sounds/"+name+".ogg")),name);}
  assertEquals(11,names.size());
 }
}
