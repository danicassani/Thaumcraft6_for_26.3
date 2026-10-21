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
}
