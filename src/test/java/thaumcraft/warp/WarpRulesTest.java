package thaumcraft.warp;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import thaumcraft.persistence.PlayerWarpStore.State;
class WarpRulesTest {
 @Test void positiveWarpRearmsAnExhaustedCounter(){assertEquals(new State(1,20,11,5,36),WarpRules.add(new State(1,20,10,5,0),0,1,0));}
 @Test void removalDoesNotRearm(){assertEquals(new State(1,20,9,5,7),WarpRules.add(new State(1,20,10,5,7),0,-1,0));}
 @Test void clampsWithoutOverflow(){assertEquals(new State(1,500,0,500,1000),WarpRules.add(new State(1,1,1,1,0),Integer.MAX_VALUE,Integer.MIN_VALUE,Integer.MAX_VALUE));}
 @Test void zeroCounterNeverTriggers(){var d=WarpRules.decide(new State(1,500,500,500,0),9,0,0);assertFalse(d.triggered());assertEquals(499,d.temporary());}
 @Test void rollOutsideThresholdDoesNotSpendCounter(){var d=WarpRules.decide(new State(1,10,0,0,100),0,11,9);assertFalse(d.triggered());assertEquals(100,d.counter());}
 @Test void triggerUsesBoundaryAndSpendsCounter(){var d=WarpRules.decide(new State(1,10,0,0,100),0,10,9);assertTrue(d.triggered());assertEquals(80,d.counter());assertEquals(40,d.power());assertEquals(9,d.severity());}
 @Test void soapPreservesPermanentAndCounter(){assertEquals(new State(1,50,7,0,400),WarpRules.clean(new State(1,50,10,100,400),true,true));assertEquals(0,WarpRules.clean(new State(1,50,1,0,0),true,true).normal());}
 @Test void severityRetainsExceptionalOriginalReliefBand(){assertEquals(18,WarpRules.band(72));assertEquals(20,WarpRules.band(73));assertEquals(19,WarpRules.band(76));assertEquals(21,WarpRules.band(81));assertEquals(22,WarpRules.band(92));assertEquals(23,WarpRules.band(93));}
 @Test void invalidRandomInputRejected(){assertThrows(IllegalArgumentException.class,()->WarpRules.decide(new State(1,1,1,1,1),0,100,0));}
}
