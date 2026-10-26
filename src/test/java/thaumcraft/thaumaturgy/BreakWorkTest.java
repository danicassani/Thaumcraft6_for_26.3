package thaumcraft.thaumaturgy;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
class BreakWorkTest {
 @Test void hardBlocksRemainMineableAndPowerReducesTime(){float obsidian=BreakWork.durability(50);assertEquals(Math.sqrt(5000),obsidian,.0001);assertTrue(Math.ceil(obsidian/5)<Math.ceil(obsidian));assertEquals(0,BreakWork.durability(0));assertThrows(IllegalArgumentException.class,()->BreakWork.durability(-1));assertThrows(IllegalArgumentException.class,()->BreakWork.durability(Float.NaN));}
 @Test void areaTargetsAreStaggeredAndBounded(){float work=BreakWork.durability(1.5F);assertEquals(0,BreakWork.delay(work,1,0));assertEquals(4,BreakWork.delay(work,1,1));assertTrue(BreakWork.delay(work,1,2)>BreakWork.delay(work,1,1));assertEquals(1200,BreakWork.delay(work,.01F,342));}
 @Test void AdditionalAuraCostIncludesSilkAndFortune(){assertEquals(.25F,BreakWork.cost(new FocusProgram.Effect("break",1,1)));assertEquals(.9F,BreakWork.cost(new FocusProgram.Effect("break",5,1,8,4,true)),.00001);}
}
