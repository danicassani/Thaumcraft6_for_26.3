package thaumcraft.golems;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
class GolemDesignTest {
    @Test void supportedDesignsRoundTrip(){for(int m=0;m<6;m++)for(int h=0;h<3;h++)for(int a=0;a<4;a++)for(int x=0;x<4;x++){var d=new GolemDesign(m,h,a,x);assertEquals(d,GolemDesign.CODEC.parse(JsonOps.INSTANCE,GolemDesign.CODEC.encodeStart(JsonOps.INSTANCE,d).getOrThrow()).getOrThrow());assertTrue(d.health()>0);assertTrue(d.cost()>0);assertTrue(d.components().values().stream().allMatch(n->n>0));}}
    @Test void opposingTraitsCancelInPartOrder(){var d=new GolemDesign(0,1,0,1);assertFalse(d.has("light"));assertFalse(d.has("heavy"));assertFalse(d.has("fragile"));assertFalse(d.has("armored"));assertTrue(d.has("smart"));}
    @Test void exactBasicCostsIncludeWalkerComponents(){var d=GolemDesign.BASIC;assertEquals(java.util.Map.of("thaumcraft:plank_greatwood",3,"thaumcraft:mechanism_simple",2,"thaumcraft:mind_clockwork",1),d.components());assertEquals(8,d.cost());assertEquals(1,d.capacity());}
    @Test void haulerAndArmsGateTasks(){var basic=GolemDesign.BASIC;assertFalse(SealKind.HARVEST.accepts(basic));assertFalse(SealKind.GUARD.accepts(basic));assertFalse(SealKind.BREAKER.accepts(basic));assertTrue(SealKind.HARVEST.accepts(new GolemDesign(3,0,1,3)));assertTrue(SealKind.GUARD.accepts(new GolemDesign(1,0,2,0)));assertTrue(SealKind.BREAKER.accepts(new GolemDesign(1,0,3,0)));assertEquals(2,new GolemDesign(0,0,0,3).capacity());}
    @Test void advancedPartsKeepResearchAndComponentCosts(){var d=new GolemDesign(4,2,3,1);assertTrue(d.research().containsAll(java.util.List.of("MATSTUDTHAUMIUM","MINDBIOTHAUMIC","GOLEMCOMBATADV","GOLEMBREAKER")));assertEquals(2,d.components().get("minecraft:diamond"));assertEquals(9,d.components().get("thaumcraft:plate_thaumium"));}
    @Test void invalidComponentsAreRejected(){assertThrows(IllegalArgumentException.class,()->new GolemDesign(6,0,0,0));assertTrue(GolemDesign.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString("{\"material\":0,\"head\":0,\"arms\":4,\"addon\":0}")).error().isPresent());}
}
