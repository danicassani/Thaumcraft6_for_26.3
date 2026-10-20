package thaumcraft.alchemy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.atomic.AtomicInteger;
class SmelterRulesTest {
 @Test void tierEfficienciesKeepFluxPenalty(){assertEquals(80,SmelterRules.efficiency(0));assertEquals(90,SmelterRules.efficiency(1));assertEquals(95,SmelterRules.efficiency(2));assertFalse(SmelterRules.retains(0,false,.85F));assertTrue(SmelterRules.retains(1,false,.85F));assertFalse(SmelterRules.retains(1,false,.94F));assertTrue(SmelterRules.retains(2,false,.94F));for(int tier=0;tier<3;tier++){assertTrue(SmelterRules.retains(tier,true,.5F));assertFalse(SmelterRules.retains(tier,true,.65F));}}
 @Test void threeBellowsReduceCookingWithoutZeroOrExcessBoost(){assertEquals(160,SmelterRules.cookTicks(80,0));assertEquals(140,SmelterRules.cookTicks(80,1));assertEquals(120,SmelterRules.cookTicks(80,2));assertEquals(100,SmelterRules.cookTicks(80,3));assertEquals(100,SmelterRules.cookTicks(80,99));assertEquals(1,SmelterRules.cookTicks(1,3));}
 @Test void alumentumNeverCreatesZeroDistillationInterval(){for(int tier=0;tier<3;tier++){int normal=SmelterRules.distillTicks(tier,false),fast=SmelterRules.distillTicks(tier,true);assertTrue(normal>fast&&fast>0);}assertEquals(15,SmelterRules.distillTicks(0,false));assertEquals(10,SmelterRules.distillTicks(1,false));assertEquals(15,SmelterRules.distillTicks(2,false));assertEquals(8,SmelterRules.distillTicks(1,true));}
 @Test void ventsAlwaysTerminateAndNeverRerollLostPollution(){var calls=new AtomicInteger();assertFalse(SmelterRules.vents(0,()->{calls.incrementAndGet();return 1;}));assertEquals(0,calls.get());assertFalse(SmelterRules.vents(3,()->{calls.incrementAndGet();return 1;}));assertEquals(3,calls.get());calls.set(0);assertTrue(SmelterRules.vents(3,()->{calls.incrementAndGet();return .1;}));assertEquals(1,calls.get());calls.set(0);assertTrue(SmelterRules.vents(3,()->calls.incrementAndGet()==3?.1:.8));assertEquals(3,calls.get());}
}
