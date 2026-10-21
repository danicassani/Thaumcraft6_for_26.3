package thaumcraft.rifts;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RiftEventsTest {
 @Test void originalDistribution(){int[] counts=new int[5];for(int i=0;i<=100;i++)counts[RiftEvents.select(i).ordinal()]++;assertArrayEquals(new int[]{50,10,20,20,1},counts);}
 @Test void invalidRollsFail(){assertThrows(IllegalArgumentException.class,()->RiftEvents.select(-1));assertThrows(IllegalArgumentException.class,()->RiftEvents.select(101));}
 @Test void durationsRespectBothSizeAndSupportedSetting(){for(int size=-1;size<=101;size++)for(int sample=-1;sample<=121;sample++){int seconds=RiftEvents.seconds(size,sample);assertTrue(seconds>=5&&seconds<=30);if(size>=60)assertEquals(30,seconds);if(size<=5)assertEquals(5,seconds);}assertEquals(10,RiftEvents.seconds(20,0));assertEquals(20,RiftEvents.seconds(20,120));assertEquals(15,RiftEvents.seconds(20,15));}
}
