package thaumcraft.worldgen;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MoundResourcesTest {
    static JsonObject read(String path) throws Exception {return JsonParser.parseString(Files.readString(Path.of(path))).getAsJsonObject();}
    static JsonObject blueprint() throws Exception {return read("src/modern/resources/data/thaumcraft/blueprints/mound.json");}
    @Test void geometryHasExactOriginalSourceAndUniqueCells() throws Exception {
        var b=blueprint();assertEquals(2441,b.getAsJsonArray("cells").size());
        var digest=MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(Path.of("src/main/java/thaumcraft/common/world/objects/WorldGenMound.java")));
        assertEquals(HexFormat.of().formatHex(digest),b.get("source_sha256").getAsString());
        var seen=new HashSet<String>();
        for (var c:b.getAsJsonArray("cells")) {var a=c.getAsJsonArray();assertTrue(seen.add(a.get(0)+","+a.get(1)+","+a.get(2)));}
    }
    @Test void allCellsFitSavedPieceAndPalette() throws Exception {
        var b=blueprint();int count=b.getAsJsonArray("palette").size();
        for (var c:b.getAsJsonArray("cells")) {var a=c.getAsJsonArray();assertTrue(a.get(0).getAsInt()>=0&&a.get(0).getAsInt()<=18);assertTrue(a.get(1).getAsInt()>=0&&a.get(1).getAsInt()<=15);assertTrue(a.get(2).getAsInt()>=0&&a.get(2).getAsInt()<=18);assertTrue(a.get(3).getAsInt()>=0&&a.get(3).getAsInt()<count);}
    }
    @Test void preservesLegacyStairOrientationsAndLadders() throws Exception {
        var palette=blueprint().getAsJsonArray("palette");var faces=new HashSet<String>();int stairStates=0;
        for (var element:palette) {var p=element.getAsJsonObject();if (p.get("block").getAsString().equals("minecraft:cobblestone_stairs")) {stairStates++;var props=p.getAsJsonObject("properties");faces.add(props.get("facing").getAsString()+props.get("half").getAsString());}if(p.get("block").getAsString().equals("minecraft:ladder")) assertEquals("east",p.getAsJsonObject("properties").get("facing").getAsString());}
        assertEquals(8,faces.size());assertTrue(stairStates>=8);
    }
    @Test void nativePlacementHasStableSaltAndLocateableStructure() throws Exception {
        var p=read("src/modern/resources/data/thaumcraft/worldgen/structure_set/mounds.json").getAsJsonObject("placement");assertEquals(10,p.get("spacing").getAsInt());assertEquals(5,p.get("separation").getAsInt());assertEquals(68413261,p.get("salt").getAsInt());
        var biomeTag=read("src/modern/resources/data/thaumcraft/tags/worldgen/biome/has_mound.json").getAsJsonArray("values").toString();assertFalse(biomeTag.contains("is_overworld"));assertFalse(biomeTag.contains("desert"));assertFalse(biomeTag.contains("is_ocean"));
        var s=read("src/modern/resources/data/thaumcraft/worldgen/structure/mound.json");assertEquals("thaumcraft:mound",s.get("type").getAsString());assertEquals("surface_structures",s.get("step").getAsString());
    }
}
