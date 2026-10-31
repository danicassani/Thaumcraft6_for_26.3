package thaumcraft.visual;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
final class CrucibleWaterTest {
 @Test void cleanWaterRetainsItsOwnTintAndIsOpaque(){assertEquals(0xff3f76e4,CrucibleWater.color(0x3f76e4,0));assertEquals(0xff123456,CrucibleWater.color(0x123456,-1));}
 @Test void essenceProducesTheOriginalPurpleStaining(){int full=CrucibleWater.color(0xffffff,500);assertEquals(0,(full>>8)&255);assertEquals(127,full&255);assertEquals(170,(full>>16)&255,1);assertEquals(0xffd47fbf,CrucibleWater.color(0xffffff,1));}
 @Test void excessiveResidueCannotWrapColourChannels(){assertEquals(CrucibleWater.color(0x3f76e4,500),CrucibleWater.color(0x3f76e4,Integer.MAX_VALUE));}
}
