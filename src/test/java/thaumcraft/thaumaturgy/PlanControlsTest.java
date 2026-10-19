package thaumcraft.thaumaturgy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
class PlanControlsTest {
    @Test void resizeCyclesAllAxesAndIndividualAxesWithoutLeavingBounds(){var all=new PlanControls.Selection(3,3,3,0);assertEquals(new PlanControls.Selection(0,0,0,0),all.next(false));var x=new PlanControls.Selection(1,2,3,1);assertEquals(new PlanControls.Selection(2,2,3,1),x.next(false));var z=x.next(true);assertEquals(new PlanControls.Selection(1,2,0,2),z.next(false));var y=z.next(true);assertEquals(new PlanControls.Selection(1,3,3,3),y.next(false));assertEquals(0,y.next(true).axis());for(int axis=0;axis<4;axis++)for(int size=0;size<4;size++){var s=new PlanControls.Selection(size,size,size,axis);assertEquals(s,PlanControls.Selection.CODEC.parse(JsonOps.INSTANCE,PlanControls.Selection.CODEC.encodeStart(JsonOps.INSTANCE,s).getOrThrow()).getOrThrow());}}
    @Test void dimensionsAndAxisCannotBeInjectedOutsideServerBounds(){for(String json:java.util.List.of("{\"x\":4,\"y\":0,\"z\":0,\"axis\":0}","{\"x\":0,\"y\":0,\"z\":0,\"axis\":4}","{\"x\":0,\"y\":-1,\"z\":0,\"axis\":0}"))assertTrue(PlanControls.Selection.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(json)).error().isPresent());}
}
