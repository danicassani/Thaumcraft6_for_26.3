package thaumcraft.condenser;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;
class CondenserStateTest{
 @Test void reservesFuelAndRequiresCompletePayment(){var s=new CondenserState(1,5,0,586,0);assertThrows(IllegalStateException.class,()->s.complete(5,587));var next=s.advance(true).complete(5,587);assertEquals(new CondenserState(1,0,1,0,1),next);assertThrows(IllegalStateException.class,()->next.complete(5,587));}
 @Test void pausesWithoutLosingPaidFuelOrElapsedTime(){var s=new CondenserState(1,67,39,311,81);for(int i=0;i<1000;i++)assertEquals(s,s.advance(false));assertEquals(s,CondenserState.CODEC.parse(JsonOps.INSTANCE,CondenserState.CODEC.encodeStart(JsonOps.INSTANCE,s).getOrThrow()).getOrThrow());}
 @Test void fullOutputDoesNotOverwriteOrSpendFuel(){var s=new CondenserState(1,100,100,587,12);assertThrows(IllegalStateException.class,()->s.complete(5,587));assertThrows(IllegalStateException.class,s::accept);assertEquals(new CondenserState(1,95,100,0,13),s.extract().complete(5,587));}
 @Test void topologyChangeKeepsFuelAndAtMostOneTickBeforeNewCycle(){var s=new CondenserState(1,31,4,587,7);assertEquals(new CondenserState(1,31,4,4,7),s.retime(5));assertEquals(s,s.retime(0));assertEquals(s,s.retime(600));assertThrows(IllegalStateException.class,CondenserState.EMPTY::extract);}
 @Test void rejectsFutureNegativeAndOverflowingBuffers(){String good="{\"version\":1,\"fuel\":0,\"output\":0,\"progress\":0,\"converted\":0}";for(var s:java.util.List.of(good.replace("\"version\":1","\"version\":2"),good.replace("\"fuel\":0","\"fuel\":101"),good.replace("\"output\":0","\"output\":101"),good.replace("\"progress\":0","\"progress\":601"),good.replace("\"converted\":0","\"converted\":-1")))assertTrue(CondenserState.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(s)).result().isEmpty(),s);}
}
