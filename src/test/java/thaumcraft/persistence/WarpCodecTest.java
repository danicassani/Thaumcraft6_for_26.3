package thaumcraft.persistence;

import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.NbtOps;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class WarpCodecTest {
    @Test void roundTripPreservesAllFields() {
        var original = new PlayerWarpStore.State(1, 500, 12, 3, -7);
        var encoded = PlayerWarpStore.CODEC.encodeStart(NbtOps.INSTANCE, original).getOrThrow();
        assertEquals(original, PlayerWarpStore.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow());
    }
    @Test void rejectsMissingFields() { rejects("{}"); }
    @Test void rejectsFutureVersion() {
        rejects("{\"version\":2,\"permanent\":0,\"normal\":0,\"temporary\":0,\"counter\":0}");
    }
    @Test void rejectsExcessiveWarp() {
        rejects("{\"version\":1,\"permanent\":501,\"normal\":0,\"temporary\":0,\"counter\":0}");
    }
    @Test void rejectsNegativeWarp() {
        rejects("{\"version\":1,\"permanent\":0,\"normal\":-1,\"temporary\":0,\"counter\":0}");
    }
    @Test void rejectsWrongFieldType() {
        rejects("{\"version\":1,\"permanent\":0,\"normal\":0,\"temporary\":\"invalid\",\"counter\":0}");
    }
    private static void rejects(String json) {
        assertTrue(PlayerWarpStore.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).error().isPresent());
    }
}
