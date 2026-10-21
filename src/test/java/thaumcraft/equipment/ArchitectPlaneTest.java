package thaumcraft.equipment;
import net.minecraft.core.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ArchitectPlaneTest {
 @Test void everyFaceAndModeSelectsNineDistinctAdjacentPositions(){for(var face:Direction.values())for(var horizontal:new Direction[]{Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST})for(int mode=0;mode<3;mode++){var origin=new BlockPos(10,20,30);var plane=ArchitectPlane.select(origin,face,horizontal,mode);assertEquals(9,plane.size());assertEquals(9,new java.util.HashSet<>(plane).size());assertTrue(plane.contains(origin.relative(face)));for(var q:plane)assertTrue(q.distSqr(origin.relative(face))<=2);}}
 @Test void normalPlaneIsPerpendicularToTheClickedFace(){for(var face:Direction.values()){var origin=BlockPos.ZERO;for(var q:ArchitectPlane.select(origin,face,Direction.NORTH,0))assertEquals(1,q.getX()*face.getStepX()+q.getY()*face.getStepY()+q.getZ()*face.getStepZ());}}
}
