package thaumcraft.alchemy;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.*;
import org.junit.jupiter.api.Test;
final class AspectAmountsTest {
    @Test void reserveAllAspectsBeforeSubtracting(){var original=Map.of("aer",5,"aqua",2);assertThrows(IllegalArgumentException.class,()->AspectAmounts.subtract(original,Map.of("aer",3,"aqua",3)));assertEquals(Map.of("aer",5,"aqua",2),original);assertEquals(Map.of("aer",2),AspectAmounts.subtract(original,Map.of("aer",3,"aqua",2)));}
    @Test void sumsAreImmutableAndBounded(){var out=AspectAmounts.add(Map.of("terra",1),Map.of("terra",2,"aqua",4));assertEquals(7,AspectAmounts.total(out));assertThrows(UnsupportedOperationException.class,()->out.put("aer",1));assertThrows(IllegalArgumentException.class,()->AspectAmounts.add(Map.of("aer",AspectAmounts.LIMIT),Map.of("terra",1)));}
    @Test void malformedAmountsNeverReturnPartialState(){for(var json:List.of("{\"bogus\":1}","{\"aer\":0}","{\"aer\":-1}","{\"aer\":20000,\"aqua\":1}"))assertTrue(AspectAmounts.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(json)).result().isEmpty());}
    @Test void originalCraftingReductionPreservesItsRoundingBoundary(){assertEquals(Map.of("aer",3),AspectAmounts.crafting(Map.of("aer",16),4));assertTrue(AspectAmounts.crafting(Map.of("aer",4),4).isEmpty());assertEquals(Map.of("aer",1),AspectAmounts.crafting(Map.of("aer",5),4));assertEquals(Map.of("aer",500),AspectAmounts.crafting(Map.of("aer",1000),1));}
    @Test void restartSnapshotRetainsWaterHeatFuelAndProgress(){var state=new AlchemyState(1,Map.of("lux",17,"vitium",3),950,187,1234,4800,7,-250);var saved=AlchemyState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();assertEquals(state,AlchemyState.CODEC.parse(JsonOps.INSTANCE,saved).getOrThrow());}
    @Test void futureAndImpossibleMachineSavesAreRejected(){var json=AlchemyState.CODEC.encodeStart(JsonOps.INSTANCE,AlchemyState.EMPTY).getOrThrow().toString();for(var bad:List.of(json.replace("\"version\":1","\"version\":2"),json.replace("\"water\":0","\"water\":1001"),json.replace("\"heat\":0","\"heat\":201"),json.replace("\"burn\":0","\"burn\":-1")))assertTrue(AlchemyState.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(bad)).result().isEmpty());}
}
