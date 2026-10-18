package thaumcraft.theory;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;
final class AdditionalCardsTest{
    private String card(String kind,int a,int b){return "{\"kind\":\""+kind+"\",\"category\":4,\"a\":"+a+",\"b\":"+b+",\"amount\":15,\"seed\":123456789012345,\"aid\":false}";}
    @Test void invalidCuriosityCannotCrashANativeMenu(){assertTrue(TheoryCards.Card.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(card("CURIO",7,0))).result().isEmpty());}
    @Test void invalidInfusionMaterialCannotCrashANativeMenu(){assertTrue(TheoryCards.Card.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(card("INFUSE",0,31))).result().isEmpty());}
    @Test void lastCuriosityAndSeedRoundTrip(){var c=TheoryCards.Card.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(card("CURIO",6,0))).getOrThrow();assertEquals(6,c.a());assertEquals(123456789012345L,c.seed());assertEquals(c,TheoryCards.Card.CODEC.parse(JsonOps.INSTANCE,TheoryCards.Card.CODEC.encodeStart(JsonOps.INSTANCE,c).getOrThrow()).getOrThrow());}
}
