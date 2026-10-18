package thaumcraft.research;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.*;
import org.junit.jupiter.api.Test;

final class KnowledgeStateTest {
    @Test void roundTripPreservesIndependentProgressAndRawFractions(){
        var state=KnowledgeState.EMPTY.flag("!aer").scan("item:minecraft:stone").craft("thaumcraft:thaumometer").stage("FIRSTSTEPS",2).addPoints("OBSERVATION","BASICS",19).addPoints("THEORY","AUROMANCY",33);
        var tag=KnowledgeState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();assertEquals(state,KnowledgeState.CODEC.parse(JsonOps.INSTANCE,tag).getOrThrow());
        assertEquals(19,state.raw("OBSERVATION","BASICS"));assertEquals(33,state.raw("THEORY","AUROMANCY"));assertTrue(KnowledgeState.EMPTY.flags().isEmpty());
    }
    @Test void invalidOrFutureSaveNeverReturnsPartialState(){
        String base="{\"version\":1,\"stages\":{},\"scans\":[],\"flags\":[],\"crafts\":[],\"points\":{}}";
        for(var bad:List.of(base.replace("version\":1","version\":2"),base.replace("\"scans\":[]","\"scans\":[\"a\",\"a\"]"),base.replace("\"points\":{}","\"points\":{\"OBSERVATION:BASICS\":-1}"),base.replace("\"stages\":{}","\"stages\":{\"FIRSTSTEPS\":9999}"))){var result=KnowledgeState.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(bad));assertTrue(result.result().isEmpty());assertTrue(result.error().isPresent());}
    }
    @Test void pointsCannotUnderflowOrOverflow(){var state=KnowledgeState.EMPTY.addPoints("OBSERVATION","BASICS",15);assertThrows(IllegalArgumentException.class,()->state.addPoints("OBSERVATION","BASICS",-16));assertEquals(KnowledgeState.MAX_POINTS,state.addPoints("OBSERVATION","BASICS",Integer.MAX_VALUE).raw("OBSERVATION","BASICS"));}
    @Test void knownStageIsDifferentFromCompletionAndFlags(){
        var state=KnowledgeState.EMPTY.stage("FIRSTSTEPS",2).flag("!aer");assertTrue(ResearchCatalog.knows(state,"FIRSTSTEPS@2"));assertFalse(ResearchCatalog.knows(state,"FIRSTSTEPS"));assertFalse(ResearchCatalog.knows(state,"FIRSTSTEPS@3"));assertTrue(ResearchCatalog.knows(state,"FIRSTSTEPS@2&&!aer"));assertFalse(ResearchCatalog.knows(state,"FIRSTSTEPS@3&&!aer"));assertTrue(ResearchCatalog.knows(state,"FIRSTSTEPS@3||!aer"));assertFalse(ResearchCatalog.knows(state,"MISSING"));assertFalse(ResearchCatalog.knows(state,"FIRSTSTEPS@bad"));
        assertTrue(ResearchCatalog.knows(state.stage("FIRSTSTEPS",4),"FIRSTSTEPS"));
    }
    @Test void originalAspectFormulaAndCombinationRemainSymmetric(){
        assertEquals("lux",AspectCatalog.combine("aer","ignis"));assertEquals("lux",AspectCatalog.combine("ignis","aer"));assertNull(AspectCatalog.combine("aer","not_an_aspect"));assertEquals(2,AspectCatalog.observation("BASICS",Map.of("terra",5)));assertEquals(0,AspectCatalog.observation("BASICS",Map.of("alienis",5)));
    }
}
