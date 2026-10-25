package thaumcraft.banners;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.mojang.serialization.JsonOps;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import thaumcraft.rifts.RiftShape;
import thaumcraft.visual.PortalGeometry;
class BannerVisualTest {
 @Test void legacyIdsAndNativeRecipeResultsAreExact()throws Exception{for(var d:DyeColor.values()){var n=BannerDesign.id(d);assertEquals(d==DyeColor.LIGHT_BLUE?"banner_lightblue":d==DyeColor.LIGHT_GRAY?"banner_silver":"banner_"+d.getName(),n);var r=read("src/modern/resources/data/thaumcraft/recipe/arcane/banner_"+d.getName()+".json");assertEquals("thaumcraft:"+n,r.getAsJsonObject("recipe").getAsJsonObject("result").get("id").getAsString());assertEquals(10,r.get("vis").getAsInt());assertEquals("BASEINFUSION",r.get("research").getAsString());}}
 @Test void aspectsPersistAndMalformedFutureDataFailClosed(){for(var a:thaumcraft.crystals.CrystalAspects.COLORS.keySet()){var data=new AspectBannerEntity.Data(1,a);assertEquals(data,AspectBannerEntity.Data.CODEC.parse(JsonOps.INSTANCE,AspectBannerEntity.Data.CODEC.encodeStart(JsonOps.INSTANCE,data).getOrThrow()).getOrThrow());}for(var s:List.of("{version:2,aspect:'aer'}","{version:1,aspect:'missing'}"))assertTrue(AspectBannerEntity.Data.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(s)).error().isPresent());}
 @Test void everyInventoryBannerUsesOriginalLayersAndAllAspectColours()throws Exception{for(var d:DyeColor.values()){var m=read("src/modern/resources/assets/thaumcraft/items/"+BannerDesign.id(d)+".json").getAsJsonObject("model");assertEquals("thaumcraft:banner_aspect",m.get("component").getAsString());var colours=new HashMap<String,Integer>();for(var c:m.getAsJsonArray("cases")){var o=c.getAsJsonObject();colours.put(o.get("when").getAsString(),o.getAsJsonObject("model").getAsJsonArray("tints").get(2).getAsJsonObject().get("value").getAsInt()&0xffffff);}assertEquals(thaumcraft.crystals.CrystalAspects.COLORS,colours);}}
 @Test void mirrorPlanesHaveCorrectDepthBoundsAndOutwardWinding(){for(var d:Direction.values()){var p=PortalGeometry.mirror(d);assertEquals(4,p.size());var n=p.get(1).subtract(p.get(0)).cross(p.get(2).subtract(p.get(0))).normalize();assertEquals(1,n.dot(new Vec3(d.getStepX(),d.getStepY(),d.getStepZ())),.000001);for(var v:p)assertTrue(v.x>0&&v.x<1&&v.y>0&&v.y<1&&v.z>0&&v.z<1);}}
 @Test void riftVisualDoesNotMutateCollisionShapeAndKeepsSixSides(){var points=RiftShape.create(72,36);var before=List.copyOf(points);var stable=PortalGeometry.rift(points,0,50,1);var still=PortalGeometry.rift(points,40,50,1);assertEquals((points.size()-1)*6,stable.size());for(int i=0;i<stable.size();i++)assertArrayEquals(stable.get(i),still.get(i));var moving=PortalGeometry.rift(points,40,0,1);assertFalse(Arrays.equals(stable.get(6),moving.get(6)));assertEquals(before,points);for(var face:moving)for(var v:face)assertTrue(Double.isFinite(v.x)&&Double.isFinite(v.y)&&Double.isFinite(v.z));}
 JsonObject read(String p)throws Exception{return JsonParser.parseString(Files.readString(Path.of(p))).getAsJsonObject();}
}
