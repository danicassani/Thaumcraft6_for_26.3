package thaumcraft.taint;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TaintRulesTest{
 @Test void spreadTracksFluxAndConfig(){assertEquals(.001,TaintRules.spreadChance(100,0,500),1e-8);assertEquals(.201,TaintRules.spreadChance(100,50,500),1e-7);assertEquals(0,TaintRules.spreadChance(0,500,500));assertEquals(1,TaintRules.spreadChance(500,500,500));}
 @Test void expansionOnlyInOuterSeedRing(){assertFalse(TaintRules.seedEdge(25*25,32));assertTrue(TaintRules.seedEdge(26*26,32));assertFalse(TaintRules.seedEdge(32*32,32));}
 @Test void wardIsBoundedAndDependsOnPermanentWarp(){assertEquals(32000,TaintRules.wardTicks(0));assertEquals(32000,TaintRules.wardTicks(25));assertEquals(20000,TaintRules.wardTicks(100));assertEquals(9090,TaintRules.wardTicks(500));assertEquals(9090,TaintRules.wardTicks(Integer.MAX_VALUE));}
 @Test void diseaseAndDeathDoNotOverflow(){assertEquals(40,TaintRules.diseaseInterval(0));assertEquals(20,TaintRules.diseaseInterval(1));assertEquals(1,TaintRules.diseaseInterval(255));assertEquals(5,TaintRules.deathDamage(0));assertEquals(2,TaintRules.deathDamage(3));assertEquals(2,TaintRules.deathDamage(100));}
 @Test void gooDecayAlwaysLosesOneUnit(){for(int n=1;n<=8;n++)assertEquals(n-1,TaintRules.gooAfterDecay(n));assertEquals(0,TaintRules.gooAfterDecay(0));}
}
