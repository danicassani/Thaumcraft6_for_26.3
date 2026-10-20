package thaumcraft.golems;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
public final class GolemProgressTest {
 @Test void originalThresholdsAndBoundary(){assertEquals(1000,GolemProgress.required(0));assertEquals(4000,GolemProgress.required(1));assertEquals(100000,GolemProgress.required(9));var p=GolemProgress.EMPTY.earn(999,true);assertEquals(new GolemProgress(0,999),p);assertEquals(new GolemProgress(1,0),p.earn(1,true));assertEquals(new GolemProgress(2,7),p.earn(4008,true));}
 @Test void onlySmartPositiveWorkCounts(){var p=new GolemProgress(2,123);assertSame(p,p.earn(500,false));assertSame(p,p.earn(-1,true));assertSame(p,p.earn(0,true));}
 @Test void maximumAndOverflowAreBounded(){assertEquals(new GolemProgress(10,0),GolemProgress.EMPTY.earn(385000,true));assertEquals(new GolemProgress(9,99999),GolemProgress.EMPTY.earn(384999,true));assertEquals(new GolemProgress(10,0),new GolemProgress(9,99999).earn(Integer.MAX_VALUE,true));assertEquals(new GolemProgress(10,0),new GolemProgress(10,99).earn(1,true));}
 @Test void oldAndMalformedSavesAreSafe(){assertEquals(GolemProgress.EMPTY,GolemProgress.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString("{}")).getOrThrow());assertEquals(GolemProgress.EMPTY,new GolemProgress(-5,-8));assertEquals(new GolemProgress(10,0),new GolemProgress(Integer.MAX_VALUE,Integer.MAX_VALUE));assertEquals(new GolemProgress(2,8999),new GolemProgress(2,Integer.MAX_VALUE));}
 @Test void codecPreservesEveryRank(){for(int r=0;r<=10;r++){var p=new GolemProgress(r,r==10?0:GolemProgress.required(r)-1);assertEquals(p,GolemProgress.CODEC.parse(JsonOps.INSTANCE,GolemProgress.CODEC.encodeStart(JsonOps.INSTANCE,p).getOrThrow()).getOrThrow());}}
 @Test void bonusesKeepRankZeroAndRespectFighter(){var base=new GolemDesign(3,1,0,0);var fighter=new GolemDesign(3,1,4,0);assertEquals(base.health(),GolemProgress.EMPTY.health(base));assertEquals(base.speed(),GolemProgress.EMPTY.speed(base));assertEquals(base.damage(),GolemProgress.EMPTY.damage(base));var p=new GolemProgress(10,0);assertEquals(base.health()+10,p.health(base));assertEquals(base.speed()*1.25,p.speed(base),.000001);assertEquals(base.damage(),p.damage(base));assertEquals(fighter.damage()+2.5,p.damage(fighter));}
}
