package thaumcraft.equipment;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SpecialArmorTest {
 @Test void originalMagicAndElementalRatiosHaveTheRightPrecedence(){assertEquals(7/35.,SpecialArmor.ratio("fortress_chest",true,true,true));assertEquals(7/20.,SpecialArmor.ratio("fortress_chest",false,true,false));assertEquals(6/20.,SpecialArmor.ratio("fortress_legs",false,false,true));assertEquals(9/35.,SpecialArmor.ratio("void_robe_chest",true,false,false));assertEquals(0,SpecialArmor.ratio("void_robe_chest",false,true,true));assertEquals(0,SpecialArmor.ratio("void_chest",true,true,true));assertEquals(0,SpecialArmor.ratio("fortress_chest",false,false,false));}
 @Test void nearBrokenArmorCannotAbsorbUnboundedDamage(){assertEquals(.0025,SpecialArmor.capped(.5,1,10000),1e-8);assertEquals(0,SpecialArmor.capped(.5,0,20));assertEquals(.5,SpecialArmor.capped(.5,100,20));}
}
