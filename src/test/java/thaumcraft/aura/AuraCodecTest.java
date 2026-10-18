package thaumcraft.aura;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

final class AuraCodecTest {
    @Test void roundTripPreservesFractionsAndNegativeCoordinates() {
        var data = new AuraData(); data.put(AuraCell.key(-31, 47), new AuraCell(281, 112.125F, 77.5F));
        var encoded = AuraData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        var decoded = AuraData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(data.get(AuraCell.key(-31, 47)), decoded.get(AuraCell.key(-31, 47)));
        assertNull(decoded.get(AuraCell.key(0, 0)));
    }
    @Test void rejectsFutureVersionAndMalformedEntriesWithoutPartialState() {
        for (String json : new String[]{"{\"version\":2,\"chunks\":[]}",
            "{\"version\":1,\"chunks\":[{\"x\":0,\"z\":0,\"base\":200,\"vis\":100,\"flux\":0},{\"x\":1,\"z\":0,\"base\":700,\"vis\":1,\"flux\":0}]}",
            "{\"version\":1,\"chunks\":[{\"x\":0,\"z\":0,\"base\":200,\"vis\":-1,\"flux\":0}]}"}) {
            var result = AuraData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
            assertTrue(result.error().isPresent()); assertTrue(result.result().isEmpty());
        }
    }
    @Test void duplicateCoordinatesAreRejected() {
        var entry = "{\"x\":0,\"z\":0,\"base\":200,\"vis\":100,\"flux\":0}";
        var json = JsonParser.parseString("{\"version\":1,\"chunks\":[" + entry + "," + entry + "]}");
        assertTrue(AuraData.CODEC.parse(JsonOps.INSTANCE, json).result().isEmpty());
    }
    @Test void unchangedWritesDoNotDirtyAnOtherwiseCleanStore() {
        var data = new AuraData(); long key = AuraCell.key(0, 0); var cell = new AuraCell(200, 100, 0);
        data.put(key, cell); assertTrue(data.isDirty()); data.setDirty(false); data.put(key, cell); assertFalse(data.isDirty());
        data.put(key, cell.withFlux(1)); assertTrue(data.isDirty());
    }
}
