package thaumcraft.essentia;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;
import thaumcraft.crystals.CrystalAspects;
import thaumcraft.research.AspectCatalog;
class CentrifugeStateTest{
 @Test void rejectsPrimalsAndUnknownInputs(){for(var a:java.util.List.of("aer","terra","ignis","aqua","ordo","perditio","bogus",""))assertFalse(CentrifugeState.compound(a));assertTrue(CentrifugeState.compound("potentia"));}
 @Test void paidUnitTakes39TicksAndPausesExactly(){var s=CentrifugeState.EMPTY.accept("potentia");for(int i=0;i<38;i++)s=s.advance(false,0);assertEquals(1,s.remaining());for(int i=0;i<80;i++)assertEquals(s,s.advance(true,1));s=s.advance(false,1);assertEquals("",s.input());assertEquals(AspectCatalog.components("potentia").get(1),s.output());assertEquals(s,s.advance(false,0));}
 @Test void blockedOutputDoesNotOverwriteEitherPaidUnit(){var s=new CentrifugeState(1,"potentia","aer",2);for(int i=0;i<100;i++)s=s.advance(false,1);assertEquals(new CentrifugeState(1,"potentia","aer",0),s);s=s.drain().advance(false,0);assertEquals("",s.input());assertEquals(AspectCatalog.components("potentia").get(0),s.output());}
 @Test void restartPreservesBothBuffersAndExactRemainingTime(){var s=new CentrifugeState(1,"lux","ignis",17);assertEquals(s,CentrifugeState.CODEC.parse(JsonOps.INSTANCE,CentrifugeState.CODEC.encodeStart(JsonOps.INSTANCE,s).getOrThrow()).getOrThrow());}
 @Test void futureUnknownPrimalAndInconsistentSavesAreRejected(){for(String s:java.util.List.of("{\"version\":2,\"input\":\"lux\",\"output\":\"\",\"remaining\":17}","{\"version\":1,\"input\":\"aer\",\"output\":\"\",\"remaining\":17}","{\"version\":1,\"input\":\"\",\"output\":\"aer\",\"remaining\":1}","{\"version\":1,\"input\":\"lux\",\"output\":\"bogus\",\"remaining\":17}","{\"version\":1,\"input\":\"lux\",\"output\":\"\",\"remaining\":40}"))assertTrue(CentrifugeState.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(s)).result().isEmpty(),s);}
 @Test void everyCompoundYieldsExactlyOneOfItsOriginalComponents(){int compounds=0;for(var a:CrystalAspects.COLORS.keySet()){var components=AspectCatalog.components(a);if(components.size()!=2)continue;compounds++;for(int choice=0;choice<2;choice++){var s=CentrifugeState.EMPTY.accept(a);for(int i=0;i<39;i++)s=s.advance(false,choice);assertEquals(components.get(choice),s.output());assertEquals("",s.input());assertEquals(CentrifugeState.EMPTY,s.drain());}}assertEquals(31,compounds);}
}
