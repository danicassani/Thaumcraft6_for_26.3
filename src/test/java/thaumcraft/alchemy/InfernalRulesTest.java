package thaumcraft.alchemy;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
import thaumcraft.infernal.*;
class InfernalRulesTest{
 @Test void originalTimingAndFourBellowsClamp(){int[] slow={140,120,102,86,72},fast={80,60,42,26,12};for(int n=0;n<=4;n++){assertEquals(slow[n],InfernalRules.cookTicks(false,n));assertEquals(fast[n],InfernalRules.cookTicks(true,n));}assertEquals(12,InfernalRules.cookTicks(true,99));assertEquals(140,InfernalRules.cookTicks(false,-1));}
 @Test void bonusesAreBoundedWithoutRerolls(){assertEquals(1,InfernalRules.bonusRolls(0));assertEquals(5,InfernalRules.bonusRolls(4));assertEquals(5,InfernalRules.bonusRolls(99));assertEquals(1,InfernalRules.bonusRolls(-1));}
 @Test void restartRetainsPaidAuraAndExactWork(){for(var s:java.util.List.of(InfernalWork.EMPTY,new InfernalWork(1,17,80,3,19,0),new InfernalWork(1,0,140,31,0,0),new InfernalWork(1,0,0,-1,4,12)))assertEquals(s,InfernalWork.CODEC.parse(JsonOps.INSTANCE,InfernalWork.CODEC.encodeStart(JsonOps.INSTANCE,s).getOrThrow()).getOrThrow());}
 @Test void futureAndImpossibleProgressIsRejected(){String base=InfernalWork.CODEC.encodeStart(JsonOps.INSTANCE,new InfernalWork(1,17,80,3,19,0)).getOrThrow().toString();for(var s:java.util.List.of(base.replace("\"version\":1","\"version\":2"),base.replace("\"remaining\":17","\"remaining\":81"),base.replace("\"slot\":3","\"slot\":32"),base.replace("\"slot\":3","\"slot\":-1"),base.replace("\"speed\":19","\"speed\":21"),base.replace("\"xp\":0","\"xp\":-1")))assertTrue(InfernalWork.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(s)).result().isEmpty(),s);}
}
