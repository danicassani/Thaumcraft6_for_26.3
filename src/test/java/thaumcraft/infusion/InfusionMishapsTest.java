package thaumcraft.infusion;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.*;
final class InfusionMishapsTest {
 @Test void allTwentyFourDrawsKeepTheOriginalWeights(){var counts=new EnumMap<InfusionMishaps.Kind,Integer>(InfusionMishaps.Kind.class);for(int n=0;n<24;n++)counts.merge(InfusionMishaps.kind(n),1,Integer::sum);assertEquals(Map.ofEntries(Map.entry(InfusionMishaps.Kind.EJECT,4),Map.entry(InfusionMishaps.Kind.WARP,3),Map.entry(InfusionMishaps.Kind.ZAP_ONE,3),Map.entry(InfusionMishaps.Kind.ZAP_ALL,2),Map.entry(InfusionMishaps.Kind.EJECT_GOO,2),Map.entry(InfusionMishaps.Kind.EJECT_FLUX,2),Map.entry(InfusionMishaps.Kind.EJECT_GOO_ALT,1),Map.entry(InfusionMishaps.Kind.DESTROY_FLUX,1),Map.entry(InfusionMishaps.Kind.HARM_ONE,2),Map.entry(InfusionMishaps.Kind.EJECT_EXPLOSION,2),Map.entry(InfusionMishaps.Kind.HARM_ALL,1),Map.entry(InfusionMishaps.Kind.EXPLOSION,1)),counts);}
 @Test void corruptDrawsCannotSelectAnAccident(){assertThrows(IllegalArgumentException.class,()->InfusionMishaps.kind(-1));assertThrows(IllegalArgumentException.class,()->InfusionMishaps.kind(24));}
}
