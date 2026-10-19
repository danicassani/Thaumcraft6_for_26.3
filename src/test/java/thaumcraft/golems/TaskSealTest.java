package thaumcraft.golems;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import net.minecraft.core.BlockPos;
class TaskSealTest {
    @Test void useRequiresBothSmartAndDeft(){assertFalse(SealKind.USE.accepts(new GolemDesign(3,0,1,0)));assertFalse(SealKind.USE.accepts(new GolemDesign(3,1,0,0)));assertTrue(SealKind.USE.accepts(new GolemDesign(3,1,1,0)));}
    @Test void lumberAndButcherKeepOriginalSpecialistRequirements(){assertFalse(SealKind.LUMBER.accepts(new GolemDesign(1,0,3,0)));assertTrue(SealKind.LUMBER.accepts(new GolemDesign(1,1,3,0)));assertFalse(SealKind.BUTCHER.accepts(new GolemDesign(1,1,0,0)));assertFalse(SealKind.BUTCHER.accepts(new GolemDesign(1,0,0,2)));assertTrue(SealKind.BUTCHER.accepts(new GolemDesign(1,1,0,2)));}
    @Test void port21SealOrdinalsRemainStable(){String[] names={"PICKUP","FILL","EMPTY","GUARD","HARVEST","BREAKER","PICKUP_ADVANCED","FILL_ADVANCED","EMPTY_ADVANCED","GUARD_ADVANCED","BREAKER_ADVANCED","PROVIDER","STOCK"};for(int i=0;i<names.length;i++)assertEquals(names[i],SealKind.values()[i].name());assertEquals(16,SealKind.values().length);}
    @Test void timberSearchCannotEscapeConfiguredVolume(){var p=new BlockPos(-20,100,30);assertTrue(GolemTasks.inTimberArea(p,3,p.offset(3,6,-3)));assertTrue(GolemTasks.inTimberArea(p,3,p.below()));for(var q:new BlockPos[]{p.east(4),p.west(4),p.north(4),p.south(4),p.above(7),p.below(2),new BlockPos(Integer.MAX_VALUE,100,30)})assertFalse(GolemTasks.inTimberArea(p,3,q));}
}
