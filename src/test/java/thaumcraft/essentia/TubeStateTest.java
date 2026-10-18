package thaumcraft.essentia;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.*;
import org.junit.jupiter.api.Test;
final class TubeStateTest{
    @Test void restartRetainsContentsIsolationAndControls(){var s=new TubeState(1,Map.of("aer",3,"ignis",2),29,List.of(0,1,2,0,1,2),"aqua",false,true,37);assertEquals(s,TubeState.CODEC.parse(JsonOps.INSTANCE,TubeState.CODEC.encodeStart(JsonOps.INSTANCE,s).getOrThrow()).getOrThrow());}
    @Test void futureFormatsAndInvalidPortsAreRejected(){String json=TubeState.CODEC.encodeStart(JsonOps.INSTANCE,TubeState.EMPTY).getOrThrow().toString();for(var s:List.of(json.replace("\"version\":1","\"version\":2"),json.replace("\"open\":63","\"open\":64"),json.replace("\"chokes\":[0,0,0,0,0,0]","\"chokes\":[0,0,0,0,0]"),json.replace("\"vent\":0","\"vent\":41")))assertTrue(TubeState.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(s)).result().isEmpty(),s);}
    @Test void filtersRequireAKnownAspect(){assertTrue(TubeState.FILTER.parse(JsonOps.INSTANCE,JsonParser.parseString("\"aer\"")).result().isPresent());assertTrue(TubeState.FILTER.parse(JsonOps.INSTANCE,JsonParser.parseString("\"bogus\"")).result().isEmpty());}
}
