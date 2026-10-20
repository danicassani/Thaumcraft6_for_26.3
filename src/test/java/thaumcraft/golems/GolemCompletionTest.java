package thaumcraft.golems;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.*;
import java.util.*;
class GolemCompletionTest {
 @Test void all2400DesignsRoundTripWithTheirTraitsAndCosts(){int count=0;for(int m=0;m<6;m++)for(int h=0;h<5;h++)for(int a=0;a<5;a++)for(int extra=0;extra<4;extra++)for(int legs=0;legs<4;legs++){var d=new GolemDesign(m,h,a,extra,legs);var json=GolemDesign.CODEC.encodeStart(JsonOps.INSTANCE,d).getOrThrow();assertEquals(d,GolemDesign.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow());assertTrue(d.cost()>0);assertEquals(extra==3?2:1,d.capacity());assertFalse(d.has("light")&&d.has("heavy"));assertFalse(d.has("deft")&&d.has("clumsy"));count++;}assertEquals(2400,count);}
 @Test void tenOriginalPatternsHaveExpectedFootprints(){int[] expected={9,1,2,2,4,3,3,6,6,8};var unique=new HashSet<String>();for(int i=0;i<10;i++){var slots=GolemPatterns.slots(i);assertEquals(expected[i],slots.length);assertEquals(slots.length,Arrays.stream(slots).distinct().count());for(int slot:slots)assertTrue(slot>=0&&slot<9);assertTrue(unique.add(Arrays.toString(slots)));}var copy=GolemPatterns.slots(0);copy[0]=8;assertEquals(0,GolemPatterns.slots(0)[0]);}
 @Test void pressRotatesWithoutOverlappingParts(){var core=new BlockPos(-31,205,16);for(var face:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST}){var parts=GolemPress.parts(core,face);assertEquals(4,new HashSet<>(parts).size());assertFalse(parts.contains(core));assertEquals(core.above(),parts.getFirst());for(var part:parts)assertTrue(Math.abs(part.getX()-core.getX())<=1&&Math.abs(part.getY()-core.getY())<=1&&Math.abs(part.getZ()-core.getZ())<=1);}}
}
