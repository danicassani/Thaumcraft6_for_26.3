package thaumcraft.equipment;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.junit.jupiter.api.Test;
import thaumcraft.client.EquipmentGeometry;
import static org.junit.jupiter.api.Assertions.*;
class EquipmentGeometryTest {
 @Test void allEightLegacyLayersSatisfyTheActualNativeHumanoidConstructor(){
  List<Supplier<LayerDefinition>> layers=List.of(EquipmentGeometry::fortress,EquipmentGeometry::fortress_inner,EquipmentGeometry::crimson_plate,EquipmentGeometry::crimson_plate_inner,EquipmentGeometry::crimson_robe,EquipmentGeometry::crimson_robe_inner,EquipmentGeometry::crimson_praetor,EquipmentGeometry::crimson_praetor_inner);
  for(var layer:layers){var model=assertDoesNotThrow(()->new HumanoidModel<HumanoidRenderState>(layer.get().bakeRoot()));assertNotNull(model.hat);assertNotNull(model.leftArm);assertNotNull(model.rightLeg);}
 }
 @Test void clothUsesOriginalAnglesAndResetsBetweenEntities(){
  var robe=EquipmentGeometry.crimson_robe_inner().bakeRoot().getChild("body");
  thaumcraft.client.EquipmentCloth.animate(robe,"crimson_robe",0,1);
  assertEquals(-1.4F-.1047198F,robe.getChild("FrontclothR1").xRot,.00001);
  assertEquals(1.4F+.2268928F,robe.getChild("ClothBackL3").xRot,.00001);
  thaumcraft.client.EquipmentCloth.animate(robe,"crimson_robe",0,0);
  assertEquals(-.1047198F,robe.getChild("FrontclothR1").xRot,.00001);
  var plate=EquipmentGeometry.crimson_plate().bakeRoot().getChild("body");
  thaumcraft.client.EquipmentCloth.animate(plate,"crimson_plate",0,1);
  assertEquals(.7F+.1396263F,plate.getChild("Cloak1").xRot,.00001);
  var leader=EquipmentGeometry.crimson_praetor().bakeRoot().getChild("body");thaumcraft.client.EquipmentCloth.animate(leader,"crimson_praetor",0,1);assertEquals(1.4F-.1047198F,leader.getChild("LegClothR").xRot,.00001);assertEquals(-1.4F-.1047198F,leader.getChild("LegClothL").xRot,.00001);
 }

 @Test void innerModelsKeepTheBeltAndClothWithoutDuplicatingChestArmor(){
  var robe=EquipmentGeometry.crimson_robe_inner().bakeRoot().getChild("body");assertTrue(robe.hasChild("FrontclothR1"));assertFalse(robe.hasChild("Chestplate"));
  var plate=EquipmentGeometry.crimson_plate_inner().bakeRoot().getChild("body");assertTrue(plate.hasChild("Mbelt"));assertFalse(plate.hasChild("Chestplate"));
  var leader=EquipmentGeometry.crimson_praetor_inner().bakeRoot().getChild("body");assertFalse(leader.hasChild("Chestplate"));
 }

}
