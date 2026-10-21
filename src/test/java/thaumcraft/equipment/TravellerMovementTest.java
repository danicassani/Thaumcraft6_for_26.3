package thaumcraft.equipment;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TravellerMovementTest {
 @Test void originalFallProtectionComposesBootsThenCloud(){assertEquals(18,TravellerMovement.fallDamage(18,false,false));assertEquals(8,TravellerMovement.fallDamage(18,true,false));assertEquals(4,TravellerMovement.fallDamage(18,false,true));assertEquals(0,TravellerMovement.fallDamage(18,true,true));assertEquals(2,TravellerMovement.fallDamage(6,true,false));assertEquals(0,TravellerMovement.fallDamage(3,true,false));}
}
