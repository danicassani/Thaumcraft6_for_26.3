package thaumcraft.golems;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import net.minecraft.core.*;
import net.minecraft.world.phys.*;
import com.mojang.serialization.JsonOps;
class SealAreaTest {
 @Test void allFacesHaveOriginalDefaultExtents(){for(var face:Direction.values()){var a=SealArea.initial(face);var box=a.bounds(BlockPos.ZERO,face);assertEquals(face.getStepX()==0?5:1,box.getXsize());assertEquals(face.getStepY()==0?5:1,box.getYsize());assertEquals(face.getStepZ()==0?5:1,box.getZsize());assertTrue(box.contains(Vec3.atCenterOf(BlockPos.ZERO.relative(face))));assertFalse(box.contains(Vec3.atCenterOf(BlockPos.ZERO)));}}
 @Test void oppositeFacesExtendAwayFromSupport(){var p=new BlockPos(-8,90,-20);for(var face:Direction.values()){var b=new SealArea(8,8,8).bounds(p,face);assertTrue(b.contains(Vec3.atCenterOf(p.relative(face,8))));assertFalse(b.contains(Vec3.atCenterOf(p.relative(face,9))));assertFalse(b.contains(Vec3.atCenterOf(p)));assertFalse(b.contains(Vec3.atCenterOf(p.relative(face.getOpposite()))));}}
 @Test void axesAreIndependentAndBoundaryIsExclusive(){var b=new SealArea(3,1,2).bounds(BlockPos.ZERO,Direction.UP);assertEquals(5,b.getXsize());assertEquals(1,b.getYsize());assertEquals(3,b.getZsize());assertTrue(b.contains(new Vec3(-2,1,-1)));assertFalse(b.contains(new Vec3(3,1,0)));assertFalse(b.contains(new Vec3(0,2,0)));assertFalse(b.contains(new Vec3(0,1,2)));}
 @Test void invalidDimensionsAndEditsStayBounded(){var a=new SealArea(Integer.MIN_VALUE,Integer.MAX_VALUE,0);assertEquals(new SealArea(1,8,1),a);assertEquals(a,a.change(0,-1));assertEquals(a,a.change(1,1));assertEquals(new SealArea(2,8,1),a.change(0,1));}
 @Test void codecRoundTripsAllConfigurations(){for(int x=1;x<=8;x++)for(int y=1;y<=8;y++)for(int z=1;z<=8;z++){var a=new SealArea(x,y,z);assertEquals(a,SealArea.CODEC.parse(JsonOps.INSTANCE,SealArea.CODEC.encodeStart(JsonOps.INSTANCE,a).getOrThrow()).getOrThrow());}}
 @Test void onlySpatialSealsOfferAreas(){for(var k:SealKind.values())assertEquals(k.base()==SealKind.PICKUP||k.base()==SealKind.GUARD||k.base()==SealKind.BREAKER||k==SealKind.HARVEST||k==SealKind.LUMBER||k==SealKind.BUTCHER,k.hasArea());assertFalse(SealKind.STOCK.hasArea());assertFalse(SealKind.USE.hasArea());}
}
