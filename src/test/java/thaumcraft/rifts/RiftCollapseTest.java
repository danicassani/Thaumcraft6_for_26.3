package thaumcraft.rifts;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RiftCollapseTest {
 @Test void originalBandsIncludeExactBoundaries(){assertEquals(RiftCollapse.Stability.VERY_UNSTABLE,RiftCollapse.stability(-25));assertEquals(RiftCollapse.Stability.UNSTABLE,RiftCollapse.stability(-24.9F));assertEquals(RiftCollapse.Stability.STABLE,RiftCollapse.stability(0));assertEquals(RiftCollapse.Stability.STABLE,RiftCollapse.stability(50));assertEquals(RiftCollapse.Stability.VERY_STABLE,RiftCollapse.stability(50.1F));}
 @Test void dropOddsTrackSize(){for(int size=1;size<=100;size++){int hits=0;for(int roll=0;roll<100;roll++)if(RiftCollapse.pearl(size,roll))hits++;assertEquals((int)Math.sqrt(size),hits);assertEquals(hits,RiftCollapse.seeds(size));}assertThrows(IllegalArgumentException.class,()->RiftCollapse.pearl(100,100));}
 @Test void legacyExposureFormulaAndEffectiveRadius(){assertEquals(120,RiftCollapse.exposure(0,120));assertEquals(60,RiftCollapse.exposure(16,120));assertEquals(0,RiftCollapse.exposure(32,120));assertEquals(0,RiftCollapse.exposure(1024,120));assertEquals(0,RiftCollapse.exposure(Double.NaN,120));}
}
