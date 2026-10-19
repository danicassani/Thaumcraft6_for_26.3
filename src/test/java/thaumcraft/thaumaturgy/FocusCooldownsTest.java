package thaumcraft.thaumaturgy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
class FocusCooldownsTest {
    @Test void sharedWindowSurvivesSavingAndExpiresAtFortyTicks(){var state=new FocusCooldowns();assertTrue(state.acquire("entity:test",100));assertFalse(state.acquire("entity:test",100));var saved=FocusCooldowns.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();var loaded=FocusCooldowns.CODEC.parse(JsonOps.INSTANCE,saved).getOrThrow();assertFalse(loaded.acquire("entity:test",139));assertTrue(loaded.acquire("entity:test",140));assertTrue(loaded.acquire("block:37",140));}
    @Test void corruptedOrFutureDataCannotBePartiallyAccepted(){for(String json:new String[]{"{\"version\":2,\"cooldowns\":{}}","{\"version\":1,\"cooldowns\":{\"entity:good\":100,\"invalid\":100}}"})assertTrue(FocusCooldowns.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(json)).result().isEmpty());}
}
