package thaumcraft.thaumaturgy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
class FocusProgramTest {
    @Test void repeatedEffectsCostMoreAndCrystalsRemainExact(){var p=new FocusProgram("touch",List.of(new FocusProgram.Effect("fire",1,1),new FocusProgram.Effect("fire",1,1)));assertEquals(10,p.complexity());assertEquals(2,p.crystals().get("ignis"));assertEquals(1,p.crystals().get("aversio"));assertEquals(103,p.craftingVis(15));assertEquals(3,p.levels());}
    @Test void malformedNetworkProgramsAreRejected(){for(String json:List.of("{\"medium\":\"unknown\",\"effects\":[{\"kind\":\"fire\",\"power\":1,\"duration\":1}]}","{\"medium\":\"touch\",\"effects\":[]}","{\"medium\":\"touch\",\"effects\":[{\"kind\":\"op\",\"power\":1,\"duration\":1}]}","{\"medium\":\"touch\",\"effects\":[{\"kind\":\"fire\",\"power\":99,\"duration\":1}]}"))assertTrue(FocusProgram.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(json)).error().isPresent());}
    @Test void persistenceKeepsExactOrderAndSettings(){var p=new FocusProgram("projectile",List.of(new FocusProgram.Effect("earth",3,1),new FocusProgram.Effect("frost",1,4)));var encoded=FocusProgram.CODEC.encodeStart(JsonOps.INSTANCE,p).getOrThrow();assertEquals(p,FocusProgram.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow());assertEquals(19,p.complexity());}
}
