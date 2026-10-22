package thaumcraft.worldgen;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public final class MoundBlueprint {
    public record Cell(int x,int y,int z,BlockState state) {}
    private MoundBlueprint() {}
    private static final class Loaded {static final List<Cell> CELLS=load();}
    public static List<Cell> cells() {return Loaded.CELLS;}
    private static List<Cell> load() {
        try (var stream=MoundBlueprint.class.getResourceAsStream("/data/thaumcraft/blueprints/mound.json")) {
            if (stream==null) throw new IllegalStateException("Mound blueprint missing");
            var json=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
            var palette=new ArrayList<BlockState>();
            for (var entry:json.getAsJsonArray("palette")) {
                var value=entry.getAsJsonObject();var id=Identifier.parse(value.get("block").getAsString());
                if (!BuiltInRegistries.BLOCK.containsKey(id)) throw new IllegalStateException("Unknown mound block "+id);
                var state=BuiltInRegistries.BLOCK.getValue(id).defaultBlockState();
                for (var property:value.getAsJsonObject("properties").entrySet()) state=property(state,property.getKey(),property.getValue().getAsString());
                palette.add(state);
            }
            var result=new ArrayList<Cell>();
            for (var entry:json.getAsJsonArray("cells")) {var c=entry.getAsJsonArray();result.add(new Cell(c.get(0).getAsInt(),c.get(1).getAsInt(),c.get(2).getAsInt(),palette.get(c.get(3).getAsInt())));}
            if (result.size()!=2441) throw new IllegalStateException("Incomplete mound blueprint");
            return List.copyOf(result);
        } catch(IOException e) {throw new UncheckedIOException(e);}
    }
    private static <T extends Comparable<T>> BlockState property(BlockState state,String key,String value) {
        @SuppressWarnings("unchecked") var property=(Property<T>)state.getBlock().getStateDefinition().getProperty(key);
        if (property==null) throw new IllegalStateException("Unknown mound property "+key);
        return state.setValue(property,property.getValue(value).orElseThrow());
    }
}
