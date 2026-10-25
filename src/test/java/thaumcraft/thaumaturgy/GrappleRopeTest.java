package thaumcraft.thaumaturgy;
import java.util.*;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class GrappleRopeTest {
 @Test void cableHasExactHandAndHookEndpointsAndBoundedGeometry(){for(var end:List.of(new Vec3(12,5,-7),new Vec3(0,32,0),new Vec3(.01,0,0))){var points=GrappleRope.points(end,20,false,0,0);assertEquals(Vec3.ZERO,points.getFirst());assertEquals(end,points.getLast());assertTrue(points.size()<=GrappleRope.MAX_SEGMENTS+1);for(var p:points)assertTrue(Double.isFinite(p.x)&&Double.isFinite(p.y)&&Double.isFinite(p.z));}}
 @Test void anchoredCableSettlesWithoutMovingItsEndpoints(){var end=new Vec3(0,0,20);var flight=GrappleRope.points(end,25,false,0,0);var taut=GrappleRope.points(end,40,true,30,25);double flightError=0,tautError=0;for(int i=1;i<flight.size()-1;i++){var straight=end.scale(i/(double)(flight.size()-1));flightError+=flight.get(i).distanceToSqr(straight);tautError+=taut.get(i).distanceToSqr(straight);}assertTrue(flightError>.01);assertTrue(tautError<flightError*.000001);assertEquals(end,taut.getLast());}
 @Test void invalidAndOutOfRangeEndpointsCannotReachTheRenderer(){for(var end:List.of(Vec3.ZERO,new Vec3(Double.NaN,0,0),new Vec3(Double.POSITIVE_INFINITY,0,0),new Vec3(41,0,0)))assertTrue(GrappleRope.points(end,10,false,0,0).isEmpty());assertTrue(GrappleRope.points(new Vec3(5,0,0),Float.NaN,false,0,0).isEmpty());}
}
