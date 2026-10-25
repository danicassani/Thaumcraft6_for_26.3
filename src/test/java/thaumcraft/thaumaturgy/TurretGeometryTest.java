package thaumcraft.thaumaturgy;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TurretGeometryTest {
 @Test void basicModelRetainsEveryOriginalPartAndPivot() throws Exception {
  var original=Files.readString(Path.of("src/main/java/thaumcraft/client/renderers/models/entity/ModelCrossbow.java"));var model=JsonParser.parseString(Files.readString(Path.of("src/modern/resources/assets/thaumcraft/models/turrets/basic.json"))).getAsJsonObject();assertEquals(20,model.size());for(var entry:model.entrySet()){assertTrue(original.contains(entry.getKey()+" = new ModelRenderer"));var p=entry.getValue().getAsJsonObject();assertEquals(6,p.getAsJsonArray("box").size());assertEquals(3,p.getAsJsonArray("pos").size());assertEquals(3,p.getAsJsonArray("rot").size());assertEquals(original.contains("crossbow.addChild("+entry.getKey()+")")?"crossbow":"root",p.get("parent").getAsString());}assertEquals(10,model.getAsJsonObject("crossbow").getAsJsonArray("pos").get(1).getAsInt());
 }
 @Test void advancedMeshHasAllOriginalAnimatedGroupsAndFiniteUnitNormals() throws Exception {
  var groups=JsonParser.parseString(Files.readString(Path.of("src/modern/resources/assets/thaumcraft/models/turrets/advanced.json"))).getAsJsonObject().getAsJsonObject("advanced");assertEquals(Set.of("bow1","bow2","legs","mech","box","shield","brain","loader"),groups.keySet());int triangles=0;for(var entry:groups.entrySet()){assertFalse(entry.getValue().getAsJsonArray().isEmpty());for(var tri:entry.getValue().getAsJsonArray()){triangles++;assertEquals(3,tri.getAsJsonArray().size());for(var vertex:tri.getAsJsonArray()){var v=vertex.getAsJsonArray();assertEquals(8,v.size());for(var n:v)assertTrue(Float.isFinite(n.getAsFloat()));double length=0;for(int i=5;i<8;i++)length+=Math.pow(v.get(i).getAsDouble(),2);assertEquals(1,length,.001);}}}assertEquals(834,triangles);
 }
}
