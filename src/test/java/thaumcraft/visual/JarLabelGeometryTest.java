package thaumcraft.visual;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class JarLabelGeometryTest {
 @Test void labelsStayStableAcrossFramesAndRepeatEveryFourBlocks(){for(var face:Direction.Plane.HORIZONTAL)for(int x=-20;x<=20;x++){float angle=JarLabelGeometry.degrees("aer",x,face,true);assertEquals(angle,JarLabelGeometry.degrees("aer",x,face,true));assertEquals(angle,JarLabelGeometry.degrees("aer",x+4,face,true));}assertNotEquals(JarLabelGeometry.degrees("aer",0,Direction.NORTH,true),JarLabelGeometry.degrees("aer",0,Direction.SOUTH,true));}
 @Test void negativeCoordinatesKeepTheOriginalSignedRemainder(){int x=-"aer".hashCode()-5;assertEquals(-5,JarLabelGeometry.degrees("aer",x,Direction.NORTH,true));assertEquals(-1,JarLabelGeometry.degrees("aer",x+4,Direction.NORTH,true));}
 @Test void disablingCrookedStraightensEveryAspectAndFace(){for(var aspect:thaumcraft.crystals.CrystalAspects.COLORS.keySet())for(var face:Direction.Plane.HORIZONTAL)for(int x:new int[]{Integer.MIN_VALUE,-100,0,100,Integer.MAX_VALUE})assertEquals(0,JarLabelGeometry.degrees(aspect,x,face,false));assertEquals(0,JarLabelGeometry.degrees("",0,Direction.NORTH,true));}
 @Test void paperFacesOutwardAndTheGlyphSitsInFrontOfIt(){for(var face:Direction.Plane.HORIZONTAL){var paper=JarLabelGeometry.quad(face,.36F,.64F,.22F,.5F,.181F);var normal=paper.get(1).subtract(paper.get(0)).cross(paper.get(2).subtract(paper.get(0))).normalize();var outside=new net.minecraft.world.phys.Vec3(face.getStepX(),face.getStepY(),face.getStepZ());assertEquals(1,normal.dot(outside),.000001);var glyph=JarLabelGeometry.quad(face,.36F,.64F,.22F,.5F,.180F);assertTrue(glyph.get(0).subtract(paper.get(0)).dot(outside)>0);}}
}
