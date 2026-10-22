package thaumcraft.mirrors;
import net.minecraft.core.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MirrorRulesTest {
 @Test void originalInstabilityThresholdsDifferForItemsAndEssence(){assertEquals(128,MirrorRules.threshold(false));assertEquals(64,MirrorRules.threshold(true));}
 @Test void everyOrientationScansOnlyTheOriginalForwardHalfVolume(){var origin=new BlockPos(10,30,20);for(var face:Direction.values()){assertTrue(MirrorRules.inFront(origin,origin.relative(face,7),face));assertFalse(MirrorRules.inFront(origin,origin.relative(face,8),face));assertFalse(MirrorRules.inFront(origin,origin.relative(face.getOpposite()),face));assertFalse(MirrorRules.inFront(origin,origin,face));}}
 @Test void lateralRadiusIncludesEightAndRejectsNine(){var origin=BlockPos.ZERO;assertTrue(MirrorRules.inFront(origin,new BlockPos(8,8,7),Direction.SOUTH));assertFalse(MirrorRules.inFront(origin,new BlockPos(9,0,1),Direction.SOUTH));assertFalse(MirrorRules.inFront(origin,new BlockPos(0,9,1),Direction.SOUTH));}
}
