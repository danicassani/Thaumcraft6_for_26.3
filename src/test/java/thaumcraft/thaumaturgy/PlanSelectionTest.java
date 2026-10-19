package thaumcraft.thaumaturgy;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.phys.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PlanSelectionTest {
    static BlockHitResult hit(Direction side){return new BlockHitResult(Vec3.atCenterOf(BlockPos.ZERO),side,BlockPos.ZERO,false);}
    @Test void fullBoundsExtendIntoEachHitFaceAndSortNearestFirst(){for(var face:Direction.values()){var selection=PlanSelection.select(hit(face),new FocusProgram.PlanSettings(3,3,3,false),q->true,q->true,q->true);assertEquals(343,selection.size());assertEquals(BlockPos.ZERO,selection.getFirst());for(var p:selection){int normal=p.getX()*face.getStepX()+p.getY()*face.getStepY()+p.getZ()*face.getStepZ();assertTrue(normal<=0&&normal>=-6);}}}
    @Test void surfaceNeedsAConnectedMatchingExposedPath(){var blocks=Set.of(BlockPos.ZERO,new BlockPos(1,0,0),new BlockPos(3,0,0),new BlockPos(0,1,0));var selection=PlanSelection.select(hit(Direction.UP),new FocusProgram.PlanSettings(3,3,3,true),q->true,blocks::contains,q->true);assertEquals(Set.of(BlockPos.ZERO,new BlockPos(1,0,0)),Set.copyOf(selection));assertTrue(PlanSelection.select(hit(Direction.UP),FocusProgram.PlanSettings.DEFAULT,q->false,q->true,q->true).isEmpty());}
    @Test void surfaceAxesRotateWithFaceAndOnlyIncludeLoadedExposedBlocks(){for(var face:Direction.values()){var selected=PlanSelection.select(hit(face),new FocusProgram.PlanSettings(1,0,2,true),q->true,q->true,q->true);assertEquals(15,selected.size());for(var q:selected)assertEquals(0,face.getAxis().choose(q.getX(),q.getY(),q.getZ()));}var selection=PlanSelection.select(hit(Direction.UP),new FocusProgram.PlanSettings(3,3,3,true),q->q.getX()<=0,q->true,q->q.getZ()<=0);assertEquals(16,selection.size());}
    @Test void zeroDimensionsOnlySelectTheImpactAndInvalidDimensionsSelectNothing(){assertEquals(List.of(BlockPos.ZERO),PlanSelection.select(hit(Direction.NORTH),new FocusProgram.PlanSettings(0,0,0,false),q->true,q->true,q->true));assertTrue(PlanSelection.select(hit(Direction.NORTH),new FocusProgram.PlanSettings(4,0,0,false),q->true,q->true,q->true).isEmpty());}
}
