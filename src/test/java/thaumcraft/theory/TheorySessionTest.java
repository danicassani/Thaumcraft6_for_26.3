package thaumcraft.theory;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.*;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
final class TheorySessionTest {
    private TheorySession example(){var s=TheorySession.start(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),5,List.of("thaumcraft:arcane_workbench"));s.add("BASICS",75);s.choices=List.of(new TheoryCards.Card(TheoryCards.Kind.EXPERIMENTATION,0,0,0,0,123456789012345L,false));return s;}
    @Test void reloadPreservesOwnerChoicesSeedAndFiniteAidPool(){var s=example();var json=TheorySession.CODEC.encodeStart(JsonOps.INSTANCE,s).getOrThrow();var read=TheorySession.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow();assertEquals(json,TheorySession.CODEC.encodeStart(JsonOps.INSTANCE,read).getOrThrow());assertEquals(4,read.inspiration);assertEquals(3,read.aids.size());assertEquals(123456789012345L,read.choices.getFirst().seed());}
    @Test void failedProposalCannotMutateOriginal(){var s=example();var copy=s.copy();copy.add("BASICS",25);copy.blocked.add("BASICS");copy.aids.clear();copy.choices=List.of();assertEquals(75,s.total("BASICS"));assertFalse(s.blocked.contains("BASICS"));assertEquals(3,s.aids.size());assertEquals(1,s.choices.size());}
    @Test void rewardFractionsRankingAndPenaltyUseOriginalProgression(){var s=TheorySession.start(UUID.randomUUID(),5,List.of());s.add("BASICS",100);s.add("ALCHEMY",50);s.add("ARTIFICE",25);assertEquals(Map.of("BASICS",32,"ALCHEMY",10,"ARTIFICE",5),s.rewards());s.penalty=1;assertEquals(16,s.rewards().get("ALCHEMY"));assertEquals(5,s.rewards().get("ARTIFICE"));}
    @Test void malformedAndFutureSessionsAreRejected(){var json=TheorySession.CODEC.encodeStart(JsonOps.INSTANCE,example()).getOrThrow().toString();for(var bad:List.of(json.replace("\"version\":1","\"version\":2"),json.replace("\"inspiration\":4","\"inspiration\":6"),json.replace("\"BASICS\":75","\"NO_CATEGORY\":75"),json.replace("EXPERIMENTATION","MADE_UP_CARD"),json.replace("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb","invalid"))){assertTrue(TheorySession.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(bad)).result().isEmpty(),bad);}}
    @Test void inspirationNeverExceedsCapacityAndEmptyTotalsDisappear(){var s=example();s.inspire(100);assertEquals(5,s.inspiration);s.inspire(-100);assertEquals(0,s.inspiration);s.add("BASICS",-75);assertTrue(s.totals.isEmpty());}
    @Test void celestialAimUsesActualCelestialAngleAndIsIndependentOfYawAtZenith(){assertTrue(CelestialObservations.aimed(new Vec3(0,1,0),0));assertTrue(CelestialObservations.aimed(new Vec3(-1,0,0),90));assertFalse(CelestialObservations.aimed(new Vec3(1,0,0),90));assertFalse(CelestialObservations.aimed(new Vec3(0,-1,0),0));}
}
