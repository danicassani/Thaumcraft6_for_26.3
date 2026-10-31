package thaumcraft.research;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
final class TheoryTranslationsTest {
 @Test void everyCardEffectInterpolatesArgumentsAndLiteralPercentages()throws Exception{
  for(var locale:List.of("en_us","es_es")){
   var data=JsonParser.parseString(Files.readString(Path.of("src/modern/resources/assets/thaumcraft/lang/"+locale+".json"))).getAsJsonObject();int count=0;
   for(var entry:data.entrySet())if(entry.getKey().startsWith("theory.thaumcraft.effect.")){
    String first=entry.getKey().endsWith(".infuse")?"25":"Alchemy";
    String text=assertDoesNotThrow(()->String.format(Locale.ROOT,entry.getValue().getAsString(),first,25),entry.getKey());assertFalse(text.contains("%s")||text.contains("%%"),entry.getKey());count++;
   }
   assertEquals(32,count);assertEquals("Bloquea Alchemy; Básicos +5%.",String.format(Locale.ROOT,JsonParser.parseString(Files.readString(Path.of("src/modern/resources/assets/thaumcraft/lang/es_es.json"))).getAsJsonObject().get("theory.thaumcraft.effect.reject").getAsString(),"Alchemy",25));
  }
 }
}
